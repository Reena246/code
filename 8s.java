package com.accesscontrol.service;

import com.accesscontrol.dto.ValidateAccessRequest;
import com.accesscontrol.dto.ValidateAccessResponse;
import com.accesscontrol.entity.*;
import com.accesscontrol.exception.AccessDeniedException;
import com.accesscontrol.exception.ControllerNotFoundException;
import com.accesscontrol.repository.*;
import com.accesscontrol.security.AesEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for real-time access validation
 */
@Service
public class AccessControlService {

    private static final Logger logger = LoggerFactory.getLogger(AccessControlService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final ControllerRepository controllerRepository;
    private final ReaderRepository readerRepository;
    private final AccessCardRepository accessCardRepository;
    private final EmployeeRepository employeeRepository;
    private final AccessGroupDoorRepository accessGroupDoorRepository;
    private final DoorRepository doorRepository;
    private final AuditService auditService;

    public AccessControlService(ControllerRepository controllerRepository,
                                ReaderRepository readerRepository,
                                AccessCardRepository accessCardRepository,
                                EmployeeRepository employeeRepository,
                                AccessGroupDoorRepository accessGroupDoorRepository,
                                DoorRepository doorRepository,
                                AuditService auditService) {
        this.controllerRepository = controllerRepository;
        this.readerRepository = readerRepository;
        this.accessCardRepository = accessCardRepository;
        this.employeeRepository = employeeRepository;
        this.accessGroupDoorRepository = accessGroupDoorRepository;
        this.doorRepository = doorRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ValidateAccessResponse validateAccess(ValidateAccessRequest request, 
                                                  LocalDateTime requestReceivedAt) {
        LocalDateTime processedAt = LocalDateTime.now();
        
        logger.info("Validating access - controller_mac: {}, reader_uuid: {}, card_uid: {}",
                request.getControllerMac(),
                request.getReaderUuid(),
                AesEncryptionUtil.maskCardUid(request.getCardUid()));

        // Validate controller
        Controller controller = controllerRepository
                .findByControllerMacAndIsActive(request.getControllerMac(), true)
                .orElseThrow(() -> new ControllerNotFoundException(
                        "Controller not found or inactive: " + request.getControllerMac()));

        // Validate reader
        Reader reader = readerRepository
                .findByReaderUuidAndIsActive(request.getReaderUuid(), true)
                .orElseThrow(() -> new AccessDeniedException(
                        "Reader not found or inactive", "READER_NOT_FOUND"));

        // Validate reader belongs to controller
        if (!reader.getControllerId().equals(controller.getControllerId())) {
            throw new AccessDeniedException(
                    "Reader does not belong to controller", "READER_CONTROLLER_MISMATCH");
        }

        // Validate card
        AccessCard card = accessCardRepository
                .findValidCardByCardUid(request.getCardUid(), LocalDateTime.now())
                .orElseThrow(() -> new AccessDeniedException(
                        "Card not found, expired, or inactive", "CARD_INVALID"));

        // Validate employee
        Employee employee = employeeRepository
                .findByEmployeePkAndIsActive(card.getEmployeePk(), true)
                .orElseThrow(() -> new AccessDeniedException(
                        "Employee not found or inactive", "EMPLOYEE_INACTIVE"));

        // Check if employee has access group
        if (employee.getAccessGroupId() == null) {
            auditService.logAccessAttempt(controller, reader, card, employee, 
                    Audit.AuditResult.DENIED, "NO_ACCESS_GROUP", 
                    requestReceivedAt, processedAt);
            throw new AccessDeniedException(
                    "Employee has no access group assigned", "NO_ACCESS_GROUP");
        }

        // Get door from reader
        if (reader.getDoorId() == null) {
            auditService.logAccessAttempt(controller, reader, card, employee,
                    Audit.AuditResult.DENIED, "DOOR_NOT_CONFIGURED",
                    requestReceivedAt, processedAt);
            throw new AccessDeniedException(
                    "Reader has no door configured", "DOOR_NOT_CONFIGURED");
        }

        Door door = doorRepository
                .findByDoorIdAndIsActive(reader.getDoorId(), true)
                .orElseThrow(() -> new AccessDeniedException(
                        "Door not found or inactive", "DOOR_INACTIVE"));

        // Check access permissions
        AccessGroupDoor accessGroupDoor = accessGroupDoorRepository
                .findByAccessGroupIdAndDoorIdAndIsActive(employee.getAccessGroupId(), door.getDoorId())
                .orElse(null);

        if (accessGroupDoor == null || 
            accessGroupDoor.getAccessType() == AccessGroupDoor.AccessType.DENY) {
            auditService.logAccessAttempt(controller, reader, card, employee,
                    Audit.AuditResult.DENIED, "ACCESS_NOT_ALLOWED",
                    requestReceivedAt, processedAt);
            
            logger.warn("Access denied - employee: {}, door: {}, reason: ACCESS_NOT_ALLOWED",
                    employee.getEmployeePk(), door.getDoorId());
            
            return new ValidateAccessResponse("DENIED", null, 
                    request.getReaderUuid(), "ACCESS_NOT_ALLOWED");
        }

        // Access granted
        auditService.logAccessAttempt(controller, reader, card, employee,
                Audit.AuditResult.SUCCESS, null,
                requestReceivedAt, processedAt);

        logger.info("Access granted - employee: {}, door: {}", 
                employee.getEmployeePk(), door.getDoorId());

        LocalDateTime responseSentAt = LocalDateTime.now();
        
        return new ValidateAccessResponse(
                "SUCCESS",
                door.getLockType() != null ? door.getLockType().name() : "MAGNETIC",
                request.getReaderUuid(),
                null
        );
    }
}

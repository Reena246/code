package com.accesscontrol.service;

import com.accesscontrol.dto.BulkEventLogsRequest;
import com.accesscontrol.dto.BulkEventLogsResponse;
import com.accesscontrol.entity.*;
import com.accesscontrol.exception.ControllerNotFoundException;
import com.accesscontrol.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Service for processing bulk event logs from offline controllers
 * Events are processed chronologically
 */
@Service
public class BulkEventService {

    private static final Logger logger = LoggerFactory.getLogger(BulkEventService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final ControllerRepository controllerRepository;
    private final ReaderRepository readerRepository;
    private final AccessCardRepository accessCardRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditRepository auditRepository;

    public BulkEventService(ControllerRepository controllerRepository,
                           ReaderRepository readerRepository,
                           AccessCardRepository accessCardRepository,
                           EmployeeRepository employeeRepository,
                           AuditRepository auditRepository) {
        this.controllerRepository = controllerRepository;
        this.readerRepository = readerRepository;
        this.accessCardRepository = accessCardRepository;
        this.employeeRepository = employeeRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional
    public BulkEventLogsResponse processBulkEvents(BulkEventLogsRequest request) {
        logger.info("Processing bulk events - controller_mac: {}, event_count: {}",
                request.getControllerMac(), 
                request.getEvents() != null ? request.getEvents().size() : 0);

        // Validate controller
        Controller controller = controllerRepository
                .findByControllerMacAndIsActive(request.getControllerMac(), true)
                .orElseThrow(() -> new ControllerNotFoundException(
                        "Controller not found or inactive: " + request.getControllerMac()));

        if (request.getEvents() == null || request.getEvents().isEmpty()) {
            logger.warn("No events to process for controller: {}", request.getControllerMac());
            return new BulkEventLogsResponse("RECEIVED", 0);
        }

        // Sort events chronologically by event_time
        List<BulkEventLogsRequest.EventLog> sortedEvents = request.getEvents()
                .stream()
                .sorted(Comparator.comparing(e -> parseTimestamp(e.getEventTime())))
                .toList();

        int processedCount = 0;

        for (BulkEventLogsRequest.EventLog eventLog : sortedEvents) {
            try {
                processEvent(controller, eventLog);
                processedCount++;
            } catch (Exception e) {
                logger.error("Failed to process event - reader_uuid: {}, card_uid: {}, error: {}",
                        eventLog.getReaderUuid(),
                        eventLog.getCardUid(),
                        e.getMessage());
                // Continue processing remaining events
            }
        }

        logger.info("Bulk events processed - controller_mac: {}, processed: {}/{}, received_at: {}",
                request.getControllerMac(),
                processedCount,
                sortedEvents.size(),
                LocalDateTime.now());

        return new BulkEventLogsResponse("RECEIVED", processedCount);
    }

    private void processEvent(Controller controller, BulkEventLogsRequest.EventLog eventLog) {
        LocalDateTime eventTime = parseTimestamp(eventLog.getEventTime());

        // Find reader
        Reader reader = readerRepository
                .findByReaderUuidAndIsActive(eventLog.getReaderUuid(), true)
                .orElse(null);

        // Find card
        AccessCard card = accessCardRepository
                .findByCardUidAndIsActive(eventLog.getCardUid(), true)
                .orElse(null);

        Employee employee = null;
        if (card != null) {
            employee = employeeRepository
                    .findByEmployeePkAndIsActive(card.getEmployeePk(), true)
                    .orElse(null);
        }

        // Create audit record
        Audit audit = new Audit();
        audit.setControllerMac(controller.getControllerMac());
        audit.setEventTime(eventTime);
        
        if (reader != null) {
            audit.setReaderId(reader.getReaderId());
            audit.setDoorId(reader.getDoorId());
        }
        
        if (card != null) {
            audit.setCardId(card.getCardId());
        }
        
        if (employee != null) {
            audit.setEmployeePk(employee.getEmployeePk());
            audit.setCompanyId(employee.getCompanyId());
        }

        // Determine result based on event type
        if ("OPEN".equalsIgnoreCase(eventLog.getEventType())) {
            audit.setOpenedAt(eventTime);
            audit.setResult(Audit.AuditResult.SUCCESS);
        } else if ("CLOSE".equalsIgnoreCase(eventLog.getEventType())) {
            audit.setClosedAt(eventTime);
            // Calculate open duration if we have a previous OPEN event
        } else if ("FORCED".equalsIgnoreCase(eventLog.getEventType())) {
            audit.setResult(Audit.AuditResult.DENIED);
            audit.setReason("FORCED_ENTRY");
        }

        audit.setRequestReceivedAt(LocalDateTime.now());
        audit.setProcessedAt(LocalDateTime.now());
        audit.setIsActive(true);

        auditRepository.save(audit);

        logger.debug("Event processed - reader: {}, card: {}, type: {}, time: {}",
                eventLog.getReaderUuid(),
                eventLog.getCardUid() != null ? "****" + eventLog.getCardUid().substring(
                        Math.max(0, eventLog.getCardUid().length() - 4)) : "null",
                eventLog.getEventType(),
                eventTime);
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp, TIMESTAMP_FORMATTER);
        } catch (Exception e) {
            logger.warn("Failed to parse timestamp: {}, using current time", timestamp);
            return LocalDateTime.now();
        }
    }
}

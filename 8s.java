package com.accesscontrol.service;

import com.accesscontrol.dto.DoorEventRequest;
import com.accesscontrol.entity.Audit;
import com.accesscontrol.entity.Controller;
import com.accesscontrol.entity.Reader;
import com.accesscontrol.exception.ControllerNotFoundException;
import com.accesscontrol.repository.ControllerRepository;
import com.accesscontrol.repository.ReaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for handling door events (OPEN, CLOSE, FORCED)
 */
@Service
public class DoorEventService {

    private static final Logger logger = LoggerFactory.getLogger(DoorEventService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final ControllerRepository controllerRepository;
    private final ReaderRepository readerRepository;
    private final AuditService auditService;

    public DoorEventService(ControllerRepository controllerRepository,
                           ReaderRepository readerRepository,
                           AuditService auditService) {
        this.controllerRepository = controllerRepository;
        this.readerRepository = readerRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void processDoorEvent(DoorEventRequest request) {
        logger.info("Processing door event - controller_mac: {}, reader_uuid: {}, event_type: {}",
                request.getControllerMac(),
                request.getReaderUuid(),
                request.getEventType());

        // Validate controller
        Controller controller = controllerRepository
                .findByControllerMacAndIsActive(request.getControllerMac(), true)
                .orElseThrow(() -> new ControllerNotFoundException(
                        "Controller not found or inactive: " + request.getControllerMac()));

        // Validate reader
        Reader reader = readerRepository
                .findByReaderUuidAndIsActive(request.getReaderUuid(), true)
                .orElse(null);

        LocalDateTime eventTime = parseTimestamp(request.getTimestamp());

        // Log event to audit
        auditService.logDoorEvent(controller, reader, request.getEventType(), eventTime);

        logger.info("Door event processed successfully - event_type: {}", request.getEventType());
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

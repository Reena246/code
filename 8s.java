package com.accesscontrol.service;

import com.accesscontrol.entity.*;
import com.accesscontrol.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for audit logging with structured logging
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditRepository auditRepository;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional
    public void logAccessAttempt(Controller controller, 
                                 Reader reader, 
                                 AccessCard card,
                                 Employee employee,
                                 Audit.AuditResult result,
                                 String reason,
                                 LocalDateTime requestReceivedAt,
                                 LocalDateTime processedAt) {
        
        Audit audit = new Audit();
        audit.setControllerMac(controller.getControllerMac());
        audit.setReaderId(reader.getReaderId());
        audit.setDoorId(reader.getDoorId());
        audit.setCardId(card.getCardId());
        audit.setEmployeePk(employee.getEmployeePk());
        audit.setCompanyId(employee.getCompanyId());
        audit.setEventTime(LocalDateTime.now());
        audit.setResult(result);
        audit.setReason(reason);
        audit.setRequestReceivedAt(requestReceivedAt);
        audit.setProcessedAt(processedAt);
        audit.setResponseSentAt(LocalDateTime.now());
        audit.setIsActive(true);

        auditRepository.save(audit);

        // Structured logging
        logger.info("AUDIT: controller_mac={}, reader_id={}, door_id={}, employee_pk={}, " +
                   "card_id={}, result={}, reason={}, request_received={}, processed={}, response_sent={}",
                audit.getControllerMac(),
                audit.getReaderId(),
                audit.getDoorId(),
                audit.getEmployeePk(),
                audit.getCardId(),
                audit.getResult(),
                audit.getReason(),
                audit.getRequestReceivedAt(),
                audit.getProcessedAt(),
                audit.getResponseSentAt());
    }

    @Transactional
    public void logDoorEvent(Controller controller, 
                            Reader reader, 
                            String eventType, 
                            LocalDateTime eventTime) {
        
        Audit audit = new Audit();
        audit.setControllerMac(controller.getControllerMac());
        
        if (reader != null) {
            audit.setReaderId(reader.getReaderId());
            audit.setDoorId(reader.getDoorId());
        }
        
        audit.setEventTime(eventTime);
        
        // Set appropriate fields based on event type
        if ("OPEN".equalsIgnoreCase(eventType)) {
            audit.setOpenedAt(eventTime);
        } else if ("CLOSE".equalsIgnoreCase(eventType)) {
            audit.setClosedAt(eventTime);
            // Try to find matching OPEN event to calculate duration
            calculateDoorOpenDuration(audit);
        } else if ("FORCED".equalsIgnoreCase(eventType)) {
            audit.setResult(Audit.AuditResult.DENIED);
            audit.setReason("FORCED_ENTRY");
        }
        
        audit.setRequestReceivedAt(LocalDateTime.now());
        audit.setProcessedAt(LocalDateTime.now());
        audit.setIsActive(true);

        auditRepository.save(audit);

        logger.info("DOOR_EVENT: controller_mac={}, reader_id={}, door_id={}, event_type={}, event_time={}",
                audit.getControllerMac(),
                audit.getReaderId(),
                audit.getDoorId(),
                eventType,
                eventTime);
    }

    private void calculateDoorOpenDuration(Audit closeAudit) {
        if (closeAudit.getDoorId() == null || closeAudit.getClosedAt() == null) {
            return;
        }

        try {
            // Find the most recent OPEN event for this door
            LocalDateTime searchStart = closeAudit.getClosedAt().minusMinutes(30);
            List<Audit> recentEvents = auditRepository
                    .findByControllerMacAndEventTimeBetween(
                            closeAudit.getControllerMac(),
                            searchStart,
                            closeAudit.getClosedAt());

            recentEvents.stream()
                    .filter(a -> a.getDoorId() != null 
                            && a.getDoorId().equals(closeAudit.getDoorId())
                            && a.getOpenedAt() != null)
                    .max((a1, a2) -> a1.getOpenedAt().compareTo(a2.getOpenedAt()))
                    .ifPresent(openAudit -> {
                        long seconds = ChronoUnit.SECONDS.between(
                                openAudit.getOpenedAt(), 
                                closeAudit.getClosedAt());
                        closeAudit.setOpenSeconds((int) seconds);

                        // Calculate average open time for this door
                        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
                        Double avgSeconds = auditRepository
                                .findAverageOpenSecondsByDoorId(closeAudit.getDoorId(), last30Days);
                        if (avgSeconds != null) {
                            closeAudit.setAvgOpenSeconds(avgSeconds.intValue());
                        }
                    });
        } catch (Exception e) {
            logger.warn("Failed to calculate door open duration: {}", e.getMessage());
        }
    }
}

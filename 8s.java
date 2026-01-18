package com.project.badgemate.service;

import com.project.badgemate.dto.EventLog;
import com.project.badgemate.dto.EventLogResponse;
import com.project.badgemate.entity.*;
import com.project.badgemate.exception.ResourceNotFoundException;
import com.project.badgemate.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventLogService.class);
    
    @Autowired
    private AccessCardRepository accessCardRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private DoorRepository doorRepository;
    
    @Autowired
    private AccessGroupDoorRepository accessGroupDoorRepository;
    
    @Autowired
    private ReaderRepository readerRepository;
    
    @Autowired
    private AuditRepository auditRepository;
    
    /**
     * Normalizes card hex/uid format from various RFID reader formats
     * Handles: 0x9d3b9f1a, 9d3b9f1a, 9D3B9F1A, 9d:3b:9f:1a, etc.
     */
    private String normalizeCardUid(String cardHex) {
        if (cardHex == null || cardHex.trim().isEmpty()) {
            return null;
        }
        
        // Remove common prefixes and separators
        String normalized = cardHex.trim()
            .replaceFirst("^0[xX]", "")  // Remove 0x or 0X prefix
            .replaceAll("[:\\s-]", "")   // Remove colons, spaces, hyphens
            .toUpperCase();              // Convert to uppercase
        
        return normalized;
    }
    
    @Transactional
    public EventLogResponse processEventLog(EventLog eventLog) {
        logger.info("Processing event log: event_id={}, door_id={}, card_hex={}", 
            eventLog.getEventId(), eventLog.getDoorId(), eventLog.getCardHex());
        
        LocalDateTime eventTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(eventLog.getTimestamp()), 
            java.time.ZoneId.systemDefault()
        );
        
        // Fetch door and lock type - required for real door operations
        Door door = doorRepository.findByDoorIdAndIsActiveTrue(eventLog.getDoorId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Door not found or inactive: " + eventLog.getDoorId()));
        
        String doorType = door.getLockType().name();
        logger.debug("Door found: door_id={}, lock_type={}", eventLog.getDoorId(), doorType);
        
        // Find reader for the door (first active reader if multiple exist)
        Optional<Reader> readerOpt = readerRepository.findByDoorIdAndIsActiveTrue(eventLog.getDoorId());
        Long readerId = readerOpt.map(Reader::getReaderId).orElse(null);
        if (readerId == null) {
            logger.warn("No active reader found for door_id={}", eventLog.getDoorId());
        }
        
        // Normalize card_hex to card_uid (handles various RFID reader formats)
        String cardUid = normalizeCardUid(eventLog.getCardHex());
        if (cardUid == null || cardUid.isEmpty()) {
            logger.warn("Invalid card_hex format: {}", eventLog.getCardHex());
            // Create audit entry for invalid card format
            createAuditEntry(eventLog, null, null, readerId, eventTime, null, null, 
                null, false, "Invalid card format", door.getDoorId());
            return createResponse(false, doorType, null, null);
        }
        
        // Find access card by card_uid (try both normalized and original formats)
        Optional<AccessCard> cardOpt = accessCardRepository.findByCardUidAndIsActiveTrue(cardUid);
        
        // If not found with normalized format, try with original format
        if (cardOpt.isEmpty() && !cardUid.equals(eventLog.getCardHex())) {
            cardOpt = accessCardRepository.findByCardUidAndIsActiveTrue(eventLog.getCardHex().toUpperCase());
        }
        
        boolean accessGranted = false;
        String reason = "Card not found";
        Long employeePk = null;
        Long cardId = null;
        
        if (cardOpt.isPresent()) {
            AccessCard card = cardOpt.get();
            cardId = card.getCardId();
            employeePk = card.getEmployeePk();
            logger.debug("Card found: card_id={}, employee_pk={}", cardId, employeePk);
            
            // Check if card is expired
            if (card.getExpiresAt() != null && card.getExpiresAt().isBefore(LocalDateTime.now())) {
                reason = "Card expired on " + card.getExpiresAt();
                logger.warn("Card expired: card_id={}, expires_at={}", cardId, card.getExpiresAt());
            } else if (card.getEmployeePk() == null) {
                reason = "Card not assigned to employee";
                logger.warn("Card not assigned: card_id={}", cardId);
            } else {
                // Find employee
                Optional<Employee> employeeOpt = employeeRepository.findByEmployeePkAndIsActiveTrue(card.getEmployeePk());
                
                if (employeeOpt.isPresent()) {
                    Employee employee = employeeOpt.get();
                    Long accessGroupId = employee.getAccessGroupId();
                    
                    if (accessGroupId == null) {
                        reason = "Employee not assigned to access group";
                        logger.warn("Employee not in access group: employee_pk={}", employee.getEmployeePk());
                    } else {
                        // Check access group door permissions
                        List<AccessGroupDoor> permissions = accessGroupDoorRepository
                            .findByAccessGroupIdAndDoorIdAndIsActiveTrue(accessGroupId, eventLog.getDoorId());
                        
                        if (permissions.isEmpty()) {
                            reason = "No access permission for this door";
                            logger.warn("No permissions: access_group_id={}, door_id={}", accessGroupId, eventLog.getDoorId());
                        } else {
                            // Check if there's an ALLOW permission
                            boolean hasAllow = permissions.stream()
                                .anyMatch(p -> p.getAccessType() == AccessGroupDoor.AccessType.ALLOW);
                            
                            boolean hasDeny = permissions.stream()
                                .anyMatch(p -> p.getAccessType() == AccessGroupDoor.AccessType.DENY);
                            
                            if (hasDeny && !hasAllow) {
                                reason = "Access denied by access group";
                                logger.warn("Access denied by group: access_group_id={}, door_id={}", accessGroupId, eventLog.getDoorId());
                            } else if (hasAllow) {
                                accessGranted = true;
                                reason = "Access granted";
                                logger.info("Access granted: employee_pk={}, door_id={}", employeePk, eventLog.getDoorId());
                            } else {
                                reason = "No explicit allow permission";
                            }
                        }
                    }
                } else {
                    reason = "Employee not found or inactive";
                    logger.warn("Employee not found: employee_pk={}", card.getEmployeePk());
                }
            }
        } else {
            logger.warn("Card not found: card_uid={} (normalized from {})", cardUid, eventLog.getCardHex());
        }
        
        // In real scenario: opened_at and closed_at should come from the controller
        // when door actually opens/closes, or be calculated based on door sensor feedback
        // For now, we'll set them based on access grant status
        // Controller should send these timestamps via separate endpoint or callback
        LocalDateTime openedAt = null;
        LocalDateTime closedAt = null;
        Integer openSeconds = null;
        
        if (accessGranted) {
            // In real scenario, these would be set when controller reports door state
            // For now, estimate: door opens 1 second after scan, closes after typical duration
            openedAt = eventTime.plusSeconds(1);
            // Typical door open duration: 5-15 seconds depending on lock type
            int openDuration = (door.getLockType() == Door.LockType.MAGNETIC) ? 10 : 8;
            closedAt = openedAt.plusSeconds(openDuration);
            openSeconds = openDuration;
        }
        
        // Calculate average open seconds (simplified - in real scenario, calculate from historical data)
        BigDecimal avgOpenSeconds = null;
        if (accessGranted && eventLog.getDoorId() != null) {
            List<Audit> recentAudits = auditRepository.findAll().stream()
                .filter(a -> a.getDoorId() != null && a.getDoorId().equals(eventLog.getDoorId()))
                .filter(a -> a.getResult() == Audit.AuditResult.SUCCESS)
                .filter(a -> a.getOpenSeconds() != null)
                .limit(10)
                .toList();
            
            if (!recentAudits.isEmpty()) {
                double avg = recentAudits.stream()
                    .mapToInt(Audit::getOpenSeconds)
                    .average()
                    .orElse(0.0);
                avgOpenSeconds = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
            }
        }
        
        // Create audit entry
        Audit audit = new Audit();
        audit.setCompanyId(eventLog.getCompanyId());
        audit.setEmployeePk(employeePk);
        audit.setCardId(cardId);
        audit.setDoorId(eventLog.getDoorId());
        audit.setReaderId(readerId);
        audit.setEventTime(eventTime);
        audit.setOpenedAt(openedAt);
        audit.setClosedAt(closedAt);
        audit.setOpenSeconds(openSeconds);
        audit.setAvgOpenSeconds(avgOpenSeconds);
        audit.setResult(accessGranted ? Audit.AuditResult.SUCCESS : Audit.AuditResult.DENIED);
        audit.setReason(reason);
        audit.setIsActive(true);
        audit.setCreated(LocalDateTime.now());
        audit.setUpdated(LocalDateTime.now());
        audit.setCreatedBy("SYSTEM");
        audit.setUpdatedBy("SYSTEM");
        
        auditRepository.save(audit);
        
        // Create audit entry (always log, whether granted or denied)
        createAuditEntry(eventLog, employeePk, cardId, readerId, eventTime, openedAt, closedAt, 
            openSeconds, accessGranted, reason, door.getDoorId());
        
        // Build response for controller
        EventLogResponse response = createResponse(accessGranted, doorType, openedAt, closedAt);
        
        logger.info("Event processed: event_id={}, status={}, reason={}", 
            eventLog.getEventId(), response.getStatus(), reason);
        
        return response;
    }
    
    private void createAuditEntry(EventLog eventLog, Long employeePk, Long cardId, Long readerId,
                                   LocalDateTime eventTime, LocalDateTime openedAt, LocalDateTime closedAt,
                                   Integer openSeconds, boolean accessGranted, String reason, Long doorId) {
        // Calculate average open seconds from historical data
        BigDecimal avgOpenSeconds = null;
        if (accessGranted && doorId != null && openSeconds != null) {
            List<Audit> recentAudits = auditRepository.findAll().stream()
                .filter(a -> a.getDoorId() != null && a.getDoorId().equals(doorId))
                .filter(a -> a.getResult() == Audit.AuditResult.SUCCESS)
                .filter(a -> a.getOpenSeconds() != null)
                .limit(20) // Use more samples for better average
                .toList();
            
            if (!recentAudits.isEmpty()) {
                double avg = recentAudits.stream()
                    .mapToInt(Audit::getOpenSeconds)
                    .average()
                    .orElse(openSeconds.doubleValue());
                avgOpenSeconds = BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
            } else {
                avgOpenSeconds = BigDecimal.valueOf(openSeconds).setScale(2, RoundingMode.HALF_UP);
            }
        }
        
        Audit audit = new Audit();
        audit.setCompanyId(eventLog.getCompanyId());
        audit.setEmployeePk(employeePk);
        audit.setCardId(cardId);
        audit.setDoorId(doorId);
        audit.setReaderId(readerId);
        audit.setEventTime(eventTime);
        audit.setOpenedAt(openedAt);
        audit.setClosedAt(closedAt);
        audit.setOpenSeconds(openSeconds);
        audit.setAvgOpenSeconds(avgOpenSeconds);
        audit.setResult(accessGranted ? Audit.AuditResult.SUCCESS : Audit.AuditResult.DENIED);
        audit.setReason(reason);
        audit.setIsActive(true);
        audit.setCreated(LocalDateTime.now());
        audit.setUpdated(LocalDateTime.now());
        audit.setCreatedBy("SYSTEM");
        audit.setUpdatedBy("SYSTEM");
        
        auditRepository.save(audit);
        logger.debug("Audit entry created: audit_id={}, result={}", audit.getAuditId(), audit.getResult());
    }
    
    private EventLogResponse createResponse(boolean accessGranted, String doorType, 
                                            LocalDateTime openedAt, LocalDateTime closedAt) {
        EventLogResponse response = new EventLogResponse();
        response.setStatus(accessGranted ? "access_granted" : "access_denied");
        response.setDoorType(doorType);
        response.setTimestampSent(Instant.now().getEpochSecond());
        
        if (openedAt != null) {
            response.setOpenedAt(openedAt.atZone(java.time.ZoneId.systemDefault()).toEpochSecond());
        }
        if (closedAt != null) {
            response.setClosedAt(closedAt.atZone(java.time.ZoneId.systemDefault()).toEpochSecond());
        }
        
        return response;
    }
    
}

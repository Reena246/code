package com.project.badgemate.service;

import com.project.badgemate.dto.EventLog;
import com.project.badgemate.dto.EventLogResponse;
import com.project.badgemate.entity.*;
import com.project.badgemate.entity.Audit.AuditResult;
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
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class EventLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventLogService.class);
    
    private final AuditRepository auditRepository;
    private final AccessCardRepository accessCardRepository;
    private final EmployeeRepository employeeRepository;
    private final DoorRepository doorRepository;
    private final ReaderRepository readerRepository;
    private final AccessGroupDoorRepository accessGroupDoorRepository;
    
    @Autowired
    public EventLogService(AuditRepository auditRepository,
                          AccessCardRepository accessCardRepository,
                          EmployeeRepository employeeRepository,
                          DoorRepository doorRepository,
                          ReaderRepository readerRepository,
                          AccessGroupDoorRepository accessGroupDoorRepository) {
        this.auditRepository = auditRepository;
        this.accessCardRepository = accessCardRepository;
        this.employeeRepository = employeeRepository;
        this.doorRepository = doorRepository;
        this.readerRepository = readerRepository;
        this.accessGroupDoorRepository = accessGroupDoorRepository;
    }
    
    @Transactional
    public EventLogResponse processEventLog(EventLog eventLog) {
        logger.info("Processing event log: {} - {}", eventLog.getEventType(), eventLog.getEventId());
        
        String eventId = eventLog.getEventId();
        long timestamp = eventLog.getTimestamp() != null ? eventLog.getTimestamp() : Instant.now().getEpochSecond();
        
        try {
            // Step 1: Find card by card_hex
            AccessCard card = findCardByHex(eventLog.getCardHex());
            if (card == null) {
                return createDeniedResponse(eventId, "Card not found in system", timestamp, null);
            }
            
            // Step 2: Validate card is active
            if (!Boolean.TRUE.equals(card.getIsActive())) {
                return createDeniedResponse(eventId, "Card is inactive", timestamp, null);
            }
            
            // Step 3: Find employee
            Employee employee = employeeRepository.findById(card.getEmployeePk())
                .orElse(null);
            if (employee == null) {
                return createDeniedResponse(eventId, "Employee not found", timestamp, null);
            }
            
            // Step 4: Validate employee is active
            if (!Boolean.TRUE.equals(employee.getIsActive())) {
                return createDeniedResponse(eventId, "Employee is inactive", timestamp, null);
            }
            
            // Step 5: Find door by door_id (door code)
            Door door = doorRepository.findByDoorCode(eventLog.getDoorId())
                .orElse(null);
            if (door == null) {
                return createDeniedResponse(eventId, "Door not found", timestamp, null);
            }
            
            // Step 6: Validate door is active
            if (!Boolean.TRUE.equals(door.getIsActive())) {
                return createDeniedResponse(eventId, "Door is inactive", timestamp, door.getLockType().name());
            }
            
            // Step 7: Get door lock type (from database)
            String doorLockType = door.getLockType().name();
            
            // Step 8: Find reader for the door
            List<Reader> readers = readerRepository.findByDoorId(door.getDoorId());
            Reader reader = readers.isEmpty() ? null : readers.get(0);
            Long readerId = reader != null ? reader.getReaderId() : null;
            
            // Step 9: Validate access using access_group_door
            boolean hasAccess = validateAccess(employee.getAccessGroupId(), door.getDoorId());
            
            // Step 10: Create response
            EventLogResponse response;
            if (hasAccess) {
                response = createGrantedResponse(eventId, "Access granted", timestamp, doorLockType);
            } else {
                response = createDeniedResponse(eventId, "Access denied - No permission for this door", timestamp, doorLockType);
            }
            
            // Step 11: Calculate open_seconds and avg_open_seconds if timestamps provided
            Integer openSeconds = null;
            BigDecimal avgOpenSeconds = null;
            
            if (eventLog.getOpenedAt() != null && eventLog.getClosedAt() != null) {
                openSeconds = (int) (eventLog.getClosedAt() - eventLog.getOpenedAt());
                
                // Calculate average open seconds for this door
                Optional<BigDecimal> avgOpt = auditRepository.findAverageOpenSecondsByDoorId(door.getDoorId());
                if (avgOpt.isPresent()) {
                    // Calculate new average including current open_seconds
                    BigDecimal currentAvg = avgOpt.get();
                    // For simplicity, we'll use the existing average
                    // In production, you might want to recalculate based on all historical data
                    avgOpenSeconds = currentAvg;
                } else {
                    // First entry for this door, use current open_seconds as average
                    avgOpenSeconds = BigDecimal.valueOf(openSeconds).setScale(2, RoundingMode.HALF_UP);
                }
            }
            
            // Step 12: Create and save audit record
            Audit audit = createAuditRecord(eventLog, card, employee, door, readerId, 
                                          hasAccess, openSeconds, avgOpenSeconds);
            auditRepository.save(audit);
            
            logger.info("Event log processed and saved to audit: {} - Access: {}", 
                       eventId, hasAccess ? "GRANTED" : "DENIED");
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing event log: {}", eventId, e);
            return createDeniedResponse(eventId, "Error processing event: " + e.getMessage(), timestamp, null);
        }
    }
    
    private AccessCard findCardByHex(Long cardHex) {
        if (cardHex == null) {
            return null;
        }
        
        // Convert hex to string (remove 0x prefix if present)
        String cardHexStr = Long.toHexString(cardHex).toLowerCase();
        
        // Try to find by card_uid
        Optional<AccessCard> cardOpt = accessCardRepository.findByCardUid(cardHexStr);
        if (cardOpt.isPresent()) {
            return cardOpt.get();
        }
        
        // Try with uppercase
        cardOpt = accessCardRepository.findByCardUid(cardHexStr.toUpperCase());
        if (cardOpt.isPresent()) {
            return cardOpt.get();
        }
        
        // Try with 0x prefix
        cardOpt = accessCardRepository.findByCardUid("0x" + cardHexStr);
        if (cardOpt.isPresent()) {
            return cardOpt.get();
        }
        
        return null;
    }
    
    private boolean validateAccess(Long accessGroupId, Long doorId) {
        if (accessGroupId == null || doorId == null) {
            return false;
        }
        
        // Check if access group has permission for this door
        Optional<AccessGroupDoor> agdOpt = accessGroupDoorRepository
            .findByAccessGroupIdAndDoorId(accessGroupId, doorId);
        
        if (agdOpt.isPresent()) {
            AccessGroupDoor agd = agdOpt.get();
            // Check if access type is ALLOW
            return agd.getAccessType() == AccessGroupDoor.AccessType.ALLOW;
        }
        
        // No entry means no access
        return false;
    }
    
    private EventLogResponse createGrantedResponse(String eventId, String reason, long timestamp, String doorLockType) {
        EventLogResponse response = new EventLogResponse();
        response.setEventId(eventId);
        response.setAccessStatus(EventLogResponse.AccessStatus.access_granted);
        response.setReason(reason);
        response.setTimestamp(timestamp);
        response.setDoorLockType(doorLockType);
        return response;
    }
    
    private EventLogResponse createDeniedResponse(String eventId, String reason, long timestamp, String doorLockType) {
        EventLogResponse response = new EventLogResponse();
        response.setEventId(eventId);
        response.setAccessStatus(EventLogResponse.AccessStatus.access_denied);
        response.setReason(reason);
        response.setTimestamp(timestamp);
        response.setDoorLockType(doorLockType);
        return response;
    }
    
    private Audit createAuditRecord(EventLog eventLog, AccessCard card, Employee employee, 
                                   Door door, Long readerId, boolean hasAccess,
                                   Integer openSeconds, BigDecimal avgOpenSeconds) {
        Audit audit = new Audit();
        
        // Set basic information
        audit.setCompanyId(card.getCompanyId());
        audit.setEmployeePk(employee.getEmployeePk());
        audit.setCardId(card.getCardId());
        audit.setDoorId(door.getDoorId());
        audit.setReaderId(readerId);
        
        // Set event time
        if (eventLog.getTimestamp() != null) {
            audit.setEventTime(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(eventLog.getTimestamp()), ZoneId.systemDefault()));
        } else {
            audit.setEventTime(LocalDateTime.now());
        }
        
        // Set opened_at and closed_at
        if (eventLog.getOpenedAt() != null) {
            audit.setOpenedAt(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(eventLog.getOpenedAt()), ZoneId.systemDefault()));
        }
        
        if (eventLog.getClosedAt() != null) {
            audit.setClosedAt(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(eventLog.getClosedAt()), ZoneId.systemDefault()));
        }
        
        // Set calculated fields
        audit.setOpenSeconds(openSeconds);
        audit.setAvgOpenSeconds(avgOpenSeconds);
        
        // Set result
        audit.setResult(hasAccess ? AuditResult.SUCCESS : AuditResult.DENIED);
        
        // Set reason
        String reason = eventLog.getDetails();
        if (reason == null || reason.isEmpty()) {
            reason = hasAccess ? "Access granted" : "Access denied";
        }
        audit.setReason(reason);
        
        // Set active and audit fields
        audit.setIsActive(true);
        audit.setCreatedBy("system");
        
        return audit;
    }
}

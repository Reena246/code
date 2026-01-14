package com.project.badgemate.service;

import com.project.badgemate.dto.EventLog;
import com.project.badgemate.dto.EventLogResponse;
import com.project.badgemate.entity.*;
import com.project.badgemate.repository.*;
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
    
    @Transactional
    public EventLogResponse processEventLog(EventLog eventLog) {
        LocalDateTime eventTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(eventLog.getTimestamp()), 
            java.time.ZoneId.systemDefault()
        );
        
        // Fetch door and lock type
        Door door = doorRepository.findByDoorIdAndIsActiveTrue(eventLog.getDoorId())
            .orElseThrow(() -> new RuntimeException("Door not found or inactive: " + eventLog.getDoorId()));
        
        String doorType = door.getLockType().name();
        
        // Find reader for the door
        Optional<Reader> readerOpt = readerRepository.findByDoorIdAndIsActiveTrue(eventLog.getDoorId());
        Long readerId = readerOpt.map(Reader::getReaderId).orElse(null);
        
        // Convert card_hex to card_uid (remove 0x prefix if present)
        String cardUid = eventLog.getCardHex().replaceFirst("^0x", "").toUpperCase();
        
        // Find access card by card_uid
        Optional<AccessCard> cardOpt = accessCardRepository.findByCardUidAndIsActiveTrue(cardUid);
        
        boolean accessGranted = false;
        String reason = "Card not found";
        Long employeePk = null;
        Long cardId = null;
        
        if (cardOpt.isPresent()) {
            AccessCard card = cardOpt.get();
            cardId = card.getCardId();
            employeePk = card.getEmployeePk();
            
            // Check if card is expired
            if (card.getExpiresAt() != null && card.getExpiresAt().isBefore(LocalDateTime.now())) {
                reason = "Card expired";
            } else if (card.getEmployeePk() == null) {
                reason = "Card not assigned to employee";
            } else {
                // Find employee
                Optional<Employee> employeeOpt = employeeRepository.findByEmployeePkAndIsActiveTrue(card.getEmployeePk());
                
                if (employeeOpt.isPresent()) {
                    Employee employee = employeeOpt.get();
                    Long accessGroupId = employee.getAccessGroupId();
                    
                    if (accessGroupId == null) {
                        reason = "Employee not assigned to access group";
                    } else {
                        // Check access group door permissions
                        List<AccessGroupDoor> permissions = accessGroupDoorRepository
                            .findByAccessGroupIdAndDoorIdAndIsActiveTrue(accessGroupId, eventLog.getDoorId());
                        
                        if (permissions.isEmpty()) {
                            reason = "No access permission for this door";
                        } else {
                            // Check if there's an ALLOW permission
                            boolean hasAllow = permissions.stream()
                                .anyMatch(p -> p.getAccessType() == AccessGroupDoor.AccessType.ALLOW);
                            
                            boolean hasDeny = permissions.stream()
                                .anyMatch(p -> p.getAccessType() == AccessGroupDoor.AccessType.DENY);
                            
                            if (hasDeny && !hasAllow) {
                                reason = "Access denied by access group";
                            } else if (hasAllow) {
                                accessGranted = true;
                                reason = "Access granted";
                            } else {
                                reason = "No explicit allow permission";
                            }
                        }
                    }
                } else {
                    reason = "Employee not found or inactive";
                }
            }
        }
        
        // Calculate timestamps
        LocalDateTime openedAt = accessGranted ? eventTime.plusSeconds(1) : null;
        LocalDateTime closedAt = accessGranted ? eventTime.plusSeconds(10) : null; // Assuming 10 seconds open time
        Integer openSeconds = accessGranted ? 9 : null;
        
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
        
        // Build response
        EventLogResponse response = new EventLogResponse();
        response.setStatus(accessGranted ? "access_granted" : "access_denied");
        response.setDoorType(doorType);
        response.setTimestampSent(Instant.now().getEpochSecond());
        response.setOpenedAt(openedAt != null ? openedAt.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() : null);
        response.setClosedAt(closedAt != null ? closedAt.atZone(java.time.ZoneId.systemDefault()).toEpochSecond() : null);
        
        return response;
    }
}

package com.project.accesscontrol.service;

import com.project.accesscontrol.entity.Audit;
import com.project.accesscontrol.repository.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuditService {
    
    @Autowired
    private AuditRepository auditRepository;
    
    @Transactional
    public Audit createCardScanAudit(Long companyId, Long employeePk, Long cardId, Long doorId, 
                                     Long readerId, LocalDateTime eventTime, boolean granted, String reason) {
        Audit audit = new Audit();
        audit.setCompanyId(companyId);
        audit.setEmployeePk(employeePk);
        audit.setCardId(cardId);
        audit.setDoorId(doorId);
        audit.setReaderId(readerId);
        audit.setEventTime(eventTime);
        audit.setResult(granted ? Audit.Result.SUCCESS : Audit.Result.DENIED);
        audit.setReason(reason);
        audit.setIsActive(true);
        audit.setCreated(LocalDateTime.now());
        audit.setUpdated(LocalDateTime.now());
        audit.setCreatedBy("SYSTEM");
        audit.setUpdatedBy("SYSTEM");
        
        return auditRepository.save(audit);
    }
    
    @Transactional
    public Audit updateAuditWithDoorOpen(Long auditId, LocalDateTime openedAt) {
        Optional<Audit> auditOpt = auditRepository.findById(auditId);
        if (auditOpt.isEmpty()) {
            return null;
        }
        
        Audit audit = auditOpt.get();
        audit.setOpenedAt(openedAt);
        audit.setUpdated(LocalDateTime.now());
        audit.setUpdatedBy("SYSTEM");
        
        return auditRepository.save(audit);
    }
    
    @Transactional
    public Audit updateAuditWithDoorClose(Long auditId, LocalDateTime closedAt) {
        Optional<Audit> auditOpt = auditRepository.findById(auditId);
        if (auditOpt.isEmpty()) {
            return null;
        }
        
        Audit audit = auditOpt.get();
        audit.setClosedAt(closedAt);
        
        // Calculate open_seconds if opened_at exists
        if (audit.getOpenedAt() != null && closedAt != null) {
            long seconds = java.time.Duration.between(audit.getOpenedAt(), closedAt).getSeconds();
            audit.setOpenSeconds((int) seconds);
            
            // Calculate average open seconds for this door
            Double avgSeconds = auditRepository.findAverageOpenSecondsByDoorId(audit.getDoorId());
            if (avgSeconds != null) {
                audit.setAvgOpenSeconds(BigDecimal.valueOf(avgSeconds).setScale(2, RoundingMode.HALF_UP));
            }
        }
        
        audit.setUpdated(LocalDateTime.now());
        audit.setUpdatedBy("SYSTEM");
        
        return auditRepository.save(audit);
    }
    
    public Audit findLatestAuditByCardAndDoor(Long cardId, Long doorId) {
        var audits = auditRepository.findByCardIdAndDoorIdOrderByEventTimeDesc(cardId, doorId);
        return audits.isEmpty() ? null : audits.get(0);
    }
}

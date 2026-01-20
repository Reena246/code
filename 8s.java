package com.project.accesscontrol.service;

import com.project.accesscontrol.entity.*;
import com.project.accesscontrol.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CardValidationService {
    
    @Autowired
    private AccessCardRepository accessCardRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AccessGroupDoorRepository accessGroupDoorRepository;
    
    @Autowired
    private DoorRepository doorRepository;
    
    public CardValidationResult validateCardAccess(String cardHex, Long doorId) {
        CardValidationResult result = new CardValidationResult();
        
        // Find card by card_uid (cardHex)
        Optional<AccessCard> cardOpt = accessCardRepository.findByCardUidAndIsActiveTrue(cardHex);
        if (cardOpt.isEmpty()) {
            result.setGranted(false);
            result.setReason("Card not found or inactive");
            return result;
        }
        
        AccessCard card = cardOpt.get();
        
        // Check if card is expired
        if (card.getExpiresAt() != null && card.getExpiresAt().isBefore(LocalDateTime.now())) {
            result.setGranted(false);
            result.setReason("Card expired");
            return result;
        }
        
        // Check if card is not yet issued
        if (card.getIssuedAt() != null && card.getIssuedAt().isAfter(LocalDateTime.now())) {
            result.setGranted(false);
            result.setReason("Card not yet issued");
            return result;
        }
        
        // Get employee
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeePkAndIsActiveTrue(card.getEmployeePk());
        if (employeeOpt.isEmpty()) {
            result.setGranted(false);
            result.setReason("Employee not found or inactive");
            return result;
        }
        
        Employee employee = employeeOpt.get();
        
        // Get door
        Optional<Door> doorOpt = doorRepository.findByDoorIdAndIsActiveTrue(doorId);
        if (doorOpt.isEmpty()) {
            result.setGranted(false);
            result.setReason("Door not found or inactive");
            return result;
        }
        
        Door door = doorOpt.get();
        
        // Check access group permissions
        if (employee.getAccessGroupId() != null) {
            List<AccessGroupDoor> accessGroupDoors = accessGroupDoorRepository
                    .findByIdAccessGroupIdAndIdDoorIdAndIsActiveTrue(employee.getAccessGroupId(), doorId);
            
            boolean hasAllowAccess = false;
            boolean hasDenyAccess = false;
            
            for (AccessGroupDoor agd : accessGroupDoors) {
                if (AccessGroupDoor.AccessType.ALLOW.equals(agd.getAccessType())) {
                    hasAllowAccess = true;
                } else if (AccessGroupDoor.AccessType.DENY.equals(agd.getAccessType())) {
                    hasDenyAccess = true;
                }
            }
            
            // DENY takes precedence
            if (hasDenyAccess) {
                result.setGranted(false);
                result.setReason("Access denied by access group");
                return result;
            }
            
            // Check if ALLOW exists
            if (!hasAllowAccess) {
                result.setGranted(false);
                result.setReason("No access permission for this door");
                return result;
            }
        } else {
            result.setGranted(false);
            result.setReason("Employee has no access group assigned");
            return result;
        }
        
        // All checks passed
        result.setGranted(true);
        result.setCardId(card.getCardId());
        result.setEmployeePk(employee.getEmployeePk());
        result.setCompanyId(employee.getCompanyId());
        result.setDoorType(door.getLockType().name());
        result.setReason("Access granted");
        
        return result;
    }
    
    public Optional<AccessCard> findCardByUid(String cardUid) {
        return accessCardRepository.findByCardUidAndIsActiveTrue(cardUid);
    }
    
    public static class CardValidationResult {
        private boolean granted;
        private String reason;
        private Long cardId;
        private Long employeePk;
        private Long companyId;
        private String doorType;
        
        // Getters and setters
        public boolean isGranted() { return granted; }
        public void setGranted(boolean granted) { this.granted = granted; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public Long getCardId() { return cardId; }
        public void setCardId(Long cardId) { this.cardId = cardId; }
        
        public Long getEmployeePk() { return employeePk; }
        public void setEmployeePk(Long employeePk) { this.employeePk = employeePk; }
        
        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }
        
        public String getDoorType() { return doorType; }
        public void setDoorType(String doorType) { this.doorType = doorType; }
    }
}

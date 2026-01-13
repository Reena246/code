package com.demo.accesscontrolsystem.service;

import com.demo.accesscontrolsystem.dto.ValidateRequest;
import com.demo.accesscontrolsystem.dto.ValidateResponse;
import com.demo.accesscontrolsystem.entity.AccessCard;
import com.demo.accesscontrolsystem.entity.Audit;
import com.demo.accesscontrolsystem.repository.AccessCardRepository;
import com.demo.accesscontrolsystem.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final AccessCardRepository accessCardRepository;
    private final AuditRepository auditRepository;

    public ValidateResponse validateCard(ValidateRequest request) {
        ValidateResponse response = new ValidateResponse();
        LocalDateTime now = LocalDateTime.now();

        AccessCard card = accessCardRepository.findByCardUidAndIsActive(request.getCardUid(), true).orElse(null);

        Audit audit = new Audit();
        audit.setCardId(card != null ? card.getCardId() : null);
        audit.setDoorId(request.getDoorId());
        audit.setReaderId(request.getReaderId());
        audit.setEventTime(now);
        audit.setCreated(now);
        audit.setIsActive(true);

        if (card == null) {
            response.setAccessGranted(false);
            response.setReason("Card not found or inactive");
            audit.setResult(Audit.ResultType.DENIED);
            audit.setReason("Card invalid");
        } else if (card.getExpiresAt() != null && card.getExpiresAt().isBefore(now)) {
            response.setAccessGranted(false);
            response.setReason("Card expired");
            audit.setResult(Audit.ResultType.DENIED);
            audit.setReason("Card expired");
        } else {
            response.setAccessGranted(true);
            response.setReason("Access granted");
            audit.setResult(Audit.ResultType.SUCCESS);
            audit.setReason("Access granted");
            audit.setOpenedAt(now);
            audit.setClosedAt(now.plusSeconds(5)); // simulate door open duration
            audit.setOpenSeconds(5);
        }

        auditRepository.save(audit);
        return response;
    }
}

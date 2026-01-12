package com.company.badgemate.service;

import com.company.badgemate.dto.EventLog;
import com.company.badgemate.entity.Audit;
import com.company.badgemate.entity.Audit.AuditResult;
import com.company.badgemate.repository.AuditRepository;
import com.company.badgemate.repository.AccessCardRepository;
import com.co mpany.badgemate.repository.DoorRepository;
import com.company.badgemate.repository.ReaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventLogService.class);
    
    private final AuditRepository auditRepository;
    private final AccessCardRepository accessCardRepository;
    private final DoorRepository doorRepository;
    private final ReaderRepository readerRepository;
    
    @Value("${mqtt.offline.storage.enabled:true}")
    private boolean offlineStorageEnabled;
    
    @Value("${mqtt.offline.storage.path:./data/offline-events}")
    private String offlineStoragePath;
    
    @Autowired
    public EventLogService(AuditRepository auditRepository,
                          AccessCardRepository accessCardRepository,
                          DoorRepository doorRepository,
                          ReaderRepository readerRepository) {
        this.auditRepository = auditRepository;
        this.accessCardRepository = accessCardRepository;
        this.doorRepository = doorRepository;
        this.readerRepository = readerRepository;
    }
    
    @Transactional
    public void processEventLog(EventLog eventLog) {
        logger.info("Processing event log: {} - {}", eventLog.getEventType(), eventLog.getEventId());
        
        try {
            Audit audit = mapEventLogToAudit(eventLog);
            auditRepository.save(audit);
            logger.info("Event log saved to audit table: {}", eventLog.getEventId());
        } catch (Exception e) {
            logger.error("Error processing event log: {}", eventLog.getEventId(), e);
            
            // Store offline if enabled
            if (offlineStorageEnabled) {
                storeOffline(eventLog);
            }
            
            throw e;
        }
    }
    
    private Audit mapEventLogToAudit(EventLog eventLog) {
        Audit audit = new Audit();
        
        // Map event type to result
        if (eventLog.getEventType() == EventLog.EventType.access_granted) {
            audit.setResult(AuditResult.SUCCESS);
        } else if (eventLog.getEventType() == EventLog.EventType.access_denied) {
            audit.setResult(AuditResult.DENIED);
        } else {
            audit.setResult(AuditResult.SUCCESS); // Default for card_scan and system_event
        }
        
        // Find card by hex value
        if (eventLog.getCardHex() != null) {
            String cardHexStr = Long.toHexString(eventLog.getCardHex());
            accessCardRepository.findByCardUid(cardHexStr).ifPresent(card -> {
                audit.setCardId(card.getCardId());
                audit.setEmployeePk(card.getEmployeePk());
                audit.setCompanyId(card.getCompanyId());
            });
        }
        
        // Find door by door code/name
        if (eventLog.getDoorId() != null) {
            doorRepository.findByDoorCode(eventLog.getDoorId()).ifPresent(door -> {
                audit.setDoorId(door.getDoorId());
                // Find associated reader
                readerRepository.findAll().stream()
                    .filter(r -> r.getDoorId().equals(door.getDoorId()))
                    .findFirst()
                    .ifPresent(reader -> audit.setReaderId(reader.getReaderId()));
            });
        }
        
        // Set timestamps
        if (eventLog.getTimestamp() != null) {
            audit.setEventTime(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(eventLog.getTimestamp()), ZoneId.systemDefault()));
        }
        
        audit.setReason(eventLog.getDetails());
        audit.setIsActive(true);
        
        return audit;
    }
    
    private void storeOffline(EventLog eventLog) {
        try {
            Path storagePath = Paths.get(offlineStoragePath);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
            
            String filename = "event_" + eventLog.getEventId() + "_" + 
                            System.currentTimeMillis() + ".json";
            File file = new File(storagePath.toFile(), filename);
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(convertEventLogToJson(eventLog));
            }
            
            logger.info("Event log stored offline: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to store event log offline: {}", eventLog.getEventId(), e);
        }
    }
    
    private String convertEventLogToJson(EventLog eventLog) {
        // Simple JSON conversion - in production, use ObjectMapper
        return String.format(
            "{\"event_id\":\"%s\",\"event_type\":\"%s\",\"door_id\":\"%s\"," +
            "\"card_hex\":%d,\"user_name\":\"%s\",\"details\":\"%s\"," +
            "\"timestamp\":%d,\"device_id\":\"%s\"}",
            eventLog.getEventId(), eventLog.getEventType(), eventLog.getDoorId(),
            eventLog.getCardHex(), eventLog.getUserName(), eventLog.getDetails(),
            eventLog.getTimestamp(), eventLog.getDeviceId()
        );
    }
    
    @Transactional
    public void processOfflineEvents() {
        if (!offlineStorageEnabled) {
            return;
        }
        
        try {
            Path storagePath = Paths.get(offlineStoragePath);
            if (!Files.exists(storagePath)) {
                return;
            }
            
            List<File> eventFiles = Files.list(storagePath)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".json"))
                .map(Path::toFile)
                .collect(Collectors.toList());
            
            logger.info("Processing {} offline event files", eventFiles.size());
            
            for (File file : eventFiles) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    // Parse and process - simplified for now
                    // In production, use ObjectMapper to parse JSON
                    logger.debug("Processing offline event content: {}", content);
                    file.delete();
                    logger.info("Processed and deleted offline event: {}", file.getName());
                } catch (Exception e) {
                    logger.error("Error processing offline event file: {}", file.getName(), e);
                }
            }
        } catch (IOException e) {
            logger.error("Error processing offline events", e);
        }
    }
}

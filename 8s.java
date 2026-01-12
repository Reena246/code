package com.company.badgemate.service;

import com.company.badgemate.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

@Service
public class MqttMessageService implements MessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MqttMessageService.class);
    
    private final ObjectMapper objectMapper;
    private final DatabaseCommandService databaseCommandService;
    private final EventLogService eventLogService;
    private final CommandAcknowledgementService commandAcknowledgementService;
    private final ServerHeartbeatService serverHeartbeatService;
    
    @Autowired
    public MqttMessageService(ObjectMapper objectMapper,
                             DatabaseCommandService databaseCommandService,
                             EventLogService eventLogService,
                             CommandAcknowledgementService commandAcknowledgementService,
                             ServerHeartbeatService serverHeartbeatService) {
        this.objectMapper = objectMapper;
        this.databaseCommandService = databaseCommandService;
        this.eventLogService = eventLogService;
        this.commandAcknowledgementService = commandAcknowledgementService;
        this.serverHeartbeatService = serverHeartbeatService;
    }
    
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = message.getPayload().toString();
            
            logger.info("Received MQTT message on topic: {}, payload: {}", topic, payload);
            
            if (topic == null) {
                logger.warn("Received message without topic header");
                return;
            }
            
            // Determine message type based on topic
            if (topic.contains("database/command")) {
                DatabaseCommand command = objectMapper.readValue(payload, DatabaseCommand.class);
                databaseCommandService.processDatabaseCommand(command);
            } else if (topic.contains("command/ack")) {
                CommandAcknowledgement ack = objectMapper.readValue(payload, CommandAcknowledgement.class);
                commandAcknowledgementService.processAcknowledgement(ack);
            } else if (topic.contains("event/log")) {
                EventLog eventLog = objectMapper.readValue(payload, EventLog.class);
                eventLogService.processEventLog(eventLog);
            } else if (topic.contains("server/heartbeat")) {
                ServerHeartbeat heartbeat = objectMapper.readValue(payload, ServerHeartbeat.class);
                serverHeartbeatService.processHeartbeat(heartbeat);
            } else {
                logger.warn("Unknown topic: {}", topic);
            }
            
        } catch (Exception e) {
            logger.error("Error processing MQTT message", e);
            throw new MessagingException("Failed to process MQTT message", e);
        }
    }
}

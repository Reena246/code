package com.company.badgemate.simulator;

import com.company.badgemate.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MQTT Simulator for testing BadgeMate access control system
 * 
 * This class simulates a controller device by publishing MQTT messages
 * to the configured broker. It can be run independently or as part of
 * the Spring Boot application.
 * 
 * Usage:
 * - Configure MQTT broker settings in application.properties
 * - Run the Spring Boot application
 * - The simulator will automatically start and send messages
 * - Or run this class directly for standalone simulation
 */
@Component
public class MQTTSimulator implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(MQTTSimulator.class);
    
    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;
    
    @Value("${mqtt.broker.clientId:badgemate-simulator}")
    private String clientId;
    
    @Value("${mqtt.broker.username:}")
    private String username;
    
    @Value("${mqtt.broker.password:}")
    private String password;
    
    @Value("${mqtt.topic.database.command:badgemate/controller/database/command}")
    private String dbCommandTopic;
    
    @Value("${mqtt.topic.command.ack:badgemate/controller/command/ack}")
    private String commandAckTopic;
    
    @Value("${mqtt.topic.event.log:badgemate/controller/event/log}")
    private String eventLogTopic;
    
    @Value("${mqtt.topic.server.heartbeat:badgemate/controller/server/heartbeat}")
    private String heartbeatTopic;
    
    @Value("${mqtt.simulator.enabled:true}")
    private boolean simulatorEnabled;
    
    private MqttClient mqttClient;
    private ObjectMapper objectMapper;
    private ScheduledExecutorService scheduler;
    private Random random;
    
    public MQTTSimulator() {
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
    }
    
    @Override
    public void run(String... args) {
        if (!simulatorEnabled) {
            logger.info("MQTT Simulator is disabled");
            return;
        }
        
        try {
            connect();
            logger.info("MQTT Simulator connected to broker: {}", brokerUrl);
            
            // Start sending messages
            startSimulation();
        } catch (Exception e) {
            logger.error("Error starting MQTT Simulator", e);
        }
    }
    
    private void connect() throws MqttException {
        mqttClient = new MqttClient(brokerUrl, clientId + "_" + UUID.randomUUID().toString());
        MqttConnectOptions options = new MqttConnectOptions();
        
        if (!username.isEmpty()) {
            options.setUserName(username);
        }
        if (!password.isEmpty()) {
            options.setPassword(password.toCharArray());
        }
        
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        
        mqttClient.connect(options);
        
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                logger.warn("MQTT connection lost", cause);
            }
            
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                logger.debug("Received message on topic: {}", topic);
            }
            
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                logger.debug("Message delivery complete");
            }
        });
    }
    
    private void startSimulation() {
        scheduler = Executors.newScheduledThreadPool(4);
        
        // Send DatabaseCommand every 10 seconds
        scheduler.scheduleAtFixedRate(this::sendDatabaseCommand, 5, 10, TimeUnit.SECONDS);
        
        // Send CommandAcknowledgement every 12 seconds
        scheduler.scheduleAtFixedRate(this::sendCommandAcknowledgement, 7, 12, TimeUnit.SECONDS);
        
        // Send EventLog every 3 seconds (more frequent)
        scheduler.scheduleAtFixedRate(this::sendEventLog, 2, 3, TimeUnit.SECONDS);
        
        // Send ServerHeartbeat every 30 seconds
        scheduler.scheduleAtFixedRate(this::sendServerHeartbeat, 1, 30, TimeUnit.SECONDS);
        
        logger.info("MQTT Simulator started - sending messages...");
    }
    
    private void sendDatabaseCommand() {
        try {
            DatabaseCommand command = createDatabaseCommand();
            String json = objectMapper.writeValueAsString(command);
            publish(dbCommandTopic, json);
            logger.info("Sent DatabaseCommand: {}", command.getCommandId());
        } catch (Exception e) {
            logger.error("Error sending DatabaseCommand", e);
        }
    }
    
    private void sendCommandAcknowledgement() {
        try {
            CommandAcknowledgement ack = createCommandAcknowledgement();
            String json = objectMapper.writeValueAsString(ack);
            publish(commandAckTopic, json);
            logger.info("Sent CommandAcknowledgement: {}", ack.getCommandId());
        } catch (Exception e) {
            logger.error("Error sending CommandAcknowledgement", e);
        }
    }
    
    private void sendEventLog() {
        try {
            EventLog eventLog = createEventLog();
            String json = objectMapper.writeValueAsString(eventLog);
            publish(eventLogTopic, json);
            logger.info("Sent EventLog: {} - {}", eventLog.getEventId(), eventLog.getEventType());
        } catch (Exception e) {
            logger.error("Error sending EventLog", e);
        }
    }
    
    private void sendServerHeartbeat() {
        try {
            ServerHeartbeat heartbeat = createServerHeartbeat();
            String json = objectMapper.writeValueAsString(heartbeat);
            publish(heartbeatTopic, json);
            logger.info("Sent ServerHeartbeat: {} - Online: {}", 
                       heartbeat.getDeviceId(), heartbeat.getIsOnline());
        } catch (Exception e) {
            logger.error("Error sending ServerHeartbeat", e);
        }
    }
    
    private DatabaseCommand createDatabaseCommand() {
        DatabaseCommand command = new DatabaseCommand();
        command.setCommandId("cmd_" + UUID.randomUUID().toString().substring(0, 8));
        command.setCommandType(DatabaseCommand.CommandType.INSERT);
        command.setTableName("CardDetails");
        command.setTimestamp(Instant.now().getEpochSecond());
        command.setRetryCount(0);
        
        // Create realistic payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("company_id", 1L);
        payload.put("provider_id", 1L);
        payload.put("employee_pk", random.nextInt(100) + 1);
        payload.put("card_uid", generateCardUid());
        payload.put("card_number", "CARD" + String.format("%06d", random.nextInt(999999)));
        payload.put("issued_at", Instant.now().getEpochSecond());
        payload.put("expires_at", Instant.now().plusSeconds(31536000).getEpochSecond()); // 1 year
        payload.put("is_active", true);
        
        command.setPayload(payload);
        return command;
    }
    
    private CommandAcknowledgement createCommandAcknowledgement() {
        CommandAcknowledgement ack = new CommandAcknowledgement();
        ack.setCommandId("cmd_" + UUID.randomUUID().toString().substring(0, 8));
        ack.setStatus(CommandAcknowledgement.AcknowledgementStatus.applied);
        ack.setReason("Command executed successfully");
        ack.setTimestamp(Instant.now().getEpochSecond());
        ack.setAffectedRows("1");
        return ack;
    }
    
    private EventLog createEventLog() {
        EventLog eventLog = new EventLog();
        eventLog.setEventId("evt_" + UUID.randomUUID().toString().substring(0, 8));
        
        // Random event type
        EventLog.EventType[] types = EventLog.EventType.values();
        eventLog.setEventType(types[random.nextInt(types.length)]);
        
        eventLog.setDoorId("Cubicle Door " + (random.nextInt(10) + 1));
        eventLog.setCardHex(0x9d3b9f1aL + random.nextInt(1000));
        eventLog.setUserName("User " + (random.nextInt(50) + 1));
        eventLog.setDetails("Access attempt at " + Instant.now());
        eventLog.setTimestamp(Instant.now().getEpochSecond());
        eventLog.setDeviceId("pi_zero_00" + (random.nextInt(5) + 1));
        
        return eventLog;
    }
    
    private ServerHeartbeat createServerHeartbeat() {
        ServerHeartbeat heartbeat = new ServerHeartbeat();
        heartbeat.setDeviceId("pi5_local_server");
        heartbeat.setTimestamp(Instant.now().getEpochSecond());
        heartbeat.setIsOnline(true);
        heartbeat.setQueueSize(random.nextInt(50));
        heartbeat.setDbVersionHash("v1.0");
        heartbeat.setUptimeSeconds(3600L + random.nextInt(86400));
        return heartbeat;
    }
    
    private String generateCardUid() {
        return String.format("%08x", random.nextLong() & 0xFFFFFFFFL);
    }
    
    private void publish(String topic, String message) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            logger.warn("MQTT client not connected, attempting to reconnect...");
            connect();
        }
        
        MqttMessage mqttMessage = new MqttMessage(message.getBytes());
        mqttMessage.setQos(1);
        mqttMessage.setRetained(false);
        
        mqttClient.publish(topic, mqttMessage);
    }
    
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
            } catch (MqttException e) {
                logger.error("Error closing MQTT client", e);
            }
        }
    }
}

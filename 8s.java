package com.company.badgemate.config;

import com.company.badgemate.service.MqttMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;

@Configuration
public class MqttMessageHandlerConfig {
    
    @Autowired
    private MqttMessageService mqttMessageService;
    
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MqttMessageService mqttMessageHandler() {
        return mqttMessageService;
    }
}

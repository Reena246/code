1//

package com.example.accesscontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccesscontrolApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccesscontrolApplication.class, args);
    }
}

    
2//



    package com.example.accesscontrol.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }
}

3//

    package com.example.accesscontrol.controller;

import com.example.accesscontrol.dto.AccessRequest;
import com.example.accesscontrol.dto.AccessResponse;
import com.example.accesscontrol.service.AccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/access")
public class AccessController {

    @Autowired
    private AccessService service;

    @PostMapping("/validate")
    public ResponseEntity<AccessResponse> validate(@RequestBody AccessRequest request) {
        return ResponseEntity.ok(service.validate(request));
    }
}

  
4//



    package com.example.accesscontrol.controller;

import com.example.accesscontrol.dto.DoorEventRequest;
import com.example.accesscontrol.dto.DoorEventResponse;
import com.example.accesscontrol.service.DoorEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/door")
public class DoorEventController {

    @Autowired
    private DoorEventService service;

    @PostMapping("/open")
    public ResponseEntity<DoorEventResponse> open(@RequestBody DoorEventRequest req) {
        return ResponseEntity.ok(service.open(req));
    }

    @PostMapping("/close")
    public ResponseEntity<DoorEventResponse> close(@RequestBody DoorEventRequest req) {
        return ResponseEntity.ok(service.close(req));
    }
}


5//

    package com.example.accesscontrol.dto;

public class AccessRequest {
    private String cardId;
    private String doorId;
    private String locationId;

    // getters & setters
    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public String getDoorId() { return doorId; }
    public void setDoorId(String doorId) { this.doorId = doorId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
}


6//

    package com.example.accesscontrol.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public class AccessResponse {
    private boolean accessGranted;
    private String message;
    private LocalDateTime requestReceivedAt;
    private LocalDateTime responseSentAt;
    private long processingTimeMillis;

    public AccessResponse(boolean accessGranted, String message, LocalDateTime req, LocalDateTime res) {
        this.accessGranted = accessGranted;
        this.message = message;
        this.requestReceivedAt = req;
        this.responseSentAt = res;
        this.processingTimeMillis = Duration.between(req, res).toMillis();
    }

    // getters
    public boolean isAccessGranted() { return accessGranted; }
    public String getMessage() { return message; }
    public LocalDateTime getRequestReceivedAt() { return requestReceivedAt; }
    public LocalDateTime getResponseSentAt() { return responseSentAt; }
    public long getProcessingTimeMillis() { return processingTimeMillis; }
}

7//

    package com.example.accesscontrol.dto;

public class DoorEventRequest {
    private String cardId;
    private String doorId;
    private String locationId;

    // getters & setters
    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public String getDoorId() { return doorId; }
    public void setDoorId(String doorId) { this.doorId = doorId; }

    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
}

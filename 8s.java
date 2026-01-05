package com.example.accesscontrol.dto;

import java.time.LocalDateTime;

public class DoorEventResponse {
    private String message;
    private LocalDateTime eventTime;

    public DoorEventResponse(String message, LocalDateTime eventTime) {
        this.message = message;
        this.eventTime = eventTime;
    }

    // getters
    public String getMessage() { return message; }
    public LocalDateTime getEventTime() { return eventTime; }
}


9//

  package com.example.accesscontrol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="access_log")
public class AccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardId;
    private String doorId;
    private String locationId;

    private LocalDateTime requestReceivedAt;
    private LocalDateTime validationCompletedAt;
    private LocalDateTime responseSentAt;

    private Boolean accessGranted;

    // getters & setters
}


10//

  package com.example.accesscontrol.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="door_event_log")
public class DoorEventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardId;
    private String doorId;
    private String locationId;

    private String eventType;
    private String status;
    private LocalDateTime eventTime;

    // getters & setters
}


11//

  package com.example.accesscontrol.entity;

import jakarta.persistence.*;

@Entity
@Table(name="card_access")
public class CardAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardId;
    private String doorId;
    private Boolean isActive;

    // getters & setters
}


12//

  package com.example.accesscontrol.repository;

import com.example.accesscontrol.entity.CardAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardAccessRepository extends JpaRepository<CardAccess, Long> {
    boolean existsByCardIdAndDoorIdAndIsActiveTrue(String cardId, String doorId);
}



13//

  package com.example.accesscontrol.repository;

import com.example.accesscontrol.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> { }



14//

  package com.example.accesscontrol.repository;

import com.example.accesscontrol.entity.DoorEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoorEventLogRepository extends JpaRepository<DoorEventLog, Long> {
    Optional<DoorEventLog> findTopByDoorIdAndStatusOrderByEventTimeDesc(String doorId, String status);
}




15//

  package com.example.accesscontrol.service;

import com.example.accesscontrol.dto.AccessRequest;
import com.example.accesscontrol.dto.AccessResponse;
import com.example.accesscontrol.entity.AccessLog;
import com.example.accesscontrol.repository.AccessLogRepository;
import com.example.accesscontrol.repository.CardAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccessService {

    @Autowired
    private CardAccessRepository cardAccessRepository;

    @Autowired
    private AccessLogRepository accessLogRepository;

    public AccessResponse validate(AccessRequest request) {

        LocalDateTime start = LocalDateTime.now();

        boolean access = cardAccessRepository
                .existsByCardIdAndDoorIdAndIsActiveTrue(
                        request.getCardId(),
                        request.getDoorId());

        AccessLog log = new AccessLog();
        log.setCardId(request.getCardId());
        log.setDoorId(request.getDoorId());
        log.setLocationId(request.getLocationId());
        log.setRequestReceivedAt(start);
        log.setValidationCompletedAt(LocalDateTime.now());
        log.setResponseSentAt(LocalDateTime.now());
        log.setAccessGranted(access);

        accessLogRepository.save(log);

        return new AccessResponse(
                access,
                access ? "Access granted" : "Access denied",
                start,
                log.getResponseSentAt());
    }
}




16//


  package com.example.accesscontrol.service;

import com.example.accesscontrol.dto.DoorEventRequest;
import com.example.accesscontrol.dto.DoorEventResponse;
import com.example.accesscontrol.entity.DoorEventLog;
import com.example.accesscontrol.repository.DoorEventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class DoorEventService {

    @Value("${door.open.timeout.seconds}")
    private int timeoutSeconds;

    @Autowired
    private DoorEventLogRepository repo;

    @Autowired
    private TaskScheduler scheduler;

    public DoorEventResponse open(DoorEventRequest req) {

        LocalDateTime now = LocalDateTime.now();

        DoorEventLog log = new DoorEventLog();
        log.setCardId(req.getCardId());
        log.setDoorId(req.getDoorId());
        log.setLocationId(req.getLocationId());
        log.setEventType("OPEN");
        log.setStatus("OPENED");
        log.setEventTime(now);

        repo.save(log);

        scheduler.schedule(
                () -> timeout(log.getId()),
                Instant.now().plusSeconds(timeoutSeconds));

        return new DoorEventResponse("Door opened", now);
    }

    public DoorEventResponse close(DoorEventRequest req) {

        repo.findTopByDoorIdAndStatusOrderByEventTimeDesc(
                req.getDoorId(), "OPENED")
            .ifPresent(log -> {
                log.setStatus("CLOSED");
                log.setEventType("CLOSE");
                log.setEventTime(LocalDateTime.now());
                repo.save(log);
            });

        return new DoorEventResponse("Door closed", LocalDateTime.now());
    }

    private void timeout(Long id) {
        repo.findById(id).ifPresent(log -> {
            if ("OPENED".equals(log.getStatus())) {
                log.setStatus("TIMED_OUT");
                log.setEventType("TIMEOUT");
                log.setEventTime(LocalDateTime.now());
                repo.save(log);
            }
        });
    }
}






17//


  spring.datasource.url=jdbc:mysql://localhost:3306/access_control
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

server.port=8080

door.open.timeout.seconds=10





  

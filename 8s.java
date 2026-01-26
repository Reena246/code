package com.accesscontrol.service;

import com.accesscontrol.dto.DbSyncRequest;
import com.accesscontrol.dto.DbSyncResponse;
import com.accesscontrol.entity.*;
import com.accesscontrol.exception.ControllerNotFoundException;
import com.accesscontrol.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for database synchronization with controllers
 * Provides minimal payloads with reader-card mappings
 */
@Service
public class DbSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DbSyncService.class);

    private final ControllerRepository controllerRepository;
    private final ReaderRepository readerRepository;
    private final AccessCardRepository accessCardRepository;
    private final EmployeeRepository employeeRepository;
    private final AccessGroupDoorRepository accessGroupDoorRepository;

    public DbSyncService(ControllerRepository controllerRepository,
                        ReaderRepository readerRepository,
                        AccessCardRepository accessCardRepository,
                        EmployeeRepository employeeRepository,
                        AccessGroupDoorRepository accessGroupDoorRepository) {
        this.controllerRepository = controllerRepository;
        this.readerRepository = readerRepository;
        this.accessCardRepository = accessCardRepository;
        this.employeeRepository = employeeRepository;
        this.accessGroupDoorRepository = accessGroupDoorRepository;
    }

    @Transactional(readOnly = true)
    public DbSyncResponse syncDatabase(DbSyncRequest request) {
        logger.info("DB sync requested - controller_mac: {}", request.getControllerMac());

        // Validate controller
        Controller controller = controllerRepository
                .findByControllerMacAndIsActive(request.getControllerMac(), true)
                .orElseThrow(() -> new ControllerNotFoundException(
                        "Controller not found or inactive: " + request.getControllerMac()));

        // Get all readers for this controller
        List<Reader> readers = readerRepository
                .findByControllerIdAndIsActive(controller.getControllerId(), true);

        List<DbSyncResponse.ReaderSync> readerSyncs = new ArrayList<>();

        for (Reader reader : readers) {
            if (reader.getDoorId() == null) {
                // Skip readers without doors
                continue;
            }

            // Get allowed cards for this door
            List<String> allowedCards = getAllowedCardsForDoor(reader.getDoorId());

            DbSyncResponse.ReaderSync readerSync = new DbSyncResponse.ReaderSync(
                    reader.getReaderUuid(),
                    allowedCards
            );
            readerSyncs.add(readerSync);

            logger.debug("Reader: {} - Allowed cards: {}", 
                    reader.getReaderUuid(), allowedCards.size());
        }

        logger.info("DB sync completed - controller_mac: {}, readers: {}, total_cards: {}",
                request.getControllerMac(),
                readerSyncs.size(),
                readerSyncs.stream().mapToInt(r -> r.getAllowedCards().size()).sum());

        return new DbSyncResponse(readerSyncs);
    }

    private List<String> getAllowedCardsForDoor(Long doorId) {
        Set<String> allowedCards = new HashSet<>();

        // Get all access groups that have ALLOW access to this door
        List<AccessGroupDoor> accessGroupDoors = accessGroupDoorRepository
                .findAll()
                .stream()
                .filter(agd -> agd.getDoorId().equals(doorId) 
                        && agd.getIsActive()
                        && agd.getAccessType() == AccessGroupDoor.AccessType.ALLOW)
                .toList();

        for (AccessGroupDoor agd : accessGroupDoors) {
            // Get all employees in this access group
            List<Employee> employees = employeeRepository
                    .findAll()
                    .stream()
                    .filter(e -> e.getAccessGroupId() != null
                            && e.getAccessGroupId().equals(agd.getAccessGroupId())
                            && e.getIsActive())
                    .toList();

            // Get all valid cards for these employees
            for (Employee employee : employees) {
                List<AccessCard> cards = accessCardRepository
                        .findByEmployeePkAndIsActive(employee.getEmployeePk(), true)
                        .stream()
                        .filter(card -> card.getExpiresAt() == null 
                                || card.getExpiresAt().isAfter(LocalDateTime.now()))
                        .toList();

                for (AccessCard card : cards) {
                    allowedCards.add(card.getCardHex());
                }
            }
        }

        return new ArrayList<>(allowedCards);
    }
}

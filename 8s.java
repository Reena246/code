package com.company.badgemate.service;

import com.company.badgemate.dto.DatabaseCommand;
import com.company.badgemate.entity.*;
import com.company.badgemate.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
public class DatabaseCommandService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCommandService.class);
    
    private final CompanyRepository companyRepository;
    private final CardProviderRepository cardProviderRepository;
    private final SiteRepository siteRepository;
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final DoorRepository doorRepository;
    private final DoorLockRepository doorLockRepository;
    private final ReaderRepository readerRepository;
    private final JobTitleRepository jobTitleRepository;
    private final AccessGroupRepository accessGroupRepository;
    private final AccessGroupDoorRepository accessGroupDoorRepository;
    private final EmployeeRepository employeeRepository;
    private final AccessCardRepository accessCardRepository;
    
    @Autowired
    public DatabaseCommandService(CompanyRepository companyRepository,
                                  CardProviderRepository cardProviderRepository,
                                  SiteRepository siteRepository,
                                  BuildingRepository buildingRepository,
                                  FloorRepository floorRepository,
                                  DoorRepository doorRepository,
                                  DoorLockRepository doorLockRepository,
                                  ReaderRepository readerRepository,
                                  JobTitleRepository jobTitleRepository,
                                  AccessGroupRepository accessGroupRepository,
                                  AccessGroupDoorRepository accessGroupDoorRepository,
                                  EmployeeRepository employeeRepository,
                                  AccessCardRepository accessCardRepository) {
        this.companyRepository = companyRepository;
        this.cardProviderRepository = cardProviderRepository;
        this.siteRepository = siteRepository;
        this.buildingRepository = buildingRepository;
        this.floorRepository = floorRepository;
        this.doorRepository = doorRepository;
        this.doorLockRepository = doorLockRepository;
        this.readerRepository = readerRepository;
        this.jobTitleRepository = jobTitleRepository;
        this.accessGroupRepository = accessGroupRepository;
        this.accessGroupDoorRepository = accessGroupDoorRepository;
        this.employeeRepository = employeeRepository;
        this.accessCardRepository = accessCardRepository;
    }
    
    @Transactional
    public void processDatabaseCommand(DatabaseCommand command) {
        logger.info("Processing database command: {} for table: {}", 
                   command.getCommandType(), command.getTableName());
        
        try {
            switch (command.getCommandType()) {
                case INSERT:
                    handleInsert(command);
                    break;
                case UPDATE:
                    handleUpdate(command);
                    break;
                case DELETE:
                    handleDelete(command);
                    break;
                case SYNC:
                case SYNC_RESPONSE:
                    handleSync(command);
                    break;
                default:
                    logger.warn("Unknown command type: {}", command.getCommandType());
            }
        } catch (Exception e) {
            logger.error("Error processing database command: {}", command.getCommandId(), e);
            throw e;
        }
    }
    
    private void handleInsert(DatabaseCommand command) {
        Map<String, Object> payload = command.getPayload();
        String tableName = command.getTableName();
        
        switch (tableName) {
            case "Company":
                companyRepository.save(mapToCompany(payload));
                break;
            case "CardProvider":
                cardProviderRepository.save(mapToCardProvider(payload));
                break;
            case "Site":
                siteRepository.save(mapToSite(payload));
                break;
            case "Building":
                buildingRepository.save(mapToBuilding(payload));
                break;
            case "Floor":
                floorRepository.save(mapToFloor(payload));
                break;
            case "Door":
                doorRepository.save(mapToDoor(payload));
                break;
            case "DoorLock":
                doorLockRepository.save(mapToDoorLock(payload));
                break;
            case "Reader":
                readerRepository.save(mapToReader(payload));
                break;
            case "JobTitle":
                jobTitleRepository.save(mapToJobTitle(payload));
                break;
            case "AccessGroup":
                accessGroupRepository.save(mapToAccessGroup(payload));
                break;
            case "AccessGroupDoor":
                accessGroupDoorRepository.save(mapToAccessGroupDoor(payload));
                break;
            case "Employee":
                employeeRepository.save(mapToEmployee(payload));
                break;
            case "CardDetails":
            case "AccessCard":
                accessCardRepository.save(mapToAccessCard(payload));
                break;
            default:
                logger.warn("Unknown table name for INSERT: {}", tableName);
        }
    }
    
    private void handleUpdate(DatabaseCommand command) {
        String tableName = command.getTableName();
        
        // Similar to insert but with ID lookup first
        logger.info("UPDATE operation for table: {} with payload: {}", 
                   tableName, command.getPayload());
        // Implementation would update existing entities
    }
    
    private void handleDelete(DatabaseCommand command) {
        String tableName = command.getTableName();
        
        logger.info("DELETE operation for table: {} with payload: {}", 
                   tableName, command.getPayload());
        // Implementation would delete entities
    }
    
    private void handleSync(DatabaseCommand command) {
        logger.info("SYNC operation for table: {}", command.getTableName());
        // Implementation would handle synchronization
    }
    
    // Mapper methods
    private Company mapToCompany(Map<String, Object> payload) {
        Company company = new Company();
        if (payload.containsKey("company_id")) {
            company.setCompanyId(Long.valueOf(payload.get("company_id").toString()));
        }
        company.setCompanyName((String) payload.get("company_name"));
        company.setIsActive(payload.containsKey("is_active") ? 
                           Boolean.valueOf(payload.get("is_active").toString()) : true);
        return company;
    }
    
    private CardProvider mapToCardProvider(Map<String, Object> payload) {
        CardProvider provider = new CardProvider();
        provider.setCompanyId(Long.valueOf(payload.get("company_id").toString()));
        provider.setProviderName((String) payload.get("provider_name"));
        provider.setIsActive(payload.containsKey("is_active") ? 
                            Boolean.valueOf(payload.get("is_active").toString()) : true);
        return provider;
    }
    
    private Site mapToSite(Map<String, Object> payload) {
        Site site = new Site();
        site.setCompanyId(Long.valueOf(payload.get("company_id").toString()));
        site.setSiteName((String) payload.get("site_name"));
        site.setAddressLine1((String) payload.get("address_line1"));
        site.setCity((String) payload.get("city"));
        site.setState((String) payload.get("state"));
        site.setCountry((String) payload.get("country"));
        site.setIsActive(payload.containsKey("is_active") ? 
                        Boolean.valueOf(payload.get("is_active").toString()) : true);
        return site;
    }
    
    private Building mapToBuilding(Map<String, Object> payload) {
        Building building = new Building();
        building.setSiteId(Long.valueOf(payload.get("site_id").toString()));
        building.setBuildingName((String) payload.get("building_name"));
        building.setIsActive(payload.containsKey("is_active") ? 
                           Boolean.valueOf(payload.get("is_active").toString()) : true);
        return building;
    }
    
    private Floor mapToFloor(Map<String, Object> payload) {
        Floor floor = new Floor();
        floor.setBuildingId(Long.valueOf(payload.get("building_id").toString()));
        floor.setFloorNumber(Short.valueOf(payload.get("floor_number").toString()));
        floor.setFloorName((String) payload.get("floor_name"));
        floor.setIsActive(payload.containsKey("is_active") ? 
                         Boolean.valueOf(payload.get("is_active").toString()) : true);
        return floor;
    }
    
    private Door mapToDoor(Map<String, Object> payload) {
        Door door = new Door();
        door.setFloorId(Long.valueOf(payload.get("floor_id").toString()));
        door.setDoorCode((String) payload.get("door_code"));
        door.setDoorNumber(Short.valueOf(payload.get("door_number").toString()));
        door.setLockType(Door.LockType.valueOf((String) payload.get("lock_type")));
        door.setIsActive(payload.containsKey("is_active") ? 
                        Boolean.valueOf(payload.get("is_active").toString()) : true);
        return door;
    }
    
    private DoorLock mapToDoorLock(Map<String, Object> payload) {
        DoorLock doorLock = new DoorLock();
        doorLock.setDoorId(Long.valueOf(payload.get("door_id").toString()));
        doorLock.setLockType(DoorLock.LockType.valueOf((String) payload.get("lock_type")));
        if (payload.containsKey("status")) {
            doorLock.setStatus(DoorLock.LockStatus.valueOf((String) payload.get("status")));
        }
        doorLock.setIsActive(payload.containsKey("is_active") ? 
                            Boolean.valueOf(payload.get("is_active").toString()) : true);
        return doorLock;
    }
    
    private Reader mapToReader(Map<String, Object> payload) {
        Reader reader = new Reader();
        reader.setDoorId(Long.valueOf(payload.get("door_id").toString()));
        reader.setReaderCode((String) payload.get("reader_code"));
        reader.setIsActive(payload.containsKey("is_active") ? 
                          Boolean.valueOf(payload.get("is_active").toString()) : true);
        return reader;
    }
    
    private JobTitle mapToJobTitle(Map<String, Object> payload) {
        JobTitle jobTitle = new JobTitle();
        jobTitle.setCompanyId(Long.valueOf(payload.get("company_id").toString()));
        jobTitle.setTitleName((String) payload.get("title_name"));
        jobTitle.setIsActive(payload.containsKey("is_active") ? 
                            Boolean.valueOf(payload.get("is_active").toString()) : true);
        return jobTitle;
    }
    
    private AccessGroup mapToAccessGroup(Map<String, Object> payload) {
        AccessGroup group = new AccessGroup();
        group.setCompanyId(Long.valueOf(payload.get("company_id").toString()));
        group.setGroupName((String) payload.get("group_name"));
        group.setDescription((String) payload.get("description"));
        group.setIsActive(payload.containsKey("is_active") ? 
                         Boolean.valueOf(payload.get("is_active").toString()) : true);
        return group;
    }
    
    private AccessGroupDoor mapToAccessGroupDoor(Map<String, Object> payload) {
        AccessGroupDoor agd = new AccessGroupDoor();
        agd.setAccessGroupId(Long.valueOf(payload.get("access_group_id").toString()));
        agd.setDoorId(Long.valueOf(payload.get("door_id").toString()));
        agd.setAccessType(AccessGroupDoor.AccessType.valueOf((String) payload.get("access_type")));
        agd.setIsActive(payload.containsKey("is_active") ? 
                       Boolean.valueOf(payload.get("is_active").toString()) : true);
        return agd;
    }
    
    private Employee mapToEmployee(Map<String, Object> payload) {
        Employee employee = new Employee();
        employee.setEmployeeCode((String) payload.get("employee_code"));
        employee.setCompanyId(Long.valueOf(payload.get("company_id").toString()));
        employee.setFullName((String) payload.get("full_name"));
        employee.setEmail((String) payload.get("email"));
        if (payload.containsKey("job_title_id")) {
            employee.setJobTitleId(Long.valueOf(payload.get("job_title_id").toString()));
        }
        if (payload.containsKey("access_group_id")) {
            employee.setAccessGroupId(Long.valueOf(payload.get("access_group_id").toString()));
        }
        employee.setIsActive(payload.containsKey("is_active") ? 
                            Boolean.valueOf(payload.get("is_active").toString()) : true);
        return employee;
    }
    
    private AccessCard mapToAccessCard(Map<String, Object> payload) {
        AccessCard card = new AccessCard();
        card.setCompanyId(Long.valueOf(payload.get("company_id").toString()));
        card.setProviderId(Long.valueOf(payload.get("provider_id").toString()));
        card.setEmployeePk(Long.valueOf(payload.get("employee_pk").toString()));
        card.setCardUid((String) payload.get("card_uid"));
        card.setCardNumber((String) payload.get("card_number"));
        
        if (payload.containsKey("issued_at")) {
            card.setIssuedAt(parseDateTime(payload.get("issued_at")));
        }
        if (payload.containsKey("expires_at")) {
            card.setExpiresAt(parseDateTime(payload.get("expires_at")));
        }
        
        card.setIsActive(payload.containsKey("is_active") ? 
                        Boolean.valueOf(payload.get("is_active").toString()) : true);
        return card;
    }
    
    private LocalDateTime parseDateTime(Object dateTime) {
        if (dateTime instanceof Long) {
            return LocalDateTime.ofInstant(Instant.ofEpochSecond((Long) dateTime), ZoneId.systemDefault());
        } else if (dateTime instanceof String) {
            return LocalDateTime.parse((String) dateTime);
        }
        return LocalDateTime.now();
    }
}

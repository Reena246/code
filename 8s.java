package com.project.badgemate.service;

import com.project.badgemate.dto.CommandAcknowledgement;
import com.project.badgemate.dto.DatabaseCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatabaseCommandService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCommandService.class);
    
    private final EntityManager entityManager;
    
    @Autowired
    public DatabaseCommandService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Transactional
    public CommandAcknowledgement processDatabaseCommand(DatabaseCommand command) {
        logger.info("Processing database command: {} for table: {}", 
                   command.getCommandType(), command.getTableName());
        
        String commandId = command.getCommandId();
        long timestamp = command.getTimestamp() != null ? command.getTimestamp() : Instant.now().getEpochSecond();
        
        try {
            // Validate table exists
            if (!tableExists(command.getTableName())) {
                return createAcknowledgement(commandId, CommandAcknowledgement.AcknowledgementStatus.failed,
                    "Table '" + command.getTableName() + "' does not exist", timestamp, "0");
            }
            
            // Validate payload is not empty
            if (command.getPayload() == null || command.getPayload().isEmpty()) {
                return createAcknowledgement(commandId, CommandAcknowledgement.AcknowledgementStatus.failed,
                    "Payload cannot be empty", timestamp, "0");
            }
            
            int affectedRows = 0;
            String reason = "Command executed successfully";
            
            switch (command.getCommandType()) {
                case INSERT:
                    affectedRows = handleInsert(command);
                    break;
                case UPDATE:
                    affectedRows = handleUpdate(command);
                    break;
                case DELETE:
                    affectedRows = handleDelete(command);
                    break;
                case SYNC:
                case SYNC_RESPONSE:
                    affectedRows = handleSync(command);
                    reason = "Sync operation completed";
                    break;
                default:
                    return createAcknowledgement(commandId, CommandAcknowledgement.AcknowledgementStatus.failed,
                        "Unknown command type: " + command.getCommandType(), timestamp, "0");
            }
            
            return createAcknowledgement(commandId, CommandAcknowledgement.AcknowledgementStatus.applied,
                reason, timestamp, String.valueOf(affectedRows));
                
        } catch (PersistenceException e) {
            logger.error("Database error processing command: {}", commandId, e);
            String errorMessage = extractErrorMessage(e);
            return createAcknowledgement(commandId, CommandAcknowledgement.AcknowledgementStatus.failed,
                "Database error: " + errorMessage, timestamp, "0");
        } catch (IllegalArgumentException e) {
            logger.error("Validation error processing command: {}", commandId, e);
            return createAcknowledgement(commandId, CommandAcknowledgement.AcknowledgementStatus.failed,
                "Validation error: " + e.getMessage(), timestamp, "0");
        } catch (Exception e) {
            logger.error("Unexpected error processing command: {}", commandId, e);
            return createAcknowledgement(commandId, CommandAcknowledgement.AcknowledgementStatus.failed,
                "Unexpected error: " + e.getMessage(), timestamp, "0");
        }
    }
    
    private int handleInsert(DatabaseCommand command) {
        String tableName = command.getTableName();
        Map<String, Object> payload = command.getPayload();
        
        // Get table columns
        List<String> columns = getTableColumns(tableName);
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Cannot retrieve columns for table: " + tableName);
        }
        
        // Filter payload to only include valid columns
        Map<String, Object> validPayload = filterValidColumns(payload, columns, tableName);
        
        if (validPayload.isEmpty()) {
            throw new IllegalArgumentException("No valid columns found in payload for table: " + tableName);
        }
        
        // Build INSERT query
        String columnNames = String.join(", ", validPayload.keySet());
        String placeholders = validPayload.keySet().stream()
            .map(k -> "?")
            .collect(Collectors.joining(", "));
        
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", 
            escapeTableName(tableName), columnNames, placeholders);
        
        logger.debug("Executing INSERT: {}", sql);
        
        Query query = entityManager.createNativeQuery(sql);
        int paramIndex = 1;
        for (Object value : validPayload.values()) {
            query.setParameter(paramIndex++, convertValue(value));
        }
        
        return query.executeUpdate();
    }
    
    private int handleUpdate(DatabaseCommand command) {
        String tableName = command.getTableName();
        Map<String, Object> payload = command.getPayload();
        
        // Get table columns
        List<String> columns = getTableColumns(tableName);
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Cannot retrieve columns for table: " + tableName);
        }
        
        // Separate WHERE conditions from SET values
        // Assume primary key is in payload (e.g., id, table_name_id, etc.)
        String primaryKeyColumn = findPrimaryKeyColumn(tableName);
        if (primaryKeyColumn == null || !payload.containsKey(primaryKeyColumn)) {
            throw new IllegalArgumentException("Primary key column not found in payload for UPDATE operation. " +
                "Expected column: " + primaryKeyColumn + " or common ID columns");
        }
        
        Object primaryKeyValue = payload.get(primaryKeyColumn);
        
        // Remove primary key from SET clause
        Map<String, Object> updatePayload = new HashMap<>(payload);
        updatePayload.remove(primaryKeyColumn);
        
        // Filter to valid columns
        Map<String, Object> validPayload = filterValidColumns(updatePayload, columns, tableName);
        
        if (validPayload.isEmpty()) {
            throw new IllegalArgumentException("No valid columns to update for table: " + tableName);
        }
        
        // Build UPDATE query
        String setClause = validPayload.keySet().stream()
            .map(col -> col + " = ?")
            .collect(Collectors.joining(", "));
        
        String sql = String.format("UPDATE %s SET %s WHERE %s = ?",
            escapeTableName(tableName), setClause, escapeColumnName(primaryKeyColumn));
        
        logger.debug("Executing UPDATE: {}", sql);
        
        Query query = entityManager.createNativeQuery(sql);
        int paramIndex = 1;
        for (Object value : validPayload.values()) {
            query.setParameter(paramIndex++, convertValue(value));
        }
        query.setParameter(paramIndex, convertValue(primaryKeyValue));
        
        return query.executeUpdate();
    }
    
    private int handleDelete(DatabaseCommand command) {
        String tableName = command.getTableName();
        Map<String, Object> payload = command.getPayload();
        
        if (payload.isEmpty()) {
            throw new IllegalArgumentException("Payload cannot be empty for DELETE operation");
        }
        
        // Get table columns
        List<String> columns = getTableColumns(tableName);
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("Cannot retrieve columns for table: " + tableName);
        }
        
        // Find primary key or use first available key
        String keyColumn = findPrimaryKeyColumn(tableName);
        if (keyColumn == null || !payload.containsKey(keyColumn)) {
            // Try common ID column names
            for (String commonId : Arrays.asList("id", tableName.toLowerCase() + "_id", "pk")) {
                if (payload.containsKey(commonId) && columns.contains(commonId)) {
                    keyColumn = commonId;
                    break;
                }
            }
        }
        
        if (keyColumn == null || !payload.containsKey(keyColumn)) {
            throw new IllegalArgumentException("Primary key or ID column not found in payload for DELETE operation");
        }
        
        Object keyValue = payload.get(keyColumn);
        
        // Build DELETE query
        String sql = String.format("DELETE FROM %s WHERE %s = ?",
            escapeTableName(tableName), escapeColumnName(keyColumn));
        
        logger.debug("Executing DELETE: {}", sql);
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter(1, convertValue(keyValue));
        
        return query.executeUpdate();
    }
    
    private int handleSync(DatabaseCommand command) {
        logger.info("SYNC operation for table: {}", command.getTableName());
        // For now, return 0 as sync operations may need custom logic
        return 0;
    }
    
    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, tableName.toLowerCase());
            Object result = query.getSingleResult();
            return ((Number) result).intValue() > 0;
        } catch (Exception e) {
            logger.error("Error checking if table exists: {}", tableName, e);
            return false;
        }
    }
    
    private List<String> getTableColumns(String tableName) {
        try {
            String sql = "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = ? " +
                        "ORDER BY ordinal_position";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, tableName.toLowerCase());
            
            @SuppressWarnings("unchecked")
            List<Object> results = query.getResultList();
            return results.stream()
                .map(obj -> obj.toString())
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving columns for table: {}", tableName, e);
            return Collections.emptyList();
        }
    }
    
    private String findPrimaryKeyColumn(String tableName) {
        try {
            String sql = "SELECT column_name FROM information_schema.key_column_usage " +
                        "WHERE table_schema = DATABASE() AND table_name = ? " +
                        "AND constraint_name = 'PRIMARY' " +
                        "LIMIT 1";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, tableName.toLowerCase());
            
            Object result = query.getSingleResult();
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            logger.debug("Error finding primary key for table: {}", tableName, e);
            // Try common primary key names
            return "id";
        }
    }
    
    private Map<String, Object> filterValidColumns(Map<String, Object> payload, 
                                                   List<String> validColumns, 
                                                   String tableName) {
        Map<String, Object> filtered = new HashMap<>();
        Set<String> validColumnSet = validColumns.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String columnName = entry.getKey().toLowerCase();
            if (validColumnSet.contains(columnName)) {
                // Find the actual column name (case-sensitive from DB)
                String actualColumnName = validColumns.stream()
                    .filter(col -> col.equalsIgnoreCase(columnName))
                    .findFirst()
                    .orElse(entry.getKey());
                filtered.put(actualColumnName, entry.getValue());
            } else {
                logger.warn("Column '{}' not found in table '{}', skipping", entry.getKey(), tableName);
            }
        }
        
        return filtered;
    }
    
    private Object convertValue(Object value) {
        if (value == null) {
            return null;
        }
        
        // Handle common type conversions
        if (value instanceof Boolean) {
            return value;
        }
        if (value instanceof Number) {
            return value;
        }
        if (value instanceof String) {
            return value;
        }
        
        // Convert to string as fallback
        return value.toString();
    }
    
    private String escapeTableName(String tableName) {
        // Basic escaping - in production, use proper SQL escaping
        return "`" + tableName + "`";
    }
    
    private String escapeColumnName(String columnName) {
        return "`" + columnName + "`";
    }
    
    private String extractErrorMessage(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException) {
            SQLException sqlEx = (SQLException) cause;
            return sqlEx.getMessage();
        }
        return e.getMessage();
    }
    
    private CommandAcknowledgement createAcknowledgement(String commandId,
                                                        CommandAcknowledgement.AcknowledgementStatus status,
                                                        String reason,
                                                        long timestamp,
                                                        String affectedRows) {
        CommandAcknowledgement ack = new CommandAcknowledgement();
        ack.setCommandId(commandId);
        ack.setStatus(status);
        ack.setReason(reason);
        ack.setTimestamp(timestamp);
        ack.setAffectedRows(affectedRows);
        return ack;
    }
}



package com.project.badgemate.controller;

import com.project.badgemate.dto.CommandAcknowledgement;
import com.project.badgemate.dto.DatabaseCommand;
import com.project.badgemate.service.DatabaseCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/database-command")
@Tag(name = "Database Command", description = "API for handling dynamic database commands on any table")
public class DatabaseCommandController {
    
    private final DatabaseCommandService databaseCommandService;
    
    @Autowired
    public DatabaseCommandController(DatabaseCommandService databaseCommandService) {
        this.databaseCommandService = databaseCommandService;
    }
    
    @PostMapping
    @Operation(
        summary = "Process a database command", 
        description = "Accepts a database command and dynamically executes INSERT, UPDATE, or DELETE on any table. " +
                      "The payload keys are automatically mapped to table columns. Returns CommandAcknowledgement with status and affected rows."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Command processed successfully",
            content = @Content(schema = @Schema(implementation = CommandAcknowledgement.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid command or validation error",
            content = @Content(schema = @Schema(implementation = CommandAcknowledgement.class))
        )
    })
    public ResponseEntity<CommandAcknowledgement> processCommand(@RequestBody DatabaseCommand command) {
        // Validate required fields
        if (command.getCommandId() == null || command.getCommandId().isEmpty()) {
            CommandAcknowledgement errorAck = new CommandAcknowledgement();
            errorAck.setCommandId(command.getCommandId() != null ? command.getCommandId() : "unknown");
            errorAck.setStatus(CommandAcknowledgement.AcknowledgementStatus.failed);
            errorAck.setReason("command_id is required");
            errorAck.setTimestamp(System.currentTimeMillis() / 1000);
            errorAck.setAffectedRows("0");
            return ResponseEntity.badRequest().body(errorAck);
        }
        
        if (command.getCommandType() == null) {
            CommandAcknowledgement errorAck = new CommandAcknowledgement();
            errorAck.setCommandId(command.getCommandId());
            errorAck.setStatus(CommandAcknowledgement.AcknowledgementStatus.failed);
            errorAck.setReason("command_type is required");
            errorAck.setTimestamp(System.currentTimeMillis() / 1000);
            errorAck.setAffectedRows("0");
            return ResponseEntity.badRequest().body(errorAck);
        }
        
        if (command.getTableName() == null || command.getTableName().isEmpty()) {
            CommandAcknowledgement errorAck = new CommandAcknowledgement();
            errorAck.setCommandId(command.getCommandId());
            errorAck.setStatus(CommandAcknowledgement.AcknowledgementStatus.failed);
            errorAck.setReason("table_name is required");
            errorAck.setTimestamp(System.currentTimeMillis() / 1000);
            errorAck.setAffectedRows("0");
            return ResponseEntity.badRequest().body(errorAck);
        }
        
        // Process the command
        CommandAcknowledgement acknowledgement = databaseCommandService.processDatabaseCommand(command);
        
        // Return appropriate HTTP status based on acknowledgement status
        if (acknowledgement.getStatus() == CommandAcknowledgement.AcknowledgementStatus.applied) {
            return ResponseEntity.ok(acknowledgement);
        } else {
            return ResponseEntity.badRequest().body(acknowledgement);
        }
    }
}


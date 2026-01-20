package com.project.accesscontrol.service;

import com.project.accesscontrol.dto.DatabaseCommand;
import com.project.accesscontrol.dto.CommandAcknowledgement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class DatabaseCommandService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Transactional
    public CommandAcknowledgement processCommand(DatabaseCommand command) {
        CommandAcknowledgement ack = new CommandAcknowledgement();
        ack.setCommandId(command.getCommandId());
        ack.setTimestamp(System.currentTimeMillis());
        
        try {
            int affectedRows = 0;
            
            switch (command.getCommandType().toUpperCase()) {
                case "INSERT":
                    affectedRows = executeInsert(command.getTableName(), command.getPayload());
                    break;
                case "UPDATE":
                    affectedRows = executeUpdate(command.getTableName(), command.getPayload());
                    break;
                case "DELETE":
                    affectedRows = executeDelete(command.getTableName(), command.getPayload());
                    break;
                case "SYNC":
                case "SYNC_RESPONSE":
                    // Handle sync commands if needed
                    ack.setStatus("applied");
                    ack.setReason("Sync command processed");
                    ack.setAffectedRows(0);
                    return ack;
                default:
                    throw new IllegalArgumentException("Unknown command type: " + command.getCommandType());
            }
            
            ack.setStatus("applied");
            ack.setReason("Command executed successfully");
            ack.setAffectedRows(affectedRows);
            
        } catch (Exception e) {
            ack.setStatus("failed");
            ack.setReason("Error: " + e.getMessage());
            ack.setAffectedRows(0);
        }
        
        return ack;
    }
    
    private int executeInsert(String tableName, Map<String, Object> payload) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");
        
        StringBuilder values = new StringBuilder(" VALUES (");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (!first) {
                sql.append(", ");
                values.append(", ");
            }
            sql.append(entry.getKey());
            values.append("?");
            first = false;
        }
        
        sql.append(")").append(values).append(")");
        
        Object[] params = payload.values().toArray();
        return jdbcTemplate.update(sql.toString(), params);
    }
    
    private int executeUpdate(String tableName, Map<String, Object> payload) {
        // Extract ID field (assuming first field or a specific pattern)
        String idField = tableName.substring(0, tableName.length() - 1) + "_id"; // e.g., "access_card" -> "card_id"
        if (!payload.containsKey(idField)) {
            // Try common ID patterns
            if (payload.containsKey("id")) {
                idField = "id";
            } else {
                throw new IllegalArgumentException("ID field not found in payload for UPDATE");
            }
        }
        
        Object idValue = payload.remove(idField);
        
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName).append(" SET ");
        
        boolean first = true;
        for (String key : payload.keySet()) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(key).append(" = ?");
            first = false;
        }
        
        sql.append(" WHERE ").append(idField).append(" = ?");
        
        Object[] params = new Object[payload.size() + 1];
        int i = 0;
        for (Object value : payload.values()) {
            params[i++] = value;
        }
        params[i] = idValue;
        
        return jdbcTemplate.update(sql.toString(), params);
    }
    
    private int executeDelete(String tableName, Map<String, Object> payload) {
        // Extract ID field
        String idField = tableName.substring(0, tableName.length() - 1) + "_id";
        if (!payload.containsKey(idField)) {
            if (payload.containsKey("id")) {
                idField = "id";
            } else {
                throw new IllegalArgumentException("ID field not found in payload for DELETE");
            }
        }
        
        Object idValue = payload.get(idField);
        String sql = "DELETE FROM " + tableName + " WHERE " + idField + " = ?";
        
        return jdbcTemplate.update(sql, idValue);
    }
}

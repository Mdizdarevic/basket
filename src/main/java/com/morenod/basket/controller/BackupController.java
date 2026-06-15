package com.morenod.basket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

@RestController
@RequestMapping("/api/donations") 
public class BackupController {

    @Autowired
    private DataSource dataSource;

    private static final String BACKUP_FILE_PATH = "backup.sql";

    @GetMapping("/backup") 
    public ResponseEntity<?> backupDatabase() {
        String backupQuery = "SCRIPT TO '" + BACKUP_FILE_PATH + "'";
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.execute(backupQuery);
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Database backup created successfully at: " + BACKUP_FILE_PATH
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "ERROR",
                "message", "Backup failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/restore") 
    public ResponseEntity<?> restoreDatabase() {
        File backupFile = new File(BACKUP_FILE_PATH);
        if (!backupFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", "ERROR",
                "message", "Restore failed: No backup file found at " + BACKUP_FILE_PATH
            ));
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.execute("DROP ALL OBJECTS");
            statement.execute("RUNSCRIPT FROM '" + BACKUP_FILE_PATH + "'");
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Database successfully rolled back to snapshot."
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "ERROR",
                "message", "Restore execution failed: " + e.getMessage()
            ));
        }
    }
}
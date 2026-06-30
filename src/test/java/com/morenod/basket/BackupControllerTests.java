package com.morenod.basket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.morenod.basket.controller.BackupController;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) 
class BackupControllerTests {

    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private Statement statement;

    private BackupController backupController;

    @BeforeEach
    void setUp() throws Exception {
        backupController = new BackupController(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
    }

    @Test
    void backupDatabaseSuccess() throws Exception {
        ResponseEntity<?> response = backupController.backupDatabase();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(statement).execute(contains("SCRIPT TO"));
    }

    @Test
    void backupDatabaseFailureReturns500() throws Exception {
        when(statement.execute(anyString())).thenThrow(new RuntimeException("DB Error"));
        
        ResponseEntity<?> response = backupController.backupDatabase();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void restoreDatabaseFileNotFound_Returns404() {
        File backupFile = new File("backup.sql");
        if (backupFile.exists()) backupFile.delete();

        ResponseEntity<?> response = backupController.restoreDatabase();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void restoreDatabaseSuccess() throws Exception {
        // need a dummy file for for this
        new File("backup.sql").createNewFile();
        ResponseEntity<?> response = backupController.restoreDatabase();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(statement).execute("RUNSCRIPT FROM 'backup.sql'");
        // double check if this is happening
        new File("backup.sql").delete();
    }
}
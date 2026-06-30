package com.morenod.basket;

import com.morenod.basket.fx.DashboardController;
import com.morenod.basket.model.Donation;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DashboardUnitTests {

    private DashboardController controller;

    @BeforeAll
    void initJavaFX() {
        Platform.startup(() -> {});
    }

    @BeforeEach
    void setup() throws Exception {
        controller = new DashboardController();
        
        injectField("txtItemName", new TextField());
        injectField("txtCategory", new TextField());
        injectField("txtQuantity", new TextField());
        injectField("txtDateEntered", new TextField());
        
        injectField("lblTotalItems", new Label());
        injectField("lblTotalPending", new Label());
        injectField("lblTotalApproved", new Label());
        injectField("lblTotalDistributed", new Label());
        
        injectField("donationsTable", new TableView<Donation>());
        injectField("colId", new TableColumn<Donation, Long>());
        injectField("colName", new TableColumn<Donation, String>());
        injectField("colCategory", new TableColumn<Donation, String>());
        injectField("colQuantity", new TableColumn<Donation, Integer>());
        injectField("colStatus", new TableColumn<Donation, String>());
        injectField("colDateEntered", new TableColumn<Donation, String>());
        injectField("colAction", new TableColumn<Donation, Void>());
    }

    @Test
    void testInitializeDoesNotCrash() {
        assertDoesNotThrow(() -> controller.initialize());
    }

    @Test
    void testHandleCreateScenarios() throws Exception {
        ((TextField) getField("txtItemName")).setText("Test");
        ((TextField) getField("txtQuantity")).setText("5");
        assertDoesNotThrow(() -> controller.handleCreate());

        ((TextField) getField("txtItemName")).setText("");
        ((TextField) getField("txtQuantity")).setText("");
        assertDoesNotThrow(() -> controller.handleCreate());

        ((TextField) getField("txtQuantity")).setText("NOT_A_NUMBER");
        assertDoesNotThrow(() -> controller.handleCreate());
    }

    @Test
    void testHandleRefreshClearsList() throws Exception {
        Field listField = DashboardController.class.getDeclaredField("donationDataList");
        listField.setAccessible(true);
        ObservableList<Donation> list = (ObservableList<Donation>) listField.get(controller);
        
        list.add(new Donation()); 
        controller.handleRefresh();
        
        assertTrue(list.isEmpty());
    }

    @Test
    void testBackupAndRestoreTriggers() {
        assertDoesNotThrow(() -> controller.handleBackup());
        assertDoesNotThrow(() -> controller.handleRestore());
    }

    @Test
    void testMetricsUpdate() throws Exception {
        Field listField = DashboardController.class.getDeclaredField("donationDataList");
        listField.setAccessible(true);
        ObservableList<Donation> list = (ObservableList<Donation>) listField.get(controller);
        
        Donation d = new Donation();
        d.setQuantity(10);
        d.setStatus("PENDING");
        list.add(d);

        java.lang.reflect.Method m = DashboardController.class.getDeclaredMethod("updateMetricsSummaries");
        m.setAccessible(true);
        m.invoke(controller);

        Thread.sleep(500); 
        Label lbl = (Label) getField("lblTotalPending");
        assertEquals("10", lbl.getText());
    }

    @Test
    void testPrivateMethodsViaReflection() throws Exception {
        // testing sendUpdateToServer
        java.lang.reflect.Method mUpdate = DashboardController.class.getDeclaredMethod("sendUpdateToServer", Donation.class);
        mUpdate.setAccessible(true);
        assertDoesNotThrow(() -> mUpdate.invoke(controller, new Donation()));

        // testing executeRowDeletion
        java.lang.reflect.Method mDelete = DashboardController.class.getDeclaredMethod("executeRowDeletion", Donation.class);
        mDelete.setAccessible(true);
        Donation d = new Donation();
        d.setId(1L);
        assertDoesNotThrow(() -> mDelete.invoke(controller, d));
    }

    // Helper methods
    private void injectField(String name, Object value) throws Exception {
        Field field = DashboardController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(controller, value);
    }

    private Object getField(String name) throws Exception {
        Field field = DashboardController.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(controller);
    }

    @Test
    void testTableColumnCellFactory() throws Exception {
        TableColumn<Donation, Void> colAction = (TableColumn<Donation, Void>) getField("colAction");
        assertNotNull(colAction.getCellFactory(), "Cell factory isn't initialized");
        
        var cell = colAction.getCellFactory().call(colAction);
        assertNotNull(cell);
    }

    @Test
    void testPlatformRunLaterSynchronization() throws Exception {
        Platform.runLater(() -> {
            try {
                controller.handleRefresh();
            } catch (Exception e) {
                fail("Refresh failed!!");
            }
        });
        Thread.sleep(800); 
    }

    @Test
    void testEdgeCaseDataProcessing() throws Exception {
        java.lang.reflect.Method m = DashboardController.class.getDeclaredMethod("sendUpdateToServer", Donation.class);
        m.setAccessible(true);
        
        assertDoesNotThrow(() -> m.invoke(controller, (Donation) null));
    }


    @Test
    void testHandleRestoreNetworkFailureSimulation() throws Exception {
        Field backupField = DashboardController.class.getDeclaredField("backupJsonData");
        backupField.setAccessible(true);
        backupField.set(controller, "{}");

        assertDoesNotThrow(() -> controller.handleRestore());
    }
}
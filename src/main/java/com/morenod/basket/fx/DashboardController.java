package com.morenod.basket.fx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.morenod.basket.model.Donation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DashboardController {

    @FXML private TableView<Donation> donationsTable;
    @FXML private TableColumn<Donation, Long> colId;
    @FXML private TableColumn<Donation, String> colName;
    @FXML private TableColumn<Donation, String> colCategory;
    @FXML private TableColumn<Donation, Integer> colQuantity;
    @FXML private TableColumn<Donation, String> colStatus;
    @FXML private TableColumn<Donation, String> colDateEntered; 
    @FXML private TableColumn<Donation, Void> colAction;

    @FXML private TextField txtItemName;
    @FXML private TextField txtCategory;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtDateEntered; 

    @FXML private Label lblTotalItems;
    @FXML private Label lblTotalPending;
    @FXML private Label lblTotalApproved;
    @FXML private Label lblTotalDistributed;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ObservableList<Donation> donationDataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDateEntered.setCellValueFactory(new PropertyValueFactory<>("dateEntered"));

        colName.setCellFactory(TextFieldTableCell.forTableColumn());
        colName.setOnEditCommit(event -> {
            Donation donation = event.getRowValue();
            donation.setItemName(event.getNewValue()); 
            sendUpdateToServer(donation);
            updateMetricsSummaries();
        });

        colCategory.setCellFactory(TextFieldTableCell.forTableColumn());
        colCategory.setOnEditCommit(event -> {
            Donation donation = event.getRowValue();
            donation.setCategory(event.getNewValue()); 
            sendUpdateToServer(donation);
            updateMetricsSummaries();
        });

        colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQuantity.setOnEditCommit(event -> {
            Donation donation = event.getRowValue();
            donation.setQuantity(event.getNewValue()); 
            sendUpdateToServer(donation);
            updateMetricsSummaries();
        });

        colDateEntered.setCellFactory(TextFieldTableCell.forTableColumn());
        colDateEntered.setOnEditCommit(event -> {
            Donation donation = event.getRowValue();
            donation.setDateEntered(event.getNewValue()); 
            sendUpdateToServer(donation);
            updateMetricsSummaries();
        });

        colStatus.setCellFactory(TextFieldTableCell.forTableColumn());
        colStatus.setOnEditCommit(event -> {
            Donation donation = event.getRowValue();
            donation.setStatus(event.getNewValue()); 
            sendUpdateToServer(donation);
            updateMetricsSummaries();
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDeleteRow = new Button("Remove");
            {
                btnDeleteRow.getStyleClass().add("btn-row-delete");
                btnDeleteRow.setOnAction(event -> {
                    Donation targetedDonation = getTableView().getItems().get(getIndex());
                    executeRowDeletion(targetedDonation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDeleteRow);
                }
            }
        });

        donationDataList.addListener((ListChangeListener<Donation>) change -> updateMetricsSummaries());
        donationsTable.setItems(donationDataList);
        loadDonationsFromApi();
    }

    private void updateMetricsSummaries() {
        int totalItemsCount = 0;
        int totalPendingSum = 0;
        int totalApprovedSum = 0;
        int totalDistributedSum = 0;

        for (Donation donation : donationDataList) {
            if (donation == null) continue;
            
            int qty = donation.getQuantity();
            totalItemsCount += qty;

            String statusValue = donation.getStatus() != null ? donation.getStatus().toUpperCase().trim() : "";
            switch (statusValue) {
                case "PENDING": totalPendingSum += qty; break;
                case "APPROVED": totalApprovedSum += qty; break;
                case "DISTRIBUTED": totalDistributedSum += qty; break;
                default: break;
            }
        }

        final String finalTotal = String.format("%,d", totalItemsCount);
        final String finalPending = String.format("%,d", totalPendingSum);
        final String finalApproved = String.format("%,d", totalApprovedSum);
        final String finalDistributed = String.format("%,d", totalDistributedSum);

        Platform.runLater(() -> {
            lblTotalItems.setText(finalTotal);
            lblTotalPending.setText(finalPending);
            lblTotalApproved.setText(finalApproved);
            lblTotalDistributed.setText(finalDistributed);
        });
    }

    private void loadDonationsFromApi() {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/api/donations"))
            .GET()
            .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept(response -> {
                // console logging status, http method isn't working
                System.out.println("HTTP Status Code: " + response.statusCode());
                String body = response.body();
                System.out.println("Raw Response Content: " + body);

                Platform.runLater(() -> {
                    try {
                        if (response.statusCode() == 200) {  
                            Donation[] donations = objectMapper.readValue(body, Donation[].class);
                            donationDataList.setAll(donations);
                            updateMetricsSummaries();
                        } else {
                            System.out.println("Skipping parsing because server returned an error code.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error parsing JSON data");
                        e.printStackTrace();
                    }
                });
            });
    }

    @FXML
    private void handleCreate() {
        if (txtItemName.getText().isEmpty() || txtQuantity.getText().isEmpty()) {
            return;
        }

        try {
            Donation newDonation = new Donation();
            newDonation.setItemName(txtItemName.getText().trim());
            newDonation.setCategory(txtCategory.getText().trim());
            newDonation.setQuantity(Integer.parseInt(txtQuantity.getText().trim()));
            newDonation.setDateEntered(txtDateEntered.getText().trim());
            newDonation.setStatus("PENDING");

            String jsonBody = objectMapper.writeValueAsString(newDonation);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/donations"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    System.out.println("Server responded with status code: " + response.statusCode());
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        Platform.runLater(() -> {
                            txtItemName.clear();
                            txtCategory.clear();
                            txtQuantity.clear();
                            txtDateEntered.clear();
                            loadDonationsFromApi();
                        });
                    } else {
                        System.out.println("Server rejected the add request with code: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    System.out.println("NETWORK CONNECTION FAILED: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
        } catch (Exception e) {
            System.out.println("JSON Parsing details:");
            e.printStackTrace(); 
        }
    }

    private void sendUpdateToServer(Donation donation) {
        try {
            String jsonBody = objectMapper.writeValueAsString(donation);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/donations/" + donation.getId()))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            System.out.println("Update failed on server side, code: " + response.statusCode());
                        }
                    });
        } catch (Exception e) {
            System.out.println("Update execution error");
        }
    }

    private void executeRowDeletion(Donation donation) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/donations/" + donation.getId()))
                .DELETE()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            donationDataList.remove(donation);
                            System.out.println("Row item successfully removed");
                        });
                    }
                });
    }

    private String backupJsonData = null;

    @FXML
    private void handleBackup() {
        System.out.println("Initiating database backup process...");
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/donations/backup"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        this.backupJsonData = "READY"; 
                        Platform.runLater(() -> System.out.println("Success: Server database backup snapshot captured successfully!"));
                    } else {
                        System.out.println("Error: Server backup failed with code: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    System.out.println("Network request failure: " + ex.getMessage());
                    return null;
                });
    }

    @FXML
    private void handleRestore() {
        System.out.println("Initiating database restore process...");
        
        if (this.backupJsonData == null) {
            System.out.println("Abort: Cannot restore database state. No backup snapshot has been captured yet!");
            return;
        }
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/donations/restore"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> {
                            System.out.println("Success: In-memory database has been wiped and restored successfully via SQL Script!");
                            loadDonationsFromApi(); // Refresh the table display rows
                        });
                    } else {
                        System.out.println("Error: Server restoration failed with code: " + response.statusCode());
                    }
                })
                .exceptionally(ex -> {
                    System.out.println("Network request failure: " + ex.getMessage());
                    return null;
                });
    }
    @FXML
    private void handleRefresh() {
        donationDataList.clear();
        loadDonationsFromApi();
    }
}
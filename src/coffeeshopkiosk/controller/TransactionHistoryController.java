package coffeeshopkiosk.controller;

import coffeeshopkiosk.dao.TransactionDAO;
import coffeeshopkiosk.model.TransactionRecord;
import coffeeshopkiosk.util.AlertUtil;
import coffeeshopkiosk.util.Session;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;

public class TransactionHistoryController {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML
    private TableView<TransactionRecord> transactionTable;
    @FXML
    private TableColumn<TransactionRecord, Integer> idColumn;
    @FXML
    private TableColumn<TransactionRecord, LocalDateTime> dateColumn;
    @FXML
    private TableColumn<TransactionRecord, String> cashierColumn;
    @FXML
    private TableColumn<TransactionRecord, BigDecimal> subtotalColumn;
    @FXML
    private TableColumn<TransactionRecord, BigDecimal> discountColumn;
    @FXML
    private TableColumn<TransactionRecord, BigDecimal> totalColumn;
    @FXML
    private TableColumn<TransactionRecord, BigDecimal> cashColumn;
    @FXML
    private TableColumn<TransactionRecord, BigDecimal> changeColumn;
    @FXML
    private TableColumn<TransactionRecord, String> statusColumn;
    @FXML
    private TableColumn<TransactionRecord, String> voidedByColumn;
    @FXML
    private TableColumn<TransactionRecord, String> reasonColumn;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        cashierColumn.setCellValueFactory(new PropertyValueFactory<>("cashierName"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        cashColumn.setCellValueFactory(new PropertyValueFactory<>("cashReceived"));
        changeColumn.setCellValueFactory(new PropertyValueFactory<>("changeAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        voidedByColumn.setCellValueFactory(new PropertyValueFactory<>("voidedByName"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("voidReason"));
        transactionTable.setRowFactory(table -> new TableRow<TransactionRecord>() {
            @Override
            protected void updateItem(TransactionRecord item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("voided-row");
                if (!empty && item != null && item.isVoided()) {
                    getStyleClass().add("voided-row");
                }
            }
        });
        refresh();
    }

    @FXML
    private void voidSelectedTransaction() {
        TransactionRecord selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.warning("Select Transaction", "Choose a transaction to void.");
            return;
        }
        if (selected.isVoided()) {
            AlertUtil.warning("Already Voided", "This transaction has already been voided.");
            return;
        }

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Void Transaction");
        reasonDialog.setHeaderText("Void transaction #" + selected.getTransactionId());
        reasonDialog.setContentText("Reason:");
        Optional<String> reasonResult = reasonDialog.showAndWait();
        if (!reasonResult.isPresent() || reasonResult.get().trim().isEmpty()) {
            AlertUtil.warning("Reason Required", "Enter a reason so the void is auditable.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Void");
        confirm.setHeaderText("This will restore product stock and ingredient inventory.");
        confirm.setContentText("Void transaction #" + selected.getTransactionId() + "?");
        Optional<ButtonType> confirmation = confirm.showAndWait();
        if (!confirmation.isPresent() || confirmation.get() != ButtonType.OK) {
            return;
        }

        try {
            int userId = Session.getCurrentUser() == null ? 1 : Session.getCurrentUser().getUserId();
            transactionDAO.voidTransaction(selected.getTransactionId(), userId, reasonResult.get().trim());
            refresh();
            AlertUtil.info("Transaction Voided", "Stock and inventory were restored.");
        } catch (SQLException ex) {
            AlertUtil.error("Void Failed", ex.getMessage());
        }
    }

    @FXML
    private void refresh() {
        try {
            transactionTable.setItems(transactionDAO.findRecent());
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }
}

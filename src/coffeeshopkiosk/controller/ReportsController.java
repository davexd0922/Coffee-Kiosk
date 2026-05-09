package coffeeshopkiosk.controller;

import coffeeshopkiosk.dao.ReportDAO;
import coffeeshopkiosk.model.SalesReportRow;
import coffeeshopkiosk.util.AlertUtil;
import coffeeshopkiosk.util.MoneyUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ReportsController {

    private final ReportDAO reportDAO = new ReportDAO();

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label revenueLabel;
    @FXML
    private TableView<SalesReportRow> salesTable;
    @FXML
    private TableColumn<SalesReportRow, String> dayColumn;
    @FXML
    private TableColumn<SalesReportRow, Integer> ordersColumn;
    @FXML
    private TableColumn<SalesReportRow, BigDecimal> revenueColumn;
    @FXML
    private TableView<SalesReportRow> bestSellerTable;
    @FXML
    private TableColumn<SalesReportRow, String> productColumn;
    @FXML
    private TableColumn<SalesReportRow, Integer> quantityColumn;
    @FXML
    private TableColumn<SalesReportRow, BigDecimal> productRevenueColumn;

    @FXML
    private void initialize() {
        dayColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
        ordersColumn.setCellValueFactory(new PropertyValueFactory<>("orders"));
        revenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("orders"));
        productRevenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        generateReport();
    }

    @FXML
    private void today() {
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        generateReport();
    }

    @FXML
    private void thisWeek() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.minusDays(6));
        endDatePicker.setValue(today);
        generateReport();
    }

    @FXML
    private void thisMonth() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.withDayOfMonth(1));
        endDatePicker.setValue(today);
        generateReport();
    }

    @FXML
    private void generateReport() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null || end.isBefore(start)) {
            AlertUtil.warning("Invalid Date Range", "Choose a valid start and end date.");
            return;
        }
        try {
            revenueLabel.setText(MoneyUtil.format(reportDAO.totalRevenue(start, end)));
            salesTable.setItems(reportDAO.salesByDay(start, end));
            bestSellerTable.setItems(reportDAO.bestSellers(start, end));
        } catch (SQLException ex) {
            AlertUtil.error("Database Error", ex.getMessage());
        }
    }
}

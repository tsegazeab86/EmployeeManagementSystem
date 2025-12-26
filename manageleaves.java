package UNIEMPOYEEMANAGMENT;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.Objects;

public class manageleaves extends Application {

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/employee_management",
                "root",
                ""
        );
    }

    public static class LeaveRecord {
        private final String empID;
        private final String leaveType;
        private final LocalDate fromDate;
        private final LocalDate toDate;
        private final String description;
        private final LocalDate appliedDate;
        private final String status;

        public LeaveRecord(String empID, String leaveType, LocalDate fromDate,
                           LocalDate toDate, String description,
                           LocalDate appliedDate, String status) {
            this.empID = empID;
            this.leaveType = leaveType;
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.description = description;
            this.appliedDate = appliedDate;
            this.status = status;
        }

        public String getEmpID() { return empID; }
        public String getLeaveType() { return leaveType; }
        public LocalDate getFromDate() { return fromDate; }
        public LocalDate getToDate() { return toDate; }
        public String getDescription() { return description; }
        public LocalDate getAppliedDate() { return appliedDate; }
        public String getStatus() { return status; }
    }

    private final ObservableList<LeaveRecord> leaveList = FXCollections.observableArrayList();

    public void showleaveeWindow() {
        try {
            start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        TextField searchField = new TextField();
        searchField.setPromptText("Search by Status or Employee ID");

        Button addLeaveBtn = new Button("Add Leave");
        addLeaveBtn.setOnAction(e -> openAddLeaveWindow());

        HBox topBar = new HBox(10, searchField, addLeaveBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        TableView<LeaveRecord> table = new TableView<>(leaveList);

        TableColumn<LeaveRecord, String> empIDCol = new TableColumn<>("EMP ID");
        empIDCol.setCellValueFactory(new PropertyValueFactory<>("empID"));

        TableColumn<LeaveRecord, String> typeCol = new TableColumn<>("LEAVE TYPE");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("leaveType"));

        TableColumn<LeaveRecord, LocalDate> fromCol = new TableColumn<>("FROM");
        fromCol.setCellValueFactory(new PropertyValueFactory<>("fromDate"));

        TableColumn<LeaveRecord, LocalDate> toCol = new TableColumn<>("TO");
        toCol.setCellValueFactory(new PropertyValueFactory<>("toDate"));

        TableColumn<LeaveRecord, String> descCol = new TableColumn<>("DESCRIPTION");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<LeaveRecord, LocalDate> appliedCol = new TableColumn<>("APPLIED DATE");
        appliedCol.setCellValueFactory(new PropertyValueFactory<>("appliedDate"));

        TableColumn<LeaveRecord, String> statusCol = new TableColumn<>("STATUS");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(empIDCol, typeCol, fromCol, toCol, descCol, appliedCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadLeaveData();

        // ================= SEARCH FUNCTION =================
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                table.setItems(leaveList);
            } else {
                ObservableList<LeaveRecord> filtered = FXCollections.observableArrayList();
                for (LeaveRecord record : leaveList) {
                    if (record.getStatus().toLowerCase().contains(newText.toLowerCase()) ||
                            record.getEmpID().toLowerCase().contains(newText.toLowerCase())) {
                        filtered.add(record);
                    }
                }
                table.setItems(filtered);
            }
        });

        VBox root = new VBox(10, topBar, table);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 900, 500);
        scene.getStylesheets().add(Objects.requireNonNull(getClass()
                .getResource("/css/style.css")).toExternalForm());

        stage.setTitle("Manage Leaves");
        stage.setScene(scene);
        stage.show();
    }

    private void loadLeaveData() {
        leaveList.clear();
        try (Connection conn = getConnection()) {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM leave_records ORDER BY emp_id ASC");
            while (rs.next()) {
                leaveList.add(new LeaveRecord(
                        rs.getString("emp_id"),
                        rs.getString("leave_type"),
                        rs.getDate("from_date").toLocalDate(),
                        rs.getDate("to_date").toLocalDate(),
                        rs.getString("description"),
                        rs.getDate("applied_date").toLocalDate(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database error: " + e.getMessage()).showAndWait();
        }
    }

    // ================= ADD LEAVE WINDOW WITH EMPLOYEE COMBOBOX =================
    private void openAddLeaveWindow() {
        Stage stage = new Stage();
        stage.setTitle("Add Leave");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // EMPLOYEE ID COMBOBOX
        ComboBox<String> empIDCombo = new ComboBox<>();
        empIDCombo.setPromptText("Select Employee ID");

        try (Connection conn = getConnection()) {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT empid FROM employees ORDER BY empid ASC");
            while (rs.next()) {
                empIDCombo.getItems().add(rs.getString("empid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load employee IDs: " + e.getMessage()).showAndWait();
        }

        DatePicker fromDate = new DatePicker();
        DatePicker toDate = new DatePicker();
        TextField typeField = new TextField();
        TextField descField = new TextField();
        DatePicker appliedDate = new DatePicker(LocalDate.now());
        TextField statusField = new TextField();

        grid.add(new Label("Employee ID:"), 0, 0);
        grid.add(empIDCombo, 1, 0);
        grid.add(new Label("Leave Type:"), 0, 1);
        grid.add(typeField, 1, 1);
        grid.add(new Label("From Date:"), 0, 2);
        grid.add(fromDate, 1, 2);
        grid.add(new Label("To Date:"), 0, 3);
        grid.add(toDate, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descField, 1, 4);
        grid.add(new Label("Applied Date:"), 0, 5);
        grid.add(appliedDate, 1, 5);
        grid.add(new Label("Status:"), 0, 6);
        grid.add(statusField, 1, 6);

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            String selectedEmpID = empIDCombo.getValue();
            if (selectedEmpID == null || selectedEmpID.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please select an Employee ID!").showAndWait();
                return;
            }
            try (Connection conn = getConnection()) {
                String sql = "INSERT INTO leave_records(emp_id, leave_type, from_date, to_date, description, applied_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, selectedEmpID);
                ps.setString(2, typeField.getText());
                ps.setDate(3, Date.valueOf(fromDate.getValue()));
                ps.setDate(4, Date.valueOf(toDate.getValue()));
                ps.setString(5, descField.getText());
                ps.setDate(6, Date.valueOf(appliedDate.getValue()));
                ps.setString(7, statusField.getText());
                ps.executeUpdate();
                stage.close();
                loadLeaveData();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to add leave: " + ex.getMessage()).showAndWait();
            }
        });

        HBox buttonBox = new HBox(saveBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        VBox vbox = new VBox(10, grid, buttonBox);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 400, 400);
        stage.setScene(scene);
        stage.show();
    }
}

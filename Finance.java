package UNIEMPOYEEMANAGMENT;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;

public class Finance {

    private final ObservableList<EmployeeFinance> financeList = FXCollections.observableArrayList();

    // ================= SHOW FINANCE WINDOW =================
    public void showFinanceWindow() {
        Stage stage = new Stage();
        stage.setTitle("Employee Finance");

        Button loadBtn = new Button("Load Employees");
        Button payBtn = new Button("Pay Selected");
        Button backBtn = new Button("Back");

        // ================= TABLE =================
        TableView<EmployeeFinance> table = new TableView<>();
        table.setItems(financeList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<EmployeeFinance, String> idCol = new TableColumn<>("Emp ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("empId"));

        TableColumn<EmployeeFinance, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<EmployeeFinance, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));

        TableColumn<EmployeeFinance, Double> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));

        TableColumn<EmployeeFinance, ComboBox<String>> statusCol = new TableColumn<>("Payment Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        table.getColumns().addAll(idCol, nameCol, deptCol, salaryCol, statusCol);

        // ================= BUTTON ACTIONS =================
        loadBtn.setOnAction(e -> loadEmployees());
        payBtn.setOnAction(e -> payEmployees());
        backBtn.setOnAction(e -> {
            stage.close();                  // close current window
            new Dashboard().showDashboard(); // reopen dashboard
        });
        HBox top = new HBox(15, loadBtn, payBtn, backBtn);
        top.setPadding(new Insets(15));
        top.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(10, top, table);
        root.setPadding(new Insets(10));
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.4), table);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.7), table);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        fadeOut.play();
        stage.setScene(new Scene(root, 800, 500));
        stage.show();
    }

    // ================= LOAD EMPLOYEES =================
    private void loadEmployees() {
        financeList.clear();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/employee_management", "root", "")) {

            // Assuming 'salary' column exists in 'employees' table
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT empid, fname, lname, department, salary FROM employees"
            );

            while (rs.next()) {
                String empId = rs.getString("empid");
                String name = rs.getString("fname") + " " + rs.getString("lname");
                String dept = rs.getString("department");
                double salary = rs.getDouble("salary");

                // Check if payment already exists in 'finance' table
                String status = "Not Paid";
                String checkSql = "SELECT status FROM finance WHERE empid = ?";
                PreparedStatement ps = conn.prepareStatement(checkSql);
                ps.setString(1, empId);
                ResultSet rsCheck = ps.executeQuery();
                if (rsCheck.next()) {
                    status = rsCheck.getString("status");
                }

                financeList.add(new EmployeeFinance(empId, name, dept, salary, status));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Database Error: " + ex.getMessage());
        }
    }

    // ================= PAY EMPLOYEES =================
    private void payEmployees() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/employee_management", "root", "")) {

            String insertSql = "INSERT INTO finance (empid, status) VALUES (?, ?)";
            String updateSql = "UPDATE finance SET status = ? WHERE empid = ?";

            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);

            for (EmployeeFinance ef : financeList) {
                String empId = ef.getEmpId();
                String status = ef.getPaymentStatus().getValue();

                // Check if record exists
                String checkSql = "SELECT COUNT(*) FROM finance WHERE empid = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, empId);
                ResultSet rsCheck = checkStmt.executeQuery();
                if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                    // Update existing
                    updateStmt.setString(1, status);
                    updateStmt.setString(2, empId);
                    updateStmt.addBatch();
                } else {
                    // Insert new
                    insertStmt.setString(1, empId);
                    insertStmt.setString(2, status);
                    insertStmt.addBatch();
                }
            }

            insertStmt.executeBatch();
            updateStmt.executeBatch();
            showAlert("Payment status updated successfully!");

        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Database Error: " + ex.getMessage());
        }
    }

    // ================= ALERT =================
    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    // ================= MODEL =================
    public static class EmployeeFinance {
        private final String empId;
        private final String name;
        private final String department;
        private final double salary;
        private final ComboBox<String> paymentStatus;

        public EmployeeFinance(String empId, String name, String department, double salary, String status) {
            this.empId = empId;
            this.name = name;
            this.department = department;
            this.salary = salary;
            paymentStatus = new ComboBox<>();
            paymentStatus.getItems().addAll("Paid", "Not Paid");
            paymentStatus.setValue(status); // Set current status
        }

        public String getEmpId() {
            return empId;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public double getSalary() {
            return salary;
        }

        public ComboBox<String> getPaymentStatus() {
            return paymentStatus;
        }
    }
}

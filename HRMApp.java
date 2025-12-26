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
import java.util.ArrayList;
import java.util.List;

public class HRMApp extends Application {

    // --- DB connection settings (change as needed) ---
    private static final String DB_URL = "\"jdbc:mysql://localhost/employee_management\", \"root\", \"\"";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // --- Entry point ---
    public static void main(String[] args) {
        launch(args);
    }

    // --- JavaFX start ---
    @Override
    public void start(Stage primaryStage) {
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(employeeTab(), payrollTab());

        Scene scene = new Scene(tabs, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Single-file HRM - Employee & Payroll");
        primaryStage.show();
    }

    private Tab employeeTab() {
        Tab tab = new Tab("Employees");
        tab.setClosable(false);

        // Form fields
        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField deptField = new TextField();
        TextField salaryField = new TextField();

        idField.setPromptText("E.g. E1001");
        nameField.setPromptText("Full name");
        deptField.setPromptText("Department");
        salaryField.setPromptText("Salary");

        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        HBox formButtons = new HBox(10, addBtn, updateBtn, deleteBtn);
        formButtons.setAlignment(Pos.CENTER);

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(10));
        form.addRow(0, new Label("Emp ID:"), idField);
        form.addRow(1, new Label("Name:"), nameField);
        form.addRow(2, new Label("Department:"), deptField);
        form.addRow(3, new Label("Salary:"), salaryField);
        form.add(formButtons, 0, 4, 2, 1);

        // Table
        TableView<Employee> table = new TableView<>();
        TableColumn<Employee, String> idCol = new TableColumn<>("Emp ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Employee, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        TableColumn<Employee, Double> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        table.getColumns().addAll(idCol, nameCol, deptCol, salaryCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load employees
        refreshEmployees(table);

        // Table selection -> populate form
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, sel) -> {
            if (sel != null) {
                idField.setText(sel.getEmpId());
                nameField.setText(sel.getName());
                deptField.setText(sel.getDepartment());
                salaryField.setText(String.valueOf(sel.getSalary()));
                idField.setDisable(true);
            } else {
                idField.setDisable(false);
            }
        });

        addBtn.setOnAction(e -> {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String dept = deptField.getText().trim();
                double salary = Double.parseDouble(salaryField.getText().trim());
                if (id.isEmpty() || name.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Fill required fields");
                    return;
                }
                Employee emp = new Employee(id, name, dept, salary);
                if (addEmployee(emp)) {
                    showAlert(Alert.AlertType.INFORMATION, "Employee added");
                    refreshEmployees(table);
                    clearFields(idField, nameField, deptField, salaryField);
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid salary");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
            }
        });

        updateBtn.setOnAction(e -> {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String dept = deptField.getText().trim();
                double salary = Double.parseDouble(salaryField.getText().trim());
                if (id.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Select employee"); return; }
                Employee emp = new Employee(id, name, dept, salary);
                if (updateEmployee(emp)) {
                    showAlert(Alert.AlertType.INFORMATION, "Employee updated");
                    refreshEmployees(table);
                    clearFields(idField, nameField, deptField, salaryField);
                    idField.setDisable(false);
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid salary");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            if (id.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Select employee"); return; }
            try {
                if (deleteEmployee(id)) {
                    showAlert(Alert.AlertType.INFORMATION, "Employee deleted");
                    refreshEmployees(table);
                    clearFields(idField, nameField, deptField, salaryField);
                    idField.setDisable(false);
                }
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
            }
        });

        VBox vbox = new VBox(10, form, table);
        vbox.setPadding(new Insets(12));
        tab.setContent(vbox);
        return tab;
    }

    private Tab payrollTab() {
        Tab tab = new Tab("Payroll");
        tab.setClosable(false);

        ComboBox<String> empCombo = new ComboBox<>();
        TextField salaryField = new TextField();
        TextField bonusField = new TextField("0");
        TextField deductionField = new TextField("0");
        TextField netField = new TextField();
        netField.setEditable(false);
        DatePicker payDate = new DatePicker(LocalDate.now());

        // Load employee IDs into combo
        refreshEmployeeCombo(empCombo);

        Button calcBtn = new Button("Calculate Net");
        Button saveBtn = new Button("Save Payroll");
        Button refreshBtn = new Button("Refresh Employees");

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10); form.setPadding(new Insets(10));
        form.addRow(0, new Label("Employee:"), empCombo);
        form.addRow(1, new Label("Salary:"), salaryField);
        form.addRow(2, new Label("Bonus:"), bonusField);
        form.addRow(3, new Label("Deductions:"), deductionField);
        form.addRow(4, new Label("Net Salary:"), netField);
        form.addRow(5, new Label("Pay Date:"), payDate);
        HBox actions = new HBox(10, calcBtn, saveBtn, refreshBtn);
        actions.setAlignment(Pos.CENTER);
        form.add(actions, 0, 6, 2, 1);

        // Table
        TableView<Payroll> table = new TableView<>();
        TableColumn<Payroll, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Payroll, String> empCol = new TableColumn<>("Emp ID");
        empCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        TableColumn<Payroll, Double> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
        TableColumn<Payroll, Double> bonusCol = new TableColumn<>("Bonus");
        bonusCol.setCellValueFactory(new PropertyValueFactory<>("bonus"));
        TableColumn<Payroll, Double> dedCol = new TableColumn<>("Deductions");
        dedCol.setCellValueFactory(new PropertyValueFactory<>("deductions"));
        TableColumn<Payroll, Double> netCol = new TableColumn<>("Net");
        netCol.setCellValueFactory(new PropertyValueFactory<>("netSalary"));
        TableColumn<Payroll, LocalDate> dateCol = new TableColumn<>("Pay Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));
        table.getColumns().addAll(idCol, empCol, salaryCol, bonusCol, dedCol, netCol, dateCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load payrolls
        refreshPayrolls(table);

        // Events
        empCombo.setOnAction(e -> {
            String empId = empCombo.getValue();
            if (empId != null) {
                try {
                    Employee emp = findEmployee(empId);
                    if (emp != null) salaryField.setText(String.valueOf(emp.getSalary()));
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
                }
            }
        });

        calcBtn.setOnAction(e -> {
            try {
                double salary = Double.parseDouble(salaryField.getText().trim());
                double bonus = Double.parseDouble(bonusField.getText().trim());
                double ded = Double.parseDouble(deductionField.getText().trim());
                double net = salary + bonus - ded;
                netField.setText(String.format("%.2f", net));
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Enter valid numbers");
            }
        });

        saveBtn.setOnAction(e -> {
            String empId = empCombo.getValue();
            if (empId == null) { showAlert(Alert.AlertType.WARNING, "Select employee"); return; }
            try {
                double salary = Double.parseDouble(salaryField.getText().trim());
                double bonus = Double.parseDouble(bonusField.getText().trim());
                double ded = Double.parseDouble(deductionField.getText().trim());
                double net = salary + bonus - ded;
                Payroll p = new Payroll(empId, salary, bonus, ded, payDate.getValue());
                p.setNetSalary(net);
                if (addPayroll(p)) {
                    showAlert(Alert.AlertType.INFORMATION, "Payroll saved");
                    refreshPayrolls(table);
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Enter valid numbers");
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
            }
        });

        refreshBtn.setOnAction(e -> refreshEmployeeCombo(empCombo));

        VBox vbox = new VBox(10, form, table);
        vbox.setPadding(new Insets(12));
        tab.setContent(vbox);
        return tab;
    }

    // -----------------------
    // DB and helper methods
    // -----------------------

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Employee CRUD
    private boolean addEmployee(Employee e) throws SQLException {
        String sql = "INSERT INTO employees (empid, name, department, salary) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getEmpId());
            ps.setString(2, e.getName());
            ps.setString(3, e.getDepartment());
            ps.setDouble(4, e.getSalary());
            return ps.executeUpdate() == 1;
        }
    }

    private boolean updateEmployee(Employee e) throws SQLException {
        String sql = "UPDATE employees SET name=?, department=?, salary=? WHERE empid=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getDepartment());
            ps.setDouble(3, e.getSalary());
            ps.setString(4, e.getEmpId());
            return ps.executeUpdate() == 1;
        }
    }

    private boolean deleteEmployee(String empId) throws SQLException {
        String sql = "DELETE FROM employees WHERE empid=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empId);
            return ps.executeUpdate() == 1;
        }
    }

    private Employee findEmployee(String empId) throws SQLException {
        String sql = "SELECT * FROM employees WHERE empid=?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Employee(
                            rs.getString("empid"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getDouble("salary")
                    );
                }
            }
        }
        return null;
    }

    private List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Employee(
                        rs.getString("empid"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getDouble("salary")
                ));
            }
        }
        return list;
    }

    // Payroll
    private boolean addPayroll(Payroll p) throws SQLException {
        String sql = "INSERT INTO payroll (empid, salary, bonus, deductions, net_salary, pay_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getEmpId());
            ps.setDouble(2, p.getSalary());
            ps.setDouble(3, p.getBonus());
            ps.setDouble(4, p.getDeductions());
            ps.setDouble(5, p.getNetSalary());
            ps.setDate(6, Date.valueOf(p.getPayDate()));
            int affected = ps.executeUpdate();
            if (affected == 1) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) p.setId(keys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    private List<Payroll> getAllPayrolls() throws SQLException {
        List<Payroll> list = new ArrayList<>();
        String sql = "SELECT * FROM payroll ORDER BY pay_date DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Payroll p = new Payroll();
                p.setId(rs.getInt("id"));
                p.setEmpId(rs.getString("empid"));
                p.setSalary(rs.getDouble("salary"));
                p.setBonus(rs.getDouble("bonus"));
                p.setDeductions(rs.getDouble("deductions"));
                p.setNetSalary(rs.getDouble("net_salary"));
                p.setPayDate(rs.getDate("pay_date").toLocalDate());
                list.add(p);
            }
        }
        return list;
    }

    // UI helpers
    private void refreshEmployees(TableView<Employee> table) {
        try {
            List<Employee> list = getAllEmployees();
            ObservableList<Employee> obs = FXCollections.observableArrayList(list);
            table.setItems(obs);
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
        }
    }

    private void refreshEmployeeCombo(ComboBox<String> combo) {
        combo.getItems().clear();
        try {
            List<Employee> list = getAllEmployees();
            for (Employee e : list) combo.getItems().add(e.getEmpId());
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
        }
    }

    private void refreshPayrolls(TableView<Payroll> table) {
        try {
            List<Payroll> list = getAllPayrolls();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
        }
    }

    private void clearFields(TextField... fields) {
        for (TextField f : fields) f.clear();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.showAndWait();
    }

    // -----------------------
    // Inner model classes
    // -----------------------

    public static class Employee {
        private String empId;
        private String name;
        private String department;
        private double salary;

        public Employee() {}

        public Employee(String empId, String name, String department, double salary) {
            this.empId = empId;
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        public String getEmpId() { return empId; }
        public void setEmpId(String empId) { this.empId = empId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public double getSalary() { return salary; }
        public void setSalary(double salary) { this.salary = salary; }
    }

    public static class Payroll {
        private int id;
        private String empId;
        private double salary;
        private double bonus;
        private double deductions;
        private double netSalary;
        private LocalDate payDate;

        public Payroll() {}

        public Payroll(String empId, double salary, double bonus, double deductions, LocalDate payDate) {
            this.empId = empId;
            this.salary = salary;
            this.bonus = bonus;
            this.deductions = deductions;
            this.netSalary = salary + bonus - deductions;
            this.payDate = payDate;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getEmpId() { return empId; }
        public void setEmpId(String empId) { this.empId = empId; }

        public double getSalary() { return salary; }
        public void setSalary(double salary) { this.salary = salary; }

        public double getBonus() { return bonus; }
        public void setBonus(double bonus) { this.bonus = bonus; }

        public double getDeductions() { return deductions; }
        public void setDeductions(double deductions) { this.deductions = deductions; }

        public double getNetSalary() { return netSalary; }
        public void setNetSalary(double netSalary) { this.netSalary = netSalary; }

        public LocalDate getPayDate() { return payDate; }
        public void setPayDate(LocalDate payDate) { this.payDate = payDate; }
    }
}

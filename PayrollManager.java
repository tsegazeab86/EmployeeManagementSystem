package UNIEMPOYEEMANAGMENT;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class PayrollManager {

    private final ObservableList<PayrollRecord> payrollList = FXCollections.observableArrayList();

    public void showPayrollWindow() {
        Stage stage = new Stage();
        stage.setTitle("Employee Payroll");

        TableView<PayrollRecord> table = new TableView<>(payrollList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PayrollRecord, String> idCol = new TableColumn<>("Emp ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("empId"));

        TableColumn<PayrollRecord, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<PayrollRecord, String> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));

        TableColumn<PayrollRecord, String> bonusCol = new TableColumn<>("Bonus");
        bonusCol.setCellValueFactory(new PropertyValueFactory<>("bonus"));

        TableColumn<PayrollRecord, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

        TableColumn<PayrollRecord, String> photoCol = new TableColumn<>("Photo");
        photoCol.setCellValueFactory(new PropertyValueFactory<>("photoPath"));
        photoCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView img = new ImageView();
            {
                img.setFitWidth(50);
                img.setFitHeight(50);
                img.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null || path.isEmpty()) {
                    setGraphic(null);
                } else {
                    img.setImage(new Image("file:" + path));
                    setGraphic(img);
                }
            }
        });

        table.getColumns().addAll(idCol, photoCol, nameCol, salaryCol, bonusCol, totalCol);

        Button calculateBtn = new Button("Calculate Payroll");
        Button addEmployeeBtn = new Button("Add Employee");
        Button updateEmployeeBtn = new Button("Update Employee");

        calculateBtn.setOnAction(e -> calculatePayroll());
        addEmployeeBtn.setOnAction(e -> addEmployee());
        updateEmployeeBtn.setOnAction(e -> updateEmployee());

        HBox buttonBar = new HBox(10, calculateBtn, addEmployeeBtn, updateEmployeeBtn);
        VBox root = new VBox(10, table, buttonBar);
        root.setPadding(new Insets(10));
        stage.setScene(new Scene(root, 1000, 500));
        stage.show();
    }

    private void calculatePayroll() {
        payrollList.clear();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/employee_management", "root", "")) {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT e.empid, e.fname, e.salary, a.status, e.photo_path FROM employees e " +
                            "JOIN attendance a ON e.empid = a.empid WHERE a.status = 'Present'"
            );
            while (rs.next()) {
                String bonus = rs.getString("status").equals("Present") ? "1000" : "0";
                String total = String.valueOf(Integer.parseInt(rs.getString("salary")) + Integer.parseInt(bonus));
                payrollList.add(new PayrollRecord(
                        rs.getString("empid"),
                        rs.getString("fname"),
                        rs.getString("salary"),
                        bonus,
                        total,
                        rs.getString("photo_path")
                ));
            }
        } catch (SQLException ex) {
            showAlert(ex.getMessage());
        }
    }

    private void addEmployee() {
        Dialog<PayrollRecord> dialog = new Dialog<>();
        dialog.setTitle("Add Employee");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        TextField nameField = new TextField();
        TextField salaryField = new TextField();
        TextField photoPathField = new TextField();

        grid.add(new Label("Employee ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Salary:"), 0, 2);
        grid.add(salaryField, 1, 2);
        grid.add(new Label("Photo Path:"), 0, 3);
        grid.add(photoPathField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addBtn) {
                return new PayrollRecord(
                        idField.getText(),
                        nameField.getText(),
                        salaryField.getText(),
                        "0",
                        salaryField.getText(),
                        photoPathField.getText()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(record -> {
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/employee_management", "root", "")) {
                String sql = "INSERT INTO employees(empid, fname, salary, photo_path) VALUES(?,?,?,?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, record.getEmpId());
                ps.setString(2, record.getName());
                ps.setString(3, record.getSalary());
                ps.setString(4, record.getPhotoPath());
                ps.executeUpdate();
                showAlert("Employee added!");
            } catch (SQLException ex) {
                showAlert(ex.getMessage());
            }
        });
    }

    private void updateEmployee() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Update Employee");
        dialog.setHeaderText("Enter Employee ID:");
        dialog.setContentText("Employee ID:");
        dialog.showAndWait().ifPresent(id -> {
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/employee_management", "root", "")) {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT fname, salary, photo_path FROM employees WHERE empid = ?");
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Dialog<PayrollRecord> updateDialog = new Dialog<>();
                    updateDialog.setTitle("Update Employee");

                    ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
                    updateDialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(20, 150, 10, 10));

                    TextField nameField = new TextField(rs.getString("fname"));
                    TextField salaryField = new TextField(rs.getString("salary"));
                    TextField photoPathField = new TextField(rs.getString("photo_path"));

                    grid.add(new Label("Name:"), 0, 0);
                    grid.add(nameField, 1, 0);
                    grid.add(new Label("Salary:"), 0, 1);
                    grid.add(salaryField, 1, 1);
                    grid.add(new Label("Photo Path:"), 0, 2);
                    grid.add(photoPathField, 1, 2);

                    updateDialog.getDialogPane().setContent(grid);

                    updateDialog.setResultConverter(dialogButton -> {
                        if (dialogButton == updateBtn) {
                            return new PayrollRecord(
                                    id,
                                    nameField.getText(),
                                    salaryField.getText(),
                                    "0",
                                    salaryField.getText(),
                                    photoPathField.getText()
                            );
                        }
                        return null;
                    });

                    updateDialog.showAndWait().ifPresent(record -> {
                        try (Connection conn2 = DriverManager.getConnection(
                                "jdbc:mysql://localhost:3306/employee_management", "root", "")) {
                            String sql = "UPDATE employees SET fname=?, salary=?, photo_path=? WHERE empid=?";
                            PreparedStatement ps2 = conn2.prepareStatement(sql);
                            ps2.setString(1, record.getName());
                            ps2.setString(2, record.getSalary());
                            ps2.setString(3, record.getPhotoPath());
                            ps2.setString(4, record.getEmpId());
                            ps2.executeUpdate();
                            showAlert("Employee updated!");
                        } catch (SQLException ex) {
                            showAlert(ex.getMessage());
                        }
                    });
                } else {
                    showAlert("Employee not found!");
                }
            } catch (SQLException ex) {
                showAlert(ex.getMessage());
            }
        });
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    public static class PayrollRecord {
        private final String empId, name, salary, bonus, total, photoPath;

        public PayrollRecord(String empId, String name, String salary, String bonus, String total, String photoPath) {
            this.empId = empId;
            this.name = name;
            this.salary = salary;
            this.bonus = bonus;
            this.total = total;
            this.photoPath = photoPath;
        }

        public String getEmpId() { return empId; }
        public String getName() { return name; }
        public String getSalary() { return salary; }
        public String getBonus() { return bonus; }
        public String getTotal() { return total; }
        public String getPhotoPath() { return photoPath; }
    }
}

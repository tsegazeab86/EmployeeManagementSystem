package UNIEMPOYEEMANAGMENT;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;

public class Attendance {

    /* ========== DB CONFIG ========== */
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/employee_management";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    /* ========== DATA LISTS ========== */
    private final ObservableList<EmployeeAttendance> attendanceList = FXCollections.observableArrayList();

    /* ========== MAIN WINDOW ========== */
    public void showAttendanceWindow() {
        Stage stage = new Stage();
        stage.setTitle("Employee Attendance System 2025");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        Button saveBtn   = new Button("Save Attendance");
        Button viewBtn   = new Button("View Daily");
        Button weeklyBtn = new Button("Weekly Report");
        Button backBtn   = new Button("Back");

        /* ----- TABLE ----- */
        TableView<EmployeeAttendance> table = new TableView<>();
        table.setItems(attendanceList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<EmployeeAttendance, String> idCol = new TableColumn<>("Emp ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("empId"));

        TableColumn<EmployeeAttendance, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<EmployeeAttendance, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<EmployeeAttendance, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));

        TableColumn<EmployeeAttendance, String> photoCol = new TableColumn<>("Photo");
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

        TableColumn<EmployeeAttendance, ComboBox<String>> statusCol =
                new TableColumn<>("Attendance");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, photoCol, nameCol, genderCol, deptCol, statusCol);

        // Load employees initially
        loadEmployees();

        /* ----- BUTTON ACTIONS ----- */
        saveBtn.setOnAction(e -> saveAttendance(datePicker.getValue()));
        viewBtn.setOnAction(e -> showSavedAttendance());
        weeklyBtn.setOnAction(e -> showWeeklyReport());

        backBtn.setOnAction(e -> {
            FadeTransition fadeOut =
                    new FadeTransition(Duration.seconds(0.6), stage.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> {
                stage.close();
                new Dashboard().showDashboard();
            });
            fadeOut.play();
        });

        HBox top = new HBox(12,
                new Label("Date:"), datePicker, saveBtn, viewBtn, weeklyBtn, backBtn);
        top.setPadding(new Insets(15));
        top.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(10, top, table);
        root.setPadding(new Insets(10));
        root.setOpacity(0);

        stage.setScene(new Scene(root, 1000, 650));
        stage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /* ========== LOAD EMPLOYEES ========== */
    private void loadEmployees() {
        attendanceList.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT empid, fname, lname, gender, department, photo_path FROM employees")) {

            while (rs.next()) {
                attendanceList.add(new EmployeeAttendance(
                        rs.getString("empid"),
                        rs.getString("fname") + " " + rs.getString("lname"),
                        rs.getString("gender"),
                        rs.getString("department"),
                        rs.getString("photo_path")
                ));
            }
        } catch (SQLException ex) {
            showAlert(ex.getMessage());
        }
    }

    /* ========== SAVE ATTENDANCE (ONE DAY) ========== */
    private void saveAttendance(LocalDate date) {
        if (date == null) {
            showAlert("Select a date!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

            String check = "SELECT COUNT(*) FROM attendance WHERE attendance_date=?";
            PreparedStatement checkPs = conn.prepareStatement(check);
            checkPs.setDate(1, Date.valueOf(date));
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                showAlert("Attendance already taken for this date!");
                return;
            }

            String sql =
                    "INSERT INTO attendance(empid, attendance_date, status) VALUES(?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);

            for (EmployeeAttendance ea : attendanceList) {
                ps.setString(1, ea.getEmpId());
                ps.setDate(2, Date.valueOf(date));
                ps.setString(3, ea.getStatus().getValue());
                ps.addBatch();
            }
            ps.executeBatch();
            showAlert("Attendance saved!");

        } catch (SQLException ex) {
            showAlert(ex.getMessage());
        }
    }

    /* ========== DAILY VIEW WINDOW ========== */
    private void showSavedAttendance() {
        Stage stage = new Stage();
        stage.setTitle("View Attendance");

        ObservableList<AttendanceRecord> records = FXCollections.observableArrayList();

        DatePicker picker = new DatePicker(LocalDate.now());
        Button load = new Button("Load");

        TableView<AttendanceRecord> table = new TableView<>(records);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AttendanceRecord, String> idCol = new TableColumn<>("Emp ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("empId"));

        TableColumn<AttendanceRecord, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<AttendanceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<AttendanceRecord, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(idCol, nameCol, statusCol, dateCol);

        load.setOnAction(e -> {
            records.clear();
            if (picker.getValue() == null) {
                showAlert("Select a date!");
                return;
            }
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT a.empid, a.status, a.attendance_date, " +
                                "e.fname, e.lname " +
                                "FROM attendance a " +
                                "JOIN employees e ON a.empid = e.empid " +
                                "WHERE a.attendance_date = ?");
                ps.setDate(1, Date.valueOf(picker.getValue()));
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    records.add(new AttendanceRecord(
                            rs.getString("empid"),
                            rs.getString("fname") + " " + rs.getString("lname"),
                            rs.getString("status"),
                            rs.getDate("attendance_date")
                    ));
                }
            } catch (SQLException ex) {
                showAlert(ex.getMessage());
            }
        });

        VBox root = new VBox(10, new HBox(10, picker, load), table);
        root.setPadding(new Insets(10));
        root.setOpacity(0);

        stage.setScene(new Scene(root, 700, 500));
        stage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /* ========== WEEKLY REPORT WINDOW ========== */
    public void showWeeklyReport() {
        Stage stage = new Stage();
        stage.setTitle("Weekly Attendance Report (Last 7 Days)");

        ObservableList<AttendanceRecord> weeklyRecords = FXCollections.observableArrayList();
        TableView<AttendanceRecord> table = new TableView<>(weeklyRecords);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AttendanceRecord, String> idCol = new TableColumn<>("Emp ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("empId"));

        TableColumn<AttendanceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<AttendanceRecord, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(idCol, statusCol, dateCol);

        String sql =
                "SELECT empid, status, attendance_date FROM attendance " +
                        "WHERE attendance_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                        "ORDER BY attendance_date DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                weeklyRecords.add(new AttendanceRecord(
                        rs.getString("empid"),
                        "", // name skipped; join with employees table if needed
                        rs.getString("status"),
                        rs.getDate("attendance_date")
                ));
            }
        } catch (SQLException ex) {
            showAlert("Report Error: " + ex.getMessage());
        }

        VBox layout = new VBox(10,
                new Label("Attendance Records from the Last 7 Days"), table);
        layout.setPadding(new Insets(15));
        stage.setScene(new Scene(layout, 600, 450));
        stage.show();
    }

    /* ========== ALERT ========== */
    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    /* ========== MODEL: ROW IN MAIN TABLE ========== */
    public static class EmployeeAttendance {
        private final String empId, name, gender, department, photoPath;
        private final ComboBox<String> status;

        public EmployeeAttendance(String empId, String name,
                                  String gender, String department, String photoPath) {
            this.empId = empId;
            this.name = name;
            this.gender = gender;
            this.department = department;
            this.photoPath = photoPath;
            this.status = new ComboBox<>();
            status.getItems().addAll("Present", "Absent", "Late", "Excuse");
            status.setValue("Present");
        }

        public String getEmpId() { return empId; }
        public String getName() { return name; }
        public String getGender() { return gender; }
        public String getDepartment() { return department; }
        public String getPhotoPath() { return photoPath; }
        public ComboBox<String> getStatus() { return status; }
    }

    /* ========== MODEL: VIEW RECORD ========== */
    public static class AttendanceRecord {
        private final String empId, name, status;
        private final Date date;

        public AttendanceRecord(String empId, String name,
                                String status, Date date) {
            this.empId = empId;
            this.name = name;
            this.status = status;
            this.date = date;
        }

        public String getEmpId() { return empId; }
        public String getName()  { return name; }
        public String getStatus(){ return status; }
        public Date getDate()    { return date; }
    }
}

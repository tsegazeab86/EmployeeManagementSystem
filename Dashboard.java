package UNIEMPOYEEMANAGMENT;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class Dashboard {

    public void showDashboard() {

        Stage stage = new Stage();
        stage.setTitle("Employee Management System");
        stage.setMaximized(true);

        /* ================= BUTTONS ================= */

        Button employeeBtn = new Button("Employee");
        Button departmentBtn = new Button("Department");
        Button attendanceBtn = new Button("Attendance");
        Button leaveBtn = new Button("Manage Leave");
        Button payrollBtn = new Button("Payroll");
        Button financeBtn = new Button("Finance");
        Button logoutBtn = new Button("Logout");

        Button[] buttons = {employeeBtn, departmentBtn, attendanceBtn, leaveBtn, payrollBtn, financeBtn, logoutBtn};

        for (Button b : buttons) {
            b.setId("menu-button");
            b.setPrefSize(180, 40);
        }

        logoutBtn.setId("logout-button");

        /* ================= ACTIONS ================= */

        Employee emp = new Employee();
        Department dep = new Department();
        Attendance at = new Attendance();
        PayrollManager payroll = new PayrollManager();
        Finance finance = new Finance();
        manageleaves leave = new manageleaves();

        employeeBtn.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> {
                stage.hide();          // hide dashboard after fade
                emp.viewEmployee();    // load employee window
            });
            fadeOut.play();
        });



        departmentBtn.setOnAction(e -> {

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> {
                stage.hide();          // hide dashboard after fade
                dep.viewDepartment();
            });
            fadeOut.play();
        });

        attendanceBtn.setOnAction(e -> {
            stage.hide();
            at.showAttendanceWindow();
        });
        leaveBtn.setOnAction(e -> leave.showleaveeWindow());
        payrollBtn.setOnAction(e -> payroll.showPayrollWindow());
        financeBtn.setOnAction(e -> finance.showFinanceWindow());
        logoutBtn.setOnAction(e -> {
            stage.close();
            new Main().start(new Stage());
        });

        /* ================= SIDE MENU ================= */

        VBox menuBox = new VBox(12, employeeBtn, departmentBtn, attendanceBtn, leaveBtn, payrollBtn, financeBtn);
        menuBox.setAlignment(Pos.TOP_CENTER);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox sideMenu = new VBox(15, menuBox, spacer, logoutBtn);
        sideMenu.setId("side-menu");
        sideMenu.setPadding(new Insets(15));
        sideMenu.setPrefWidth(230);

        /* ================= CENTER ================= */

        Label welcome = new Label("WELCOME TO UNIVERSITY EMPLOYEE MANAGEMENT SYSTEM");
        welcome.setId("welcomeLabel");

        FadeTransition fade = new FadeTransition(Duration.seconds(3), welcome);
        fade.setFromValue(1);
        fade.setToValue(0.3);
        fade.setCycleCount(Timeline.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        Label empCount = new Label();
        Label depCount = new Label();

        empCount.setId("summary-box");
        depCount.setId("summary-box");

        HBox summary = new HBox(25, empCount, depCount);
        summary.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(25, welcome, summary);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(20));
        centerBox.setId("center-box");

        /* ================= DATABASE ================= */

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "")) {

            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) total FROM employees");
            if (rs1.next()) empCount.setText("Total Employees\n" + rs1.getInt("total"));

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) total FROM department");
            if (rs2.next()) depCount.setText("Total Departments\n" + rs2.getInt("total"));

        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }

        /* ================= BACKGROUND ================= */

        ImageView bg = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("/image/2.jpg")).toExternalForm()));
        bg.setPreserveRatio(false);

        StackPane overlay = new StackPane();
        overlay.setId("dark-overlay");

        BorderPane layout = new BorderPane();
        layout.setLeft(sideMenu);
        layout.setCenter(centerBox);

        StackPane root = new StackPane(bg, overlay, layout);

        Scene scene = new Scene(root, 1000, 650);
        bg.fitWidthProperty().bind(scene.widthProperty());
        bg.fitHeightProperty().bind(scene.heightProperty());
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.4));
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.7));
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        fadeOut.play();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        stage.setScene(scene);
        stage.show();
    }
}

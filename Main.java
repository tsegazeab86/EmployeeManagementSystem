package UNIEMPOYEEMANAGMENT;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Base64;
import java.util.Objects;

public class Main extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
         Label titleLabel = new Label("EMPLOYEE MANAGEMENT SYSTEM");
        titleLabel.setId("title-label");

        Label loginText = new Label("Login");
        loginText.setId("login-text");

        Hyperlink forgotLink = new Hyperlink("Forgot password?");
        forgotLink.setId("forgot-link");

        Label emaillabel = new Label("Email");
        emaillabel.setId("form-label");
        emaillabel.setMinWidth(100);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        emailField.setId("form-field");

        Label passwordLabel = new Label("Password");
        passwordLabel.setId("form-label");
        passwordLabel.setMinWidth(100);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        passField.setId("form-field");

        Button loginBtn = new Button("Login");
        loginBtn.setId("login-btn");

        HBox emailBox = new HBox(10, emaillabel, emailField);
        emailBox.setAlignment(Pos.CENTER);

        HBox passBox = new HBox(10, passwordLabel, passField);
        passBox.setAlignment(Pos.CENTER);

        HBox loginBox = new HBox(20, loginBtn);
        loginBox.setAlignment(Pos.CENTER);

        VBox loginCard = new VBox(15, titleLabel, loginText,
                emailBox,
                passBox,
                loginBox,
                forgotLink);
        loginCard.setId("login-card");
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(20));

         ImageView loginImg = new ImageView(
                Objects.requireNonNull(getClass().getResource("/image/1.jpg")).toExternalForm());
        loginImg.setPreserveRatio(false);

        StackPane root = new StackPane(loginImg, loginCard);
        Scene scene = new Scene(root, 800, 600);
        loginImg.fitWidthProperty().bind(scene.widthProperty());
        loginImg.fitHeightProperty().bind(scene.heightProperty());

        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );

        primaryStage.setTitle("Login Page");
        primaryStage.setScene(scene);
        primaryStage.show();

         loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String pass = passField.getText().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please fill in both fields!").showAndWait();
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "")) {
                String hashedInput = SecurityUtil.hashPassword(pass);
                String sql = "SELECT * FROM admin WHERE email=? AND password=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, email);
                pstmt.setString(2, hashedInput);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    new Dashboard().showDashboard();
                    primaryStage.close();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Invalid email or password!").showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Database Error: " + ex.getMessage()).showAndWait();
            }
        });

        forgotLink.setOnAction(e -> forgotPassword());
    }
     public void forgotPassword() {
        Stage stage = new Stage();
        stage.setTitle("Forgot Password - Admin");

        Label emailLabel = new Label("New Email:");
        emailLabel.setMinWidth(100);
        TextField emailField = new TextField();

        Label newPassLabel = new Label("New Password:");
        newPassLabel.setMinWidth(100);
        PasswordField newPassField = new PasswordField();

        Button resetBtn = new Button("Reset Account");
        Button backBtn = new Button("Back");

        VBox formLayout = new VBox(15,
                new HBox(10, emailLabel, emailField),
                new HBox(10, newPassLabel, newPassField),
                resetBtn, backBtn);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(20));

        Scene scene = new Scene(formLayout, 450, 250);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        stage.setScene(scene);
        stage.show();

        backBtn.setOnAction(e -> stage.close());

        resetBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String newPass = newPassField.getText().trim();

            if (email.isEmpty() || newPass.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please fill all fields!").showAndWait();
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "")) {
                String checkSql = "SELECT * FROM admin WHERE email=?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();

                String hashedPass = SecurityUtil.hashPassword(newPass);
                if (rs.next()) {
                    // Update existing admin
                    String updateSql = "UPDATE admin SET password=? WHERE email=?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, hashedPass);
                    updateStmt.setString(2, email);
                    updateStmt.executeUpdate();
                    new Alert(Alert.AlertType.INFORMATION, "Password reset successfully!").showAndWait();
                    stage.close();
                } else {
                    // Create new admin with only email + password
                    String insertSql = "INSERT INTO admin (email, password) VALUES (?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                    insertStmt.setString(1, email);
                    insertStmt.setString(2, hashedPass);
                    insertStmt.executeUpdate();
                    new Alert(Alert.AlertType.INFORMATION, "New admin account created successfully!").showAndWait();
                    stage.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Database Error: " + ex.getMessage()).showAndWait();
            }
        });
    }

    // --- Utility class for hashing ---
    public static class SecurityUtil {
        public static String hashPassword(String plainPass) throws Exception {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPass.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        }
    }

}

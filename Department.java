package UNIEMPOYEEMANAGMENT;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class Department {

    private String deptId;
    private String deptName;
    private String description;
    private String location;
    private String headOfDepartment;

    // ===== Getters and Setters =====
    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getHeadOfDepartment() {
        return headOfDepartment;
    }

    public void setHeadOfDepartment(String headOfDepartment) {
        this.headOfDepartment = headOfDepartment;
    }

    // ===== Add Department =====
    public void addDepartment() {
        Stage stage = new Stage();
        stage.setTitle("Add Department");

        Label titleLabel = new Label("Department Details - Add");
        titleLabel.getStyleClass().add("department-title"); // New CSS class

        Label nameLabel = new Label("Department Name:");
        TextField nameField = new TextField();

        Label descLabel = new Label("Description:");
        TextField descField = new TextField();

        Label locLabel = new Label("Location:");
        TextField locField = new TextField();

        Label headLabel = new Label("Head of Department:");
        TextField headField = new TextField();

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("action-btn");

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("back-btn");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, nameLabel, nameField);
        grid.addRow(1, descLabel, descField);
        grid.addRow(2, locLabel, locField);
        grid.addRow(3, headLabel, headField);

        HBox actions = new HBox(10, saveBtn, backBtn);
        actions.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, titleLabel, grid, actions);
        root.setPadding(new Insets(15));
        root.getStyleClass().add("add-department-root");

        Scene scene = new Scene(root, 500, 400);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String loc = locField.getText().trim();
            String head = headField.getText().trim();

            if (name.isEmpty() || desc.isEmpty() || loc.isEmpty() || head.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please fill all fields.").showAndWait();
                return;
            }

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost/employee_management", "root", "");
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO department (dept_id, dept_name, description, location, head_of_department) VALUES (?, ?, ?, ?, ?)")) {

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM department");
                int nextId = 1;
                if (rs.next()) nextId = rs.getInt("total") + 1;
                String deptId = "D" + String.format("%02d", nextId);

                ps.setString(1, deptId);
                ps.setString(2, name);
                ps.setString(3, desc);
                ps.setString(4, loc);
                ps.setString(5, head);
                ps.executeUpdate();

                new Alert(Alert.AlertType.INFORMATION, "Department added successfully!").showAndWait();
                stage.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
            }
        });

        backBtn.setOnAction(e -> {
            stage.close();                  // close current window
            new Dashboard().showDashboard(); // reopen dashboard
        });
       }

    // ===== View / Manage Departments =====
    public void viewDepartment() {
        Stage stage = new Stage();
        stage.setTitle("Manage Departments");

        Label titleLabel = new Label("Department Details - Manage");
        titleLabel.getStyleClass().add("department-title");

        Label searchLabel = new Label("Search:");
        TextField searchField = new TextField();
        searchField.setPromptText("Enter Department Name");

        ComboBox<String> searchCombo = new ComboBox<>();
        searchCombo.setPromptText("Select Department ID");

        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("search-btn");

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-btn");

        Button updateBtn = new Button("Update");
        updateBtn.getStyleClass().add("update-btn");

        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().add("remove-btn");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("refresh-btn");

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("back-btn");

        HBox topBar = new HBox(10, searchLabel, searchField, searchCombo, searchBtn, addBtn, updateBtn, removeBtn, refreshBtn, backBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.getStyleClass().add("top-bar");

        TableView<Department> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Department, String> idCol = new TableColumn<>("Department ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));

        TableColumn<Department, String> nameCol = new TableColumn<>("Department Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("deptName"));

        TableColumn<Department, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Department, String> locCol = new TableColumn<>("Location");
        locCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Department, String> headCol = new TableColumn<>("Head of Department");
        headCol.setCellValueFactory(new PropertyValueFactory<>("headOfDepartment"));

        tableView.getColumns().addAll(idCol, nameCol, descCol, locCol, headCol);

        VBox root = new VBox(10, titleLabel, topBar, tableView);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("view-department-root");

        Scene scene = new Scene(root, 1000, 500);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        loadDepartments(tableView, searchCombo);
        // Button actions
        addBtn.setOnAction(e -> addDepartment());
        searchBtn.setOnAction(e -> {
            String text = searchField.getText().trim().toLowerCase();
            String selectedId = searchCombo.getValue();
            ObservableList<Department> filtered = FXCollections.observableArrayList();
            for (Department d : tableView.getItems()) {
                boolean matchesText = text.isEmpty() || d.getDeptName().toLowerCase().contains(text);
                boolean matchesId = selectedId == null || selectedId.isEmpty() || d.getDeptId().equals(selectedId);
                if (matchesText && matchesId) filtered.add(d);
            }
            tableView.setItems(filtered);
        });
        refreshBtn.setOnAction(e -> loadDepartments(tableView, searchCombo));
        updateBtn.setOnAction(e -> updateDepartment(tableView));
        removeBtn.setOnAction(e -> {
            Department selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a department to remove").showAndWait();
                return;
            }
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "");
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM department WHERE dept_id=?")) {
                ps.setString(1, selected.getDeptId());
                ps.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Department removed successfully!").showAndWait();
                loadDepartments(tableView, searchCombo);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        backBtn.setOnAction(e -> {
            stage.close();                  // close current window
            new Dashboard().showDashboard(); // reopen dashboard
        });
        }

    // ===== Load Departments into TableView and ComboBox =====
    private void loadDepartments(TableView<Department> tableView, ComboBox<String> searchCombo) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM department")) {

            ObservableList<Department> list = FXCollections.observableArrayList();
            ObservableList<String> comboList = FXCollections.observableArrayList();

            while (rs.next()) {
                Department d = new Department();
                d.setDeptId(rs.getString("dept_id"));
                d.setDeptName(rs.getString("dept_name"));
                d.setDescription(rs.getString("description"));
                d.setLocation(rs.getString("location"));
                d.setHeadOfDepartment(rs.getString("head_of_department"));
                list.add(d);
                comboList.add(d.getDeptId());
            }

            tableView.setItems(list);
            searchCombo.setItems(comboList);

        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
        }
    }

    // ===== Update Department =====
    private void updateDepartment(TableView<Department> tableView) {
        Department selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a department to update").showAndWait();
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Update Department");

        Label titleLabel = new Label("Department Details - Update");
        titleLabel.getStyleClass().add("department-title");

        TextField nameField = new TextField(selected.getDeptName());
        TextField descField = new TextField(selected.getDescription());
        TextField locField = new TextField(selected.getLocation());
        TextField headField = new TextField(selected.getHeadOfDepartment());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.addRow(0, new Label("Department Name:"), nameField);
        grid.addRow(1, new Label("Description:"), descField);
        grid.addRow(2, new Label("Location:"), locField);
        grid.addRow(3, new Label("Head of Department:"), headField);

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("action-btn");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("cancel-btn");

        HBox actions = new HBox(10, saveBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, titleLabel, grid, actions);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("update-department-root");

        stage.setScene(new Scene(root, 500, 400));
        stage.show();

        saveBtn.setOnAction(e -> {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "");
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE department SET dept_name=?, description=?, location=?, head_of_department=? WHERE dept_id=?")) {

                ps.setString(1, nameField.getText().trim());
                ps.setString(2, descField.getText().trim());
                ps.setString(3, locField.getText().trim());
                ps.setString(4, headField.getText().trim());
                ps.setString(5, selected.getDeptId());
                ps.executeUpdate();

                selected.setDeptName(nameField.getText().trim());
                selected.setDescription(descField.getText().trim());
                selected.setLocation(locField.getText().trim());
                selected.setHeadOfDepartment(headField.getText().trim());
                tableView.refresh();

                new Alert(Alert.AlertType.INFORMATION, "Department updated successfully!").showAndWait();
                stage.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
            }
        });

        cancelBtn.setOnAction(e -> stage.close());
    }
}

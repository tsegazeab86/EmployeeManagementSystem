package UNIEMPOYEEMANAGMENT;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class Employee {
    private String empid;
    private String fname;
    private String lname;
    private String email;
    private String address;
    private String phone;
    private String job;
    private String department;
    private Double salary;   // use int if DB column is INT
    private String designation;
    private String gender;
    private Image photo;
    private String photoPath;

    // getters and setters
    public String getEmpid() {
        return empid;
    }

    public void setEmpid(String empid) {
        this.empid = empid;
    }

    public Image getPhoto() {
        return photo;
    }

    public void setPhoto(Image photo) {
        this.photo = photo;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public void addEmployee() {
        Stage stage = new Stage();
        stage.setTitle("Add Employee");

        // ====== Title ======
        Label titleLabel = new Label("Add Employee Details");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.getStyleClass().add("form-title");
        VBox topContainer = new VBox(titleLabel);

        // ====== Form Fields ======
        TextField fnamef = new TextField();
        TextField lnamef = new TextField();
        TextField emailf = new TextField();
        TextField addressf = new TextField();
        TextField phonef = new TextField();
        TextField jobf = new TextField();
        TextField salaryf = new TextField();
        TextField designationf = new TextField();

        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female");
        genderCombo.setPromptText("Select Gender");

        ComboBox<String> departmentCombo = new ComboBox<>();
        departmentCombo.setPromptText("Select Department");

        // Load departments from DB
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost/employee_management", "root", "")) {

                String sql = "SELECT dept_name FROM department";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    departmentCombo.getItems().add(rs.getString("dept_name"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error loading departments: " + ex.getMessage()).showAndWait();
        }

        // ====== Image Field ======
        TextField photoPathField = new TextField();
        photoPathField.setPromptText("No image selected");
        photoPathField.setEditable(false);
        Button browseBtn = new Button("Browse Image");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        browseBtn.setOnAction(ev -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                photoPathField.setText(file.getAbsolutePath());
            }
        });

        // ====== Layout ======
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        grid.addRow(0, new Label("First Name"), fnamef);
        grid.addRow(1, new Label("Last Name"), lnamef);
        grid.addRow(2, new Label("Email"), emailf);
        grid.addRow(3, new Label("Address"), addressf);
        grid.addRow(4, new Label("Phone"), phonef);
        grid.addRow(5, new Label("Job"), jobf);
        grid.addRow(6, new Label("Department"), departmentCombo);
        grid.addRow(7, new Label("Salary"), salaryf);
        grid.addRow(8, new Label("Designation"), designationf);
        grid.addRow(9, new Label("Gender"), genderCombo);
        grid.addRow(10, new Label("Photo"), photoPathField, browseBtn);

        // ====== Buttons ======
        Button save = new Button("Save");
        Button backbtn = new Button("Back");
        HBox buttons = new HBox(10, save, backbtn);
        buttons.setAlignment(Pos.CENTER);
        grid.add(buttons, 0, 11, 2, 1);

        // ====== Content Layout ======
        BorderPane content = new BorderPane();
        content.setTop(topContainer);
        content.setCenter(grid);

        // ====== Background Image ======
        ImageView bgImage = new ImageView(
                Objects.requireNonNull(getClass().getResource("/image/3.jpg")).toExternalForm());
        bgImage.setPreserveRatio(false);

        StackPane root = new StackPane(bgImage, content);
        Scene scene = new Scene(root, 800, 600);
        bgImage.fitWidthProperty().bind(scene.widthProperty());
        bgImage.fitHeightProperty().bind(scene.heightProperty());

        stage.setScene(scene);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        stage.show();

        // ====== Back Button ======
        backbtn.setOnAction(e -> {
            stage.close();
            new Dashboard().showDashboard();
        });

        // ====== Save Button ======
        save.setOnAction(e -> {
            if (fnamef.getText().isEmpty() || lnamef.getText().isEmpty() ||
                    emailf.getText().isEmpty() || salaryf.getText().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please fill all required fields.").showAndWait();
                return;
            }

            double salaryValue;
            try {
                salaryValue = Double.parseDouble(salaryf.getText());
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Salary must be a valid number.").showAndWait();
                return;
            }

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost/employee_management", "root", "")) {

                    String sql = "INSERT INTO employees (fname, lname, email, address, phone, job, department, salary, designation, gender, photo_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pstmt.setString(1, fnamef.getText());
                    pstmt.setString(2, lnamef.getText());
                    pstmt.setString(3, emailf.getText());
                    pstmt.setString(4, addressf.getText());
                    pstmt.setString(5, phonef.getText());
                    pstmt.setString(6, jobf.getText());
                    pstmt.setString(7, departmentCombo.getValue() != null ? departmentCombo.getValue() : "");
                    pstmt.setDouble(8, salaryValue);
                    pstmt.setString(9, designationf.getText());
                    pstmt.setString(10, genderCombo.getValue() != null ? genderCombo.getValue() : "");

                    String photoPath = photoPathField.getText();
                    if (photoPath == null || photoPath.isEmpty()) {
                        pstmt.setNull(11, Types.VARCHAR);
                    } else {
                        pstmt.setString(11, photoPath);
                    }

                    pstmt.executeUpdate();

                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        String newEmpId = "E" + String.format("%03d", newId);

                        PreparedStatement up = conn.prepareStatement("UPDATE employees SET empid = ? WHERE id = ?");
                        up.setString(1, newEmpId);
                        up.setInt(2, newId);
                        up.executeUpdate();

                        new Alert(Alert.AlertType.INFORMATION, "Employee Added Successfully\nGenerated ID: " + newEmpId).showAndWait();
                        stage.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Database Error: " + ex.getMessage()).showAndWait();
            }
        });
    }


    public void viewEmployee() {
        Stage stage = new Stage();
        stage.setTitle("View Employee");
        Label titleLabel = new Label("View Employee Details");
        titleLabel.getStyleClass().add("form-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        Label searchLabel = new Label("Search by ID:");
        searchLabel.setId("search-label");

        TextField searchField = new TextField();
        searchField.setPromptText("Enter Employee ID");
        searchField.setId("search-field");
        ComboBox<String> searchCombo = new ComboBox<>();
        searchCombo.setPromptText("Select Employee ID");
        searchCombo.setId("search-combo");

        ObservableList<String> empIds = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT empid FROM employees")) {

            while (rs.next()) {
                empIds.add(rs.getString("empid"));
            }
            searchCombo.setItems(empIds);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        ComboBox<String> deptCombo = new ComboBox<>();
        deptCombo.setPromptText("Select Department");
        deptCombo.setId("dept-combo");

        ObservableList<String> deptNames = FXCollections.observableArrayList();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT department FROM employees")) {

            while (rs.next()) {
                deptNames.add(rs.getString("department"));
            }
            deptCombo.setItems(deptNames);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }


        Button addBtn = new Button("Add");
        addBtn.setId("add-btn");

        Button searchBtn = new Button("Search");
        searchBtn.setId("search-btn");
        Button viewProfileBtn = new Button("view profile");
        viewProfileBtn.setId("viewprofile");
        Button printBtn = new Button("Print");
        printBtn.setId("print-btn");

        Button updateBtn = new Button("Update");
        updateBtn.setId("update-btn");
        Button removeBtn = new Button("Remove");
        removeBtn.setId("remove-btn");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setId("refresh-btn");

        Button backBtn = new Button("Back");
        backBtn.setId("back-btn");

        HBox leftBox = new HBox(10, searchLabel, searchField, searchBtn, searchCombo, deptCombo);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        HBox rightBox = new HBox(10, viewProfileBtn, addBtn, updateBtn, printBtn, refreshBtn, removeBtn, backBtn);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        BorderPane searchBox = new BorderPane();
        searchBox.setLeft(leftBox);
        searchBox.setRight(rightBox);
        searchBox.setPadding(new Insets(20));

        TableView<Employee> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("No employees found"));

        TableColumn<Employee, String> empidColumn = new TableColumn<>("Employee ID");
        empidColumn.setCellValueFactory(new PropertyValueFactory<>("empid"));

        TableColumn<Employee, String> fnameColumn = new TableColumn<>("First Name");
        fnameColumn.setCellValueFactory(new PropertyValueFactory<>("fname"));

        TableColumn<Employee, String> lnameColumn = new TableColumn<>("Last Name");
        lnameColumn.setCellValueFactory(new PropertyValueFactory<>("lname"));

        TableColumn<Employee, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Employee, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Employee, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Employee, String> jobColumn = new TableColumn<>("Job");
        jobColumn.setCellValueFactory(new PropertyValueFactory<>("job"));

        TableColumn<Employee, String> departmentColumn = new TableColumn<>("Department");
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));


        TableColumn<Employee, Double> salaryColumn = new TableColumn<>("Salary");
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));

        TableColumn<Employee, String> designationColumn = new TableColumn<>("Designation");
        designationColumn.setCellValueFactory(new PropertyValueFactory<>("designation"));

        TableColumn<Employee, String> genderColumn = new TableColumn<>("Gender");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<Employee, String> photoColumn = new TableColumn<>("Photo");
        photoColumn.setCellValueFactory(new PropertyValueFactory<>("photoPath"));
        photoColumn.setCellFactory(col -> new TableCell<Employee, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null || path.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        imageView.setImage(new Image("file:" + path));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        tableView.getColumns().addAll(
                empidColumn, photoColumn, fnameColumn, lnameColumn, emailColumn,
                addressColumn, phoneColumn, jobColumn, departmentColumn,
                salaryColumn, designationColumn, genderColumn
        );

        VBox topContainer = new VBox(10);
        topContainer.setPadding(new Insets(10));
        topContainer.setAlignment(Pos.CENTER_LEFT);

        topContainer.getChildren().addAll(titleLabel, searchBox);

        BorderPane root = new BorderPane();
        root.setTop(topContainer);
        root.setCenter(tableView);


        Scene scene = new Scene(root, 1100, 520);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();

        ObservableList<Employee> employees = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setEmpid(rs.getString("empid"));
                emp.setFname(rs.getString("fname"));
                emp.setLname(rs.getString("lname"));
                emp.setEmail(rs.getString("email"));
                emp.setAddress(rs.getString("address"));
                emp.setPhone(rs.getString("phone"));
                emp.setJob(rs.getString("job"));
                emp.setDepartment(rs.getString("department"));
                emp.setSalary(rs.getDouble("salary"));
                emp.setDesignation(rs.getString("designation"));
                emp.setGender(rs.getString("gender"));
                emp.setPhotoPath(rs.getString("photo_path"));
                employees.add(emp);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
        }
        tableView.setItems(employees);

        ImageView photoView = new ImageView();
        photoView.setFitWidth(120);
        photoView.setFitHeight(120);
        photoView.setPreserveRatio(true);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null && newSel.getPhotoPath() != null) {
                photoView.setImage(new Image("file:" + newSel.getPhotoPath()));
            } else {
                photoView.setImage(null);
            }
        });

        searchCombo.setOnAction(e -> {
            String selectedId = searchCombo.getSelectionModel().getSelectedItem();
            if (selectedId != null) {
                // Fill the search field automatically
                searchField.setText(selectedId);

                // Filter table by selected empid
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "")) {
                    String sql = "SELECT * FROM employees WHERE empid = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, selectedId);
                    ResultSet rs = pstmt.executeQuery();

                    ObservableList<Employee> searchResult = FXCollections.observableArrayList();
                    while (rs.next()) {
                        Employee emp = new Employee();
                        emp.setEmpid(rs.getString("empid"));
                        emp.setFname(rs.getString("fname"));
                        emp.setLname(rs.getString("lname"));
                        emp.setEmail(rs.getString("email"));
                        emp.setAddress(rs.getString("address"));
                        emp.setPhone(rs.getString("phone"));
                        emp.setJob(rs.getString("job"));
                        emp.setDepartment(rs.getString("department"));
                        emp.setSalary(rs.getDouble("salary"));
                        emp.setDesignation(rs.getString("designation"));
                        emp.setGender(rs.getString("gender"));
                        emp.setPhotoPath(rs.getString("photo_path"));
                        searchResult.add(emp);
                    }

                    tableView.setItems(searchResult);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
                }
            }
        });

        deptCombo.setOnAction(e -> {
            String selectedDept = deptCombo.getSelectionModel().getSelectedItem();
            if (selectedDept != null) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "")) {
                    String sql = "SELECT * FROM employees WHERE department = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, selectedDept);
                    ResultSet rs = pstmt.executeQuery();

                    ObservableList<Employee> deptResult = FXCollections.observableArrayList();
                    while (rs.next()) {
                        Employee emp = new Employee();
                        emp.setEmpid(rs.getString("empid"));
                        emp.setFname(rs.getString("fname"));
                        emp.setLname(rs.getString("lname"));
                        emp.setEmail(rs.getString("email"));
                        emp.setAddress(rs.getString("address"));
                        emp.setPhone(rs.getString("phone"));
                        emp.setJob(rs.getString("job"));
                        emp.setDepartment(rs.getString("department"));
                        emp.setSalary(rs.getDouble("salary"));
                        emp.setDesignation(rs.getString("designation"));
                        emp.setGender(rs.getString("gender"));
                        emp.setPhotoPath(rs.getString("photo_path"));
                        deptResult.add(emp);
                    }

                    tableView.setItems(deptResult);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
                }
            }
        });

        searchBtn.setOnAction(e -> {
            String searchId = searchField.getText().trim();

            if (searchId.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please enter an Employee ID!").showAndWait();
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "")) {
                String sql = "SELECT * FROM employees WHERE empid = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, searchId);
                ResultSet rs = pstmt.executeQuery();

                ObservableList<Employee> searchResult = FXCollections.observableArrayList();
                while (rs.next()) {
                    Employee emp = new Employee();
                    emp.setEmpid(rs.getString("empid"));
                    emp.setFname(rs.getString("fname"));
                    emp.setLname(rs.getString("lname"));
                    emp.setEmail(rs.getString("email"));
                    emp.setAddress(rs.getString("address"));
                    emp.setPhone(rs.getString("phone"));
                    emp.setJob(rs.getString("job"));
                    emp.setDepartment(rs.getString("department"));
                    emp.setSalary(rs.getDouble("salary"));
                    emp.setDesignation(rs.getString("designation"));
                    emp.setGender(rs.getString("gender"));
                    emp.setPhotoPath(rs.getString("photo_path"));
                    searchResult.add(emp);
                }

                if (searchResult.isEmpty()) {
                    new Alert(Alert.AlertType.INFORMATION, "No employee found with ID: " + searchId).showAndWait();
                }
                tableView.setItems(searchResult);

            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
            }
        });

        updateBtn.setOnAction(e -> {
            updateEmployee(tableView);
        });

        printBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(stage)) {
                boolean success = job.printPage(tableView);
                if (success) {
                    job.endJob();
                    new Alert(Alert.AlertType.INFORMATION, "Printed successfully!").showAndWait();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Printing failed.").showAndWait();
                }
            }

        });
        refreshBtn.setOnAction(e -> {
            ObservableList<Employee> employee = FXCollections.observableArrayList();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {

                while (rs.next()) {
                    Employee emp = new Employee();
                    emp.setEmpid(rs.getString("empid"));
                    emp.setFname(rs.getString("fname"));
                    emp.setLname(rs.getString("lname"));
                    emp.setEmail(rs.getString("email"));
                    emp.setAddress(rs.getString("address"));
                    emp.setPhone(rs.getString("phone"));
                    emp.setJob(rs.getString("job"));
                    emp.setDepartment(rs.getString("department"));
                    emp.setSalary(rs.getDouble("salary"));
                    emp.setDesignation(rs.getString("designation"));
                    emp.setGender(rs.getString("gender"));
                    emp.setPhotoPath(rs.getString("photo_path"));
                    employee.add(emp);
                }

                tableView.setItems(employee);

            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
            }
        });
        viewProfileBtn.setOnAction(e -> {
            Employee emp = tableView.getSelectionModel().getSelectedItem();
            if (emp == null) {
                new Alert(Alert.AlertType.WARNING, "Select employee first").showAndWait();
                return;
            }
            showEmployeeProfile(emp);
        });

        removeBtn.setOnAction(e -> {

            Employee selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Please select an employee to remove!").showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Removal");
            alert.setHeaderText("Are you sure you want to remove this employee?");
            alert.setContentText("Choose Yes or No.");

            ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "")) {
                        String sql = "DELETE FROM employees WHERE empid = ?";
                        PreparedStatement pstmt = conn.prepareStatement(sql);
                        pstmt.setString(1, selected.getEmpid());
                        pstmt.executeUpdate();

                        tableView.getItems().remove(selected);
//                        new Alert(Alert.AlertType.INFORMATION, "Employee removed successfully!").showAndWait();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
                    }
                }
            });

        });
        addBtn.setOnAction(event -> {
            addEmployee();
        });
        backBtn.setOnAction(e -> {
            stage.close();                  // close current window
            new Dashboard().showDashboard(); // reopen dashboard
        });
    }

    private void showEmployeeProfile(Employee emp) {
        Stage profileStage = new Stage();
        profileStage.setTitle("Employee Profile");

        // Top header
        Label header = new Label("University Employee Management System");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        header.setAlignment(Pos.CENTER);

        // Employee details
        Label nameLabel = new Label("Name: " + emp.getFname() + " " + emp.getLname());
        Label idLabel = new Label("Employee ID: " + emp.getEmpid());
        Label deptLabel = new Label("Department: " + emp.getDepartment());
        Label genderLabel = new Label("Gender: " + emp.getGender());

        nameLabel.setStyle("-fx-font-size: 16px;");
        idLabel.setStyle("-fx-font-size: 16px;");
        deptLabel.setStyle("-fx-font-size: 16px;");
        genderLabel.setStyle("-fx-font-size: 16px;");

        // Photo
        ImageView photoView = new ImageView();
        if (emp.getPhotoPath() != null && !emp.getPhotoPath().isEmpty()) {
            photoView.setImage(new Image("file:" + emp.getPhotoPath()));
        }
        photoView.setFitWidth(120);
        photoView.setFitHeight(120);
        photoView.setPreserveRatio(true);

        // Card-like container
        VBox cardBox = new VBox(10, photoView, nameLabel, idLabel, deptLabel, genderLabel);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPadding(new Insets(20));
        cardBox.setStyle(
                "-fx-background-color: #ecf0f1; " +
                        "-fx-border-color: #34495e; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);"
        );


        BorderPane root = new BorderPane();
        root.setTop(header);
        BorderPane.setAlignment(header, Pos.CENTER);
        root.setCenter(cardBox);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 500, 400);
        profileStage.setScene(scene);
        profileStage.show();
    }

    public void updateEmployee(TableView<Employee> tableView) {
        Employee emp = tableView.getSelectionModel().getSelectedItem();
        if (emp == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an employee to update!").showAndWait();
            return;
        }

        Stage updateStage = new Stage();
        updateStage.setTitle("Update Employee");

        // ====== Title Bar ======
        Label titleLabel = new Label("Update Employee Details");
        titleLabel.getStyleClass().add("update-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.getStyleClass().add("form-title");
        VBox topContainer = new VBox(titleLabel);


        // ====== Form Fields ======
        TextField empidf = new TextField(emp.getEmpid());
        empidf.setEditable(false);

        TextField fnamef = new TextField(emp.getFname());
        TextField lnamef = new TextField(emp.getLname());
        TextField emailf = new TextField(emp.getEmail());
        TextField addressf = new TextField(emp.getAddress());
        TextField phonef = new TextField(emp.getPhone());
        TextField jobf = new TextField(emp.getJob());
        TextField departmentf = new TextField(emp.getDepartment());
        TextField salaryf = new TextField(String.valueOf(emp.getSalary()));
        TextField designationf = new TextField(emp.getDesignation());

        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female");
        genderCombo.setValue(emp.getGender());

        TextField photoPathField = new TextField();
        photoPathField.setPromptText("No image selected");
        photoPathField.setEditable(false);
        Button browseBtn = new Button("Browse Image");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        browseBtn.setOnAction(ev -> {
            File file = fileChooser.showOpenDialog(updateStage);
            if (file != null) {
                photoPathField.setText(file.getAbsolutePath());
            }
        });

        // ====== Grid Layout ======
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setAlignment(Pos.CENTER);

        grid.addRow(0, new Label("Employee ID"), empidf);
        grid.addRow(1, new Label("First Name"), fnamef);
        grid.addRow(2, new Label("Last Name"), lnamef);
        grid.addRow(3, new Label("Email"), emailf);
        grid.addRow(4, new Label("Address"), addressf);
        grid.addRow(5, new Label("Phone"), phonef);
        grid.addRow(6, new Label("Job"), jobf);
        grid.addRow(7, new Label("Department"), departmentf);
        grid.addRow(8, new Label("Salary"), salaryf);
        grid.addRow(9, new Label("Designation"), designationf);
        grid.addRow(10, new Label("Gender"), genderCombo);
        grid.addRow(11, new Label("Photo"), photoPathField, browseBtn);

        Button updateBtn = new Button("Update");
        Button cancelBtn = new Button("Cancel");
        HBox actions = new HBox(15, updateBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER);
        grid.add(actions, 1, 12);

        // ====== Root Layout ======
        BorderPane root = new BorderPane();
        root.setTop(topContainer);
        root.setCenter(grid);

        Scene scene = new Scene(root, 650, 520);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        updateStage.setScene(scene);
        updateStage.show();

        // ====== Update Button Action ======
        updateBtn.setOnAction(ep -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to update this employee?",
                    ButtonType.YES, ButtonType.NO);
            confirmAlert.setTitle("Confirm Update");
            confirmAlert.setHeaderText(null);

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                double newSalary;
                try {
                    newSalary = Double.parseDouble(salaryf.getText().trim());
                } catch (NumberFormatException nfe) {
                    new Alert(Alert.AlertType.WARNING, "Salary must be a valid number.").showAndWait();
                    return;
                }

                try (Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost/employee_management", "root", "")) {
                    String sql = "UPDATE employees SET fname=?, lname=?, email=?, address=?, phone=?, job=?, department=?, salary=?, designation=?, gender=?, photo_path=? WHERE empid=?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, fnamef.getText().trim());
                    pstmt.setString(2, lnamef.getText().trim());
                    pstmt.setString(3, emailf.getText().trim());
                    pstmt.setString(4, addressf.getText().trim());
                    pstmt.setString(5, phonef.getText().trim());
                    pstmt.setString(6, jobf.getText().trim());
                    pstmt.setString(7, departmentf.getText().trim());
                    pstmt.setDouble(8, newSalary);
                    pstmt.setString(9, designationf.getText().trim());
                    pstmt.setString(10, genderCombo.getValue());
                    pstmt.setString(11, photoPathField.getText().trim());
                    pstmt.setString(12, emp.getEmpid());

                    pstmt.executeUpdate();

                    // Update local object
                    emp.setFname(fnamef.getText().trim());
                    emp.setLname(lnamef.getText().trim());
                    emp.setEmail(emailf.getText().trim());
                    emp.setAddress(addressf.getText().trim());
                    emp.setPhone(phonef.getText().trim());
                    emp.setJob(jobf.getText().trim());
                    emp.setDepartment(departmentf.getText().trim());
                    emp.setSalary(newSalary);
                    emp.setDesignation(designationf.getText().trim());
                    emp.setGender(genderCombo.getValue());

                    tableView.refresh();
                    new Alert(Alert.AlertType.INFORMATION, "Employee updated successfully!").showAndWait();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
                }
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Update cancelled.").showAndWait();
            }
        });

        // ====== Cancel Button ======
        cancelBtn.setOnAction(event -> updateStage.close());
    }

    public void employeeReport() {
        Stage stage = new Stage();
        stage.setTitle("Employee Report");

        TableView<Employee> table = new TableView<>();

        Button backbutton = new Button("Back");
        backbutton.setId("allreportback");

        Button printButton = new Button("Print");

        Label totalEmployeesLabel = new Label("Total Employees: 0");
        totalEmployeesLabel.setId("totalemployees");

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No employees found"));

        // --- Columns ---
        TableColumn<Employee, String> empidColumn = new TableColumn<>("EmpID");
        empidColumn.setCellValueFactory(new PropertyValueFactory<>("empid"));

        TableColumn<Employee, String> fnameColumn = new TableColumn<>("FName");
        fnameColumn.setCellValueFactory(new PropertyValueFactory<>("fname"));

        TableColumn<Employee, String> lnameColumn = new TableColumn<>("LName");
        lnameColumn.setCellValueFactory(new PropertyValueFactory<>("lname"));

        TableColumn<Employee, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Employee, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Employee, String> phoneColumn = new TableColumn<>("Phone");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Employee, String> jobColumn = new TableColumn<>("Job");
        jobColumn.setCellValueFactory(new PropertyValueFactory<>("job"));

        TableColumn<Employee, String> departmentColumn = new TableColumn<>("Department");
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));

        TableColumn<Employee, Double> salaryColumn = new TableColumn<>("Salary");
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));

        TableColumn<Employee, String> designationColumn = new TableColumn<>("Designation");
        designationColumn.setCellValueFactory(new PropertyValueFactory<>("designation"));

        TableColumn<Employee, String> genderColumn = new TableColumn<>("Gender");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        // --- Photo Column with ImageView ---
        TableColumn<Employee, String> photoColumn = new TableColumn<>("Photo");
        photoColumn.setCellValueFactory(new PropertyValueFactory<>("photoPath"));
        photoColumn.setCellFactory(col -> new TableCell<Employee, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null || path.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        imageView.setImage(new Image("file:" + path));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                    }
                }
            }
        });

        table.getColumns().addAll(
                empidColumn, photoColumn, fnameColumn, lnameColumn, emailColumn,
                addressColumn, phoneColumn, jobColumn, departmentColumn,
                salaryColumn, designationColumn, genderColumn
        );

        // --- Load Data ---
        ObservableList<Employee> employees = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/employee_management", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setEmpid(rs.getString("empid"));
                emp.setFname(rs.getString("fname"));
                emp.setLname(rs.getString("lname"));
                emp.setEmail(rs.getString("email"));
                emp.setAddress(rs.getString("address"));
                emp.setPhone(rs.getString("phone"));
                emp.setJob(rs.getString("job"));
                emp.setDepartment(rs.getString("department"));
                emp.setSalary(rs.getDouble("salary"));
                emp.setDesignation(rs.getString("designation"));
                emp.setGender(rs.getString("gender"));
                emp.setPhotoPath(rs.getString("photo_path")); // ✅ Correct column name
                employees.add(emp);
            }

            table.setItems(employees);
            totalEmployeesLabel.setText("Total Employees: " + employees.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        printButton.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(stage)) {
                boolean success = job.printPage(table);
                if (success) {
                    job.endJob();
                    new Alert(Alert.AlertType.INFORMATION, "Printed successfully!").showAndWait();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Printing failed.").showAndWait();
                }
            }
        });

        backbutton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10, printButton, backbutton);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(10, table, totalEmployeesLabel, buttonBox);
        vbox.setPadding(new Insets(10));

        Scene scene = new Scene(vbox, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

}



package clientside;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import serverside.DatabaseOperations;

public class UserInterface {

    private DatabaseOperations db_operations;

    private Scene scene;
    private VBox root;

    // Signup fields
    private TextField studentIdField;
    private TextField firstNameField;
    private TextField surnameField;
    private TextField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;

    // Login fields
    private TextField loginStudentIdField;
    private PasswordField loginPasswordField;

    private Label messageLabel;

    private final Stage primaryStage;
    private final Runnable switchToLoginPage;

    private enum PageType { SIGNUP, LOGIN }
    private PageType currentPage;

    public UserInterface(Stage primaryStage, Runnable switchToLoginPage) {
        this.primaryStage = primaryStage;
        this.switchToLoginPage = switchToLoginPage;

        db_operations = new DatabaseOperations();

        createSignupPage();
    }

    private void createSignupPage() {
        currentPage = PageType.SIGNUP;
        root = new VBox(24);
        root.setPadding(new Insets(48, 64, 48, 64));
        root.setAlignment(Pos.TOP_CENTER);

        Image backgroundImage = new Image(getClass().getResource("/Resources/wallpaper.jpg").toExternalForm());
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(true);
        backgroundImageView.setSmooth(true);
        backgroundImageView.setCache(true);
        backgroundImageView.setOpacity(0.7);

        Label titleLabel = new Label("Sign Up");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Segoe UI", 28));

        VBox form = new VBox(20);
        form.setMaxWidth(400);
        form.setPrefWidth(400);
        form.setPadding(new Insets(40));
        form.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(10), Insets.EMPTY)));
        form.setEffect(new DropShadow(10, Color.DARKGRAY));
        form.setBorder(new Border(new BorderStroke(Color.LIGHTGREY, BorderStrokeStyle.SOLID, new CornerRadii(10),BorderWidths.DEFAULT)));

        form.getChildren().add(createLabeledInput("Student ID", studentIdField = new TextField()));
        form.getChildren().add(createLabeledInput("First Name", firstNameField = new TextField()));
        form.getChildren().add(createLabeledInput("Surname", surnameField = new TextField()));
        form.getChildren().add(createLabeledInput("Email", emailField = new TextField()));
        form.getChildren().add(createLabeledPasswordInput("New Password", passwordField = new PasswordField()));
        form.getChildren().add(createLabeledPasswordInput("Confirm Password", confirmPasswordField = new PasswordField()));

        Button submitButton = new Button("Submit");
        submitButton.setPrefWidth(400);
        submitButton.setFont(Font.font("Segoe UI", 16));
        submitButton.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-background-radius: 12px;");
        submitButton.setOnAction(e -> handleSignupSubmit());

        messageLabel = new Label();
        messageLabel.setTextFill(Color.web("#B00020"));
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Segoe UI", 14));
        messageLabel.setMaxWidth(400);

        Region spacer = new Region();
        spacer.setPrefHeight(12);

        Label loginRedirectLabel = new Label("Already have an account? Log in");
       /* loginRedirectLabel.setFont(Font.font("Segoe UI", 14));*/
        loginRedirectLabel.setTextFill(Color.web("#6366F1"));
        loginRedirectLabel.setUnderline(true);
        loginRedirectLabel.setOnMouseClicked(e -> createLoginPage());
        loginRedirectLabel.setCursor(javafx.scene.Cursor.HAND);

        root.getChildren().addAll(titleLabel, form, submitButton, messageLabel, spacer, loginRedirectLabel);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(backgroundImageView, root);

        scene = new Scene(stackPane, 520, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sign Up");
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    private void createLoginPage() {
        currentPage = PageType.LOGIN;
        root = new VBox(24);
        root.setPadding(new Insets(48, 64, 48, 64));
        root.setAlignment(Pos.TOP_CENTER);

        Image backgroundImage = new Image(getClass().getResource("/Resources/wallpaper.jpg").toExternalForm());
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setPreserveRatio(true);
        backgroundImageView.setSmooth(true);
        backgroundImageView.setCache(true);
        backgroundImageView.setOpacity(0.7);
       

        Label titleLabel = new Label("Log In");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("Segoe UI", 28));

        VBox form = new VBox(20);
        form.setMaxWidth(400);
        form.setPrefWidth(400);
        form.setPadding(new Insets(40));
        form.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(10), Insets.EMPTY)));
        form.setEffect(new DropShadow(10, Color.DARKGRAY));
        form.setBorder(new Border(new BorderStroke(Color.LIGHTGREY, BorderStrokeStyle.SOLID, new CornerRadii(10),BorderWidths.DEFAULT)));
        

        form.getChildren().add(createLabeledInput("Student ID", loginStudentIdField = new TextField()));
        form.getChildren().add(createLabeledPasswordInput("Password", loginPasswordField = new PasswordField()));

        Button loginButton = new Button("Log In");
        loginButton.setPrefWidth(400);
       /* loginButton.setFont(Font.font("Segoe UI", 16)); */
        loginButton.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-background-radius: 12px;");
        loginButton.setOnAction(e -> handleLoginSubmit());

        Label forgotPasswordLabel = new Label("Forgot Password?");
        forgotPasswordLabel.setFont(Font.font("Segoe UI", 14));
        forgotPasswordLabel.setTextFill(Color.web("#6366F1"));
        forgotPasswordLabel.setUnderline(true);
        forgotPasswordLabel.setOnMouseClicked(e -> handleForgotPassword());
        forgotPasswordLabel.setCursor(javafx.scene.Cursor.HAND);

        messageLabel = new Label();
        messageLabel.setTextFill(Color.web("#B00020"));
        messageLabel.setWrapText(true);
        messageLabel.setFont(Font.font("Segoe UI", 14));
        messageLabel.setMaxWidth(400);

        Region spacer = new Region();
        spacer.setPrefHeight(12);

        Label signupRedirectLabel = new Label("Don't have an account? Sign up");
        signupRedirectLabel.setFont(Font.font("Segoe UI", 14));
        signupRedirectLabel.setTextFill(Color.web("#6366F1"));
        signupRedirectLabel.setUnderline(true);
        signupRedirectLabel.setOnMouseClicked(e -> createSignupPage());
        signupRedirectLabel.setCursor(javafx.scene.Cursor.HAND);

        root.getChildren().addAll(titleLabel, form, loginButton, forgotPasswordLabel, messageLabel, spacer, signupRedirectLabel);

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(backgroundImageView, root);

        scene = new Scene(stackPane, 520, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Log In");
        primaryStage.show();
        primaryStage.setMaximized(true);
    }

    private VBox createLabeledInput(String labelText, TextField inputField) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", 14));
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setTextFill(Color.web("#333333"));
        label.setTranslateY(-2);

        inputField.setPromptText(labelText);
        inputField.setPrefHeight(44);
        inputField.setFont(Font.font("Segoe UI", 14));
        inputField.setStyle("-fx-background-radius: 12px; -fx-border-radius: 12px; -fx-border-color: #9CA3AF; -fx-padding: 0 12px;");

        VBox container = new VBox(6);
        container.getChildren().addAll(label, inputField);
        return container;
    }

    private VBox createLabeledPasswordInput(String labelText, PasswordField passwordField) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", 14));
        label.setTextFill(Color.web("#333333"));
        label.setTranslateY(-2);
        passwordField.setPromptText(labelText);
        passwordField.setPrefHeight(44);
        passwordField.setFont(Font.font("Segoe UI", 14));
        passwordField.setStyle("-fx-background-radius: 12px; -fx-border-radius: 12px; -fx-border-color: #9CA3AF; -fx-padding: 0 12px;");
        VBox container = new VBox(6);
        container.getChildren().addAll(label, passwordField);
        return container;
    }

    private void handleSignupSubmit() {
        String id = studentIdField.getText().trim();
        String fname = firstNameField.getText().trim();
        String surname = surnameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (id.isEmpty() || fname.isEmpty() || surname.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }
        if (!pass.equals(confirmPass)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }
        if (!isValidEmail(email)) {
            messageLabel.setText("Please enter a valid email address.");
            return;
        }
        if (!isStrongPassword(pass)) {
            messageLabel.setText("Password must be at least 8 characters, include uppercase, lowercase, number and special character.");
            return;
        }

        messageLabel.setText("");
        String response = db_operations.registerStudent(id, fname, surname, email, pass, confirmPass);
        messageLabel.setTextFill(response.equals("Student registered successfully.") ? Color.GREEN : Color.web("#B00020"));
        messageLabel.setText(response);

        if (response.equals("Student registered successfully.")) {
            clearForm();
        }
    }

    private void handleLoginSubmit() {
        String id = loginStudentIdField.getText().trim();
        String pass = loginPasswordField.getText();

        if (id.isEmpty() || pass.isEmpty()) {
            messageLabel.setTextFill(Color.web("#B00020"));
            messageLabel.setText("Please enter student ID and password.");
            return;
        }

        messageLabel.setText("");
        String response = db_operations.loginStudent(id, pass);

        if (response.equals("Login successful.")) {
            messageLabel.setTextFill(Color.GREEN);
            messageLabel.setText("Login successful.");
            
            String[] studentNameParts = db_operations.getStudentNameById(id);
            String firstName = studentNameParts != null ? studentNameParts[0] : "Student";
            String surname = studentNameParts != null ? studentNameParts[1] : "";
            
            Dashboard dashboard = new Dashboard(firstName, surname);
            Scene dashboardScene = new Scene(dashboard, 1000, 600);
            primaryStage.setScene(dashboardScene);
            primaryStage.setTitle("Student Dashboard");

           
        } else {
            messageLabel.setTextFill(Color.web("#B00020"));
            messageLabel.setText(response);
        }
    }

    private void handleForgotPassword() {
        // Basic placeholder logic for forgot password
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText(null);
        alert.setContentText("Please contact admin or use the password reset feature.");
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[!@#$%^&*()].*");
    }

    private void clearForm() {
        studentIdField.clear();
        firstNameField.clear();
        surnameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    public Scene getScene() {
        return scene;
    }

}

























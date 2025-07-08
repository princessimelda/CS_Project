package clientside;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import serverside.DatabaseOperations;
import serverside.SessionManager;
import javafx.util.Duration;
import java.io.File;
/*import java.time.Duration;*/
import javafx.animation.PauseTransition;
import javafx.stage.Popup;

public class Account {

    private String profilePicPath = null;

    public Pane buildContent() {
        // Center layout inside a wrapper
        BorderPane wrapper = new BorderPane();
        wrapper.setStyle("-fx-background-color: linear-gradient(to right, #B3E5FC, #FFF9C4);");
        wrapper.setPrefSize(800,600);

        // Main content layout
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setMaxWidth(500); // Optional max width
        VBox.setVgrow(layout, Priority.ALWAYS);

        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter new email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter new password");

        Button uploadButton = new Button("Upload Profile Picture");

        ImageView preview = new ImageView();
        preview.setFitHeight(100);
        preview.setFitWidth(100);
        preview.setPreserveRatio(true);

        CheckBox reminderCheck = new CheckBox("Receive reminders?");
        reminderCheck.setSelected(new DatabaseOperations().getReminderSetting(SessionManager.getLoggedInStudentId()));

        // Reminder preferences section
        Label preferencesLabel = new Label("Reminder Preferences");
        preferencesLabel.setStyle("-fx-font-weight: bold;");

        HBox timeRow = new HBox(10);
        Label timeLabel = new Label("Preferred Reminder Time:");
        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("06:00", "08:00", "12:00", "18:00", "21:00");
        timeCombo.setValue("08:00");
        timeRow.getChildren().addAll(timeLabel, timeCombo);

        HBox frequencyRow = new HBox(10);
        Label frequencyLabel = new Label("Frequency (hrs):");
        Spinner<Integer> frequencySpinner = new Spinner<>(1, 72, 24);
        frequencyRow.getChildren().addAll(frequencyLabel, frequencySpinner);

        HBox priorityRow = new HBox(10);
        Label priorityLabel = new Label("Notify before priority task (hrs):");
        Spinner<Integer> prioritySpinner = new Spinner<>(1, 24, 2);
        priorityRow.getChildren().addAll(priorityLabel, prioritySpinner);

        VBox reminderPrefsBox = new VBox(10, preferencesLabel, timeRow, frequencyRow, priorityRow);
        reminderPrefsBox.setPadding(new Insets(10));
        reminderPrefsBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        reminderPrefsBox.setVisible(reminderCheck.isSelected());


        
        reminderCheck.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            reminderPrefsBox.setVisible(isNowSelected);
            reminderPrefsBox.setManaged(isNowSelected);
        });

        uploadButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg")
            );
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                profilePicPath = selectedFile.getAbsolutePath();
                preview.setImage(new Image(selectedFile.toURI().toString()));
            }
        });

        Button saveButton = new Button("Done");
        saveButton.setOnAction(e -> {
            String newEmail = emailField.getText();
            String newPassword = passwordField.getText();
            boolean remindersEnabled = reminderCheck.isSelected();
            String preferredTime = timeCombo.getValue();
            int frequency = frequencySpinner.getValue();
            int hoursBeforePriority = prioritySpinner.getValue();

            // Save to DB (you will implement this method in DatabaseOperations)
            if (remindersEnabled) {
            DatabaseOperations dbOps = new DatabaseOperations();

            String preferredTimeFormatted = preferredTime + ":00"; // e.g., "08:00"
            String frequencyFormatted = frequency + " hours";      // e.g., "24 hours"
             // e.g., "2 hours"

           
            dbOps.updateReminderPreferences(
                SessionManager.getLoggedInStudentId(),
                preferredTime,
                frequencyFormatted,
                hoursBeforePriority
              );
        }

   
            DatabaseOperations db = new DatabaseOperations();
            db.updateStudentDetails(
                SessionManager.getLoggedInStudentId(),
                newEmail,
                newPassword,
                profilePicPath,
                remindersEnabled
            );
            
            Popup popup = new Popup();
            Label message = new Label("Updates saved successfully!");
            message.setStyle("-fx-background-color: #DFF0D8; -fx-text-fill: #3C763D; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");
            popup.getContent().add(message);
            popup.setAutoHide(true);
            popup.show(saveButton.getScene().getWindow());

            // Automatically hide after 2 seconds
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(event -> popup.hide());
            delay.play();


            /*Alert alert = new Alert(Alert.AlertType.INFORMATION, "Updates saved successfully!", ButtonType.OK);
            alert.showAndWait(); */
        });

        layout.getChildren().addAll(
            title,
            new Label("Update Email:"), emailField,
            new Label("Update Password:"), passwordField,
            new Label("Update Profile:"), uploadButton, preview,
            reminderCheck,
            reminderPrefsBox,
            saveButton
        );

        wrapper.setCenter(layout);
        return wrapper;
    }
}



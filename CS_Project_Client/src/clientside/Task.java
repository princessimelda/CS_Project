package clientside;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ComboBox;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.util.StringConverter;
import serverside.DatabaseOperations;
import serverside.SessionManager;

public class Task {

    private VBox taskDisplayPanel;
    private List<CheckBox> taskCheckBoxes = new ArrayList<>();
    private VBox leftPanel;

    public Pane buildContent(Region parent) {
        // Main container - use BorderPane for better layout control
        BorderPane rootPane = new BorderPane();
        rootPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Left Panel - Form Section
        leftPanel = new VBox(15);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setStyle("-fx-background-color: linear-gradient(to right, #c8f0f5, #e8f5c8);");
        leftPanel.setMaxWidth(Double.MAX_VALUE);

        // Form Heading
        Label heading = new Label("New Task");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        heading.setMaxWidth(Double.MAX_VALUE);
        heading.setAlignment(Pos.CENTER);

        // Form Fields
        TextField titleField = createFormField("e.g IS Project Chapter 4");
        TextField descriptionField = createFormField("e.g Assigned Project Section");

        // Deadline Components
        DatePicker deadlineDatePicker = new DatePicker();
        deadlineDatePicker.setPromptText("dd/MM");
        deadlineDatePicker.setMaxWidth(Double.MAX_VALUE);
        deadlineDatePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        });

        ComboBox<String> hourBox = new ComboBox<>();
        hourBox.setMaxWidth(Double.MAX_VALUE);
        ComboBox<String> minuteBox = new ComboBox<>();
        minuteBox.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < 24; i++) {
            hourBox.getItems().add(String.format("%02d", i));
        }
        for (int i = 0; i < 60; i += 5) {
            minuteBox.getItems().add(String.format("%02d", i));
        }

        hourBox.setPromptText("HH");
        minuteBox.setPromptText("MM");

        HBox timeBox = new HBox(10, hourBox, minuteBox);
        timeBox.setMaxWidth(Double.MAX_VALUE);

        HBox deadlineBox = new HBox(10, deadlineDatePicker, timeBox);
        deadlineBox.setMaxWidth(Double.MAX_VALUE);

        // Status ComboBox
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.setMaxWidth(Double.MAX_VALUE);
        statusComboBox.getItems().addAll("Not Started", "Partially Completed", "Almost Done");
        statusComboBox.setPromptText("Select status");

        // Form Buttons
        Button cancelButton = new Button("Cancel");
        Button doneButton = new Button("Done");
        HBox buttonBox = new HBox(20, cancelButton, doneButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setMaxWidth(Double.MAX_VALUE);

        // Add components to left panel
        leftPanel.getChildren().addAll(
            heading,
            createFormLabel("Title:"), titleField,
            createFormLabel("Description:"), descriptionField,
            createFormLabel("Deadline:"), deadlineBox,
            createFormLabel("Status:"), statusComboBox,
            buttonBox
        );

        // Task Display Panel - Preview Section
        taskDisplayPanel = new VBox(15);
        taskDisplayPanel.setPadding(new Insets(20));
        taskDisplayPanel.setStyle("-fx-background-color: #f0f8ff;");
        taskDisplayPanel.setMaxWidth(Double.MAX_VALUE);

        // Display Header
        Label displayHeader = new Label("Task Preview");
        displayHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        displayHeader.setMaxWidth(Double.MAX_VALUE);
        displayHeader.setAlignment(Pos.CENTER);

        // Delete Button
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setDisable(true);
        deleteButton.setMaxWidth(Double.MAX_VALUE);

        // Task List Container
        VBox taskListContainer = new VBox(10);
        taskListContainer.setMaxWidth(Double.MAX_VALUE);
        ScrollPane scrollPane = new ScrollPane(taskListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        taskDisplayPanel.getChildren().addAll(displayHeader, deleteButton, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Delete Button Action
        deleteButton.setOnAction(e -> handleDeleteTasks(taskListContainer));

        // Done Button Action
        doneButton.setOnAction(e -> handleCreateTask(
            titleField, descriptionField, deadlineDatePicker,
            hourBox, minuteBox, statusComboBox, taskListContainer,
            deleteButton
        ));

        // Cancel Button Action
        cancelButton.setOnAction(e -> {
            titleField.clear();
            descriptionField.clear();
            deadlineDatePicker.setValue(null);
            hourBox.getSelectionModel().clearSelection();
            minuteBox.getSelectionModel().clearSelection();
            statusComboBox.getSelectionModel().clearSelection();
        });

        // Configure the root layout
        HBox contentBox = new HBox(leftPanel, taskDisplayPanel);
        contentBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        // Set proper growth policies
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(taskDisplayPanel, Priority.ALWAYS);
        
        // Set width constraints
        leftPanel.setMinWidth(350);
        taskDisplayPanel.setMinWidth(350);
        
        contentBox.setSpacing(0);
        rootPane.setCenter(contentBox);
        
        return rootPane;
    }

    private TextField createFormField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return label;
    }

    private void handleDeleteTasks(VBox taskListContainer) {
        List<String> titlesToDelete = new ArrayList<>();
        List<CheckBox> toRemove = new ArrayList<>();

        for (CheckBox cb : taskCheckBoxes) {
            if (cb.isSelected()) {
                titlesToDelete.add(cb.getText()); 
                VBox taskBox = (VBox) cb.getParent();
                taskListContainer.getChildren().remove(taskBox);
                toRemove.add(cb);
            }
        }
        
        taskCheckBoxes.removeAll(toRemove);
        
        if (!titlesToDelete.isEmpty()) {
            new DatabaseOperations().deleteTasks(SessionManager.getLoggedInStudentId(), titlesToDelete);
        }
    }

    private void handleCreateTask(
        TextField titleField, TextField descriptionField, DatePicker deadlineDatePicker,
        ComboBox<String> hourBox, ComboBox<String> minuteBox, ComboBox<String> statusComboBox,
        VBox taskListContainer, Button deleteButton
    ) {
        String title = titleField.getText();
        String description = descriptionField.getText();
        LocalDate date = deadlineDatePicker.getValue();
        String hour = hourBox.getValue();
        String minute = minuteBox.getValue();
        String time = (hour != null && minute != null) ? hour + ":" + minute : "";
        String status = statusComboBox.getValue();
        String studentId = SessionManager.getLoggedInStudentId(); 

        if (title.isEmpty() || date == null || time.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Title, Date, and Time are required!");
            alert.show();
            return;
        }

        try {
            LocalDateTime deadline = LocalDateTime.of(date, java.time.LocalTime.parse(time));
            Timestamp timestamp = Timestamp.valueOf(deadline);

            DatabaseOperations db = new DatabaseOperations();
            int taskId = db.insertTask(studentId, title, description, timestamp, status);

            if (taskId == -1) {
                Alert error = new Alert(Alert.AlertType.ERROR, "Failed to save task to database.");
                error.show();
                return;
            }

            // Create task display box
            CheckBox selectBox = new CheckBox();
            VBox taskBox = new VBox(
                new HBox(10, selectBox, new Label("Title: " + title)),
                new Label("Deadline: " + deadline.toString()),
                new Label("Status: " + status)
            );
            taskBox.setPadding(new Insets(10));
            taskBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ccc; -fx-background-radius: 10; -fx-border-radius: 10;");
            taskBox.setMaxWidth(Double.MAX_VALUE);

            selectBox.setOnAction(ev -> {
                if (selectBox.isSelected()) {
                    taskBox.setStyle("-fx-background-color: #d3d3d3; -fx-border-color: #ccc; -fx-background-radius: 10; -fx-border-radius: 10;");
                    deleteButton.setDisable(false);
                } else {
                    taskBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ccc; -fx-background-radius: 10; -fx-border-radius: 10;");
                    deleteButton.setDisable(taskCheckBoxes.stream().noneMatch(CheckBox::isSelected));
                }
            });

            taskCheckBoxes.add(selectBox);
            taskListContainer.getChildren().add(taskBox);

            // Clear fields
            titleField.clear();
            descriptionField.clear();
            deadlineDatePicker.setValue(null);
            hourBox.getSelectionModel().clearSelection();
            minuteBox.getSelectionModel().clearSelection();
            statusComboBox.getSelectionModel().clearSelection();

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR, "Invalid time format. Use HH:mm.");
            error.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
/*package clientside;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ComboBox;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.util.StringConverter;
import serverside.DatabaseOperations;
import serverside.SessionManager;

public class Task {

    private VBox taskDisplayPanel;
    private List<CheckBox> taskCheckBoxes = new ArrayList<>();
    private VBox leftPanel;

    public Pane buildContent(Region parent) {
        
        leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setMaxWidth(Double.MAX_VALUE);
        leftPanel.setStyle("-fx-background-color: linear-gradient(to right, #c8f0f5, #e8f5c8);");

        Label heading = new Label("New Task");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField titleField = new TextField();
        titleField.setPromptText("e.g IS Project Chapter 4");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("e.g Assigned Project Section");

        DatePicker deadlineDatePicker = new DatePicker();
        deadlineDatePicker.setPromptText("dd/MM");
        deadlineDatePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        });

        ComboBox<String> hourBox = new ComboBox<>();
        ComboBox<String> minuteBox = new ComboBox<>();

        for (int i = 0; i < 24; i++) {
            hourBox.getItems().add(String.format("%02d", i));
        }
        for (int i = 0; i < 60; i += 5) { // 5-minute intervals
            minuteBox.getItems().add(String.format("%02d", i));
        }

        hourBox.setPromptText("HH");
        minuteBox.setPromptText("MM");


        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Not Started", "Partially Completed", "Almost Done");
        statusComboBox.setPromptText("Select status");


        HBox deadlineBox = new HBox(10, deadlineDatePicker, hourBox, minuteBox);


        Button cancelButton = new Button("Cancel");
        Button doneButton = new Button("Done");
        HBox buttonBox = new HBox(20, cancelButton, doneButton);
        buttonBox.setAlignment(Pos.CENTER);

        leftPanel.getChildren().addAll(
            heading,
            new Label("Title:"), titleField,
            new Label("Description:"), descriptionField,
            new Label("Deadline:"), deadlineBox,
            new Label("Status:"), statusComboBox,
            buttonBox
        );

        taskDisplayPanel = new VBox(40);
        taskDisplayPanel.setPadding(new Insets(20));
        taskDisplayPanel.setMaxWidth(Double.MAX_VALUE);
        taskDisplayPanel.setStyle("-fx-background-color: #f0f8ff;");

        Label displayHeader = new Label("Task Preview");
        displayHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setDisable(true);

        deleteButton.setOnAction(e -> {
            List<String> titlesToDelete = new ArrayList<>();
            List<CheckBox> toRemove = new ArrayList<>();

            for (CheckBox cb : taskCheckBoxes) {
                if (cb.isSelected()) {
                    titlesToDelete.add(cb.getText()); 
                    VBox taskBox = (VBox) cb.getParent();
                    taskDisplayPanel.getChildren().remove(taskBox);
                    toRemove.add(cb);
                }
            }

            
            taskCheckBoxes.removeAll(toRemove);

            
            if (!titlesToDelete.isEmpty()) {
                new DatabaseOperations().deleteTasks(SessionManager.getLoggedInStudentId(), titlesToDelete);
            }

            deleteButton.setDisable(true);
        });


        taskDisplayPanel.getChildren().addAll(displayHeader, deleteButton);

        doneButton.setOnAction(e -> {
            String title = titleField.getText();
            String description = descriptionField.getText();
            LocalDate date = deadlineDatePicker.getValue();
            String hour = hourBox.getValue();
            String minute = minuteBox.getValue();
            String time = (hour != null && minute != null) ? hour + ":" + minute : "";
            String status = statusComboBox.getValue();
            String studentId = SessionManager.getLoggedInStudentId(); 

            if (title.isEmpty() || date == null || time.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Title, Date, and Time are required!");
                alert.show();
                return;
            }

            try {
                LocalDateTime deadline = LocalDateTime.of(date, java.time.LocalTime.parse(time));
                Timestamp timestamp = Timestamp.valueOf(deadline);

                DatabaseOperations db = new DatabaseOperations();
                int taskId = db.insertTask(studentId, title, description, timestamp, status);

                if (taskId == -1) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Failed to save task to database.");
                    error.show();
                    return;
                }

                CheckBox selectBox = new CheckBox();
                VBox taskBox = new VBox(
                    new HBox(10, selectBox, new Label("Title: " + title)),
                    new Label("Deadline: " + deadline.toString()),
                    new Label("Status: " + status)
                );
                taskBox.setPadding(new Insets(10));
                taskBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ccc; -fx-background-radius: 10; -fx-border-radius: 10;");

                selectBox.setOnAction(ev -> {
                    if (selectBox.isSelected()) {
                        taskBox.setStyle("-fx-background-color: #d3d3d3; -fx-border-color: #ccc; -fx-background-radius: 10; -fx-border-radius: 10;");
                        deleteButton.setDisable(false);
                    } else {
                        taskBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #ccc; -fx-background-radius: 10; -fx-border-radius: 10;");
                        deleteButton.setDisable(taskCheckBoxes.stream().noneMatch(CheckBox::isSelected));
                    }
                });

                taskCheckBoxes.add(selectBox);
                taskDisplayPanel.getChildren().add(taskBox);

                titleField.clear();
                descriptionField.clear();
                deadlineDatePicker.setValue(null);
                hourBox.getSelectionModel().clearSelection();
                minuteBox.getSelectionModel().clearSelection();

                statusComboBox.getSelectionModel().clearSelection();


            } catch (Exception ex) {
                ex.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR, "Invalid time format. Use HH:mm.");
                error.show();
            }
        });

        cancelButton.setOnAction(e -> {
            titleField.clear();
            descriptionField.clear();
            deadlineDatePicker.setValue(null);
            hourBox.getSelectionModel().clearSelection();
            minuteBox.getSelectionModel().clearSelection();

            statusComboBox.getSelectionModel().clearSelection();

        });

        HBox rootHBox = new HBox(leftPanel, taskDisplayPanel);
        rootHBox.setMaxWidth(Double.MAX_VALUE);
        rootHBox.setFillHeight(true);

        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(taskDisplayPanel, Priority.ALWAYS);

        leftPanel.setMaxWidth(Double.MAX_VALUE);
        taskDisplayPanel.setMaxWidth(Double.MAX_VALUE);
        leftPanel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        taskDisplayPanel.setPrefWidth(Region.USE_COMPUTED_SIZE);


        rootHBox.setSpacing(0);
        rootHBox.setPadding(Insets.EMPTY);

        return rootHBox;       
        
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}

*/



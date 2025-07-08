
package clientside;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import serverside.DatabaseOperations;
import serverside.SessionManager;
import java.sql.Timestamp;
import java.util.*;

public class AllTasks {

    private VBox taskListPanel;
    private VBox priorityPanel;
    private List<CheckBox> taskCheckBoxes = new ArrayList<>();

    public Pane buildContent() {
        HBox mainLayout = new HBox();
        mainLayout.setSpacing(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: linear-gradient(to right, #B3E5FC, #FFF9C4);");

        // Task List Panel
        VBox taskPanel = new VBox(15);
        taskPanel.setPrefWidth(600);
        taskPanel.setPadding(new Insets(20));
        taskPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");

        Label taskListLabel = new Label("Task List");
        taskListLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Button selectCompletedBtn = new Button("Select Completed");
        selectCompletedBtn.setOnAction(e -> handleMarkCompleted());

        taskListPanel = new VBox(10);

        taskPanel.getChildren().addAll(taskListLabel, selectCompletedBtn, taskListPanel);

        // Priority Panel
        VBox priorityBox = new VBox(10);
        priorityBox.setPadding(new Insets(15));
        priorityBox.setPrefSize(300, 100);
        priorityBox.setStyle("-fx-background-color: #eeeeee; -fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label priorityLabel = new Label("Task to Prioritise");
        priorityLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label priorityTaskLabel = new Label();
        priorityTaskLabel.setWrapText(true);

        priorityBox.getChildren().addAll(priorityLabel, priorityTaskLabel);

        mainLayout.getChildren().addAll(taskPanel, priorityBox);

        loadTasks(priorityTaskLabel);

        return mainLayout;
    }

    private void loadTasks(Label priorityTaskLabel) {
        String studentId = SessionManager.getLoggedInStudentId();
        DatabaseOperations db = new DatabaseOperations();
        List<Map<String, Object>> tasks = db.getAllTasks(studentId);

        PriorityQueue<Map<String, Object>> taskQueue = new PriorityQueue<>(Comparator.comparing(task -> ((Timestamp) task.get("deadline"))));

        for (Map<String, Object> task : tasks) {
            String title = (String) task.get("title");
            Timestamp deadline = (Timestamp) task.get("deadline");
            String status = (String) task.get("status");

            if (!status.equalsIgnoreCase("Completed")) {
                CheckBox taskCheckBox = new CheckBox(title + " - " + deadline.toLocalDateTime().toLocalDate());
                taskCheckBoxes.add(taskCheckBox);
                taskListPanel.getChildren().add(taskCheckBox);

                taskQueue.offer(task);
            }
        }

        if (!taskQueue.isEmpty()) {
            Map<String, Object> topPriority = taskQueue.poll();
            String title = (String) topPriority.get("title");
            Timestamp deadline = (Timestamp) topPriority.get("deadline");
            String status = (String) topPriority.get("status");
            priorityTaskLabel.setText(title + "\nDue: " + deadline.toLocalDateTime().toLocalDate() + "\nStatus: " + status);
        } else {
            priorityTaskLabel.setText("No pending tasks");
        }
    }

    private void handleMarkCompleted() {
        List<String> completedTitles = new ArrayList<>();
        List<CheckBox> toRemove = new ArrayList<>();

        for (CheckBox cb : taskCheckBoxes) {
            if (cb.isSelected()) {
               
                String fullText = cb.getText();
                String title = fullText.split(" - ")[0];
                completedTitles.add(title);
                toRemove.add(cb);
                
            }
        }

        if (!completedTitles.isEmpty()) {
            new DatabaseOperations().markTasksAsCompleted(SessionManager.getLoggedInStudentId(), completedTitles);
            
            for (CheckBox cb : toRemove) {
                taskListPanel.getChildren().remove(cb.getParent());
                }

            taskCheckBoxes.removeAll(toRemove);
            
        }
    }

}


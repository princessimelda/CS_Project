package clientside;

import java.io.File;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import serverside.DatabaseOperations;
import serverside.SessionManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Circle;
import java.util.function.Consumer;



public class Dashboard extends VBox {

    private String firstName;
    private String surname;
    private Label currentTaskLabel;
    private LocalDateTime currentTaskDeadline;
    private HBox mainContent;
    private BorderPane root;

    public Dashboard(String firstName, String surname) {
        this.firstName = firstName;
        this.surname = surname;
        this.setSpacing(0);
        this.setPadding(Insets.EMPTY);
        this.setStyle("-fx-background-color:transparent;");
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.setFillWidth(true);
        buildDashboard();
    }

    public Dashboard() {
        String[] nameParts = new DatabaseOperations().getStudentNameById(SessionManager.getLoggedInStudentId());
        this.firstName = nameParts != null ? nameParts[0] : "Student";
        this.surname = nameParts != null ? nameParts[1] : "";
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.setFillWidth(true);
        buildDashboard();
    }

    private void buildDashboard() {
        mainContent = new HBox(30);
        mainContent.setPadding(new Insets(20,10,20,20));
        mainContent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        mainContent.getChildren().addAll(createMainPanel(), createPrioritySidePanel());

        root = new BorderPane();
        root.setLeft(createSidebar());
        root.setCenter(mainContent);
        root.setStyle("-fx-background-color: linear-gradient(to right, #B3E5FC, #FFF9C4);");
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.getChildren().clear();
        this.getChildren().add(root);
        VBox.setVgrow(root, Priority.ALWAYS);
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #B3E5FC;");
        sidebar.setPrefWidth(200);
        sidebar.setMaxHeight(Double.MAX_VALUE);

        // Get the logged-in student's profile image path
        DatabaseOperations db = new DatabaseOperations();
        String studentId = SessionManager.getLoggedInStudentId();
        String picPath = db.getProfilePicturePath(studentId);

        ImageView userIcon;
        if (picPath != null && !picPath.trim().isEmpty()) {
            File imageFile = new File(picPath);
            if (imageFile.exists()) {
                userIcon = new ImageView(new Image(imageFile.toURI().toString()));
            } else {
                userIcon = new ImageView(new Image("/Resources/pfpplaceholder.jpg")); // fallback
            }
        } else {
            userIcon = new ImageView(new Image("/Resources/pfpplaceholder.jpg")); // fallback
        }

        userIcon.setFitHeight(40);
        userIcon.setFitWidth(40);
        userIcon.setPreserveRatio(false);
        Circle clip = new Circle(20, 20, 20); 
        clip.centerXProperty().bind(userIcon.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(userIcon.fitHeightProperty().divide(2));
        userIcon.setClip(clip);

        Label userName = new Label(firstName);
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        HBox profileBox = new HBox(10);
        profileBox.setAlignment(Pos.CENTER_LEFT);
        profileBox.getChildren().addAll(userIcon,userName);
        
        Label dashboard = new Label("Dashboard");
        dashboard.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        Label myTasks = new Label("My Tasks");
        myTasks.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        Label myCalendar = new Label("My Calendar");
        myCalendar.setFont(Font.font("Arial", FontWeight.NORMAL, 14));


        Label account = new Label("Account");
        account.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        ContextMenu taskMenu = new ContextMenu();
        MenuItem newTaskItem = new MenuItem("New Task");
        MenuItem allTasksItem = new MenuItem("All Tasks");
        taskMenu.getItems().addAll(newTaskItem, allTasksItem);

        List<Label> menuLabels = Arrays.asList(dashboard, myTasks, myCalendar, account);

        Consumer<Label> setActive = (Label activeLabel) ->  {
            for (Label label : menuLabels) {
                label.setStyle("-fx-font-size: 14px; -fx-font-weight: normal;");
            }
            activeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        };

        dashboard.setOnMouseClicked(e -> {
            setActive.accept(dashboard);
            mainContent.getChildren().clear();
            mainContent.getChildren().addAll(createMainPanel(), createPrioritySidePanel());
        });

        myTasks.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                setActive.accept(myTasks);
                taskMenu.show(myTasks, e.getScreenX(), e.getScreenY());
            }
        });

        newTaskItem.setOnAction(e -> {
            mainContent.getChildren().clear();
            Task taskApp = new Task();
            try {
                Pane taskView = taskApp.buildContent(mainContent);
                mainContent.getChildren().add(taskView);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        allTasksItem.setOnAction(e -> {
            mainContent.getChildren().clear();
            AllTasks tasks = new AllTasks();
            try {
                Pane taskViews = tasks.buildContent();
                mainContent.getChildren().add(taskViews);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        myCalendar.setOnMouseClicked(e -> {
            setActive.accept(myCalendar);
            mainContent.getChildren().clear();
            Calendar calendarApp = new Calendar();
            try {
                Pane calendarView = calendarApp.buildContent();
                mainContent.getChildren().add(calendarView);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        account.setOnMouseClicked(e -> {
            setActive.accept(account);
            mainContent.getChildren().clear();
            Account accountSection = new Account();
            try{
                Pane accountView = accountSection.buildContent();
                mainContent.getChildren().add(accountView);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });
        
        sidebar.getChildren().addAll(profileBox, dashboard, myTasks, myCalendar);
        VBox.setVgrow(account, Priority.ALWAYS);
        sidebar.getChildren().add(account);
        return sidebar;
    }

    private VBox createMainPanel() {
        VBox leftMain = new VBox(20);
        leftMain.setAlignment(Pos.TOP_CENTER);
        leftMain.setPadding(new Insets(20));
        leftMain.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(leftMain, Priority.ALWAYS);
        HBox.setHgrow(leftMain, Priority.ALWAYS);

        HBox welcomeSection = new HBox(10);
        welcomeSection.setAlignment(Pos.CENTER);

        ImageView brainImage = new ImageView(new Image("/Resources/landingpagebrain.png"));
        brainImage.setFitHeight(200);
        brainImage.setFitWidth(200);

        Label welcomeLabel = new Label("WELCOME BACK! ");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        welcomeSection.getChildren().addAll(brainImage, welcomeLabel);

        Canvas timerCanvas = new Canvas(150, 150);
        GraphicsContext gc = timerCanvas.getGraphicsContext2D();

        currentTaskLabel = new Label();
        loadPriorityTask();
        currentTaskLabel.setWrapText(true);
        currentTaskLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        VBox timerWrapper = new VBox(10, timerCanvas, currentTaskLabel);
        timerWrapper.setAlignment(Pos.CENTER);

        Timeline timer = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> {
            GraphicsContext context = timerCanvas.getGraphicsContext2D();
            updateTimer(context);
        })
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

        BarChart<String, Number> chart = createCompletionChart();
        leftMain.getChildren().addAll(welcomeSection, timerWrapper, chart);
        return leftMain;
    }

    private VBox createPrioritySidePanel() {
        VBox priorityPanel = new VBox(10);
        priorityPanel.setPadding(new Insets(20));
        priorityPanel.setStyle("-fx-background-color: #C8E6C9; -fx-background-radius: 10;");
        priorityPanel.setPrefWidth(300);
        priorityPanel.setMaxHeight(Double.MAX_VALUE);

        Label header = new Label("Your Tasks");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        VBox tasksContainer = new VBox(10);

        List<Map<String, Object>> tasks = new DatabaseOperations().getAllTasks(SessionManager.getLoggedInStudentId());
        tasks.sort((a, b) -> ((java.sql.Timestamp) a.get("deadline")).compareTo((java.sql.Timestamp) b.get("deadline")));

        for (Map<String, Object> task : tasks) {
            if (!"Completed".equalsIgnoreCase((String) task.get("status"))) {
                String title = (String) task.get("title");
                LocalDateTime deadline = ((java.sql.Timestamp) task.get("deadline")).toLocalDateTime();

                Label titleLabel = new Label(title);
                titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));  
                titleLabel.setWrapText(true);

                Label deadlineLabel = new Label("Due: " + deadline.toLocalDate());
                deadlineLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));  
                deadlineLabel.setWrapText(true);

               
                VBox taskBox = new VBox(5, titleLabel, deadlineLabel);
                taskBox.setPadding(new Insets(8));
                taskBox.setStyle("-fx-background-color: #EEEEEE; -fx-background-radius: 5;");

                tasksContainer.getChildren().add(taskBox);
            }
        }

        priorityPanel.getChildren().addAll(header, tasksContainer);
        return priorityPanel;
    }

    private void loadPriorityTask() {
        DatabaseOperations db = new DatabaseOperations();
        Map<String, Object> task = db.getPriorityTask(SessionManager.getLoggedInStudentId());
        if (task != null) {
            currentTaskDeadline = ((java.sql.Timestamp) task.get("deadline")).toLocalDateTime();
            String title = (String) task.get("title");
            currentTaskLabel.setText(title);
        } else {
            currentTaskLabel.setText("No task to prioritise");
        }
    }

    private void updateTimer(GraphicsContext gc) {
        gc.clearRect(0, 0, 150, 150);
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(4);
        gc.strokeOval(10, 10, 130, 130);

    if (currentTaskDeadline != null) {
        java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), currentTaskDeadline);

        long remainingMinutes = (long)duration.toMinutes();

        double angle = (double) remainingMinutes / 240 * 360;

        Color color = remainingMinutes <= 60 ? Color.RED : Color.LIMEGREEN;
        gc.setFill(color);
        gc.fillArc(10, 10, 130, 130, 90, -angle, javafx.scene.shape.ArcType.ROUND);

        // Format display
        String timeText;
        if (remainingMinutes > 60) {
            long hours = remainingMinutes / 60;
            long minutes = remainingMinutes % 60;
            timeText = hours + " hr" + (hours > 1 ? "s" : "");
            if (minutes > 0) {
                timeText += " " + minutes + " min";
            }
        } else {
            timeText = remainingMinutes + " min";
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.fillText(timeText, 30, 80);
    }
     else {
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
            gc.fillText("No task", 50, 80);
        }
    }
    
    private BarChart<String, Number> createCompletionChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Day");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Tasks Completed");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Tasks Completed Over the Last 7 Days");
        barChart.setPrefSize(700, 400);
        barChart.setLegendVisible(false);
        /*barChart.setMaxHeight(350);*/
        barChart.setCategoryGap(10);
        barChart.setBarGap(5);


        int studentId = Integer.parseInt(SessionManager.getLoggedInStudentId());
        Map<String, Integer> data = new DatabaseOperations().getCompletedTaskCountsLast7Days(SessionManager.getLoggedInStudentId());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);

        return barChart;
    }


}














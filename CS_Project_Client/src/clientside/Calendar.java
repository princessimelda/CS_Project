package clientside;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;



import serverside.DatabaseOperations;

public class Calendar {

    private final Map<Integer, List<String>> tasksByDay = new HashMap<>();
    private final GridPane calendarGrid = new GridPane();
    private YearMonth currentMonth = YearMonth.now(); 
    private final String studentId = serverside.SessionManager.getLoggedInStudentId();
    private BorderPane root; 
    private Text monthYear;


    public Pane buildContent() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to right, #B3E5FC, #FFF9C4);");

        // Header
        HBox header = new HBox();
        
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10));
        monthYear = new Text(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear());
        monthYear.setFont(Font.font("Arial", 36));
        monthYear.setFill(Color.web("#E53935"));
        header.getChildren().add(monthYear);
        root.setTop(header);
        Button prevButton = new Button("<");
        Button nextButton = new Button(">");

            prevButton.setOnAction(e -> {
                currentMonth = currentMonth.minusMonths(1);
                refreshCalendar();
            });

            nextButton.setOnAction(e -> {
                currentMonth = currentMonth.plusMonths(1);
                refreshCalendar();
            });

        header.getChildren().clear();
        header.getChildren().addAll(prevButton, monthYear, nextButton);

        // Load tasks from DB
        loadTasksFromDatabase();

        // Calendar Grid
        calendarGrid.setPadding(new Insets(20));
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        generateCalendar();
        root.setCenter(calendarGrid);

        // Download PDF Button
        Button downloadBtn = new Button("Download PDF");
        downloadBtn.setOnAction(e -> exportCalendarAsPdf());
        HBox bottom = new HBox(downloadBtn);
        bottom.setPadding(new Insets(15));
        bottom.setAlignment(Pos.CENTER);
        root.setBottom(bottom);

        return root;
    }

    private void loadTasksFromDatabase() {
        DatabaseOperations dbOps = new DatabaseOperations();
        Map<Integer, List<String>> taskList = dbOps.getTasksForMonth(currentMonth.getMonthValue(), currentMonth.getYear(), studentId);

        for (Map.Entry<Integer, List<String>> entry : taskList.entrySet()) {
            int dayOfMonth = entry.getKey();
            List<String> titles = entry.getValue();
            for (String title : titles) {
                addTask(dayOfMonth, title);
            }
        }
    }
    
    private void moveTaskToDay(String title, int newDay) {
        // Remove from current day
        for (Map.Entry<Integer, List<String>> entry : tasksByDay.entrySet()) {
            if (entry.getValue().remove(title)) {
                break;
            }
        }

        // Add to new day
        tasksByDay.computeIfAbsent(newDay, k -> new ArrayList<>()).add(title);

        // Update in database
        DatabaseOperations dbOps = new DatabaseOperations();
        LocalDate localDate = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue(), newDay);
        Timestamp newDeadline = Timestamp.valueOf(localDate.atStartOfDay());
        String cleanTitle = title.split("\\|")[0].trim(); 
        dbOps.updateTaskDay(cleanTitle, newDeadline, studentId);


    }


    private void addTask(int day, String title) {
        tasksByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(title);
    }

    private void updateCalendarHeader() {
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = currentMonth.getYear();
        monthYear.setText(monthName + " " + year);
    }

    
    private void refreshCalendar() {
        tasksByDay.clear();                        // clear current tasks from memory
        calendarGrid.getChildren().clear();        // clear calendar grid UI
        updateCalendarHeader();                    // update the displayed month/year
        loadTasksFromDatabase();                   // reload tasks from DB for this month/year
        generateCalendar();                        // re-render the grid UI with new data
        root.setCenter(calendarGrid);              // ensure the grid is set to center in layout
    }

    private void generateCalendar() {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        for (int i = 0; i < days.length; i++) {
            Text dayName = new Text(days[i]);
            dayName.setFont(Font.font("Arial", 14));
            dayName.setFill(Color.WHITE);
            StackPane dayHeader = new StackPane();
            dayHeader.setPrefHeight(30);
            dayHeader.setStyle("-fx-background-color: #263238;");
            dayHeader.getChildren().add(dayName);
            calendarGrid.add(dayHeader, i, 0);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startDay = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = currentMonth.lengthOfMonth();

        int row = 1;
        int col = startDay;

        for (int day = 1; day <= daysInMonth; day++) {
            VBox dayCell = new VBox(2);
            dayCell.setPadding(new Insets(5));
            dayCell.setPrefSize(100, 80);
            dayCell.setStyle("-fx-border-color: gray; -fx-background-color: white;");

            Text dateText = new Text(String.valueOf(day));
            dateText.setFont(Font.font("Arial", 14));
            if (col == 0 || col == 6) {
                dateText.setFill(Color.RED);
            }
            dayCell.getChildren().add(dateText);

            List<String> dayTasks = tasksByDay.getOrDefault(day, new ArrayList<>());
            for (String task : dayTasks) {
                HBox taskBox = new HBox(5);
                taskBox.setAlignment(Pos.CENTER_LEFT);
                Circle bullet = new Circle(5);

                String lower = task.toLowerCase();
                if (lower.contains("not started")) {
                    bullet.setFill(Color.RED);
                } else if (lower.contains("partially")) {
                    bullet.setFill(Color.ORANGE);
                } else if (lower.contains("almost done")) {
                    bullet.setFill(Color.GREEN);
                } else {
                    bullet.setFill(Color.BLUE);
                }

                Text taskText = new Text(task);
                taskText.setOnDragDetected(e -> {
                    Dragboard db = taskText.startDragAndDrop (TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(task);
                    db.setContent(content);
                    e.consume();    
                });
                taskText.setWrappingWidth(80);
                taskText.setFont(Font.font("Arial", 12));
                taskBox.getChildren().addAll(bullet, taskText);
                dayCell.getChildren().add(taskBox);
                                         
            }

            final int targetDay = day;
            dayCell.setOnDragOver(e -> {
                    if (e.getGestureSource() != dayCell && e.getDragboard().hasString()) {
                        e.acceptTransferModes(TransferMode.MOVE);
                    }
                    e.consume();
                });

                dayCell.setOnDragDropped(e -> {
                    Dragboard db = e.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String taskTitle = db.getString();
                        moveTaskToDay(taskTitle, targetDay); 
                        refreshCalendar();
                        success = true;
                    }
                    e.setDropCompleted(success);
                    e.consume();
                });
                
            calendarGrid.add(dayCell, col, row);
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }
    
    private void exportCalendarAsPdf() {
    try {
        // Take snapshot
        WritableImage snapshot = calendarGrid.snapshot(new SnapshotParameters(), null);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(snapshot, null);
        File imageFile = new File(System.getProperty("user.home") + "/Desktop/calendar.png");
        ImageIO.write(bufferedImage, "png", imageFile);

        // Embed in PDF
        Document document = new Document();
        String pdfPath = System.getProperty("user.home") + "/Desktop/StudentCalendar.pdf";
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
        document.open();
        Image img = Image.getInstance(imageFile.getAbsolutePath());
        img.scaleToFit(550, 700); // Resize image if needed
        document.add(img);
        document.close();

        // Optional: Open the file automatically
        java.awt.Desktop.getDesktop().open(new File(pdfPath));
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    
    
    
}

package serverside;

import NotificationSystem.EmailSender;
import NotificationSystem.Event;
import NotificationSystem.Notification;
import NotificationSystem.PublisherService;
import NotificationSystem.ReminderPreferences;
import org.mindrot.bcrypt.BCrypt;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.LinkedHashMap;



public class DatabaseOperations {
    private final DatabaseConnection database_connect = new DatabaseConnection();
    
    public DatabaseOperations(){
        createTableIfNotExists();
        addLoginTrackingColumns();
    }
    
    private void createTableIfNotExists(){
        String query = "CREATE TABLE IF NOT EXISTS students ("+ 
                        " student_id VARCHAR(255) PRIMARY KEY, " +
                        "student_first_name VARCHAR(255) NOT NULL, " +
                        "student_surname VARCHAR(255) NOT NULL, " +
                        "student_email VARCHAR(50) NOT NULL, " +
                        "password_hash VARCHAR(200) NOT NULL" +
                        ")";
        
        try(Connection conn = database_connect.getConnection();
                Statement statement = conn.createStatement()){
            
            if(conn==null){
                System.out.println("Connection not established for Students table.");
                return;
            }
            
            statement.executeUpdate(query);
            System.out.println("Students table is ready.");
        } catch(SQLException e){
            System.err.println("Failed to create Students table: "+ e.getMessage());
        }                 
    }
    
    
    private void addLoginTrackingColumns(){
        String alterSQL = """
        ALTER TABLE students 
        ADD COLUMN IF NOT EXISTS login_attempts INT DEFAULT 0,
        ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;
    """;
        
        try (Connection conn=database_connect.getConnection();
                Statement statement = conn.createStatement()){
                    statement.executeUpdate(alterSQL);
                } catch(SQLException e){
                        System.err.println("Could not update Students table: "+e.getMessage());
                        }
    }
        
    public String registerStudent(String studentID, String studentFname, String studentSurname, String studentEmail, String password, String confirmPassword){
        
        if (!password.equals(confirmPassword)) {
        return "Passwords do not match.";
         }
        
        String query = "INSERT INTO students (student_id, student_first_name, student_surname, student_email, password) VALUES (?, ?, ?, ?, ?)";
        String hashed_password = BCrypt.hashpw(password, BCrypt.gensalt());
        
        try (Connection conn = database_connect.getConnection();
             PreparedStatement prepared_statement = conn.prepareStatement(query)) {

            if (conn == null) return "Connection not established.";

            prepared_statement.setString(1, studentID);
            prepared_statement.setString(2, studentFname);
            prepared_statement.setString(3, studentSurname);
            prepared_statement.setString(4, studentEmail);
            prepared_statement.setString(5, hashed_password);

            int rowsAffected = prepared_statement.executeUpdate();
            return (rowsAffected > 0) ? "Student registered successfully." : "Student registration failed.";

        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                return "Student ID already exists.";
            }
            return "Registration failed: " + e.getMessage();
        }
    }
    
    public String loginStudent(String studentID, String password){
        String fetchQuery = "SELECT password, login_attempts FROM students WHERE student_id = ?";
        String updateSuccessQuery = "UPDATE students SET login_attempts = 0, last_login = NOW() WHERE student_id = ?";
        String updateFailQuery = "UPDATE students SET login_attempts = login_attempts + 1 WHERE student_id = ?";

        
        
        try (Connection conn = database_connect.getConnection();
             PreparedStatement fetchPst = conn.prepareStatement(fetchQuery)) {

            if (conn == null) return "Connection not established.";

            fetchPst.setString(1, studentID);
            ResultSet rs = fetchPst.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                int attempts = rs.getInt("login_attempts"); // login attempts
                
                if (BCrypt.checkpw(password, storedHash)) {
                    try (PreparedStatement updateSuccessPst = conn.prepareStatement(updateSuccessQuery)) {
                    updateSuccessPst.setString(1, studentID);
                    updateSuccessPst.executeUpdate();
                    }
                    
                    serverside.SessionManager.setLoggedInStudentId(studentID);
                    return "Login successful.";
                } else {
                    try (PreparedStatement updateFailPst = conn.prepareStatement(updateFailQuery)) {
                    updateFailPst.setString(1, studentID);
                    updateFailPst.executeUpdate();
                    }
                    return "Incorrect password. Attempt " + (attempts + 1);
                }
            } else {
                return "Student ID not found.";
            }

        } catch (SQLException e) {
            return "Login failed: " + e.getMessage();
        }
    }
    
    public String[] getStudentNameById(String studentId) {
    String query = "SELECT student_first_name, student_surname FROM students WHERE student_id = ?";
    try (Connection conn = database_connect.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {
        
        pst.setString(1, studentId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return new String[] {
                rs.getString("student_first_name"),
                rs.getString("student_surname")
            };
        }
    } catch (SQLException e) {
        System.err.println("Error fetching student name: " + e.getMessage());
    }
        return null;
}

    public String getProfilePicturePath(String studentId) {
        String query = "SELECT profile_picture_path FROM students WHERE student_id = ?";
        try (Connection conn = database_connect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("profile_picture_path");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    
    public boolean storeResetToken(String studentId, String token, long expiryTimeMillis) {
    String query = """
        INSERT INTO password_reset_tokens (student_id, token, expiry_time)
        VALUES (?, ?, ?)
        ON CONFLICT (student_id) DO UPDATE 
        SET token = EXCLUDED.token, expiry_time = EXCLUDED.expiry_time
        """;
    
    try (Connection conn = database_connect.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {
        
        pst.setString(1, studentId);
        pst.setString(2, token);
        pst.setTimestamp(3, new Timestamp(expiryTimeMillis));
        
        return pst.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Error storing reset token: " + e.getMessage());
        return false;
     }
    }
    
    public boolean isTokenValid(String token, String studentId) {
    String query = """
        SELECT token, expiry_time 
        FROM password_reset_tokens 
        WHERE student_id = ? AND token = ?
        """;
    
    try (Connection conn = database_connect.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {
        
        pst.setString(1, studentId);
        pst.setString(2, token);
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            Timestamp expiryTime = rs.getTimestamp("expiry_time");
            return expiryTime.after(new Timestamp(System.currentTimeMillis()));
        }
    } catch (SQLException e) {
        System.err.println("Error validating token: " + e.getMessage());
    }
    return false;
}
    
    
    
    public Map<Integer, List<String>> getTasksForMonth(int month, int year, String studentId) {
    Map<Integer, List<String>> tasksByDay = new HashMap<>();

        String query = """
        SELECT title, deadline, status 
        FROM Task 
        WHERE EXTRACT(MONTH FROM deadline) = ? 
          AND EXTRACT(YEAR FROM deadline) = ? 
          AND student_id = ?
    """;

    try (Connection conn = database_connect.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setInt(1, month);
        stmt.setInt(2, year);
        stmt.setString(3, studentId);

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            String title = rs.getString("title");
            String status = rs.getString("status");
            Timestamp deadline = rs.getTimestamp("deadline");

            if (deadline != null) {
                LocalDate date = deadline.toLocalDateTime().toLocalDate();
                int day = date.getDayOfMonth();

                String taggedTitle = title + " | " + status;
                tasksByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(taggedTitle);
            }
        }

    } catch (SQLException e) {
        System.err.println("Error fetching tasks: " + e.getMessage());
    }

    return tasksByDay;

}

    public int insertTask(String studentId, String title, String description, Timestamp deadline, String status) {
    String query = """
        INSERT INTO Task (student_id, title, description, deadline, status)
        VALUES (?, ?, ?, ?, ?) RETURNING task_id
    """;

    try (Connection conn = database_connect.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {

        pst.setString(1, studentId);
        pst.setString(2, title);
        pst.setString(3, description);
        pst.setTimestamp(4, deadline);
        pst.setString(5, status);

        ResultSet rs = pst.executeQuery();
        if(rs.next()) return rs.getInt("task_id");
    } catch (SQLException e) {
        System.err.println("Failed to insert task: " + e.getMessage());
        
    }
    return -1;
    }

    public boolean deleteTasks(String studentId, List<String> titles) {
        String query = "DELETE FROM Task WHERE student_id = ? AND title = ?";

        try (Connection conn = database_connect.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            for (String title : titles) {
                pst.setString(1, studentId);
                pst.setString(2, title);
                pst.addBatch();
            }

            pst.executeBatch();
            return true;

        } catch (SQLException e) {
            System.err.println("Error deleting tasks: " + e.getMessage());
            return false;
        }
    }

    public List<Map<String, Object>> getAllTasks(String studentId) {
    List<Map<String, Object>> tasks = new ArrayList<>();
    String query = "SELECT title, deadline, status FROM Task WHERE student_id = ? ORDER BY deadline ASC";

    try (Connection conn = database_connect.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {

        pst.setString(1, studentId);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Map<String, Object> task = new HashMap<>();
            task.put("title", rs.getString("title"));
            task.put("deadline", rs.getTimestamp("deadline"));
            task.put("status", rs.getString("status"));
            tasks.add(task);
        }

    } catch (SQLException e) {
        System.err.println("Error fetching all tasks: " + e.getMessage());
    }

    return tasks;
}

    public Map<String, Object> getPriorityTask(String studentId) {
    String query = """
        SELECT title, deadline, status
        FROM Task
        WHERE student_id = ?
          AND status IN ('Not Started', 'Almost Done')
        ORDER BY deadline ASC
        LIMIT 1
    """;

    try (Connection conn = database_connect.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {

        pst.setString(1, studentId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            Map<String, Object> task = new HashMap<>();
            task.put("title", rs.getString("title"));
            task.put("deadline", rs.getTimestamp("deadline"));
            task.put("status", rs.getString("status"));
            return task;
        }

    } catch (SQLException e) {
        System.err.println("Error fetching priority task: " + e.getMessage());
    }

    return null; 
}

    
    public boolean markTasksAsCompleted(String studentId, List<String> taskTitles) {
    
    String query = "UPDATE Task SET status = 'Completed', completion_time= NOW() WHERE student_id = ? AND title=? ";

    try (Connection conn = database_connect.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {

        for (String title : taskTitles) {
            pst.setString(1, studentId);
            pst.setString(2, title);
            pst.addBatch();
        }

        pst.executeBatch();
        return true;

    } catch (SQLException e) {
        System.err.println("Error updating task status: " + e.getMessage());
        return false;
    }
}

    
    public void updateTaskDay(String title, Timestamp newDeadline, String studentId) {
   
    String query = "UPDATE Task SET deadline = ? WHERE title = ? AND student_id = ?";

        try (Connection conn = database_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, newDeadline);
            stmt.setString(2, title);
            stmt.setString(3, studentId);

            int rows = stmt.executeUpdate();
            System.out.println("Updated deadline for " + title + " → " + newDeadline + " (" + rows + " rows affected)");

        } catch (SQLException e) {
            e.printStackTrace();
        }

}

    // Update email
        public boolean updateEmail(String studentId, String newEmail) {
            String sql = "UPDATE students SET student_email = ? WHERE student_id = ?";
            try (Connection conn = database_connect.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {

                pst.setString(1, newEmail);
                pst.setString(2, studentId);
                return pst.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("Failed to update email: " + e.getMessage());
                return false;
            }
        }


        public boolean updatePassword(String studentId, String newPassword) {
            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            String sql = "UPDATE students SET password = ? WHERE student_id = ?";
            try (Connection conn = database_connect.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {

                pst.setString(1, hashed);
                pst.setString(2, studentId);
                return pst.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("Password update failed: " + e.getMessage());
                return false;
            }
        }

        
        public boolean updateReminderPreference(String studentId, boolean enabled) {
            String sql = "UPDATE students SET receive_reminders = ? WHERE student_id = ?";
            try (Connection conn = database_connect.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql)) {

                pst.setBoolean(1, enabled);
                pst.setString(2, studentId);
                return pst.executeUpdate() > 0;

            } catch (SQLException e) {
                System.err.println("Reminder preference update failed: " + e.getMessage());
                return false;
            }
        }

        
        public void updateStudentDetails(String studentId, String newEmail, String newPassword, String profilePath, boolean remindersEnabled) {
        String query = """
            UPDATE students
            SET student_email = COALESCE(?, student_email),
                password = COALESCE(?, password),
                profile_picture_path = COALESCE(?, profile_picture_path),
                reminders_enabled = ?
            WHERE student_id = ?
        """;

        try (Connection conn = database_connect.getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setString(1, newEmail != null && !newEmail.isEmpty() ? newEmail : null);
            pst.setString(2, newPassword != null && !newPassword.isEmpty() ? BCrypt.hashpw(newPassword, BCrypt.gensalt()) : null);
            pst.setString(3, profilePath);
            pst.setBoolean(4, remindersEnabled);
            pst.setString(5, studentId);

            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update student details: " + e.getMessage());
        }
    }

        public boolean storeProfilePicture(String studentId, File file) {
            
            System.out.println("Profile picture saved: " + file.getAbsolutePath());
            return true;
        }

        public boolean getReminderSetting(String studentId) {
            String query = "SELECT reminders_enabled FROM students WHERE student_id = ?";
            try (Connection conn = database_connect.getConnection();
                 PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setString(1, studentId);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) return rs.getBoolean("reminders_enabled");
            } catch (SQLException e) {
                System.err.println("Failed to fetch reminder setting: " + e.getMessage());
            }
            return false;
}

        public Map<String, Integer> getCompletedTaskCountsLast7Days(String studentId) {
            Map<String, Integer> counts = new LinkedHashMap<>();
      
            String query = "SELECT DATE(completion_time) as day, COUNT(*) as total FROM Task " +
                           "WHERE student_id = ? AND status = 'Completed' AND completion_time >= current_date - INTERVAL '6 days' " +
                           "GROUP BY day ORDER BY day";

            try (Connection conn = database_connect.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();

                // Initialize all 7 days with 0
                for (int i = 6; i >= 0; i--) {
                    LocalDate date = LocalDate.now().minusDays(i);
                    counts.put(date.toString(), 0);
                }

                while (rs.next()) {
                    String day = rs.getString("day");
                    int count = rs.getInt("total");
                    counts.put(day, count);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return counts;
        }

    public void insertPublisher(PublisherService publisher) throws SQLException {
       String sql = "INSERT INTO PublisherService (publisher_id, service_name) VALUES (?, ?)";
       try (PreparedStatement ps = database_connect.getConnection().prepareStatement(sql)) {
           ps.setString(1, publisher.getPublisherId());
           ps.setString(2, publisher.getServiceName());
           ps.executeUpdate();
       }
   }

   public void insertEvent(Event event) throws SQLException {
       String sql = "INSERT INTO Event (event_id, event_type, timestamp, task_id, publisher_id) VALUES (?, ?, ?, ?, ?)";
       try (PreparedStatement ps = database_connect.getConnection().prepareStatement(sql)) {
           ps.setString(1, event.getEventId());
           ps.setString(2, event.getEventType());
           ps.setTimestamp(3, event.getTimestamp());
           ps.setInt(4, event.getTaskId());
           ps.setString(5, event.getPublisherId());
           ps.executeUpdate();
       }
   }

   public void insertNotification(Notification notification) throws SQLException {
       String sql = "INSERT INTO Notification (notification_id, message, sent_at, student_id, task_id, event_id) VALUES (?, ?, ?, ?, ?, ?)";
       try (PreparedStatement ps = database_connect.getConnection().prepareStatement(sql)) {
           ps.setString(1, notification.getNotificationId());
           ps.setString(2, notification.getMessage());
           ps.setTimestamp(3, notification.getSentAt());
           ps.setString(4, notification.getStudentId());
           ps.setInt(5, notification.getTaskId());
           ps.setString(6, notification.getEventId());
           ps.executeUpdate();
       }
   }      
    
    public void insertReminderPreferences(String studentId, String frequency, int hoursBeforeDeadline, boolean priorityOnly) throws SQLException {
        String sql = "INSERT INTO ReminderPreferences (student_id, frequency, hours_before_deadline, priority_only) VALUES (?, ?, ?, ?) ON CONFLICT (student_id) DO UPDATE SET frequency = EXCLUDED.frequency, hours_before_deadline = EXCLUDED.hours_before_deadline, priority_only = EXCLUDED.priority_only";
        try (PreparedStatement ps = database_connect.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, frequency);
            ps.setInt(3, hoursBeforeDeadline);
            ps.setBoolean(4, priorityOnly);
            ps.executeUpdate();
        }
    }
    
    public void updateReminderPreferences(String studentId, String preferredTime, String frequencyHours, int hoursBeforePriority) {
        String sql = """
            INSERT INTO ReminderPreferences (student_id, general_time, frequency, priority_duration)
            VALUES (?, ?, ?, ?::interval)
            ON CONFLICT (student_id) DO UPDATE SET
                general_time = EXCLUDED.general_time,
                frequency = EXCLUDED.frequency,
                priority_duration = EXCLUDED.priority_duration
        """;

        try (PreparedStatement ps = database_connect.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setTime(2, java.sql.Time.valueOf(preferredTime + ":00"));
            ps.setString(3, frequencyHours);
            ps.setString(4, hoursBeforePriority + " hours");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    public ReminderPreferences getReminderPreferences(String studentId) {
        String sql = "SELECT * FROM ReminderPreferences WHERE student_id = ?";
        try (PreparedStatement ps = database_connect.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ReminderPreferences(
                    studentId,
                    rs.getTime("general_time").toLocalTime().toString().substring(0, 5),
                    rs.getInt("frequency"),
                    rs.getInt("priority_duration")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Map<String, String>> getStudentsWithUpcomingTasks() {
        List<Map<String, String>> reminders = new ArrayList<>();

        String sql = """
            SELECT s.email, t.title AS task_title, t.deadline AS due_time
            FROM Task t
            JOIN students s ON t.student_id = s.student_id
            WHERE s.reminders_enabled = TRUE
              AND t.status != 'Completed'
              AND t.deadline BETWEEN NOW() AND NOW() + INTERVAL '1 HOUR'
        """;

        try (Connection conn = database_connect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, String> entry = new HashMap<>();
                entry.put("email", rs.getString("email"));
                entry.put("task_title", rs.getString("task_title"));
                entry.put("due_time", rs.getTimestamp("due_time").toString());

                reminders.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reminders;
    }

    
    public void processReminders() {
        List<Map<String, String>> upcomingReminders = getStudentsWithUpcomingTasks();

        EmailSender sender = new EmailSender();
        for (Map<String, String> entry : upcomingReminders) {
            String email = entry.get("email");
            String taskTitle = entry.get("task_title");
            String dueTime = entry.get("due_time");

            String subject = "⏰ Upcoming Task Reminder";
            String message = "Hello! Just a reminder that your task \"" + taskTitle + "\" is due at " + dueTime;

            sender.sendEmail(email, subject, message);
        }
    }


}


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
     
    


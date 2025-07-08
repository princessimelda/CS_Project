package NotificationSystem;

import java.sql.Timestamp;

public class Notification {
    private String notificationId;
    private String message;
    private Timestamp sentAt;
    private String studentId;
    private int taskId;
    private String eventId;
    
    public Notification(String notificationId, String message, Timestamp sentAt, String studentId, int taskId, String eventId){
        this.notificationId = notificationId;
        this.message = message;
        this.sentAt = sentAt;
        this.studentId = studentId;
        this.taskId = taskId;
        this.eventId = eventId;
    }
    
    public String getNotificationId() {
        return notificationId;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public String getStudentId() {
        return studentId;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getEventId() {
        return eventId;
    }
}


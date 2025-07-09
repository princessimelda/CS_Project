package NotificationSystem;

import java.sql.Timestamp;

public class Event {
    private String eventId;
    private String eventType;
    private Timestamp timestamp;
    private int taskId;
    private String publisherId;
    
    public Event(String eventId, String eventType, Timestamp timestamp, int taskId, String publisherId){
        this.eventId = eventId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.taskId= taskId;
        this.publisherId = publisherId; 
    }
    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getPublisherId() {
        return publisherId;
    }
}

package NotificationSystem;

public class NotificationService {
    private String notificationId;
    private String subscriberName;
    
    public NotificationService(String notificationId, String subscriberName){
        this.notificationId = notificationId;
        this.subscriberName = subscriberName;
    }
    
    public String getNotificationId(){
        return notificationId;
    }
     public String getSubscriberName(){
         return subscriberName;
     }
}

package NotificationSystem;

import java.util.UUID;
public class PublisherService {
    private String publisherId;
    private String serviceName;
    
    public PublisherService(String serviceName){
        this.publisherId = UUID.randomUUID().toString();
        this.serviceName = serviceName;
    }
    
    public String getPublisherId(){
        return publisherId;
    }
    
    public String getServiceName(){
        return serviceName;
    }
}

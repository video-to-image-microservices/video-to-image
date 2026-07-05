package video.to.image.auth_ms.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.sqs")
public class SqsProperties {

    private String userCreatedQueue;
    private String userDeletedQueue;

    public String getUserCreatedQueue() {
        return userCreatedQueue;
    }

    public void setUserCreatedQueue(String userCreatedQueue) {
        this.userCreatedQueue = userCreatedQueue;
    }

    public String getUserDeletedQueue() {
        return userDeletedQueue;
    }

    public void setUserDeletedQueue(String userDeletedQueue) {
        this.userDeletedQueue = userDeletedQueue;
    }
}

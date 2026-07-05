package video.to.image.management_ms.core.domain.entities;

import java.util.UUID;

public class User {

    private UUID id;

    public User() {
    }

    public User(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}

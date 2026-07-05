package video.to.image.management_ms.core.domain.enums;

public enum ConstMessagesEnum {
    USER_CONFLICT("Usuário já registrado");

    private final String messageBase;

    ConstMessagesEnum(String messageBase) {
        this.messageBase = messageBase;
    }

    public String getMessage() {
        return messageBase;
    }
}

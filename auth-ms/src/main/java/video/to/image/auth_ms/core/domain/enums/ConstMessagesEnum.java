package video.to.image.auth_ms.core.domain.enums;

public enum ConstMessagesEnum {
    NOT_FOUND("Entidade não encontrada"),
    EMAIL_ALREADY_EXISTS("E-mail já cadastrado"),
    VALIDATION_FAILED("Dados inválidos"),
    INVALID_REQUEST("Requisição inválida"),
    DATA_INTEGRITY_VIOLATION("Violação de integridade dos dados"),
    INTERNAL_ERROR("Erro interno do servidor"),
    INVALID_CREDENTIALS("Credenciais inválidas"),
    ACCESS_DENIED("Acesso negado");

    private final String messageBase;

    ConstMessagesEnum(String messageBase) {
        this.messageBase = messageBase;
    }

    public String getMessage() {
        return this.messageBase;
    }
}

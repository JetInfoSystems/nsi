package jet.nsi.api;

public enum NsiError {

    INTERNAL_ERROR(0, "Internal error"),
    CONSTRAINT_VIOLATION(1, "Нарушение ограничения"),
    WRITE_LOCK_ERROR(6,"Ошибка блокировки данных для модификации");

    private final int code;
    private final String message;

    NsiError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}

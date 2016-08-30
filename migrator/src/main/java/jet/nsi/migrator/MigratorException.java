package jet.nsi.migrator;

public class MigratorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MigratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigratorException(String message) {
        super(message);
    }

}

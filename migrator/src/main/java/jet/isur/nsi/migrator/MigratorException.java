package jet.isur.nsi.migrator;

public class MigratorException extends RuntimeException {

    public MigratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigratorException(String message) {
        super(message);
    }

}

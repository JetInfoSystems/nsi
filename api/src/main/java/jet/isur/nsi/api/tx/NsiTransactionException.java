package jet.isur.nsi.api.tx;

public class NsiTransactionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NsiTransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NsiTransactionException(String message) {
        super(message);
    }
}

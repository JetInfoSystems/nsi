package jet.isur.nsi.common.data;

public class NsiDataException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NsiDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public NsiDataException(String message) {
        super(message);
    }

}

package jet.isur.nsi.api;

public class NsiServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NsiServiceException(String message, Object... args) {
        super(String.format(message, args));
    }

    public NsiServiceException(Throwable th, String message, Object... args) {
        super(String.format(message, args), th);
    }
}

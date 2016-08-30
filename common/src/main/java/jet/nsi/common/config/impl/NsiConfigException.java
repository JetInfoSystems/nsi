package jet.nsi.common.config.impl;

public class NsiConfigException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NsiConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public NsiConfigException(String message) {
        super(message);
    }
}

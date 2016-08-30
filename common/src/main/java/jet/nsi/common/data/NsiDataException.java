package jet.nsi.common.data;

public class NsiDataException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String localizedMessage = null;

    public NsiDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public NsiDataException(String message) {
        super(message);
    }

    public NsiDataException localize(String localizedMessage) {
        this.localizedMessage = localizedMessage;
        return this;
    }

    @Override
    public String getLocalizedMessage() {
        if(localizedMessage != null) {
            return localizedMessage;
        } else {
            return super.getLocalizedMessage();
        }
    }
}

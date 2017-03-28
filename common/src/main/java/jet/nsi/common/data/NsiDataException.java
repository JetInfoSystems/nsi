package jet.nsi.common.data;

public class NsiDataException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String localizedMessage = null;
    private String code;

    public NsiDataException(String message, Throwable cause) {
        super(message, cause);
    }


    public NsiDataException(String code, String message) {
        super(message);
        this.code = code;
    }

    public NsiDataException(String message) {
        super(message);
    }

    public String getCode() {
        return code;
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

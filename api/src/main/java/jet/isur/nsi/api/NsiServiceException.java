package jet.isur.nsi.api;

import jet.isur.nsi.api.data.DictRow;

public class NsiServiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private NsiError error;
    private String requestId = null;
    private DictRow data;
    private String localizedMessage = null;

    public NsiServiceException(String message, Object... args) {
        super(String.format(message, args));
        setError(NsiError.INTERNAL_ERROR);
    }

    public NsiServiceException(String message) {
        super(message);
        setError(NsiError.INTERNAL_ERROR);
    }

    @Deprecated
    public NsiServiceException(Throwable th, String message, Object... args) {
        super(String.format(message, args), th);
        setError(NsiError.INTERNAL_ERROR);
    }

    public NsiServiceException(String requestId, NsiError error, String message, Object... args) {
        super(String.format(message, args));
        this.requestId = requestId;
        this.error = error;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setError(NsiError error) {
        this.error = error;
    }

    public NsiError getError() {
        return error;
    }

    @Override
    public String toString() {
        return "NsiServiceException [error=" + error + ", message="
                + getMessage() + ", requestId=" + requestId + "]";
    }

    public DictRow getData() {
        return data;
    }

    public void setData(DictRow data) {
        this.data = data;
    }

    public NsiServiceException setLocalizedMessage(String localizedMessage) {
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

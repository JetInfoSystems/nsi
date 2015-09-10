package jet.isur.nsi.api;

public class NsiServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
	private NsiError error;
	private String requestId = null;

	public NsiServiceException(String message, Object... args) {
		super(String.format(message, args));
		setError(NsiError.INTERNAL_ERROR);
	}

	public NsiServiceException(Throwable th, String message, Object... args) {
		super(String.format(message, args), th);
		setError(NsiError.INTERNAL_ERROR);
	}

	public NsiServiceException(String requestId, NsiError error, String message) {
		super(message);
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
		return "NsiProtectedException [error=" + error + ", message="
		        + getMessage() + ", requestId=" + requestId + "]";
	}

}

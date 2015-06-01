package jet.isur.nsi.api.model;

import java.io.Serializable;

public class BaseRes implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}

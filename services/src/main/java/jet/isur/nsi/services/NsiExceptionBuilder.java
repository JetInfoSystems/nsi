package jet.isur.nsi.services;

import jet.isur.nsi.api.NsiError;
import jet.isur.nsi.api.NsiServiceException;
import jet.isur.nsi.api.data.DictRow;

public class NsiExceptionBuilder {
    
    private NsiError error;
    private String requestId;
    private String message;
    private DictRow data;
    
    NsiExceptionBuilder() {
        error = NsiError.INTERNAL_ERROR;
    }
    
    public static NsiExceptionBuilder on() {
        return new NsiExceptionBuilder();
    }

    public NsiExceptionBuilder map(NsiServiceException e) {
        requestId(e.getRequestId());
        error(e.getError());
        message(e.getMessage());
        data(e.getData());
        return this;
    }
    
    public NsiExceptionBuilder error(NsiError error) {
        this.error = error;
        return this;
    }
    
    public NsiExceptionBuilder requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
    
    public NsiExceptionBuilder message(String message) {
        this.message = message;
        return this;
    }
    
    public NsiExceptionBuilder message(String format, Object ... args) {
        this.message = String.format(format, args);
        return this;
    }
    
    public NsiExceptionBuilder data(DictRow data) {
        this.data = data;
        return this;
    }
    
    public NsiServiceException build() {
        NsiServiceException result = new NsiServiceException(message);
        result.setError(error);
        result.setRequestId(requestId);
        result.setData(data);
        return result;
    }
}
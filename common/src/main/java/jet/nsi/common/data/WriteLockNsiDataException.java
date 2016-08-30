package jet.nsi.common.data;

import jet.nsi.api.data.DictRow;

public class WriteLockNsiDataException extends NsiDataException {
    private static final long serialVersionUID = 1L;
    private DictRow data;
    
    public WriteLockNsiDataException(DictRow data, String message) {
        super(message);
        this.data = data;
    }

    public DictRow getData() {
        return data;
    }
}

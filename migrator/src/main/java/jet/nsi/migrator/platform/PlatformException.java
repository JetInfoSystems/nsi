package jet.nsi.migrator.platform;

public class PlatformException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public PlatformException(String message) {
        super(message);
    }
    
    public PlatformException(String message, Throwable cause) {
        super(message, cause);
    }
    
}

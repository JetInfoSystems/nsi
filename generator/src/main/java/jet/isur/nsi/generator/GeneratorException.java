package jet.isur.nsi.generator;

public class GeneratorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneratorException(String message) {
        super(message);
    }

}

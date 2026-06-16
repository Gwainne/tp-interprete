public class SemanticException extends RuntimeException {
    private final int line;

    public SemanticException(String message, int line) {
        super("[Error semántico - línea " + line + "] " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
package eu.openvalue.hexagonalarchitecture.application.exception;

public class HexOrderOperationException extends RuntimeException {

    public HexOrderOperationException(String message) {
        super(message);
    }

    public HexOrderOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}

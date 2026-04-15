package eu.openvalue.hexagonalarchitecture.application.exception;

public class HexOrderNotFoundException extends RuntimeException {

    public HexOrderNotFoundException(Long id) {
        super("Hex order " + id + " not found");
    }
}

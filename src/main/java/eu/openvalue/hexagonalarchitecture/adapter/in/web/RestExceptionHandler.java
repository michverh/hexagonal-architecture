package eu.openvalue.hexagonalarchitecture.adapter.in.web;

import eu.openvalue.hexagonalarchitecture.application.exception.HexOrderNotFoundException;
import eu.openvalue.hexagonalarchitecture.application.exception.HexOrderOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice(basePackages = "eu.openvalue.hexagonalarchitecture.adapter.in.web")
public class RestExceptionHandler {

    @ExceptionHandler(HexOrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(HexOrderNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({HexOrderOperationException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleIllegalState(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", status.value(),
                        "error", message
                ));
    }
}

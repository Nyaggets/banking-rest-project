package com.banking.Banking.Controller;

import com.banking.Banking.validation.CustomException;
import com.banking.Banking.validation.CustomNotFoundException;
import com.banking.Banking.validation.RequestLimitException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Контроллер для перехвата исключения и возвращения отформатированного ответа
 */
@ControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response> BadCredentialsHandler(BadCredentialsException ex) {
        var errors = Map.of("client", ex.getMessage());
        return ResponseEntity.status(401).body(new Response("UNAUTHORIZED", errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> AccessDeniedHandler(AccessDeniedException ex) {
        var errors = Map.of("client", ex.getMessage());
        return ResponseEntity.status(403).body(new Response("FORBIDDEN", errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response> MethodArgumentTypeMismatchHandler(MethodArgumentTypeMismatchException ex) {
        var errors = Map.of(ex.getName(), "Некорректное значение поля");
        return ResponseEntity.status(400).body(new Response("TYPE MISMATCH", errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response> MethodArgumentNotValidHandler(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> switch (fe.getField()) {
                            case "receiverIdentifier" -> "receiver";
                            case "clientCardId" -> "sender";
                            default -> fe.getField();
                        },
                        FieldError::getDefaultMessage, (e1, e2) -> e1
                ));
        return ResponseEntity.status(400).body(new Response("VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Response> CustomExceptionHandler(CustomException ex) {
        return ResponseEntity.status(400).body(new Response(ex.getErrorCode(), ex.getErrors()));
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<Response> CustomNotFoundExceptionHandler(CustomNotFoundException ex) {
        var errors = Map.of(ex.getErrorField(), ex.getMessage());
        return ResponseEntity.status(404).body(new Response("NOT FOUND", errors));
    }

    @ExceptionHandler(RequestLimitException.class)
    public ResponseEntity<Response> RequestLimitHandler(RequestLimitException ex) {
        var errors = Map.of("client", ex.getMessage());
        return ResponseEntity.status(429).body(new Response("LIMIT EXCEEDED", errors, ex.getExpiresAt()));
    }

    @ExceptionHandler({RuntimeException.class, Exception.class})
    public ResponseEntity<Response> RuntimeHandler(Exception ex) {
        var errors = Map.of("server", ex.getLocalizedMessage());
        return ResponseEntity.status(500).body(new Response("INTERNAL SERVER ERROR", errors));
    }

    /**
     * Запись для форматирования ответа после обработки исключения
     */
    private record Response(String code, Map<String, String> errors, Instant expiresAt) {
        private Response(String code, Map<String, String> errors) {
            this(code, errors, null);
        }
    }
}

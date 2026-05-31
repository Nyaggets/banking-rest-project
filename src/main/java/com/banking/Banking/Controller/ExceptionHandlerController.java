package com.banking.Banking.Controller;

import com.banking.Banking.validation.MultipleValidationException;
import com.banking.Banking.validation.RequestLimitException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NonUniqueResultException;
import org.apache.naming.EjbRef;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response> BadCredentialsHandler(BadCredentialsException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("card", ex.getMessage());
        return ResponseEntity.status(401).body(new Response("UNAUTHORIZED", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> IllegalArgumentHandler(IllegalArgumentException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("card", ex.getMessage());
            return ResponseEntity.status(422).body(new Response("DUBLICATE", errors));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Response> EntityNotFoundHandler(EntityNotFoundException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("sender", ex.getMessage());
        return ResponseEntity.status(404).body(new Response("NOT FOUND", errors));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response> RuntimeHandler(RuntimeException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("server", ex.getMessage());
        return ResponseEntity.status(500).body(new Response("INTERNAL SERVER ERROR", errors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> AccessDeniedHandler(AccessDeniedException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("sender", ex.getMessage());
        return ResponseEntity.status(403).body(new Response("FORBIDDEN", errors));
    }

    @ExceptionHandler(RequestLimitException.class)
    public ResponseEntity<Response> RequestLimitHandler(RequestLimitException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("field", "sender");
        errors.put("message", ex.getMessage());
        errors.put("expiresAt", ex.getExpiresAt().toString());
        return ResponseEntity.status(429).body(new Response("LIMIT EXCEEDED", errors));
    }

    @ExceptionHandler(MultipleValidationException.class)
    public ResponseEntity<Response> MultipleValidationHandler(MultipleValidationException ex) {
        return ResponseEntity.status(400).body(new Response("TRANSFER ERROR", ex.getErrors()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response> MethodArgumentTypeMismatchHandler(MethodArgumentTypeMismatchException ex) {
        HashMap<String, String> errors = new HashMap<>();
        errors.put("message", "Некорректное значение параметра '%s'".formatted(ex.getName()));
        return ResponseEntity.status(400).body(new Response("TYPE MISMATCH", errors));
    }

    private record Response(String code, HashMap<String, String> errors) {
        public Response(String code, HashMap<String, String> errors) {
            this.code = code;
            this.errors = errors;
        }
    }
}

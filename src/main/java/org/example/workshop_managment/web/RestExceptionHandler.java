package org.example.workshop_managment.web;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex, HttpServletRequest req) {
        String message = ex.getMessage();
        
        if (message != null && message.contains("Duplicate entry")) {
            String errorMessage = message;
            
            int startQuote = message.indexOf("'");
            int endQuote = message.indexOf("'", startQuote + 1);
            if (startQuote >= 0 && endQuote > startQuote) {
                String duplicateValue = message.substring(startQuote + 1, endQuote);
                
                if (message.contains("email") || message.toLowerCase().contains("uk") && duplicateValue.contains("@")) {
                    errorMessage = "Email address '" + duplicateValue + "' is already in use. Please use a different email.";
                } else if (message.contains("name")) {
                    errorMessage = "Name '" + duplicateValue + "' is already in use. Please use a different name.";
                } else {
                    errorMessage = "A record with '" + duplicateValue + "' already exists. Please use a different value.";
                }
            } else {
                errorMessage = "This record already exists in the system. Please check your input and try again.";
            }
            
            Map<String, Object> body = base(HttpStatus.BAD_REQUEST, req);
            body.put("message", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
        
        if (message != null && message.contains("foreign key constraint")) {
            String errorMessage = "This operation failed because it references data that doesn't exist or can't be modified.";
            
            Map<String, Object> body = base(HttpStatus.BAD_REQUEST, req);
            body.put("message", errorMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
        
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
        String message = ex.getMessage();
        String userFriendlyMessage = "An unexpected error occurred. Please try again later.";
        
        if (message != null) {
            if (message.contains("Connection refused") || message.contains("timeout")) {
                userFriendlyMessage = "Unable to connect to the server. Please check your internet connection and try again.";
            } else if (message.contains("No value present") || message.contains("not found")) {
                userFriendlyMessage = "The requested item could not be found. It may have been deleted or doesn't exist.";
            } else if (message.contains("access denied") || message.contains("not authorized")) {
                userFriendlyMessage = "You don't have permission to perform this action.";
            }
        }
        
        Map<String, Object> body = base(HttpStatus.INTERNAL_SERVER_ERROR, req);
        body.put("message", userFriendlyMessage);
        
        body.put("devMessage", rootMessage(ex));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, req);
        
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                    FieldError::getField, 
                    fieldError -> {
                        String defaultMessage = fieldError.getDefaultMessage();
                        String field = fieldError.getField();
                        
                        String readableField = field.substring(0, 1).toUpperCase() + field.substring(1)
                            .replaceAll("([A-Z])", " $1")
                            .trim();
                        
                        if (defaultMessage == null) {
                            return readableField + " is invalid";
                        }
                        
                        if (defaultMessage.contains("NotEmpty") || defaultMessage.contains("NotBlank")) {
                            return readableField + " is required";
                        }
                        if (defaultMessage.contains("Email")) {
                            return "Please enter a valid email address";
                        }
                        if (defaultMessage.contains("Size")) {
                            if (defaultMessage.contains("min")) {
                                return readableField + " is too short";
                            }
                            if (defaultMessage.contains("max")) {
                                return readableField + " is too long";
                            }
                        }
                        
                        return defaultMessage;
                    },
                    (a, b) -> a));
        
        body.put("message", "Please fix the following issues:");
        body.put("errors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, req);
        body.put("message", "Invalid data format. Please check your input and try again.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, Throwable ex, HttpServletRequest req) {
        Map<String, Object> body = base(status, req);
        body.put("message", rootMessage(ex));
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> base(HttpStatus status, HttpServletRequest req) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("path", req.getRequestURI());
        return body;
    }

    private String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage();
    }
}

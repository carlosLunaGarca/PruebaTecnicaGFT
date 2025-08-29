package org.gft.gbt.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(
            ResponseStatusException ex, WebRequest request) {
        
        logger.warn("ResponseStatusException capturada: {} - {}", 
                   ex.getStatusCode(), ex.getReason());

        // Devolver solo el mensaje como String simple
        return new ResponseEntity<>(ex.getReason(), ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Error no manejado capturado: {}", ex.getMessage(), ex);

        return new ResponseEntity<>("Ha ocurrido un error interno del servidor", 
                                   HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

package com.documentpreview.web.exception;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.ControllerAdvice;import org.springframework.web.bind.annotation.ExceptionHandler;import org.springframework.web.context.request.WebRequest;import java.util.*;
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        String traceId = UUID.randomUUID().toString();
        logger.error("Exception occurred with trace ID: {}", traceId, ex);
        Map<String,Object> errorResponse = new HashMap<>();
        errorResponse.put("code","INTERNAL_ERROR");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("traceId", traceId);
        errorResponse.put("timestamp", new Date());
        errorResponse.put("path", request.getDescription(false).replace("uri=",""));
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof IllegalArgumentException || ex instanceof NullPointerException) { status = HttpStatus.BAD_REQUEST; errorResponse.put("code","BAD_REQUEST"); }
        else if (ex instanceof java.io.FileNotFoundException) { status = HttpStatus.NOT_FOUND; errorResponse.put("code","FILE_NOT_FOUND"); }
        else if (ex instanceof SecurityException) { status = HttpStatus.FORBIDDEN; errorResponse.put("code","FORBIDDEN"); }
        return new ResponseEntity<>(errorResponse, status);
    }
}

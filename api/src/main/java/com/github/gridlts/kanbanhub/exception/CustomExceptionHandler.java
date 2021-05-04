package com.github.gridlts.kanbanhub.exception;

import com.github.gridlts.kanbanhub.error.ResponseErrorDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(value = {ResourceNotFoundException.class})
    protected ResponseEntity<Object> handleConflict(
            RuntimeException ex, WebRequest request) {
        ResourceNotFoundException exception = (ResourceNotFoundException) ex;
        ResponseErrorDto error = new ResponseErrorDto.Builder().message(ex.getMessage()).build();
        return handleExceptionInternal(ex, error,
                new HttpHeaders(), exception.status, request);
    }
}
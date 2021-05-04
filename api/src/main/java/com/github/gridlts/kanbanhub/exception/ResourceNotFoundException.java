package com.github.gridlts.kanbanhub.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {

    public static final HttpStatus status = HttpStatus.NOT_FOUND;

    public ResourceNotFoundException(String resource) {
        super(String.format("Resource %s does not exists", resource));
    }

}

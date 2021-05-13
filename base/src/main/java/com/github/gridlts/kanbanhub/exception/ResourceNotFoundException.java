package com.github.gridlts.kanbanhub.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource) {

        super(String.format("Resource %s does not exists", resource));
    }

}

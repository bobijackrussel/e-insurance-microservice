package com.einsurance.common.exception;

/**
 * Thrown when attempting to create a resource that already exists.
 */
public class ResourceAlreadyExistsException extends BusinessException {

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                "RESOURCE_ALREADY_EXISTS");
    }
}

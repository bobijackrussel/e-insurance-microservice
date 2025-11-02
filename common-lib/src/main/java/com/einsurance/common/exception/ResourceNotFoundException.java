package com.einsurance.common.exception;

import lombok.Getter;

/**
 * Thrown when a requested resource cannot be located.
 */
@Getter
public class ResourceNotFoundException extends BusinessException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue), "RESOURCE_NOT_FOUND");
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND");
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
}

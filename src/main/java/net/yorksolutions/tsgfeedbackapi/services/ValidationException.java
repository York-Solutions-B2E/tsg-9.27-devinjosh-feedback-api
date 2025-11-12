package net.yorksolutions.tsgfeedbackapi.services;

import net.yorksolutions.tsgfeedbackapi.dtos.ErrorResponse;
import java.util.List;

public class ValidationException extends RuntimeException {    
    private final List<ErrorResponse.FieldError> fieldErrors;

    public ValidationException(List<ErrorResponse.FieldError> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }

    public List<ErrorResponse.FieldError> getFieldErrors() {
        return fieldErrors;
    }
}


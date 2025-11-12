package net.yorksolutions.tsgfeedbackapi.dtos;

import java.util.List;

public record ErrorResponse(List<FieldError> errors) {
    public record FieldError(
        String field,
        String message
    ) {}
}
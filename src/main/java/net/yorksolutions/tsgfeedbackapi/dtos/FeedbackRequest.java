package net.yorksolutions.tsgfeedbackapi.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/* FeedbackRequest DTO */
/* This DTO is used to receive feedback data from the client */
public record FeedbackRequest(
    @NotBlank(message = "Member ID is required")
    @Size(max = 36, message = "Member ID must be less than 36 characters")
    String memberId,

    @NotBlank(message = "Provider name is required")
    @Size(max = 80, message = "Provider name must be less than 80 characters")
    String providerName,

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    Integer rating,

    @Size(max = 200, message = "Comment must be less than 200 characters")
    String comment
) {

}


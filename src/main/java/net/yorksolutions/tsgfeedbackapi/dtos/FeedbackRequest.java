package net.yorksolutions.tsgfeedbackapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/* FeedbackRequest DTO */
/* This DTO is used to receive feedback data from the client */
public record FeedbackRequest(
    @Schema(description = "Member identifier", example = "m-123", required = true)
    @NotBlank(message = "Member ID is required")
    @Size(max = 36, message = "Member ID must be less than 36 characters")
    String memberId,

    @Schema(description = "Provider name", example = "Dr. Smith", required = true)
    @NotBlank(message = "Provider name is required")
    @Size(max = 80, message = "Provider name must be less than 80 characters")
    String providerName,

    @Schema(description = "Rating from 1 to 5", example = "4", required = true, minimum = "1", maximum = "5")
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    Integer rating,

    @Schema(description = "Optional comment (max 200 characters)", example = "Great experience.", required = false, maxLength = 200)
    @Size(max = 200, message = "Comment must be less than 200 characters")
    String comment
) {

}


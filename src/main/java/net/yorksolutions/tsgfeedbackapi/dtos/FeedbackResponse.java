package net.yorksolutions.tsgfeedbackapi.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

import net.yorksolutions.tsgfeedbackapi.repositories.entities.FeedbackEntity;

/* FeedbackResponse DTO */
/* This DTO is used to send feedback data to the client */
@Schema(description = "Feedback response with server-generated fields")
public record FeedbackResponse(
    @Schema(description = "Unique identifier (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    @Schema(description = "Member identifier", example = "908908908")
    String memberId,
    @Schema(description = "Provider name", example = "Jerold Calloway Offices")
    String providerName,
    @Schema(description = "Rating from 1 to 5", example = "4", minimum = "1", maximum = "5")
    int rating,
    @Schema(description = "Optional comment (max 200 characters)", example = "Great experience.", required = false, maxLength = 200)
    String comment,
    @Schema(description = "Timestamp of submission", example = "2025-11-14T12:00:00Z", format = "date-time")
    Instant submittedAt
) {
    /* Static Factory Method To Map from FeedbackEntity to FeedbackResponse */
    public static FeedbackResponse fromEntity(FeedbackEntity entity) {
        return new FeedbackResponse(
            entity.getId(),
            entity.getMemberId(),
            entity.getProviderName(),
            entity.getRating(),
            entity.getComment(),
            entity.getSubmittedAt()
        );
    }
}

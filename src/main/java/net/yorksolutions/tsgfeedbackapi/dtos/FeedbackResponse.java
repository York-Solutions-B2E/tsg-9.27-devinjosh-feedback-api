package net.yorksolutions.tsgfeedbackapi.dtos;

import java.time.Instant;
import java.util.UUID;

import net.yorksolutions.tsgfeedbackapi.repositories.entities.FeedbackEntity;

/* FeedbackResponse DTO */
/* This DTO is used to send feedback data to the client */
public record FeedbackResponse(
    UUID id,
    String memberId,
    String providerName,
    int rating,
    String comment,
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

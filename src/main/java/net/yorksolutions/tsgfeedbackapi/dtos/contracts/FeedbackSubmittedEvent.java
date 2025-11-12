package net.yorksolutions.tsgfeedbackapi.dtos.contracts;

import java.time.Instant;

public record FeedbackSubmittedEvent(
        String id,
        String memberId,
        String providerName,
        int rating,
        String comment,
        Instant submittedAt,
        int schemaVersion
) {

}

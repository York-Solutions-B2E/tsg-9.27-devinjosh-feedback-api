package net.yorksolutions.tsgfeedbackapi.services;

import java.util.UUID;

public class FeedbackNotFoundException extends RuntimeException {
    public FeedbackNotFoundException(UUID id) {
        super("Feedback not found with id: " + id);
    }

    public FeedbackNotFoundException(String memberId) { super(String.format("Feedback not found with id: %s", memberId)); }
}

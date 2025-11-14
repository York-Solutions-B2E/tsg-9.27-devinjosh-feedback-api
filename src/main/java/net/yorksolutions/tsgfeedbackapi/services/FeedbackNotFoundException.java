package net.yorksolutions.tsgfeedbackapi.services;

import java.util.UUID;

public class FeedbackNotFoundException extends RuntimeException {
    public FeedbackNotFoundException(UUID id) {
        super("Feedback not found with id: " + id);
    }
}

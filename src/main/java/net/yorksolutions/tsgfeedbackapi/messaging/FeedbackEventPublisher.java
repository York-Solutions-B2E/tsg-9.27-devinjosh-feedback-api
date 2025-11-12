package net.yorksolutions.tsgfeedbackapi.messaging;

import net.yorksolutions.tsgfeedbackapi.dtos.contracts.FeedbackSubmittedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/* FeedbackEventPublisher Service */
/* This service is used to publish feedback submitted events to the Kafka topic */
@Service
public class FeedbackEventPublisher {
    private static final String TOPIC = "feedback-submitted";
    private final KafkaTemplate<String, FeedbackSubmittedEvent> kafkaTemplate;

    public FeedbackEventPublisher(KafkaTemplate<String, FeedbackSubmittedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFeedbackSubmitted(FeedbackSubmittedEvent event) {
        kafkaTemplate.send(TOPIC, event.id(), event);
    }
}


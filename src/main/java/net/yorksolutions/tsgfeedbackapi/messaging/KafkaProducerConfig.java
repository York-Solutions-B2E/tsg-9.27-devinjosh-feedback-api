package net.yorksolutions.tsgfeedbackapi.messaging;

import net.yorksolutions.tsgfeedbackapi.dtos.contracts.FeedbackSubmittedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, FeedbackSubmittedEvent> producerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    }

    @Bean
    public KafkaTemplate<String, FeedbackSubmittedEvent> kafkaTemplate(ProducerFactory<String, FeedbackSubmittedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic feedbackSubmittedEventTopic() {
        return new NewTopic("feedback-submitted", 1, (short) 1);
    }
}


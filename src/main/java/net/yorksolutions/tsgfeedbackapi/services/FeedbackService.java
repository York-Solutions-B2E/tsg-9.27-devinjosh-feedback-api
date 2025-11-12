package net.yorksolutions.tsgfeedbackapi.services;

import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackRequest;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackResponse;
import net.yorksolutions.tsgfeedbackapi.dtos.contracts.FeedbackSubmittedEvent;
import net.yorksolutions.tsgfeedbackapi.messaging.FeedbackEventPublisher;
import net.yorksolutions.tsgfeedbackapi.repositories.FeedbackRepository;
import net.yorksolutions.tsgfeedbackapi.repositories.entities.FeedbackEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeedbackService {
    
    private static final int SCHEMA_VERSION = 1;
    private final FeedbackRepository feedbackRepository;
    private final FeedbackEventPublisher eventPublisher;

    public FeedbackService(FeedbackRepository feedbackRepository,
                            FeedbackEventPublisher eventPublisher) {
        this.feedbackRepository = feedbackRepository;
        this.eventPublisher = eventPublisher;
    }

    public FeedbackResponse createFeedback(FeedbackRequest request) {
        // Note: Bean validation (@Valid) handles basic validation,
        // but we can add business-level validation here if needed
        // For now, we rely on @Valid in the controller
        
        // Map request to entity
        FeedbackEntity entity = mapToEntity(request);
        
        // Save entity (submittedAt will be set automatically by @CreationTimestamp)
        FeedbackEntity savedEntity = feedbackRepository.save(entity);
        
        // Map to response
        FeedbackResponse response = mapToResponse(savedEntity);
        
        // Publish event
        FeedbackSubmittedEvent event = mapToEvent(savedEntity);
        eventPublisher.publishFeedbackSubmitted(event);
        
        return response;
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackById(UUID id) {
        FeedbackEntity entity = feedbackRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));
        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByMemberId(String memberId) {
        List<FeedbackEntity> entities = feedbackRepository.findByMemberId(memberId);
        return entities.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // Helper methods for mapping
    private FeedbackEntity mapToEntity(FeedbackRequest request) {
        FeedbackEntity entity = new FeedbackEntity();
        entity.setMemberId(request.memberId());
        entity.setProviderName(request.providerName());
        entity.setRating(request.rating());
        entity.setComment(request.comment());
        // submittedAt will be set automatically by @CreationTimestamp
        return entity;
    }
    
    private FeedbackResponse mapToResponse(FeedbackEntity entity) {
        return new FeedbackResponse(
            entity.getId(),
            entity.getMemberId(),
            entity.getProviderName(),
            entity.getRating(),
            entity.getComment(),
            entity.getSubmittedAt()
        );
    }
    
    private FeedbackSubmittedEvent mapToEvent(FeedbackEntity entity) {
        return new FeedbackSubmittedEvent(
            entity.getId().toString(),
            entity.getMemberId(),
            entity.getProviderName(),
            entity.getRating(),
            entity.getComment(),
            entity.getSubmittedAt(),
            SCHEMA_VERSION
        );
    }
}
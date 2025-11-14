package net.yorksolutions.tsgfeedbackapi.services;

import net.yorksolutions.tsgfeedbackapi.dtos.ErrorResponse;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackRequest;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackResponse;
import net.yorksolutions.tsgfeedbackapi.dtos.contracts.FeedbackSubmittedEvent;
import net.yorksolutions.tsgfeedbackapi.messaging.FeedbackEventPublisher;
import net.yorksolutions.tsgfeedbackapi.repositories.FeedbackRepository;
import net.yorksolutions.tsgfeedbackapi.repositories.entities.FeedbackEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        // Service-layer validation per spec
        List<ErrorResponse.FieldError> errors = new ArrayList<>();

        // Validate memberId: required, non-empty, length <= 36
        if (request.memberId() == null || request.memberId().isBlank()) {
            errors.add(new ErrorResponse.FieldError("memberId", "Member ID is required"));
        } else if (request.memberId().length() > 36) {
            errors.add(new ErrorResponse.FieldError("memberId", "Member ID must be less than 36 characters"));
        }

        // Validate providerName: required, non-empty, length <= 80
        if(request.providerName() == null || request.providerName().isBlank()) {
            errors.add(new ErrorResponse.FieldError("providerName", "Provider name is required"));
        } else if (request.providerName().length() > 80) {
            errors.add(new ErrorResponse.FieldError("providerName", "Provider name must be less than 80 characters"));
        }

        // Validate rating: required, integer 1-5
        if(request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            errors.add(new ErrorResponse.FieldError("rating", "Rating must be between 1 and 5"));
        }

        // Validate comment: optional, length <= 200
        if(request.comment() != null && request.comment().length() > 200) {
            errors.add(new ErrorResponse.FieldError("comment", "Comment must be less than 200 characters"));
        }

        // If errors, return 400 with error response
        if(!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // Map request to entity
        FeedbackEntity entity = mapToEntity(request);
        
        // Save entity (submittedAt will be set automatically by @CreationTimestamp)
        FeedbackEntity savedEntity = feedbackRepository.saveAndFlush(entity);
        
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
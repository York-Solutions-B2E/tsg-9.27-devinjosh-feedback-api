package net.yorksolutions.tsgfeedbackapi.services;

import net.yorksolutions.tsgfeedbackapi.dtos.ErrorResponse;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackRequest;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackResponse;
import net.yorksolutions.tsgfeedbackapi.dtos.contracts.FeedbackSubmittedEvent;
import net.yorksolutions.tsgfeedbackapi.messaging.FeedbackEventPublisher;
import net.yorksolutions.tsgfeedbackapi.repositories.FeedbackRepository;
import net.yorksolutions.tsgfeedbackapi.repositories.entities.FeedbackEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackEventPublisher eventPublisher;
    
    @InjectMocks
    private FeedbackService feedbackService;

    private FeedbackRequest validRequest;
    private FeedbackEntity savedEntity;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        validRequest = new FeedbackRequest(
            "member-123",
            "Dr. Smith",
            4,
            "Great experience"
        );

        savedEntity = new FeedbackEntity();
        savedEntity.setId(testId);
        savedEntity.setMemberId("member-123");
        savedEntity.setProviderName("Dr. Smith");
        savedEntity.setRating(4);
        savedEntity.setComment("Great experience");
        savedEntity.setSubmittedAt(Instant.now());
    }

    // ========== CREATE FEEDBACK - HAPPY PATH ==========
    @Test
    void createFeedback_WithValidRequest_ReturnsResponse() {
        // Arrange 
        when(feedbackRepository.save(any(FeedbackEntity.class))).thenReturn(savedEntity);

        // Act 
        FeedbackResponse response = feedbackService.createFeedback(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testId, response.id());
        assertEquals("member-123", response.memberId());
        assertEquals("Dr. Smith", response.providerName());
        assertEquals(4, response.rating());
        assertEquals("Great experience", response.comment());
        assertNotNull(response.submittedAt());

        // Verify repository was called
        verify(feedbackRepository, times(1)).save(any(FeedbackEntity.class));

        // Verify event was published
        ArgumentCaptor<FeedbackSubmittedEvent> eventCaptor = ArgumentCaptor.forClass(FeedbackSubmittedEvent.class);
        verify(eventPublisher, times(1)).publishFeedbackSubmitted(eventCaptor.capture());
        
        FeedbackSubmittedEvent publishedEvent = eventCaptor.getValue();
        assertEquals(testId.toString(), publishedEvent.id());
        assertEquals("member-123", publishedEvent.memberId());
        assertEquals("Dr. Smith", publishedEvent.providerName());
        assertEquals(4, publishedEvent.rating());
        assertEquals(1, publishedEvent.schemaVersion());
    }

    @Test
    void createFeedback_WithValidRequestAndNoComment_ReturnsResponse() {
        // Arrange
        FeedbackRequest requestWithoutComment = new FeedbackRequest(
            "member-123",
            "Dr. Smith",
            5,
            null
        );
        savedEntity.setComment(null);
        when(feedbackRepository.save(any(FeedbackEntity.class))).thenReturn(savedEntity);

        // Act
        FeedbackResponse response = feedbackService.createFeedback(requestWithoutComment);

        // Assert
        assertNotNull(response);
        assertNull(response.comment());
        verify(feedbackRepository, times(1)).save(any(FeedbackEntity.class));
        verify(eventPublisher, times(1)).publishFeedbackSubmitted(any());
    }

    // ========== CREATE FEEDBACK - VALIDATION TESTS ==========

    @Test
    void createFeedback_WithNullMemberId_ThrowsValidationException() {
        // Arrange
        FeedbackRequest request = new FeedbackRequest(null, "Dr. Smith", 4, "Comment");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("memberId", errors.get(0).field());
        assertEquals("Member ID is required", errors.get(0).message());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithBlankMemberId_ThrowsValidationException() {
        // Arrange
        FeedbackRequest request = new FeedbackRequest("   ", "Dr. Smith", 4, "Comment");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("memberId", errors.get(0).field());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithMemberIdTooLong_ThrowsValidationException() {
        // Arrange
        String longMemberId = "a".repeat(37); // 37 characters
        FeedbackRequest request = new FeedbackRequest(longMemberId, "Dr. Smith", 4, "Comment");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("memberId", errors.get(0).field());
        assertEquals("Member ID must be less than 36 characters", errors.get(0).message());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithNullProviderName_ThrowsValidationException() {
        // Arrange
        FeedbackRequest request = new FeedbackRequest("member-123", null, 4, "Comment");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("providerName", errors.get(0).field());
        assertEquals("Provider name is required", errors.get(0).message());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithBlankProviderName_ThrowsValidationException() {
        // Arrange
        FeedbackRequest request = new FeedbackRequest("member-123", "   ", 4, "Comment");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("providerName", errors.get(0).field());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithProviderNameTooLong_ThrowsValidationException() {
        // Arrange
        String longProviderName = "a".repeat(81); // 81 characters
        FeedbackRequest request = new FeedbackRequest("member-123", longProviderName, 4, "Comment");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("providerName", errors.get(0).field());
        assertEquals("Provider name must be less than 80 characters", errors.get(0).message());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithNullRating_ThrowsValidationException() {
        // Arrange
        FeedbackRequest request = new FeedbackRequest("member-123", "Dr. Smith", null, "Comment");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("rating", errors.get(0).field());
        assertEquals("Rating must be between 1 and 5", errors.get(0).message());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithRatingBelow1_ThrowsValidationException() {
        // Arrange
        FeedbackRequest request = new FeedbackRequest("member-123", "Dr. Smith", 0, "Comment");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("rating", errors.get(0).field());
        assertEquals("Rating must be between 1 and 5", errors.get(0).message());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithRatingAbove5_ThrowsValidationException() {
        // Arrange
        FeedbackRequest request = new FeedbackRequest("member-123", "Dr. Smith", 6, "Comment");

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("rating", errors.get(0).field());
        assertEquals("Rating must be between 1 and 5", errors.get(0).message());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithCommentTooLong_ThrowsValidationException() {
        // Arrange
        String longComment = "a".repeat(201); // 201 characters
        FeedbackRequest request = new FeedbackRequest("member-123", "Dr. Smith", 4, longComment);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("comment", errors.get(0).field());
        assertEquals("Comment must be less than 200 characters", errors.get(0).message());
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    @Test
    void createFeedback_WithMultipleValidationErrors_ThrowsValidationExceptionWithAllErrors() {
        // Arrange
        FeedbackRequest request = new FeedbackRequest(null, null, null, "a".repeat(201));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> feedbackService.createFeedback(request));
        
        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(4, errors.size());
        
        // Verify all expected errors are present
        assertTrue(errors.stream().anyMatch(e -> e.field().equals("memberId")));
        assertTrue(errors.stream().anyMatch(e -> e.field().equals("providerName")));
        assertTrue(errors.stream().anyMatch(e -> e.field().equals("rating")));
        assertTrue(errors.stream().anyMatch(e -> e.field().equals("comment")));
        
        verify(feedbackRepository, never()).save(any());
        verify(eventPublisher, never()).publishFeedbackSubmitted(any());
    }

    // ========== GET FEEDBACK BY ID ==========

    @Test
    void getFeedbackById_WithValidId_ReturnsResponse() {
        // Arrange
        when(feedbackRepository.findById(testId)).thenReturn(Optional.of(savedEntity));

        // Act
        FeedbackResponse response = feedbackService.getFeedbackById(testId);

        // Assert
        assertNotNull(response);
        assertEquals(testId, response.id());
        assertEquals("member-123", response.memberId());
        verify(feedbackRepository, times(1)).findById(testId);
    }

    @Test
    void getFeedbackById_WithInvalidId_ThrowsRuntimeException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();
        when(feedbackRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> feedbackService.getFeedbackById(invalidId));
        
        assertTrue(exception.getMessage().contains("Feedback not found"));
        verify(feedbackRepository, times(1)).findById(invalidId);
    }

    // ========== GET FEEDBACK BY MEMBER ID ==========

    @Test
    void getFeedbackByMemberId_WithValidMemberId_ReturnsList() {
        // Arrange
        FeedbackEntity entity2 = new FeedbackEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setMemberId("member-123");
        entity2.setProviderName("Dr. Jones");
        entity2.setRating(5);
        entity2.setSubmittedAt(Instant.now());

        when(feedbackRepository.findByMemberId("member-123"))
            .thenReturn(List.of(savedEntity, entity2));

        // Act
        List<FeedbackResponse> responses = feedbackService.getFeedbackByMemberId("member-123");

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("member-123", responses.get(0).memberId());
        assertEquals("member-123", responses.get(1).memberId());
        verify(feedbackRepository, times(1)).findByMemberId("member-123");
    }

    @Test
    void getFeedbackByMemberId_WithNoResults_ReturnsEmptyList() {
        // Arrange
        when(feedbackRepository.findByMemberId("member-999")).thenReturn(List.of());

        // Act
        List<FeedbackResponse> responses = feedbackService.getFeedbackByMemberId("member-999");

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(feedbackRepository, times(1)).findByMemberId("member-999");
    }
}


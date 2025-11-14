package net.yorksolutions.tsgfeedbackapi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackRequest;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackResponse;
import net.yorksolutions.tsgfeedbackapi.dtos.ErrorResponse;
import net.yorksolutions.tsgfeedbackapi.repositories.FeedbackRepository;
import net.yorksolutions.tsgfeedbackapi.repositories.entities.FeedbackEntity;
import net.yorksolutions.tsgfeedbackapi.messaging.FeedbackEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/*
 * Unit Tests for FeedbackService
 * Happy path tests for createFeedback
 * Test validation errors for createFeedback
 * Test getFeedbackById
 * Test getFeedbackByMemberId
 * Test Mapping between DTO and Entity
*/
public class FeedbackServiceTest {
    
    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackEventPublisher eventPublisher;

    @InjectMocks
    private FeedbackService feedbackService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ============================================
    // createFeedback - Happy Path
    // ============================================

    @Test
    void createFeedback_WithValidRequest_ReturnsResponse() {
        // Arrange
        FeedbackRequest request = new FeedbackRequest(
            "908908908",
            "Jerold Calloway Offices",
            4,
            "Great experience"
        );

        UUID generatedId = UUID.randomUUID();
        Instant submittedAt = Instant.now();

        FeedbackEntity savedEntity = new FeedbackEntity();
        savedEntity.setId(generatedId);
        savedEntity.setMemberId("908908908");
        savedEntity.setProviderName("Jerold Calloway Offices");
        savedEntity.setRating(4);
        savedEntity.setComment("Great experience");
        savedEntity.setSubmittedAt(submittedAt);

        when(feedbackRepository.save(any(FeedbackEntity.class))).thenReturn(savedEntity);
        doNothing().when(eventPublisher).publishFeedbackSubmitted(any());

        // Act
        FeedbackResponse response = feedbackService.createFeedback(request);

        // Assert
        assertNotNull(response);
        assertEquals(generatedId, response.id());
        assertEquals("908908908", response.memberId());
        assertEquals("Jerold Calloway Offices", response.providerName());
        assertEquals(4, response.rating());
        assertEquals("Great experience", response.comment()); // Fixed: removed period
        assertEquals(submittedAt, response.submittedAt());

        verify(feedbackRepository, times(1)).save(any(FeedbackEntity.class));
        verify(eventPublisher, times(1)).publishFeedbackSubmitted(any());
    }

    // ============================================
    // createFeedback - Validation Tests (One per rule)
    // ============================================

    @Test
    void createFeedback_WithBlankMemberId_ThrowsValidationException() {
        // Tests: memberId null/blank validation
        FeedbackRequest request = new FeedbackRequest(
            "",  // blank memberId
            "Jerold Calloway Offices",
            4,
            "Great experience"
        );

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> feedbackService.createFeedback(request)
        );

        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("memberId", errors.get(0).field());
        assertEquals("Member ID is required", errors.get(0).message());

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createFeedback_WithMemberIdTooLong_ThrowsValidationException() {
        // Tests: memberId length > 36 validation
        String longMemberId = "a".repeat(37);

        FeedbackRequest request = new FeedbackRequest(
            longMemberId,
            "Jerold Calloway Offices",
            4,
            "Great experience"
        );

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> feedbackService.createFeedback(request)
        );

        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("memberId", errors.get(0).field());
        assertEquals("Member ID must be less than 36 characters", errors.get(0).message());

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createFeedback_WithBlankProviderName_ThrowsValidationException() {
        // Tests: providerName null/blank validation
        FeedbackRequest request = new FeedbackRequest(
            "908908908",
            "",  // blank providerName
            4,
            "Great experience"
        );

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> feedbackService.createFeedback(request)
        );

        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("providerName", errors.get(0).field());
        assertEquals("Provider name is required", errors.get(0).message());

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createFeedback_WithProviderNameTooLong_ThrowsValidationException() {
        // Tests: providerName length > 80 validation
        String longProviderName = "a".repeat(81);

        FeedbackRequest request = new FeedbackRequest(
            "908908908",
            longProviderName,
            4,
            "Great experience"
        );

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> feedbackService.createFeedback(request)
        );

        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("providerName", errors.get(0).field());
        assertEquals("Provider name must be less than 80 characters", errors.get(0).message());

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createFeedback_WithNullRating_ThrowsValidationException() {
        // Tests: rating null validation
        FeedbackRequest request = new FeedbackRequest(
            "908908908",
            "Jerold Calloway Offices",
            null,  // null rating
            "Great experience"
        );

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> feedbackService.createFeedback(request)
        );

        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("rating", errors.get(0).field());
        assertEquals("Rating must be between 1 and 5", errors.get(0).message());

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createFeedback_WithRatingBelow1_ThrowsValidationException() {
        // Tests: rating < 1 validation
        FeedbackRequest request = new FeedbackRequest(
            "908908908",
            "Jerold Calloway Offices",
            0,  // rating < 1
            "Great experience"
        );

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> feedbackService.createFeedback(request)
        );

        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("rating", errors.get(0).field());
        assertEquals("Rating must be between 1 and 5", errors.get(0).message());

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createFeedback_WithRatingAbove5_ThrowsValidationException() {
        // Tests: rating > 5 validation
        FeedbackRequest request = new FeedbackRequest(
            "908908908",
            "Jerold Calloway Offices",
            6,  // rating > 5
            "Great experience"
        );

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> feedbackService.createFeedback(request)
        );

        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("rating", errors.get(0).field());
        assertEquals("Rating must be between 1 and 5", errors.get(0).message());

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createFeedback_WithCommentTooLong_ThrowsValidationException() {
        // Tests: comment length > 200 validation
        String longComment = "a".repeat(201);

        FeedbackRequest request = new FeedbackRequest(
            "908908908",
            "Jerold Calloway Offices",
            4,
            longComment
        );

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> feedbackService.createFeedback(request)
        );

        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(1, errors.size());
        assertEquals("comment", errors.get(0).field());
        assertEquals("Comment must be less than 200 characters", errors.get(0).message());

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void createFeedback_WithMultipleValidationErrors_ThrowsExceptionWithAllErrors() {
        // Tests: multiple validation errors captured together
        FeedbackRequest request = new FeedbackRequest(
            "",  // blank memberId
            "",  // blank providerName
            null,  // null rating
            "a".repeat(201)  // comment too long
        );

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> feedbackService.createFeedback(request)
        );

        List<ErrorResponse.FieldError> errors = exception.getFieldErrors();
        assertEquals(4, errors.size(), "Should have 4 validation errors");

        // Verify all error fields are present
        List<String> errorFields = errors.stream()
            .map(ErrorResponse.FieldError::field)
            .toList();

        assertTrue(errorFields.contains("memberId"));
        assertTrue(errorFields.contains("providerName"));
        assertTrue(errorFields.contains("rating"));
        assertTrue(errorFields.contains("comment"));

        verifyNoInteractions(feedbackRepository);
        verifyNoInteractions(eventPublisher);
    }

    // ============================================
    // getFeedbackById Tests
    // ============================================

    @Test
    void getFeedbackById_WithValidId_ReturnsResponse() {
        // Arrange
        UUID id = UUID.randomUUID();
        Instant submittedAt = Instant.now();

        FeedbackEntity entity = new FeedbackEntity();
        entity.setId(id);
        entity.setMemberId("908908908");
        entity.setProviderName("Jerold Calloway Offices");
        entity.setRating(4);
        entity.setComment("Great experience");
        entity.setSubmittedAt(submittedAt);

        when(feedbackRepository.findById(id))
            .thenReturn(Optional.of(entity));

        // Act
        FeedbackResponse response = feedbackService.getFeedbackById(id);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("908908908", response.memberId());
        assertEquals("Jerold Calloway Offices", response.providerName());
        assertEquals(4, response.rating());
        assertEquals("Great experience", response.comment());

        verify(feedbackRepository, times(1)).findById(id);
    }

    @Test
    void getFeedbackById_WithInvalidId_ThrowsFeedbackNotFoundException() {
        // Arrange
        UUID invalidId = UUID.randomUUID();

        when(feedbackRepository.findById(invalidId))
            .thenReturn(Optional.empty());  // Not found

        // Act & Assert
        FeedbackNotFoundException exception = assertThrows(
            FeedbackNotFoundException.class,
            () -> feedbackService.getFeedbackById(invalidId)
        );

        assertTrue(exception.getMessage().contains("Feedback not found with id: " + invalidId));

        verify(feedbackRepository, times(1)).findById(invalidId);
    }

    // ============================================
    // getFeedbackByMemberId Tests
    // ============================================

    @Test
    void getFeedbackByMemberId_WithValidMemberId_ReturnsList() {
        // Arrange
        String memberId = "908908908";
        Instant submittedAt = Instant.now();

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        FeedbackEntity entity1 = new FeedbackEntity();
        entity1.setId(id1);
        entity1.setMemberId(memberId);
        entity1.setProviderName("Jerold Calloway Offices");
        entity1.setRating(4);
        entity1.setComment("Great experience");
        entity1.setSubmittedAt(submittedAt);

        FeedbackEntity entity2 = new FeedbackEntity();
        entity2.setId(id2);
        entity2.setMemberId(memberId);
        entity2.setProviderName("Dr. Jones");
        entity2.setRating(5);
        entity2.setComment("Excellent!");
        entity2.setSubmittedAt(submittedAt);

        when(feedbackRepository.findByMemberId(memberId))
            .thenReturn(List.of(entity1, entity2));

        // Act
        List<FeedbackResponse> responses = feedbackService.getFeedbackByMemberId(memberId);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals(id1, responses.get(0).id());
        assertEquals("Jerold Calloway Offices", responses.get(0).providerName());

        assertEquals(id2, responses.get(1).id());
        assertEquals("Dr. Jones", responses.get(1).providerName());

        verify(feedbackRepository, times(1)).findByMemberId(memberId);
    }

    @Test
    void getFeedbackByMemberId_WithNoResults_ReturnsEmptyList() {
        // Arrange
        String memberId = "m-999";

        when(feedbackRepository.findByMemberId(memberId))
            .thenReturn(List.of());  // Empty list

        // Act
        List<FeedbackResponse> responses = feedbackService.getFeedbackByMemberId(memberId);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty(), "Should return empty list when no results");

        verify(feedbackRepository, times(1)).findByMemberId(memberId);
    }
}
package net.yorksolutions.tsgfeedbackapi.controllers;

// TODO: Implement REST controller
// Endpoints: POST /api/v1/feedback, GET /api/v1/feedback/{id}, 
//            GET /api/v1/feedback?memberId=, GET /api/v1/health

import lombok.RequiredArgsConstructor;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackRequest;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackResponse;
import net.yorksolutions.tsgfeedbackapi.services.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Feedback management API endpoints")
public class FeedbackController {

    private final FeedbackService feedbackService;
    // Submit Feedback Endpoint
    @PostMapping("/feedback")
    @Operation(
        summary = "Submit feedback",
        description = "Creates a new feedback entry and publishes an event to Kafka"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Feedback created successfully",
            content = @Content(schema = @Schema(implementation = FeedbackResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - see error response body for details"
        )
    })
    public ResponseEntity<FeedbackResponse> submitFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        FeedbackResponse created = feedbackService.createFeedback(feedbackRequest);
        URI location = URI.create("/api/v1/feedback");
        return  ResponseEntity.created(location).body(created);
    }

    // Get Feedback by ID Endpoint
    @GetMapping("/feedback/{id}")
    @Operation(
        summary = "Get feedback by ID",
        description = "Retrieves a specific feedback entry by its UUID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Feedback found",
            content = @Content(schema = @Schema(implementation = FeedbackResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Feedback not found"
        )
    })
    public FeedbackResponse getById(
            @Parameter(
                description = "UUID of the feedback",
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID id) {
        return  feedbackService.getFeedbackById(id);
    }

    // Get Feedback by Member ID Endpoint
    @GetMapping("/feedback")
    @Operation(
        summary = "Get feedback by member ID",
        description = "Retrieves all feedback entries for a specific member"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of feedback entries (may be empty)",
            content = @Content(schema = @Schema(implementation = FeedbackResponse.class))
        )
    })
    public List<FeedbackResponse> getByMemberId(
            @Parameter(
                description = "Member ID to filter feedback by",
                required = true,
                example = "908908908"
            )
            @RequestParam String memberId) {
        return feedbackService.getFeedbackByMemberId(memberId);
    }

    // Health Check Endpoint
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Simple health check endpoint to verify service availability"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Service is healthy"
    )
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

}


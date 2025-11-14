package net.yorksolutions.tsgfeedbackapi.controllers;

// TODO: Implement REST controller
// Endpoints: POST /api/v1/feedback, GET /api/v1/feedback/{id}, 
//            GET /api/v1/feedback?memberId=, GET /api/v1/health

import lombok.RequiredArgsConstructor;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackRequest;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackResponse;
import net.yorksolutions.tsgfeedbackapi.services.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/feedback")
    public ResponseEntity<FeedbackResponse> submitFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        FeedbackResponse created = feedbackService.createFeedback(feedbackRequest);
        URI location = URI.create("/api/v1/feedback");
        return  ResponseEntity.created(location).body(created);
    }

    @GetMapping("/feedback/{id}")
    public FeedbackResponse get(@PathVariable UUID id) {
        return  feedbackService.getFeedbackById(id);
    }

    @GetMapping("/feedback")
    public List<FeedbackResponse> byMember(@RequestParam String memberId) {
        return feedbackService.getFeedbackByMemberId(memberId);
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

}


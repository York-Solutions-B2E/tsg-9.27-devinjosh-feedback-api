package net.yorksolutions.tsgfeedbackapi.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackRequest;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackResponse;
import net.yorksolutions.tsgfeedbackapi.services.FeedbackService;
import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = FeedbackController.class)
public class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private FeedbackService feedbackService;

    @Test
    void submitFeedback_happyPath_ResponseEntityCreated() throws Exception {
        //Arrange
        FeedbackRequest request = new FeedbackRequest("m-101", "Dr. Phill", 4, "Cool guy.");
        UUID uuid = UUID.randomUUID();
        Instant now = Instant.now();
        FeedbackResponse response = new FeedbackResponse(uuid,"m-101", "Dr. Phill", 4, "Cool guy.", now);

        when(feedbackService.createFeedback(any(FeedbackRequest.class)))
                .thenReturn(response);
        //Act
        ResultActions resultActions = mockMvc.perform(post("/api/v1/feedback")
            .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))

        );
        //Assert
        resultActions.andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(response)));

        verify(feedbackService).createFeedback(any(FeedbackRequest.class));
    }

    @Test
    void submitFeedback_invalidBody_HttpMessageNotReadableExceptionThrown() throws Exception {
        String badJson = """
                {
                  "memberId": "m-101",
                  "providerName": "Dr. Phill",
                  "rating": 4,
                  "comment": "Cool guy.",
                  "randomThing": "oops"
                }
                """;

        mockMvc.perform(post("/api/v1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Exception ex = result.getResolvedException();
                    // Make sure Spring resolved the correct exception type
                    assertNotNull(ex);
                    assertInstanceOf(HttpMessageNotReadableException.class, ex, "Expected HttpMessageNotReadableException but was " + ex.getClass());

                });


        verifyNoInteractions(feedbackService);
    }

}

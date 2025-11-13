package net.yorksolutions.tsgfeedbackapi.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.utils.Json;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackRequest;
import net.yorksolutions.tsgfeedbackapi.dtos.FeedbackResponse;
import net.yorksolutions.tsgfeedbackapi.services.FeedbackService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/feedback")
            .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))

        );
        //Assert
        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(response)));

        verify(feedbackService).createFeedback(any(FeedbackRequest.class));
    }

    @Test
    public void submitFeedback_invalidBody_ExceptionThrown() throws Exception {}


}

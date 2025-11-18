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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.Assert;


import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = FeedbackController.class)
public class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private FeedbackService feedbackService;

    UUID uuid = UUID.randomUUID();
    Instant now = Instant.now();
    FeedbackResponse response = new FeedbackResponse(uuid,"m-101", "Dr. Phill", 4, "Cool guy.", now);

    //---------------------------------- Post Request Tests-------------------------------------------------
    @Test
    void submitFeedback_happyPath_ResponseEntityCreated() throws Exception {
        //Arrange
        FeedbackRequest request = new FeedbackRequest("m-101", "Dr. Phill", 4, "Cool guy.");

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
// ------------------------------- Get Request Tests ------------------------------------------
    @Test
    void getFeedbackById_happyPath_ReturnsFeedbackResponseAnd200() throws Exception {
        //Act
        when(feedbackService.getFeedbackById(any())).thenReturn(response);
        //Act/Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/feedback/" + uuid))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(response)));

        verify(feedbackService).getFeedbackById(uuid);

    }
    @Test
    void getFeedbackByMemberId_happyPath_ReturnsFeedbackResponseListAnd200() throws Exception {
        //Assert
        String memberId = "m-101";
        FeedbackResponse r1 = new FeedbackResponse(
                UUID.randomUUID(), memberId, "Dr. Phil", 4, "Nice", Instant.now()
        );
        FeedbackResponse r2 = new FeedbackResponse(
                UUID.randomUUID(), memberId, "Dr. Oz", 5, "Great", Instant.now()
        );
        List<FeedbackResponse> feedbackResponseList = Arrays.asList(r1, r2);
        //Act
        when(feedbackService.getFeedbackByMemberId(memberId)).thenReturn(feedbackResponseList);
        //Act/Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/feedback?memberId=" + memberId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(feedbackResponseList)));

        verify(feedbackService).getFeedbackByMemberId(memberId);

    }

}

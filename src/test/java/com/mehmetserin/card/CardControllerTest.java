package com.mehmetserin.card;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void issueCard_returnsMaskedPan() throws Exception {
        String body = """
                { "cardholderName": "Api Card User", "dailyLimit": 8000 }
                """;

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.maskedPan").exists());
    }

    @Test
    void validatePan_returnsResult() throws Exception {
        String body = """
                { "pan": "4111111111111111" }
                """;

        mockMvc.perform(post("/api/cards/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.panLast4").value("1111"))
                .andExpect(jsonPath("$.pan").doesNotExist());
    }

    @Test
    void authorizeCard_recordsSpendAndReturnsRemainingLimit() throws Exception {
        String cardId = issueCard("Authorization Api User", 100);

        mockMvc.perform(post("/api/cards/{cardId}/authorize", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"amount\": 35 }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(35))
                .andExpect(jsonPath("$.spentToday").value(35))
                .andExpect(jsonPath("$.availableDailyLimit").value(65))
                .andExpect(jsonPath("$.pan").doesNotExist());
    }

    @Test
    void authorizeCard_rejectsBlockedCard() throws Exception {
        String cardId = issueCard("Blocked Api User", 100);
        mockMvc.perform(post("/api/cards/{cardId}/block", cardId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cards/{cardId}/authorize", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"amount\": 10 }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("authorization_declined"));
    }

    @Test
    void authorizeCard_rejectsAmountOverDailyLimit() throws Exception {
        String cardId = issueCard("Over Limit Api User", 100);

        mockMvc.perform(post("/api/cards/{cardId}/authorize", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"amount\": 101 }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("authorization_declined"));
    }

    private String issueCard(String cardholderName, int dailyLimit) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "cardholderName": "%s", "dailyLimit": %d }
                                """.formatted(cardholderName, dailyLimit)))
                .andExpect(status().isCreated())
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.cardId");
    }
}

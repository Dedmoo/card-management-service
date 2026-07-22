package com.mehmetserin.card;

import com.mehmetserin.card.model.Card;
import com.mehmetserin.card.model.CardModels.CardStatus;
import com.mehmetserin.card.model.CardModels.CardView;
import com.mehmetserin.card.repository.CardRepository;
import com.mehmetserin.card.service.CardService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CardServiceTest {

    private final Map<String, Card> store = new HashMap<>();
    private final CardRepository cardRepository = mock(CardRepository.class);
    private final CardService service = new CardService(cardRepository);

    {
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            store.put(card.getCardId(), card);
            return card;
        });
        when(cardRepository.findById(any(String.class))).thenAnswer(invocation ->
                Optional.ofNullable(store.get(invocation.getArgument(0, String.class))));
        when(cardRepository.findAll()).thenAnswer(invocation -> new ArrayList<>(store.values()));
    }

    @Test
    void issue_generatesMaskedActiveCard() {
        CardView card = service.issue("Test User", new BigDecimal("10000"));
        assertEquals(CardStatus.ACTIVE, card.status());
        assertTrue(card.maskedPan().startsWith("**** **** **** "));
        assertEquals(new BigDecimal("10000"), card.dailyLimit());
    }

    @Test
    void issue_defaultLimit_whenNotProvided() {
        CardView view = service.issue("Default Limit User", null);
        assertEquals(new BigDecimal("5000"), view.dailyLimit());
    }

    @Test
    void blockAndUnblock_changesStatus() {
        CardView card = service.issue("Block User", new BigDecimal("5000"));
        assertEquals(CardStatus.BLOCKED, service.block(card.cardId()).status());
        assertEquals(CardStatus.ACTIVE, service.unblock(card.cardId()).status());
    }

    @Test
    void updateLimit_updatesValue() {
        CardView card = service.issue("Limit User", new BigDecimal("5000"));
        CardView updated = service.updateLimit(card.cardId(), new BigDecimal("12000"));
        assertEquals(new BigDecimal("12000"), updated.dailyLimit());
    }

    @Test
    void unknownCard_throws() {
        assertThrows(CardService.CardNotFoundException.class, () -> service.get("CARD-UNKNOWN"));
    }
}

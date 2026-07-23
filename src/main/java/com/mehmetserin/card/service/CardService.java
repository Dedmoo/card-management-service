package com.mehmetserin.card.service;

import com.mehmetserin.card.model.Card;
import com.mehmetserin.card.model.CardModels.CardStatus;
import com.mehmetserin.card.model.CardModels.CardView;
import com.mehmetserin.card.model.CardModels.AuthorizationView;
import com.mehmetserin.card.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class CardService {

    // Publicly documented test IIN (not a live issuer BIN). Demo PANs only.
    private static final String BIN = "400000";
    private static final int PAN_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CardView issue(String cardholderName, BigDecimal dailyLimit) {
        if (cardholderName == null || cardholderName.isBlank()) {
            throw new IllegalArgumentException("Cardholder name is required.");
        }
        BigDecimal limit = (dailyLimit == null || dailyLimit.signum() <= 0)
                ? new BigDecimal("5000")
                : dailyLimit;

        String pan = generatePan();
        String expiry = LocalDate.now().plusYears(4).format(DateTimeFormatter.ofPattern("MM/yy"));
        var card = new Card(
                "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                cardholderName.trim(),
                pan,
                expiry,
                CardStatus.ACTIVE,
                limit);

        cardRepository.save(card);
        return toView(card);
    }

    public CardView get(String cardId) {
        return toView(require(cardId));
    }

    public List<CardView> list() {
        return cardRepository.findAll().stream().map(this::toView).toList();
    }

    public CardView block(String cardId) {
        Card card = require(cardId);
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        return toView(card);
    }

    public CardView unblock(String cardId) {
        Card card = require(cardId);
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        return toView(card);
    }

    public CardView updateLimit(String cardId, BigDecimal dailyLimit) {
        if (dailyLimit == null || dailyLimit.signum() <= 0) {
            throw new IllegalArgumentException("Daily limit must be positive.");
        }
        Card card = require(cardId);
        card.setDailyLimit(dailyLimit);
        cardRepository.save(card);
        return toView(card);
    }

    @Transactional
    public AuthorizationView authorize(String cardId, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Authorization amount must be positive.");
        }
        Card card = require(cardId);
        LocalDate today = LocalDate.now();
        BigDecimal available = card.authorize(amount, today);
        cardRepository.save(card);
        return new AuthorizationView(card.getCardId(), amount, card.getSpentToday(today), available);
    }

    private String generatePan() {
        var sb = new StringBuilder(BIN);
        while (sb.length() < PAN_LENGTH - 1) {
            sb.append(RANDOM.nextInt(10));
        }
        int check = LuhnValidator.checkDigit(sb.toString());
        sb.append(check);
        return sb.toString();
    }

    private Card require(String cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private CardView toView(Card card) {
        return new CardView(
                card.getCardId(),
                card.getCardholderName(),
                card.maskedPan(),
                card.getExpiry(),
                card.getStatus(),
            card.getDailyLimit(),
            card.getSpentToday(LocalDate.now()),
            card.getAvailableDailyLimit(LocalDate.now()));
    }

    public static class CardNotFoundException extends RuntimeException {
        public CardNotFoundException(String cardId) {
            super("Card not found: " + cardId);
        }
    }
}

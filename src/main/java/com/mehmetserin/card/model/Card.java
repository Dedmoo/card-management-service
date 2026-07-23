package com.mehmetserin.card.model;

import com.mehmetserin.card.model.CardModels.CardStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Card {

    @Id
    private String cardId;
    private String cardholderName;
    private String pan;
    private String expiry;

    @Enumerated(EnumType.STRING)
    private CardStatus status;
    private BigDecimal dailyLimit;
    private BigDecimal spentToday;
    private LocalDate spendingDate;

    @Version
    private Long version;

    protected Card() {
        // required by JPA
    }

    public Card(String cardId, String cardholderName, String pan, String expiry,
                CardStatus status, BigDecimal dailyLimit) {
        this.cardId = cardId;
        this.cardholderName = cardholderName;
        this.pan = pan;
        this.expiry = expiry;
        this.status = status;
        this.dailyLimit = dailyLimit;
        this.spentToday = BigDecimal.ZERO;
        this.spendingDate = LocalDate.now();
    }

    public String getCardId() {
        return cardId;
    }

    public String getCardholderName() {
        return cardholderName;
    }

    public String getPan() {
        return pan;
    }

    public String getExpiry() {
        return expiry;
    }

    public CardStatus getStatus() {
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal authorize(BigDecimal amount, LocalDate today) {
        if (status == CardStatus.BLOCKED) {
            throw new CardAuthorizationException("Blocked cards cannot be authorized.");
        }
        resetSpendIfNewDay(today);
        BigDecimal available = dailyLimit.subtract(spentToday);
        if (amount.compareTo(available) > 0) {
            throw new CardAuthorizationException("Authorization exceeds the remaining daily limit.");
        }
        spentToday = spentToday.add(amount);
        return dailyLimit.subtract(spentToday);
    }

    public BigDecimal getSpentToday(LocalDate today) {
        resetSpendIfNewDay(today);
        return spentToday;
    }

    public BigDecimal getAvailableDailyLimit(LocalDate today) {
        return dailyLimit.subtract(getSpentToday(today));
    }

    private void resetSpendIfNewDay(LocalDate today) {
        if (spentToday == null || spendingDate == null || !today.equals(spendingDate)) {
            spentToday = BigDecimal.ZERO;
            spendingDate = today;
        }
    }

    public String maskedPan() {
        String last4 = pan.substring(pan.length() - 4);
        return "**** **** **** " + last4;
    }

    public static class CardAuthorizationException extends RuntimeException {
        public CardAuthorizationException(String message) {
            super(message);
        }
    }
}

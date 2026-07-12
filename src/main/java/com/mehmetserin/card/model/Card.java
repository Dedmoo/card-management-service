package com.mehmetserin.card.model;

import com.mehmetserin.card.model.CardModels.CardStatus;

import java.math.BigDecimal;

public class Card {

    private final String cardId;
    private final String cardholderName;
    private final String pan;
    private final String expiry;
    private CardStatus status;
    private BigDecimal dailyLimit;

    public Card(String cardId, String cardholderName, String pan, String expiry,
                CardStatus status, BigDecimal dailyLimit) {
        this.cardId = cardId;
        this.cardholderName = cardholderName;
        this.pan = pan;
        this.expiry = expiry;
        this.status = status;
        this.dailyLimit = dailyLimit;
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

    public String maskedPan() {
        String last4 = pan.substring(pan.length() - 4);
        return "**** **** **** " + last4;
    }
}

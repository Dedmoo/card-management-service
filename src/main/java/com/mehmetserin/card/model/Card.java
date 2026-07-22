package com.mehmetserin.card.model;

import com.mehmetserin.card.model.CardModels.CardStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.math.BigDecimal;

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

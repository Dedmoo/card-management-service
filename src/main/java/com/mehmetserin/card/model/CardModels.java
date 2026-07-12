package com.mehmetserin.card.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CardModels {

    public enum CardStatus {
        ACTIVE,
        BLOCKED
    }

    public record IssueCardRequest(
            @NotBlank String cardholderName,
            @Positive BigDecimal dailyLimit) {
    }

    public record CardView(
            String cardId,
            String cardholderName,
            String maskedPan,
            String expiry,
            CardStatus status,
            BigDecimal dailyLimit) {
    }

    public record ValidatePanRequest(@NotBlank String pan) {
    }

    public record UpdateLimitRequest(@Positive BigDecimal dailyLimit) {
    }
}

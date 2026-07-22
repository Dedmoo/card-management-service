package com.mehmetserin.card;

import com.mehmetserin.card.model.CardModels.CardStatus;
import com.mehmetserin.card.model.CardModels.CardView;
import com.mehmetserin.card.service.CardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves that an issued card survives a process restart: the first context issues a card
 * into an H2 file database and is closed (simulating shutdown), then a brand-new context is
 * opened against the same file and must still find the card.
 */
class CardPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void card_survivesRestartAgainstSameH2File() {
        String jdbcUrl = "jdbc:h2:file:" + tempDir.resolve("card-persistence-test") + ";AUTO_SERVER=TRUE";

        String cardId;
        try (ConfigurableApplicationContext firstRun = buildContext(jdbcUrl)) {
            CardView issued = firstRun.getBean(CardService.class)
                    .issue("Restart Cardholder", new BigDecimal("7500"));
            cardId = issued.cardId();
        }

        try (ConfigurableApplicationContext secondRun = buildContext(jdbcUrl)) {
            CardView reloaded = secondRun.getBean(CardService.class).get(cardId);
            assertEquals(cardId, reloaded.cardId());
            assertEquals(CardStatus.ACTIVE, reloaded.status());
            assertTrue(new BigDecimal("7500").compareTo(reloaded.dailyLimit()) == 0);
        }
    }

    private ConfigurableApplicationContext buildContext(String jdbcUrl) {
        return new SpringApplicationBuilder(CardManagementApplication.class)
                .web(WebApplicationType.NONE)
                .properties(
                        "spring.datasource.url=" + jdbcUrl,
                        "spring.jpa.hibernate.ddl-auto=update")
                .run();
    }
}

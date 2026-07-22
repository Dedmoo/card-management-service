package com.mehmetserin.card.repository;

import com.mehmetserin.card.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, String> {
}

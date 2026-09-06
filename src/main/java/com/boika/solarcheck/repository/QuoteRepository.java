package com.boika.solarcheck.repository;

import com.boika.solarcheck.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findAllByOrderByCreatedAtDesc();

    List<Quote> findByContactedFalseOrderByCreatedAtDesc();

    long countByContactedFalse();
}
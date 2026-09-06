package com.boika.solarcheck.repository;

import com.boika.solarcheck.model.Review;
import com.boika.solarcheck.model.ReviewStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByInstallerIdAndStatusOrderByCreatedAtDesc(Long installerId, ReviewStatus status);

    @EntityGraph(attributePaths = "installer")
    List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status);

    long countByInstallerIdAndStatus(Long installerId, ReviewStatus status);
}
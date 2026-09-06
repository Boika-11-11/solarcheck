package com.boika.solarcheck.repository;

import com.boika.solarcheck.model.Review;
import com.boika.solarcheck.model.ReviewStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByInstallerIdAndStatusOrderByCreatedAtDesc(Long installerId, ReviewStatus status);

    @EntityGraph(attributePaths = "installer")
    List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status);

    @EntityGraph(attributePaths = "installer")
    List<Review> findAllByOrderByCreatedAtDesc();

    long countByInstallerIdAndStatus(Long installerId, ReviewStatus status);

    @Query("""
           SELECT r.installer.id, AVG(r.rating), COUNT(r)
           FROM Review r
           WHERE r.status = :status
           GROUP BY r.installer.id
           """)
    List<Object[]> findRatingSummaries(@Param("status") ReviewStatus status);

    @Query("""
           SELECT r.rating, COUNT(r)
           FROM Review r
           WHERE r.installer.id = :installerId AND r.status = :status
           GROUP BY r.rating
           """)
    List<Object[]> findRatingBreakdown(@Param("installerId") Long installerId,
                                       @Param("status") ReviewStatus status);
}
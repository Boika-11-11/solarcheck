package com.boika.solarcheck.controller;

import com.boika.solarcheck.model.Review;
import com.boika.solarcheck.model.ReviewStatus;
import com.boika.solarcheck.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class AdminController {

    private final ReviewRepository reviewRepository;

    public AdminController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/admin/reviews")
    public String pendingReviews(Model model) {

        List<Review> pending = reviewRepository
                .findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING);

        model.addAttribute("reviews", pending);
        return "admin-reviews";
    }

    @PostMapping("/admin/reviews/{id}/approve")
    public String approve(@PathVariable Long id) {
        updateStatus(id, ReviewStatus.PUBLISHED);
        return "redirect:/admin/reviews";
    }

    @PostMapping("/admin/reviews/{id}/reject")
    public String reject(@PathVariable Long id) {
        updateStatus(id, ReviewStatus.REJECTED);
        return "redirect:/admin/reviews";
    }

    private void updateStatus(Long id, ReviewStatus status) {

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Review not found"));

        review.setStatus(status);
        reviewRepository.save(review);
    }
}
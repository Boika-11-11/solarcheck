package com.boika.solarcheck.controller;

import com.boika.solarcheck.model.Installer;
import com.boika.solarcheck.model.Review;
import com.boika.solarcheck.repository.InstallerRepository;
import com.boika.solarcheck.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ReviewController {

    private final InstallerRepository installerRepository;
    private final ReviewRepository reviewRepository;

    public ReviewController(InstallerRepository installerRepository,
                            ReviewRepository reviewRepository) {
        this.installerRepository = installerRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/installer/{id}/review")
    public String showForm(@PathVariable Long id, Model model) {

        Installer installer = installerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Installer not found"));

        model.addAttribute("installer", installer);
        return "review-form";
    }

    @PostMapping("/installer/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @RequestParam String authorName,
                               @RequestParam String authorEmail,
                               @RequestParam int rating,
                               @RequestParam String title,
                               @RequestParam String body,
                               Model model) {

        Installer installer = installerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Installer not found"));

        if (rating < 1 || rating > 5) {
            model.addAttribute("installer", installer);
            model.addAttribute("error", "Rating must be between 1 and 5.");
            return "review-form";
        }

        Review review = new Review();
        review.setInstaller(installer);
        review.setAuthorName(authorName.trim());
        review.setAuthorEmail(authorEmail.trim());
        review.setRating(rating);
        review.setTitle(title.trim());
        review.setBody(body.trim());

        reviewRepository.save(review);

        model.addAttribute("installer", installer);
        return "review-thanks";
    }
}
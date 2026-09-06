package com.boika.solarcheck.controller;

import com.boika.solarcheck.model.Installer;
import com.boika.solarcheck.model.Review;
import com.boika.solarcheck.model.ReviewStatus;
import com.boika.solarcheck.repository.InstallerRepository;
import com.boika.solarcheck.repository.ReviewRepository;
import com.boika.solarcheck.view.InstallerCard;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class InstallerController {

    private final InstallerRepository installerRepository;
    private final ReviewRepository reviewRepository;

    public InstallerController(InstallerRepository installerRepository,
                               ReviewRepository reviewRepository) {
        this.installerRepository = installerRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String city, Model model) {

        List<Installer> installers;

        if (city != null && !city.isBlank()) {
            installers = installerRepository.findByCityIgnoreCaseOrderByNameAsc(city);
        } else {
            installers = installerRepository.findAllByOrderByNameAsc();
        }

        Map<Long, double[]> summaries = new HashMap<>();

        for (Object[] row : reviewRepository.findRatingSummaries(ReviewStatus.PUBLISHED)) {
            Long installerId = (Long) row[0];
            double avg = ((Number) row[1]).doubleValue();
            long count = ((Number) row[2]).longValue();
            summaries.put(installerId, new double[]{avg, count});
        }

        List<InstallerCard> cards = new ArrayList<>();

        for (Installer installer : installers) {
            double[] summary = summaries.get(installer.getId());

            if (summary == null) {
                cards.add(new InstallerCard(installer, 0.0, 0));
            } else {
                double rounded = Math.round(summary[0] * 10) / 10.0;
                cards.add(new InstallerCard(installer, rounded, (long) summary[1]));
            }
        }

        model.addAttribute("cards", cards);
        model.addAttribute("city", city);

        return "installers";
    }

    @GetMapping("/installer/{id}")
    public String installerDetail(@PathVariable Long id, Model model) {

        Installer installer = installerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Installer not found"));

        List<Review> reviews = reviewRepository
                .findByInstallerIdAndStatusOrderByCreatedAtDesc(id, ReviewStatus.PUBLISHED);

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        model.addAttribute("installer", installer);
        model.addAttribute("reviews", reviews);
        model.addAttribute("average", Math.round(average * 10) / 10.0);
        model.addAttribute("reviewCount", reviews.size());

        return "installer-detail";
    }
}
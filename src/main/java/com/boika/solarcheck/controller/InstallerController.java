package com.boika.solarcheck.controller;

import com.boika.solarcheck.model.Installer;
import com.boika.solarcheck.model.Review;
import com.boika.solarcheck.model.ReviewStatus;
import com.boika.solarcheck.repository.InstallerRepository;
import com.boika.solarcheck.repository.ReviewRepository;
import com.boika.solarcheck.view.InstallerCard;
import com.boika.solarcheck.view.RatingBar;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
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
    public String home(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String province,
                       @RequestParam(required = false) String sort,
                       Model model) {

        String cleanSearch = blankToNull(search);
        String cleanProvince = blankToNull(province);

        List<Installer> installers = installerRepository.search(cleanSearch, cleanProvince);

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

        if ("rating".equals(sort)) {
            cards.sort(Comparator
                    .comparingDouble(InstallerCard::average).reversed()
                    .thenComparing(c -> c.installer().getName()));
        } else if ("reviews".equals(sort)) {
            cards.sort(Comparator
                    .comparingLong(InstallerCard::reviewCount).reversed()
                    .thenComparing(c -> c.installer().getName()));
        }

        model.addAttribute("cards", cards);
        model.addAttribute("search", cleanSearch);
        model.addAttribute("province", cleanProvince);
        model.addAttribute("sort", sort);
        model.addAttribute("provinces", installerRepository.findAllProvinces());

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

        Map<Integer, Long> counts = new HashMap<>();
        for (Object[] row : reviewRepository.findRatingBreakdown(id, ReviewStatus.PUBLISHED)) {
            counts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        long total = reviews.size();
        List<RatingBar> bars = new ArrayList<>();

        for (int stars = 5; stars >= 1; stars--) {
            long count = counts.getOrDefault(stars, 0L);
            int percent = (total == 0) ? 0 : (int) Math.round((count * 100.0) / total);
            bars.add(new RatingBar(stars, count, percent));
        }

        long recommendPercent = 0;
        if (total > 0) {
            long good = counts.getOrDefault(5, 0L) + counts.getOrDefault(4, 0L);
            recommendPercent = Math.round((good * 100.0) / total);
        }

        model.addAttribute("installer", installer);
        model.addAttribute("reviews", reviews);
        model.addAttribute("average", Math.round(average * 10) / 10.0);
        model.addAttribute("reviewCount", total);
        model.addAttribute("bars", bars);
        model.addAttribute("recommendPercent", recommendPercent);

        return "installer-detail";
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
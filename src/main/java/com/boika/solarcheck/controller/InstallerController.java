package com.boika.solarcheck.controller;

import com.boika.solarcheck.model.Installer;
import com.boika.solarcheck.model.Review;
import com.boika.solarcheck.model.ReviewStatus;
import com.boika.solarcheck.repository.InstallerRepository;
import com.boika.solarcheck.repository.ReviewRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

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

        model.addAttribute("installers", installers);
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
package com.boika.solarcheck.controller;

import com.boika.solarcheck.model.Installer;
import com.boika.solarcheck.model.Quote;
import com.boika.solarcheck.model.Review;
import com.boika.solarcheck.model.ReviewStatus;
import com.boika.solarcheck.repository.InstallerRepository;
import com.boika.solarcheck.repository.QuoteRepository;
import com.boika.solarcheck.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AdminController {

    private final ReviewRepository reviewRepository;
    private final InstallerRepository installerRepository;
    private final QuoteRepository quoteRepository;

    public AdminController(ReviewRepository reviewRepository,
                           InstallerRepository installerRepository,
                           QuoteRepository quoteRepository) {
        this.reviewRepository = reviewRepository;
        this.installerRepository = installerRepository;
        this.quoteRepository = quoteRepository;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("pendingCount",
                reviewRepository.findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING).size());
        model.addAttribute("installerCount", installerRepository.count());
        model.addAttribute("newLeadCount", quoteRepository.countByContactedFalse());
        return "admin-home";
    }

    @GetMapping("/admin/leads")
    public String leads(Model model) {
        List<Quote> quotes = quoteRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("quotes", quotes);
        model.addAttribute("newCount", quoteRepository.countByContactedFalse());
        return "admin-leads";
    }

    @PostMapping("/admin/leads/{id}/toggle-contacted")
    public String toggleContacted(@PathVariable Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lead not found"));
        quote.setContacted(!quote.isContacted());
        quoteRepository.save(quote);
        return "redirect:/admin/leads";
    }

    @GetMapping("/admin/reviews")
    public String pendingReviews(Model model) {
        List<Review> pending = reviewRepository
                .findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING);
        model.addAttribute("reviews", pending);
        return "admin-reviews";
    }

    @GetMapping("/admin/reviews/all")
    public String allReviews(Model model) {
        model.addAttribute("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());
        return "admin-reviews-all";
    }

    @PostMapping("/admin/reviews/{id}/reply")
    public String saveReply(@PathVariable Long id, @RequestParam String reply) {

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Review not found"));

        String clean = reply == null ? "" : reply.trim();

        if (clean.isEmpty()) {
            review.setReply(null);
            review.setReplyAt(null);
        } else {
            review.setReply(clean);
            review.setReplyAt(LocalDateTime.now());
        }

        reviewRepository.save(review);
        return "redirect:/admin/reviews/all";
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

    @GetMapping("/admin/installers")
    public String listInstallers(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("installers", installerRepository.findAllByOrderByNameAsc());
        if ("hasreviews".equals(error)) {
            model.addAttribute("error", "That installer has reviews and cannot be deleted.");
        }
        return "admin-installers";
    }

    @PostMapping("/admin/installers")
    public String addInstaller(@RequestParam String name,
                               @RequestParam String city,
                               @RequestParam(required = false) String province,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String website,
                               @RequestParam(required = false) String verified,
                               Model model) {

        String cleanName = name.trim();
        String cleanCity = city.trim();

        if (cleanName.isEmpty() || cleanCity.isEmpty()) {
            model.addAttribute("error", "Name and city are required.");
            model.addAttribute("installers", installerRepository.findAllByOrderByNameAsc());
            return "admin-installers";
        }

        if (installerRepository.existsByNameIgnoreCaseAndCityIgnoreCase(cleanName, cleanCity)) {
            model.addAttribute("error", cleanName + " is already listed in " + cleanCity + ".");
            model.addAttribute("installers", installerRepository.findAllByOrderByNameAsc());
            return "admin-installers";
        }

        Installer installer = new Installer();
        installer.setName(cleanName);
        installer.setCity(cleanCity);
        installer.setProvince(blankToNull(province));
        installer.setPhone(blankToNull(phone));
        installer.setWebsite(blankToNull(website));
        installer.setVerified(verified != null);

        installerRepository.save(installer);

        return "redirect:/admin/installers";
    }

    @PostMapping("/admin/installers/{id}/toggle-verified")
    public String toggleVerified(@PathVariable Long id) {
        Installer installer = installerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Installer not found"));
        installer.setVerified(!installer.isVerified());
        installerRepository.save(installer);
        return "redirect:/admin/installers";
    }

    @GetMapping("/admin/installers/{id}/edit")
    public String editInstallerForm(@PathVariable Long id, Model model) {

        Installer installer = installerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Installer not found"));

        model.addAttribute("installer", installer);
        return "admin-installer-edit";
    }

    @PostMapping("/admin/installers/{id}/edit")
    public String updateInstaller(@PathVariable Long id,
                                  @RequestParam String name,
                                  @RequestParam String city,
                                  @RequestParam(required = false) String province,
                                  @RequestParam(required = false) String phone,
                                  @RequestParam(required = false) String website,
                                  @RequestParam(required = false) String verified,
                                  Model model) {

        Installer installer = installerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Installer not found"));

        String cleanName = name.trim();
        String cleanCity = city.trim();

        if (cleanName.isEmpty() || cleanCity.isEmpty()) {
            model.addAttribute("installer", installer);
            model.addAttribute("error", "Name and city are required.");
            return "admin-installer-edit";
        }

        installer.setName(cleanName);
        installer.setCity(cleanCity);
        installer.setProvince(blankToNull(province));
        installer.setPhone(blankToNull(phone));
        installer.setWebsite(blankToNull(website));
        installer.setVerified(verified != null);

        installerRepository.save(installer);

        return "redirect:/admin/installers";
    }

    @PostMapping("/admin/installers/{id}/delete")
    public String deleteInstaller(@PathVariable Long id) {

        Installer installer = installerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Installer not found"));

        long reviewCount = reviewRepository.countByInstallerIdAndStatus(id, ReviewStatus.PUBLISHED)
                + reviewRepository.countByInstallerIdAndStatus(id, ReviewStatus.PENDING)
                + reviewRepository.countByInstallerIdAndStatus(id, ReviewStatus.REJECTED);

        if (reviewCount > 0) {
            return "redirect:/admin/installers?error=hasreviews";
        }

        installerRepository.delete(installer);
        return "redirect:/admin/installers";
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
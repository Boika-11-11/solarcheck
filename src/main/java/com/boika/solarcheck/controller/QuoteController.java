package com.boika.solarcheck.controller;

import com.boika.solarcheck.model.Quote;
import com.boika.solarcheck.repository.InstallerRepository;
import com.boika.solarcheck.repository.QuoteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QuoteController {

    private final QuoteRepository quoteRepository;
    private final InstallerRepository installerRepository;

    public QuoteController(QuoteRepository quoteRepository,
                           InstallerRepository installerRepository) {
        this.quoteRepository = quoteRepository;
        this.installerRepository = installerRepository;
    }

    @GetMapping("/quotes")
    public String quoteForm(Model model) {
        model.addAttribute("provinces", installerRepository.findAllProvinces());
        return "quote-form";
    }

    @PostMapping("/quotes")
    public String submitQuote(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String phone,
                              @RequestParam String city,
                              @RequestParam(required = false) String province,
                              @RequestParam String propertyType,
                              @RequestParam String budget,
                              @RequestParam(required = false) String notes,
                              @RequestParam(required = false) String consent,
                              Model model) {

        if (consent == null) {
            model.addAttribute("provinces", installerRepository.findAllProvinces());
            model.addAttribute("error", "Please tick the consent box so we may share your details with installers.");
            return "quote-form";
        }

        Quote quote = new Quote();
        quote.setName(name.trim());
        quote.setEmail(email.trim());
        quote.setPhone(phone.trim());
        quote.setCity(city.trim());
        quote.setProvince(blankToNull(province));
        quote.setPropertyType(propertyType);
        quote.setBudget(budget);
        quote.setNotes(blankToNull(notes));

        quoteRepository.save(quote);

        return "quote-thanks";
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
package com.boika.solarcheck.view;

import com.boika.solarcheck.model.Installer;

public record InstallerCard(
        Installer installer,
        double average,
        long reviewCount
) {
    public boolean hasReviews() {
        return reviewCount > 0;
    }
}
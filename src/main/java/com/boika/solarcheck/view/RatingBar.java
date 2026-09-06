package com.boika.solarcheck.view;

public record RatingBar(
        int stars,
        long count,
        int percent
) {
}
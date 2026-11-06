package com.aurafit.outfit.application;

public record OutfitFilter(
    String favorite,
    String season,
    String occasion,
    String freeText,
    String sortBy,
    String sortDirection) {}

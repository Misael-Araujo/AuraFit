package com.aurafit.wardrobe.application;

public record GarmentFilter(
    String source,
    String status,
    String category,
    String colorFamily,
    String season,
    String occasion,
    String freeText,
    String sortBy,
    String sortDirection) {}

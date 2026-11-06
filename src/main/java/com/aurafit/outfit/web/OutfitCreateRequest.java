package com.aurafit.outfit.web;

import com.aurafit.outfit.domain.OutfitItem;
import java.util.List;

public record OutfitCreateRequest(String name, List<OutfitItem> items, Boolean favorite) {}

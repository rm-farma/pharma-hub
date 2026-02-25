package com.rmfarma.pharmahub.core.model;

import java.util.List;

public record UnpagedResult<T>(List<T> items, int returnedItems, boolean truncated) {
}


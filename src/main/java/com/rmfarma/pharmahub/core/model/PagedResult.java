package com.rmfarma.pharmahub.core.model;

import java.util.List;

public record PagedResult<T>(List<T> items, int page, int pageSize, boolean hasNext) {
}


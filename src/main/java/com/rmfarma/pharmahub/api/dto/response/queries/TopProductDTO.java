package com.rmfarma.pharmahub.api.dto.response.queries;

import java.math.BigDecimal;

public record TopProductDTO(String productName, BigDecimal totalQuantity, BigDecimal totalAmount) {
}


package com.rmfarma.pharmahub.core.exception;

public class MaxRowsExceededException extends RuntimeException {

    public MaxRowsExceededException(String queryKey, int maxRows) {
        super("Número máximo de linhas (%d) excedido para a query: %s".formatted(maxRows, queryKey));
    }
}


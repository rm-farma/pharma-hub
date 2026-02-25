package com.rmfarma.pharmahub.core.exception;

public class UnpagedNotAllowedException extends RuntimeException {

    public UnpagedNotAllowedException(String queryKey) {
        super("Modo unpaged não é permitido para a query: " + queryKey);
    }
}


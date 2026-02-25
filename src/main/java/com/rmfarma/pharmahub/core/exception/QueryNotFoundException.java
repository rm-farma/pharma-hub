package com.rmfarma.pharmahub.core.exception;

public class QueryNotFoundException extends RuntimeException {

    public QueryNotFoundException(String queryKey) {
        super("Query não encontrada: " + queryKey);
    }
}


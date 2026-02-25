package com.rmfarma.pharmahub.api.exception;

import com.rmfarma.pharmahub.api.dto.response.ErrorResponse;
import com.rmfarma.pharmahub.core.exception.MaxRowsExceededException;
import com.rmfarma.pharmahub.core.exception.ParamValidationException;
import com.rmfarma.pharmahub.core.exception.QueryNotFoundException;
import com.rmfarma.pharmahub.core.exception.UnpagedNotAllowedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.UUID;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        String requestId = UUID.randomUUID().toString();

        if (exception instanceof QueryNotFoundException e) {
            return buildResponse(Response.Status.NOT_FOUND, "NOT_FOUND", e.getMessage(), null, requestId);
        }

        if (exception instanceof ParamValidationException e) {
            return buildResponse(Response.Status.fromStatusCode(422), "VALIDATION_ERROR", e.getMessage(), null, requestId);
        }

        if (exception instanceof UnpagedNotAllowedException e) {
            return buildResponse(Response.Status.FORBIDDEN, "UNPAGED_NOT_ALLOWED", e.getMessage(), null, requestId);
        }

        if (exception instanceof MaxRowsExceededException e) {
            return buildResponse(Response.Status.fromStatusCode(422), "MAX_ROWS_EXCEEDED", e.getMessage(), null, requestId);
        }

        LOG.errorv(exception, "Erro interno não tratado [requestId={0}]", requestId);
        return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Erro interno do servidor", exception.getMessage(), requestId);
    }

    private Response buildResponse(Response.StatusType status, String error, String message,
                                    String details, String requestId) {
        ErrorResponse body = new ErrorResponse(error, message, details, requestId);
        return Response.status(status)
                .entity(body)
                .build();
    }
}


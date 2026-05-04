package com.lewisrp.basemessages.backend.application.exception;

import lombok.Getter;

/**
 * Exception thrown when a template submission to Meta fails.
 * Carries the Meta API error response so it can be propagated to API consumers.
 */
@Getter
public class MetaSubmissionException extends RuntimeException {

    private final String metaErrorJson;

    public MetaSubmissionException(String message, String metaErrorJson) {
        super(message);
        this.metaErrorJson = metaErrorJson;
    }
}

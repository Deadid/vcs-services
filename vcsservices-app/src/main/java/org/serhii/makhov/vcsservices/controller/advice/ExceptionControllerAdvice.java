package org.serhii.makhov.vcsservices.controller.advice;

import org.serhii.makhov.vcsservices.client.github.exception.GithubClientException;
import org.serhii.makhov.vcsservices.dto.exception.ProblemDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionControllerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    @ExceptionHandler(GithubClientException.class)
    public ResponseEntity<ProblemDetail> githubClientException(GithubClientException githubClientException) {
        String formattedError = String.format("Exception during GitHub api call, status %s, failing URI is %s", githubClientException.getStatusCode(), githubClientException.getRequestedUri());
        LOGGER.error(formattedError, githubClientException);
        return ResponseEntity.status(githubClientException.getStatusCode())
            .body(new ProblemDetail(
                String.valueOf(githubClientException.getStatusCode().value()),
                formattedError
            ));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ProblemDetail> httpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException httpMediaTypeNotAcceptableException) {
        LOGGER.info("Media type not acceptable", httpMediaTypeNotAcceptableException);
        return ResponseEntity.status(httpMediaTypeNotAcceptableException.getStatusCode())
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ProblemDetail(
                String.valueOf(HttpStatus.NOT_ACCEPTABLE.value()),
                HttpStatus.NOT_ACCEPTABLE.getReasonPhrase()
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> defaultExceptionHandler(Exception exception) {
        LOGGER.info("Unexpected exception thrown", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ProblemDetail(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()
            ));
    }
}

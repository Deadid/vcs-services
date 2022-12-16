package org.serhii.makhov.vcsservices.client.github.exception;


import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Exception which is thrown when Github API fails to respond
 */
public class GithubClientException extends HttpStatusCodeException {

    private final String requestedUri;

    public GithubClientException(HttpStatusCode statusCode, String requestedUri) {
        super(statusCode);
        this.requestedUri = requestedUri;
    }

    public String getRequestedUri() {
        return requestedUri;
    }
}

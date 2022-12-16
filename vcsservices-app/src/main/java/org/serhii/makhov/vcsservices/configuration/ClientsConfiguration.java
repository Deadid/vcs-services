package org.serhii.makhov.vcsservices.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientsConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientsConfiguration.class);

    @Value("${config.clients.github.url:https://api.github.com}")
    private String githubUrl;

    @Value("${config.clients.github.authToken:#{null}}" )
    private String githubAuthenticationToken;

    @Bean
    public WebClient githubWebClient() {
        WebClient.Builder builder = WebClient.builder()
            .baseUrl(githubUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if(githubAuthenticationToken != null) {
            builder
                .defaultHeaders(h -> h.setBearerAuth(githubAuthenticationToken));
        } else {
            LOGGER.warn("No github authorization token provided. API rate limited to 60 requests per hour");
        }
        return builder.build();
    }
}

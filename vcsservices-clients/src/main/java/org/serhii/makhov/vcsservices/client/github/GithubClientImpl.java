package org.serhii.makhov.vcsservices.client.github;

import org.reactivestreams.Publisher;
import org.serhii.makhov.vcsservices.client.github.exception.GithubClientException;
import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Repository;
import org.serhii.makhov.vcsservices.client.github.util.PagedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class GithubClientImpl implements GithubClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubClientImpl.class);
    static final String GET_USER_REPOSITORY_BRANCHES_URL = "/repos/{userLogin}/{repositoryName}/branches?page={page}&per_page={per_page}";
    static final String GET_USER_REPOSITORIES_URI = "/users/{userLogin}/repos?page={page}&per_page={per_page}";
    static final Integer MAX_PER_PAGE = 100;
    private final WebClient webClient;

    public GithubClientImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Flux<Repository> fetchUserRepositories(String userLogin) {
        return fetchUserRepositoriesPaged(userLogin, 1)
            .expand(entitiesPaginator((page) -> fetchUserRepositoriesPaged(userLogin, page)))
            .flatMap(pagedRepositories -> Flux.fromIterable(pagedRepositories.entities()));
    }


    @Override
    public Flux<Branch> fetchRepositoryBranches(String userLogin, String repositoryName) {
        return fetchBranchesPaged(userLogin, repositoryName, 1)
            .expand(entitiesPaginator((page) -> fetchBranchesPaged(userLogin, repositoryName, page)))
            .flatMap(pagedRepositories -> Flux.fromIterable(pagedRepositories.entities()));
    }

    private <T> Function<PagedResponse<T>, Publisher<? extends PagedResponse<T>>> entitiesPaginator(Function<Integer, Publisher<? extends PagedResponse<T>>> entitiesProducer) {
        return pagedResponse -> {
            if (pagedResponse.entities().isEmpty()) {
                return Mono.empty();
            }
            return entitiesProducer.apply( pagedResponse.pageNumber() + 1);
        };
    }

    private Mono<PagedResponse<Branch>> fetchBranchesPaged(String userLogin, String repositoryName, int page) {
        Map<String, Object> uriVariables = Map.of("userLogin", userLogin,
            "repositoryName", repositoryName,
            "page", page,
            "per_page", MAX_PER_PAGE
        );
        return executeGetRequest(GET_USER_REPOSITORY_BRANCHES_URL, uriVariables)
            .bodyToMono(new ParameterizedTypeReference<List<Branch>>() {})
            .map(entities -> new PagedResponse<>(entities, page));
    }


    private Mono<PagedResponse<Repository>> fetchUserRepositoriesPaged(String userLogin, int page) {
        Map<String, Object> uriVariables = Map.of("userLogin", userLogin,
            "page", page,
            "per_page", MAX_PER_PAGE
        );
        return executeGetRequest(GET_USER_REPOSITORIES_URI, uriVariables)
            .bodyToMono(new ParameterizedTypeReference<List<Repository>>() {})
            .map(entities -> new PagedResponse<>(entities, page));
    }

    private WebClient.ResponseSpec executeGetRequest(String uri, Map<String, Object> uriVariables) {
        URI composedUri = UriComponentsBuilder.fromUriString(uri).build(uriVariables);
        LOGGER.info("Enqueueing request uri={}", composedUri);
        return webClient.get()
            .uri(uri, uriVariables)
            .retrieve()
            .onStatus(Predicate.not(HttpStatusCode::is2xxSuccessful), (clientResponse -> Mono.error(new GithubClientException(clientResponse.statusCode(), composedUri.getPath()))));
    }
}

package org.serhii.makhov.vcsservices.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.serhii.makhov.vcsservices.BaseIntegrationTest;
import org.serhii.makhov.vcsservices.client.github.exception.GithubClientException;
import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Commit;
import org.serhii.makhov.vcsservices.client.github.resource.Owner;
import org.serhii.makhov.vcsservices.client.github.resource.Repository;
import org.serhii.makhov.vcsservices.dto.RepositoryResource;
import org.serhii.makhov.vcsservices.dto.exception.ProblemDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GithubRepositoryControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturn406OnNonAcceptableMediaType() {
        webTestClient.get()
            .uri("/github/repos/test")
            .accept(MediaType.APPLICATION_XML)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)

            .expectBody(ProblemDetail.class)
                .value(ProblemDetail::status, Matchers.equalTo(String.valueOf(HttpStatus.NOT_ACCEPTABLE.value())))
                .value(ProblemDetail::Message, Matchers.equalTo(HttpStatus.NOT_ACCEPTABLE.getReasonPhrase()));
    }

    @Test
    void shouldReturnRespectiveStatusCodeOnGithubClientException() {
        HttpStatus failingStatusCode = HttpStatus.NOT_FOUND;
        when(githubClient.fetchUserRepositories(any()))
            .thenThrow(new GithubClientException(failingStatusCode, "/test"));
        webTestClient.get()
            .uri("/github/repos/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(failingStatusCode)
            .expectBody(ProblemDetail.class)
            .value(ProblemDetail::status, Matchers.equalTo(String.valueOf(failingStatusCode.value())))
            .value(ProblemDetail::Message, Matchers.stringContainsInOrder(failingStatusCode.toString(), "/test"));
    }

    @Test
    void shouldReturn500OnUnknownException() {
        when(githubClient.fetchUserRepositories(any()))
            .thenThrow(new RuntimeException("Some exception"));
        webTestClient.get()
            .uri("/github/repos/test")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody(ProblemDetail.class)
            .value(ProblemDetail::status, Matchers.equalTo(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())))
            .value(ProblemDetail::Message, Matchers.equalTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }

    @Test
    void shouldReturnUserRepositories() {
        String repositoryName = "test-repository";
        String ownerLogin = "testOwner";
        String branchName = "master";
        String commitSha = "testSha";
        when(githubClient.fetchUserRepositories(ownerLogin))
            .thenReturn(Flux.just(new Repository(repositoryName, new Owner(ownerLogin), false)));
        when(githubClient.fetchRepositoryBranches(ownerLogin, repositoryName))
            .thenReturn(Flux.just(new Branch(branchName, new Commit(commitSha))));

        EntityExchangeResult<List<RepositoryResource>> repositoryResourcesResult = webTestClient.get()
            .uri("/github/repos/{login}", Map.of("login", ownerLogin))
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(new ParameterizedTypeReference<List<RepositoryResource>>() {
            })
            .value(repositoryResources -> repositoryResources, Matchers.hasSize(1))
            .returnResult();
        RepositoryResource repositoryResource = repositoryResourcesResult.getResponseBody().get(0);
        assertThat(repositoryResource.repositoryName()).isEqualTo(repositoryName);
        assertThat(repositoryResource.ownerLogin()).isEqualTo(ownerLogin);
        assertThat(repositoryResource.branches().size()).isEqualTo(1);
        assertThat(repositoryResource.branches().get(0).name()).isEqualTo(branchName);
        assertThat(repositoryResource.branches().get(0).lastCommitSha()).isEqualTo(commitSha);
    }

}
package org.serhii.makhov.vcsservices.client.github;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.serhii.makhov.vcsservices.client.github.exception.GithubClientException;
import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Commit;
import org.serhii.makhov.vcsservices.client.github.resource.Owner;
import org.serhii.makhov.vcsservices.client.github.resource.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GithubClientImplTest {
    private static MockWebServer mockWebServer;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private GithubClientImpl githubClient;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
        githubClient = new GithubClientImpl(webClient);
    }

    @Test
    void shouldRequestRepositories() throws JsonProcessingException, InterruptedException {
        String userLogin = "test";
        Repository firstRepository = new Repository("1st", new Owner("test_user"), false);
        Repository secondRepository = new Repository("2nd", new Owner("test_user"), false);
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(List.of(firstRepository, secondRepository)))
            .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(Collections.emptyList()))
            .addHeader("Content-Type", "application/json"));

        Flux<Repository> test = githubClient.fetchUserRepositories(userLogin);
        List<Repository> repositories = test.collectList().block();
        assertNotNull(repositories);
        assertEquals(repositories.size(), 2);
        assertEquals(repositories.get(0), firstRepository);
        assertEquals(repositories.get(1), secondRepository);
        assertEquals(mockWebServer.getRequestCount(), 2);

        assertRequestPath(1, mockWebServer.takeRequest(), GithubClientImpl.GET_USER_REPOSITORIES_URI,  Map.of("userLogin", userLogin));
    }

    @Test
    void shouldRequestBranches() throws JsonProcessingException, InterruptedException {
        String userLogin = "test";
        String repositoryName = "test_repository";
        Branch firstBranch = new Branch("test_branch_name", new Commit("testsha"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(List.of(firstBranch)))
            .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(Collections.emptyList()))
            .addHeader("Content-Type", "application/json"));

        Flux<Branch> test = githubClient.fetchRepositoryBranches(userLogin, repositoryName);
        List<Branch> branches = test.collectList().block();
        assertNotNull(branches);
        assertEquals(branches.size(), 1);
        assertEquals(branches.get(0), firstBranch);
        assertEquals(mockWebServer.getRequestCount(), 2);

        assertRequestPath(1, mockWebServer.takeRequest(), GithubClientImpl.GET_USER_REPOSITORY_BRANCHES_URL,
            Map.of("userLogin", userLogin,
                "repositoryName", repositoryName));
    }

    @Test
    void shouldRunThroughPagination() throws JsonProcessingException, InterruptedException {
        String userLogin = "test";
        Repository repositoryForFirstPage = new Repository("1st", new Owner("test_user"), false);
        Repository repositoryForSecondPage = new Repository("2nd", new Owner("test_user"), false);
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(List.of(repositoryForFirstPage)))
            .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(List.of(repositoryForSecondPage)))
            .addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(Collections.emptyList()))
            .addHeader("Content-Type", "application/json"));

        Flux<Repository> test = githubClient.fetchUserRepositories(userLogin);
        List<Repository> repositories = test.collectList().block();
        assertNotNull(repositories);
        assertEquals(repositories.size(), 2);
        assertEquals(repositories.get(0), repositoryForFirstPage);
        assertEquals(repositories.get(1), repositoryForSecondPage);
        assertEquals(mockWebServer.getRequestCount(), 3);

        assertRequestPath(1, mockWebServer.takeRequest(), GithubClientImpl.GET_USER_REPOSITORIES_URI,  Map.of("userLogin", userLogin));
        assertRequestPath(2, mockWebServer.takeRequest(), GithubClientImpl.GET_USER_REPOSITORIES_URI,  Map.of("userLogin", userLogin));
        assertRequestPath(3, mockWebServer.takeRequest(), GithubClientImpl.GET_USER_REPOSITORIES_URI,  Map.of("userLogin", userLogin));
    }

    @Test
    void shouldEmitExceptionIfNon2xxStatusCde() {
        String userLogin = "test";
        String repositoryName = "test_repository";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.NOT_FOUND.value()));

        Flux<Branch> test = githubClient.fetchRepositoryBranches(userLogin, repositoryName);
        try {
            test.collectList().block();
            fail();
        } catch (GithubClientException e) {
            assertEquals(mockWebServer.getRequestCount(), 1);
            assertEquals(e.getStatusCode(), HttpStatus.NOT_FOUND);
            assertEquals(e.getRequestedUri(), buildExpectedUriPath(GithubClientImpl.GET_USER_REPOSITORY_BRANCHES_URL,Map.of("userLogin", userLogin,
                "repositoryName", repositoryName,
                "page", 1,
                "per_page", GithubClientImpl.MAX_PER_PAGE)));
        }
    }



    private void assertRequestPath(int page, RecordedRequest recordedRequest, String expectedUri, Map<String, Object> uriVariables) {
        String actualPath = recordedRequest.getPath();
        Map<String, Object> mapWithAdditionalVariables = new HashMap<>(Map.of("page", page,
            "per_page", GithubClientImpl.MAX_PER_PAGE));
        mapWithAdditionalVariables.putAll(uriVariables);
        String expectedUriPathFormatted = buildExpectedUriPath(expectedUri, mapWithAdditionalVariables);
        assertNotNull(actualPath);
        assertTrue(actualPath.contains(expectedUriPathFormatted));
    }

    private String buildExpectedUriPath(String uriToFormat, Map<String, Object> uriVariables) {
        return UriComponentsBuilder.fromUriString(uriToFormat)
            .build(uriVariables).getPath();
    }

}
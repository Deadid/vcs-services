package org.serhii.makhov.vcsservices.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.serhii.makhov.vcsservices.client.github.GithubClient;
import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Commit;
import org.serhii.makhov.vcsservices.client.github.resource.Owner;
import org.serhii.makhov.vcsservices.client.github.resource.Repository;
import org.serhii.makhov.vcsservices.dto.BranchResource;
import org.serhii.makhov.vcsservices.dto.RepositoryResource;
import org.serhii.makhov.vcsservices.mapper.RepositoryMapper;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GithubRepositoryServiceImplTest {
    private GithubClient githubClient;
    private GithubRepositoryServiceImpl githubRepositoryService;
    private RepositoryMapper repositoryMapper;

    @BeforeEach
    void setUp() {
        githubClient = Mockito.mock(GithubClient.class);
        repositoryMapper = Mockito.mock(RepositoryMapper.class);
        githubRepositoryService = new GithubRepositoryServiceImpl(githubClient, repositoryMapper);
    }

    @Test
    void shouldFindNotForksByUserLogin(){
        String login = "testLogin";
        String repositoryName = "repo1";
        String branchName = "master";
        String commitSha = "test_sha";
        String forkRepositoryName = "repoFork";
        when(githubClient.fetchUserRepositories(eq(login)))
            .thenReturn(Flux.just(
                new Repository(repositoryName, new Owner(login), false),
                new Repository(forkRepositoryName, new Owner(login), true)
            ));
        when(githubClient.fetchRepositoryBranches(eq(login), eq(repositoryName)))
            .thenReturn(Flux.just(
                new Branch(branchName, new Commit(commitSha))
            ));
        when(repositoryMapper.map(any(), any()))
            .thenReturn(new RepositoryResource(repositoryName, login, List.of(new BranchResource(branchName, commitSha))));

        List<RepositoryResource> actualRepositories = githubRepositoryService.findNotForksByUserLogin(login);

        assertEquals(actualRepositories.size(), 1);
        RepositoryResource actualRepository = actualRepositories.get(0);
        assertEquals(actualRepository.repositoryName(), repositoryName);
        assertEquals(actualRepository.ownerLogin(), login);
        assertEquals(actualRepository.branches().size(), 1);
        assertEquals(actualRepository.branches().get(0).name(), branchName);
        assertEquals(actualRepository.branches().get(0).lastCommitSha(), commitSha);

        verify(githubClient).fetchUserRepositories(login);
        verify(githubClient).fetchRepositoryBranches(login, repositoryName);
        verify(githubClient, times(0)).fetchRepositoryBranches(login, forkRepositoryName);
    }



}
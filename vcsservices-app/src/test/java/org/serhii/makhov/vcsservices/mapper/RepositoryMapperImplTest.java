package org.serhii.makhov.vcsservices.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Commit;
import org.serhii.makhov.vcsservices.client.github.resource.Owner;
import org.serhii.makhov.vcsservices.client.github.resource.Repository;
import org.serhii.makhov.vcsservices.dto.BranchResource;
import org.serhii.makhov.vcsservices.dto.RepositoryResource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryMapperImplTest {

    private RepositoryMapperImpl mapper;
    private BranchMapper branchMapper;

    @BeforeEach
    void setUp() {
        branchMapper = mock(BranchMapper.class);
        mapper = new RepositoryMapperImpl(branchMapper);
    }

    @Test
    void shouldMapRepositoryWithBranches() {
        String repositoryName = "repositoryName";
        String login = "login";
        String branchName = "master";
        String commitSha = "testsha";
        when(branchMapper.map(any())).thenReturn(new BranchResource(branchName, commitSha));

        RepositoryResource actual = mapper.map(
            new Repository(repositoryName, new Owner(login), false),
            List.of(new Branch(branchName, new Commit(commitSha))));

        assertEquals(actual.ownerLogin(), login);
        assertEquals(actual.repositoryName(), repositoryName);
        assertEquals(actual.branches().get(0).name(), branchName);
        assertEquals(actual.branches().get(0).lastCommitSha(), commitSha);
    }

}
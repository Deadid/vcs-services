package org.serhii.makhov.vcsservices.mapper;

import org.junit.jupiter.api.Test;
import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Commit;
import org.serhii.makhov.vcsservices.dto.BranchResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BranchMapperImplTest {

    private final BranchMapperImpl branchMapper = new BranchMapperImpl();

    @Test
    void shouldMapBranch() {
        String branchName = "testName";
        String commitSha = "sha";
        BranchResource actualBranchResource = branchMapper.map(new Branch(branchName, new Commit(commitSha)));

        assertEquals(actualBranchResource.name(), branchName);
        assertEquals(actualBranchResource.lastCommitSha(), commitSha);
    }

}
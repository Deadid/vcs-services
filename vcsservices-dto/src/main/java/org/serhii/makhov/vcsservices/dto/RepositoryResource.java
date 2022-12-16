package org.serhii.makhov.vcsservices.dto;

import java.util.List;

public record RepositoryResource(String repositoryName, String ownerLogin, List<BranchResource> branches) { }

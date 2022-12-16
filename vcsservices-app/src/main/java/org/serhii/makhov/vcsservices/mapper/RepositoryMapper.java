package org.serhii.makhov.vcsservices.mapper;

import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Repository;
import org.serhii.makhov.vcsservices.dto.RepositoryResource;

import java.util.List;

public interface RepositoryMapper {
    RepositoryResource map(Repository repository, List<Branch> branches);
}

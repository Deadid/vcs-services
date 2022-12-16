package org.serhii.makhov.vcsservices.mapper;

import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Repository;
import org.serhii.makhov.vcsservices.dto.RepositoryResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RepositoryMapperImpl implements RepositoryMapper {

    private final BranchMapper branchMapper;

    public RepositoryMapperImpl(BranchMapper branchMapper) {
        this.branchMapper = branchMapper;
    }

    @Override
    public RepositoryResource map(Repository repository, List<Branch> branches) {
        return new RepositoryResource(
            repository.name(),
            repository.owner().login(),
            branches.stream().map(branchMapper::map).collect(Collectors.toList()));
    }
}

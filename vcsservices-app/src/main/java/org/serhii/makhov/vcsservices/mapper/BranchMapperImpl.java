package org.serhii.makhov.vcsservices.mapper;

import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.dto.BranchResource;
import org.springframework.stereotype.Component;

@Component
public class BranchMapperImpl implements BranchMapper {

    @Override
    public BranchResource map(Branch branch) {
        return new BranchResource(branch.name(), branch.commit().sha());
    }
}

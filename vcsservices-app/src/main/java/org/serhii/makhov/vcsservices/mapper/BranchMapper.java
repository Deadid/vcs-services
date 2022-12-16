package org.serhii.makhov.vcsservices.mapper;

import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.dto.BranchResource;

public interface BranchMapper {
    BranchResource map(Branch branch);
}

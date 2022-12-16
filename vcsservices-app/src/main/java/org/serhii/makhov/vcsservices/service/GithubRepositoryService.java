package org.serhii.makhov.vcsservices.service;

import org.serhii.makhov.vcsservices.dto.RepositoryResource;

import java.util.List;

public interface GithubRepositoryService {
    List<RepositoryResource> findNotForksByUserLogin(String login);
}

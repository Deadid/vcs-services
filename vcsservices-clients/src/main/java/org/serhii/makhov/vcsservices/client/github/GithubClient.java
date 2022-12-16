package org.serhii.makhov.vcsservices.client.github;

import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Repository;
import reactor.core.publisher.Flux;

public interface GithubClient {

    /**
     * Fetches all user repositories based on user login
     */
    Flux<Repository> fetchUserRepositories(String userLogin);


    /**
     * Fetches all repositories based on user login and repository name
     */
    Flux<Branch> fetchRepositoryBranches(String userLogin, String repositoryName);
}

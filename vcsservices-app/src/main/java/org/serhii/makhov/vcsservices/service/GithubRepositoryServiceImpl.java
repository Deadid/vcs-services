package org.serhii.makhov.vcsservices.service;

import org.serhii.makhov.vcsservices.client.github.GithubClient;
import org.serhii.makhov.vcsservices.client.github.resource.Branch;
import org.serhii.makhov.vcsservices.client.github.resource.Repository;
import org.serhii.makhov.vcsservices.dto.RepositoryResource;
import org.serhii.makhov.vcsservices.mapper.RepositoryMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Service
public class GithubRepositoryServiceImpl implements GithubRepositoryService {
    private final GithubClient githubClient;
    private final RepositoryMapper repositoryMapper;

    public GithubRepositoryServiceImpl(GithubClient githubClient, RepositoryMapper repositoryMapper) {
        this.githubClient = githubClient;
        this.repositoryMapper = repositoryMapper;
    }


    @Override
    public List<RepositoryResource> findNotForksByUserLogin(String userLogin) {
        Flux<RepositoryResource> repositoryResourceFlux = githubClient.fetchUserRepositories(userLogin)
            .filter(Predicate.not(Repository::fork))
            .flatMap(repository -> {
            Flux<Branch> branchesFlux = githubClient.fetchRepositoryBranches(userLogin, repository.name());
            return Mono.zip(Mono.just(repository), branchesFlux.collectList())
                .map(tuple -> repositoryMapper.map(tuple.getT1(), tuple.getT2()));
        });
        return repositoryResourceFlux.collectList().block();
    }

}

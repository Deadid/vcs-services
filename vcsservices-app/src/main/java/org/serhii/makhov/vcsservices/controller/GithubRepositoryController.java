package org.serhii.makhov.vcsservices.controller;

import org.serhii.makhov.vcsservices.dto.RepositoryResource;
import org.serhii.makhov.vcsservices.service.GithubRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/github/repos")
public class GithubRepositoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GithubRepositoryController.class);

    private final GithubRepositoryService githubRepositoryService;

    public GithubRepositoryController(GithubRepositoryService githubRepositoryService) {
        this.githubRepositoryService = githubRepositoryService;
    }

    /**
     * Fetch Github repos which are not forks for specific user by login
     */
    @GetMapping(value = "/{userLogin}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<RepositoryResource> findNotForksByLogin(@PathVariable String userLogin) {
        LOGGER.info("Requested github repositories for user {}", userLogin);
        return githubRepositoryService.findNotForksByUserLogin(userLogin);
    }
}

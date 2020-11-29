package turbo.funicular.service;

import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.security.utils.SecurityService;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.entity.User;
import turbo.funicular.web.GistComment;
import turbo.funicular.web.GistDto;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class GitHubService {

    private final SecurityService securityService;

    public GitHubService(final SecurityService securityService) {
        this.securityService = securityService;
    }

    public List<GistDto> findGistsByUser(final String login) {
        return createApiClientInstance()
            .map(apiClient -> apiClient.findGistsByUser(login))
            .orElse(List.of());
    }

    public Optional<User> findUserByLogin(final String login) {
        return createApiClientInstance()
            .flatMap(apiClient -> apiClient.findUser(login));
    }

    public Optional<GistDto> findGistById(final String ghId) {
        return createApiClientInstance()
            .flatMap(apiClient -> apiClient.findGistById(ghId));
    }

    public List<GistComment> topGistComments(final String ghId) {
        return createApiClientInstance()
            .map(apiClient -> apiClient.topGistComments(ghId, 5))
            .orElse(List.of());
    }

    public Optional<GistComment> addCommentToGist(final String gistId, final String comment) {
        return createApiClientInstance()
            .flatMap(apiClient -> apiClient.addCommentToGist(gistId, comment));
    }

    private Optional<GithubApiClient> createApiClientInstance() {
        return securityService.getAuthentication()
            .map(authentication -> (String) authentication.getAttributes().get(OauthUserDetailsMapper.ACCESS_TOKEN_KEY))
            .map(GithubApiClient::create);
    }
}
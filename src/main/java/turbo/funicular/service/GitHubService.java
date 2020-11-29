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
        log.info("Searching gists for username {}", login);
        return GithubApiClient.create().findGistsByUser(login);
    }

    public Optional<User> findUserByLogin(final String login) {
        return GithubApiClient.create().findUser(login);
    }

    public Optional<GistDto> findGistById(final String ghId) {
        return GithubApiClient.create().findGistById(ghId);
    }

    public List<GistComment> topGistComments(final String ghId) {
        return GithubApiClient.create().topGistComments(ghId, 5);
    }

    public Optional<GistComment> addCommentToGist(final String gistId, final String comment) {
        return securityService.getAuthentication()
            .map(authentication -> (String) authentication.getAttributes().get(OauthUserDetailsMapper.ACCESS_TOKEN_KEY))
            .flatMap(accessToken -> GithubApiClient.create(accessToken).addCommentToGist(gistId, comment));
    }
}

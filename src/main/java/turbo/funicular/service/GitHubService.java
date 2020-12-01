package turbo.funicular.service;

import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.security.utils.SecurityService;
import io.vavr.control.Either;
import io.vavr.control.Option;
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

    public Either<List<String>, List<GistDto>> findGistsByUser(final String login) {
        return getApiClientOrError()
            .flatMap(apiClient -> apiClient.findGistsByUser(login));
    }

    public Optional<User> findUserByLogin(final String login) {
        return createApiClientInstance()
            .flatMap(apiClient -> apiClient.findUser(login));
    }

    public Either<List<String>, GistDto> findGistById(final String ghId) {
        return getApiClientOrError().flatMap(apiClient -> apiClient.findGistById(ghId));
    }

    public List<GistComment> topGistComments(final String ghId) {
        return createApiClientInstance()
            .map(apiClient -> apiClient.topGistComments(ghId, 20))
            .orElse(List.of());
    }

    public Either<List<String>, GistComment> addCommentToGist(final String gistId, final String comment) {
        return getApiClientOrError()
            .flatMap(apiClient -> apiClient.addCommentToGist(gistId, comment));
    }

    private Optional<GithubApiClient> createApiClientInstance() {
        return securityService.getAuthentication()
            .map(authentication -> (String) authentication.getAttributes().get(OauthUserDetailsMapper.ACCESS_TOKEN_KEY))
            .map(GithubApiClient::create);
    }

    private Either<List<String>, GithubApiClient> getApiClientOrError() {
        return Option.ofOptional(securityService.getAuthentication())
            .toEither(List.of("This is very bad, authentication data is gone!!"))
            .map(authentication -> (String) authentication.getAttributes().get(OauthUserDetailsMapper.ACCESS_TOKEN_KEY))
            .map(GithubApiClient::create);
    }
}
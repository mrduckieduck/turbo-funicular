package turbo.funicular.service;

import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.security.utils.SecurityService;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.UserService;
import turbo.funicular.entity.Failure;

import javax.inject.Singleton;
import java.util.function.Function;

@Singleton
public class GithubClientFactory {

    private final SecurityService securityService;
    private final GitHubClient githubClient;

    public GithubClientFactory(final SecurityService securityService) {
        this.securityService = securityService;
        this.githubClient = new GitHubClient();
    }

    public Either<Failure, UserService> createUserService() {
        return createGithubServiceClient(UserService::new);
    }

    public Either<Failure, GistService> createGistService() {
        return createGithubServiceClient(GistService::new);
    }

    private <T> Either<Failure, T> createGithubServiceClient(final Function<GitHubClient, T> serviceClientCreator) {
        return Option.ofOptional(securityService.getAuthentication())
            .toEither(Failure.of("github.api.failure.authentication-empty",
                "For some reason the authentication info is not present!"))
            .map(authentication -> (String) authentication.getAttributes().get(OauthUserDetailsMapper.ACCESS_TOKEN_KEY))
            .map(githubClient::setOAuth2Token)
            .map(serviceClientCreator);
    }

}

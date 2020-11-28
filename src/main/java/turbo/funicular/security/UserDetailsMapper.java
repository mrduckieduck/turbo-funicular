package turbo.funicular.security;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.reactivestreams.Publisher;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Slf4j
@Named("github")
@Singleton
public class UserDetailsMapper implements OauthUserDetailsMapper {
    public static final String ROLE_GITHUB = "ROLE_GITHUB";
    public static final List<String> ROLES = List.of(ROLE_GITHUB);

    @Override
    public Publisher<UserDetails> createUserDetails(final TokenResponse tokenResponse) {
        return Publishers.just(new UnsupportedOperationException("deprecated!"));
    }

    @Override
    public Publisher<AuthenticationResponse> createAuthenticationResponse(final TokenResponse tokenResponse,
                                                                          final State state) {
        return Flowable.create(emitter -> getUserDetails(tokenResponse, emitter), BackpressureStrategy.ERROR);
    }

    private void getUserDetails(final TokenResponse tokenResponse,
                                final Emitter<AuthenticationResponse> responseEmitter) {
        try {
            final var accessToken = tokenResponse.getAccessToken();
            final var github = new GitHubBuilder()
                .withJwtToken(accessToken)
                .build();

            responseEmitter.onNext(buildDetails(github, accessToken));
            responseEmitter.onComplete();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            responseEmitter.onError(new AuthenticationException(new AuthenticationFailed(ex.getMessage())));
        }
    }

    @SneakyThrows
    protected UserDetails buildDetails(GitHub github, String accessToken) {
        final var user = github.getMyself();
        final var username = user.getLogin();
        final var userCommand = USERS_MAPPER.githubToCommand(user);

        final Map<String, Object> attributes = Map.of(
            ACCESS_TOKEN_KEY, accessToken,
            "id", user.getId(),
            "ghUser", userCommand
        );

        return new UserDetails(username, ROLES, attributes);
    }
}

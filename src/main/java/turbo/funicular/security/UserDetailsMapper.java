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
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHubBuilder;
import org.reactivestreams.Publisher;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Named("github")
@Singleton
public class UserDetailsMapper implements OauthUserDetailsMapper {

    public static final String ROLE_GITHUB = "ROLE_GITHUB";

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
            final var github = new GitHubBuilder().withJwtToken(tokenResponse.getAccessToken()).build();
            final var user = github.getMyself();
            final var userDetails = new UserDetails(user.getLogin(), List.of(ROLE_GITHUB),
                Map.of(OauthUserDetailsMapper.ACCESS_TOKEN_KEY, tokenResponse.getAccessToken()));
            responseEmitter.onNext(userDetails);
            responseEmitter.onComplete();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            responseEmitter.onError(new AuthenticationException(new AuthenticationFailed(ex.getMessage())));
        }
    }
}

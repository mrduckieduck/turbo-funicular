package turbo.funicular.security;

import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import io.reactivex.Flowable;
import org.kohsuke.github.GitHub;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;

@Singleton
public class UserDetailsMapper implements OauthUserDetailsMapper {

    @Override
    public Publisher<UserDetails> createUserDetails(final TokenResponse tokenResponse) {
        return null;
    }

    @Override
    public Publisher<AuthenticationResponse> createAuthenticationResponse(final TokenResponse tokenResponse,
                                                                          final State state) {
        try {
            System.out.printf("Access Token: %s\n", tokenResponse.getAccessToken());
            final var github = GitHub.connectUsingOAuth(tokenResponse.getAccessToken());
            final var user = github.getUser("mrduckieduck");
            System.out.printf("User: %s\n", user.getBio());
            return Flowable.just(new UserDetails("mrduckieduck", List.of()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

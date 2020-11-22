package turbo.funicular.security;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.event.LoginSuccessfulEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import turbo.funicular.service.UsersService;

import javax.inject.Singleton;

@Singleton
@RequiredArgsConstructor
public class LoginSuccessfulEventListener implements ApplicationEventListener<LoginSuccessfulEvent> {
    private final UsersService usersService;

    @Override
    public void onApplicationEvent(LoginSuccessfulEvent event) {
        final var authentication = (UserDetails) event.getSource();
        final var accessToken = (String) authentication.getAttributes("roles", "username")
            .getOrDefault("accessToken", "");

        if (StringUtils.isNoneBlank(accessToken)) {
            usersService.loggedUser(authentication.getUsername(), accessToken);
        }
    }
}

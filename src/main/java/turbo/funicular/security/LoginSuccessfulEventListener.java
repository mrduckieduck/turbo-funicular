package turbo.funicular.security;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.event.LoginSuccessfulEvent;
import lombok.RequiredArgsConstructor;
import turbo.funicular.service.UsersService;
import turbo.funicular.web.UserCommand;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor
public class LoginSuccessfulEventListener implements ApplicationEventListener<LoginSuccessfulEvent> {
    private final UsersService usersService;

    @Override
    public void onApplicationEvent(LoginSuccessfulEvent event) {
        final var authentication = (UserDetails) event.getSource();
        final var userCommand = Optional.ofNullable((UserCommand) authentication
            .getAttributes("roles", "username")
            .getOrDefault("ghUser", null));

        userCommand.ifPresent(usersService::addUserIfMissing);
    }
}

package turbo.funicular.security;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.event.LoginSuccessfulEvent;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class LoginSuccessfulEventListener implements ApplicationEventListener<LoginSuccessfulEvent> {

    @Override
    public void onApplicationEvent(LoginSuccessfulEvent event) {
        final var userDetails = (UserDetails) event.getSource();

        log.info("New login detected for {}", userDetails.getUsername());
    }
}

package turbo.funicular.security;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.security.event.LogoutEvent;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class LogoutEventListener implements ApplicationEventListener<LogoutEvent> {
    private int invocationCounter = 0;

    @Override
    public void onApplicationEvent(LogoutEvent event) {
        invocationCounter++;

        log.info("Louout {}", event.toString());
    }

    public int getInvocationCounter() {
        return invocationCounter;
    }
}

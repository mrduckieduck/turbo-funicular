package turbo.funicular.service;

import io.micronaut.context.MessageSource;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.entity.UserRepository;
import turbo.funicular.web.UserCommand;

import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class UserValidator {
    private final ValidationService validationService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final MessageSource.MessageContext messageContext;

    public List<String> errorUsersExists() {
        return List.of(messageSource
            .getMessage("user.error.alreadyExists", messageContext)
            .orElse(""));
    }

    public Validation<List<String>, UserCommand> userDoesNotExists(UserCommand userCommand) {
        return userRepository
            .findUserWith(userCommand.getLogin(), userCommand.getGhId())
            .<Validation<List<String>, UserCommand>>map(user -> Validation.invalid(errorUsersExists()))
            .orElseGet(() -> Validation.valid(userCommand));
    }

    /**
     * @param user
     * @param <T>
     * @return
     */
    public <T> Validation<List<String>, T> validateFields(T user) {
        return validationService
            .validate(user);
    }

}

package turbo.funicular.service;

import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.entity.User;
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

    public Validation<User, UserCommand> userDoesNotExists(UserCommand userCommand) {
        return userRepository
            .findUserWith(userCommand.getLogin(), userCommand.getGhId())
            .<Validation<User, UserCommand>>map(Validation::invalid)
            .orElseGet(() -> Validation.valid(userCommand));
    }

    /**
     *
     * @param user
     * @param <T>
     * @return
     */
    public <T> Validation<List<String>, T> validateFields(T user) {
        return validationService
            .validate(user);
    }

}

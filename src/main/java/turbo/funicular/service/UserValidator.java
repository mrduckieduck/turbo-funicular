package turbo.funicular.service;

import io.micronaut.context.MessageSource;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.mrpato.failure.entity.Failure;
import turbo.funicular.entity.UserRepository;
import turbo.funicular.web.UserCommand;

import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class UserValidator {

    protected static final String PREFIX_FAILURE_CODE = "user.failure";

    private final ValidationService validationService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final MessageSource.MessageContext messageContext;

    public Failure errorUsersExists() {
        final var failureCode = "%s.alreadyExists".formatted(PREFIX_FAILURE_CODE);
        final var i18nData = Failure.I18nData.builder()
            .code(failureCode)
            .defaultMessage("The user already exists")
            .build();
        final var localizedMessage = messageSource.getMessage(failureCode, messageContext)
            .orElse(i18nData.getDefaultMessage());
        return Failure.builder()
            .i18nData(i18nData)
            .reason(localizedMessage)
            .code(failureCode)
            .build();
    }

    public Validation<Failure, UserCommand> userDoesNotExists(UserCommand userCommand) {
        return userRepository.findUserWith(userCommand.getLogin(), userCommand.getGhId())
            .<Validation<Failure, UserCommand>>map(user -> Validation.invalid(errorUsersExists()))
            .orElseGet(() -> Validation.valid(userCommand));
    }

    public <T> Validation<Failure, T> validateFields(final T user) {
        return validationService.validate(user, PREFIX_FAILURE_CODE);
    }

}

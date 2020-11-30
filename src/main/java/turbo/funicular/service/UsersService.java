package turbo.funicular.service;

import com.google.common.collect.Lists;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.entity.User;
import turbo.funicular.entity.UserRepository;
import turbo.funicular.web.UserCommand;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.vavr.control.Either.left;
import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Slf4j
@Singleton
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final GitHubService gitHubService;
    private final UserValidator userValidator;

    public Either<List<String>, User> addUser(@NotNull UserCommand command) {
        final var userCommands1 = userValidator.validateFields(command);

        if (userCommands1.isInvalid()) {
            return left(userCommands1.getError());
        }

        final var foosss = validateUserExists(command);

        if (foosss.isLeft()) {
            return left(List.of("User already exists"));
        }

        return add(command);
    }

    private Either<User, UserCommand> validateUserExists(UserCommand command) {
        return userValidator.userDoesNotExists(command).toEither();
    }

    private Either<List<String>, User> add(UserCommand usercommand) {
        return validationService
            .validateFoo(USERS_MAPPER.commandToEntity(usercommand))
            .map(this::saveNewUser);
    }

    private User saveNewUser(User user) {
        // by default all profiles are public.
        // If an user wants to run a private profile,
        // should do it by itself
        user.setPublicProfile(true);

        return userRepository.save(user);
    }

    public List<User> randomTop(Long count) {
        long usersCount = userRepository.count();
        if (count >= usersCount) {
            // we don't have enough users, so return all of them...
            return Lists.newArrayList(userRepository.findAll());
        }
        return userRepository.randomUsers(count);
    }

    public void addUserIfMissing(UserCommand userCommand) {
        userValidator
            .userDoesNotExists(userCommand)
            .map((Function<UserCommand, Object>) this::add);
    }

    public Optional<User> findUser(final String login) {
        return userRepository.findByLogin(login)
            .or(() -> {
                Optional<User> userByLogin = gitHubService.findUserByLogin(login);
                userByLogin.ifPresent(userRepository::save);
                return userByLogin;
            });
    }
}

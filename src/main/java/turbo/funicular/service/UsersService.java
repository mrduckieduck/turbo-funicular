package turbo.funicular.service;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.mrpato.failure.entity.Failure;
import turbo.funicular.entity.User;
import turbo.funicular.entity.UserRepository;
import turbo.funicular.web.UserCommand;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;

import static turbo.funicular.service.UserValidator.PREFIX_FAILURE_CODE;
import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Slf4j
@Singleton
@Transactional
@RequiredArgsConstructor
public class UsersService {

    private final UserRepository userRepository;
    private final GithubService gistsService;
    private final UserValidator userValidator;

    public Either<Failure, User> addUser(@NotNull final UserCommand command) {
        return userValidator.validateFields(command)
            .flatMap(userValidator::userDoesNotExists)
            .toEither()
            .flatMap(this::add);
    }

    public Either<Failure, List<User>> randomTop(final long count) {
        return Try.of(() -> userRepository.randomUsers(count))
            .toEither()
            .mapLeft(throwable -> Failure.of(throwable, "%s.random-top".formatted(PREFIX_FAILURE_CODE),
                "Got an error trying to get the top %s users!".formatted(count)));
    }

    public void addUserIfMissing(final UserCommand userCommand) {
        userValidator
            .userDoesNotExists(userCommand)
            .peek(this::add);
    }

    public Either<Failure, User> findUser(final String login) {
        return Try.of(() -> userRepository.findByLogin(login))
            .toEither()
            .mapLeft(throwable -> Failure.of(throwable, "%s.find-user".formatted(PREFIX_FAILURE_CODE),
                "Weird error when trying to get the user for login %s".formatted(login)))
            .flatMap(user -> user.map(Either::<Failure, User>right)
                .orElse(findUserInGithubAddIfFound(login)));
    }

    private Either<Failure, User> findUserInGithubAddIfFound(final String login) {
        return gistsService.findUserByLogin(login)
            .flatMap(this::saveNewUser);
    }

    private Either<Failure, User> add(UserCommand usercommand) {
        return userValidator.validateFields(USERS_MAPPER.commandToEntity(usercommand))
            .toEither()
            .flatMap(this::saveNewUser);
    }

    private Either<Failure, User> saveNewUser(final User user) {
        // by default all profiles are public.
        // If an user wants to run a private profile,
        // should do it by itself
        return Try.of(() -> {
                user.setPublicProfile(true);
                return userRepository.save(user);
            })
            .toEither()
            .mapLeft(throwable -> Failure.of(throwable, "%s.save".formatted(PREFIX_FAILURE_CODE),
                "Error trying to save the user %s!".formatted(user)));
    }

}

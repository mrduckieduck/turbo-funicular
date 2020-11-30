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
import java.util.function.BiPredicate;

import static io.vavr.API.*;
import static io.vavr.Predicates.is;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Slf4j
@Singleton
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;
    private final ValidationService validationService;
    private final GitHubService gitHubService;

    private BiPredicate<UserCommand, UserRepository> exists =
        (userCommand, repo) -> repo
            .findUserWith(userCommand.getLogin(), userCommand.getGhId())
            .isPresent();

    public Either<List<String>, User> addUser(@NotNull UserCommand command) {
        final var userCommands1 = validationService.validateFoo(command);
        if (userCommands1.isLeft()) {
            return left(userCommands1.getLeft());
        }

        final var foosss = foosss(command);

        if (foosss.isLeft()) {
            return left(foosss.getLeft());
        }

        return add(command);
    }

    private Either<List<String>, UserCommand> foosss(UserCommand command) {
        return Match(exists.test(command, userRepository))
            .of(
                Case($(is(false)), right(command)),
                Case($(is(true)), left(List.of("User already exists")))
            );
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
        userRepository
            .findUserWith(userCommand.getLogin(), userCommand.getGhId())
            .ifPresentOrElse(
                user -> log.info("User {} already on the database", user.getLogin()),
                () -> add(userCommand));
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

package turbo.funicular.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.entity.User;
import turbo.funicular.entity.UserRepository;
import turbo.funicular.service.exceptions.DuplicatedEntityException;
import turbo.funicular.web.UserCommand;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Slf4j
@Singleton
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;
    private final ValidationService validationService;

    public Optional<User> addUser(@NotNull UserCommand command) {
        validationService.validate(command);

        userRepository
            .findUserWith(command.getLogin(), command.getGhId())
            .ifPresent(user -> {
                throw new DuplicatedEntityException("User", command.getLogin() + ", " + command.getGhId());
            });

        return add(command);
    }

    private Optional<User> add(UserCommand usercommand) {
        return Stream.of(USERS_MAPPER.commandToEntity(usercommand))
            .map(validationService::validate)
            .peek(user -> {
                // by default all profiles are public.
                // If an user wants to run a private profile,
                // should do it by itself
                user.setPublicProfile(true);
            })
            .map(userRepository::save)
            .findFirst();
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
}

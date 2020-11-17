package turbo.funicular.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.entity.User;
import turbo.funicular.entity.UserRepository;
import turbo.funicular.service.exceptions.DuplicatedEntityException;
import turbo.funicular.web.UserCommand;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Slf4j
@Singleton
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;
    private final ValidationService validationService;

    public User addUser(@NotNull UserCommand command) {
        validationService.validate(command);

        userRepository
            .findUser(command.getLogin(), command.getGhId())
            .ifPresent(user -> {
                throw new DuplicatedEntityException("User", command.getLogin() + ", " + command.getGhId());
            });

        User user = USERS_MAPPER.commandToEntity(command);
        validationService.validate(user);

        //by default all profiles are public. If an user wants to run a private profile, should do it by itself
        user.setPublicProfile(true);

        return userRepository.save(user);
    }

}

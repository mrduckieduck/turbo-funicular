package turbo.funicular.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.entity.User;
import turbo.funicular.entity.UserRepository;
import turbo.funicular.web.UserCommand;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.Optional;

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

        User user = USERS_MAPPER.commandToEntity(command);
        validationService.validate(user);

        //by default all profiles are public. If an user wants to run a private profile, should do it by itself
        user.setPublicProfile(true);
        User saved = userRepository.save(user);
        return Optional.of(saved);
    }

}

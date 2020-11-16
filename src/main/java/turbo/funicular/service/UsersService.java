package turbo.funicular.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.entity.User;
import turbo.funicular.entity.UserRepository;
import turbo.funicular.web.UserCommand;

import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.Optional;

@Slf4j
@Singleton
@Transactional
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;

    public Optional<User> addUser(UserCommand command) {
        User user = UsersMapper.INSTANCE.commandToEntity(command);
        //by default all profiles are public. If a user want to run a private profile, should do it by itself
        user.setPublicProfile(true);
        User saved = userRepository.save(user);
        return Optional.of(saved);
    }
}

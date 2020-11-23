package turbo.funicular.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.entity.User;
import turbo.funicular.web.GistDto;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class GitHubService {

    public List<GistDto> findGistsByUser(final String login) {
        log.info("Searching gists for username {}", login);
        return GithubApiClient.create().findGistsByUser(login);
    }

    public Optional<User> findUserByLogin(final String login) {
        return GithubApiClient.create().findUser(login);
    }
}

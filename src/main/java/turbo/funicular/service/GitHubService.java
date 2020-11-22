package turbo.funicular.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.web.GistDto;

import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class GitHubService {

    public List<GistDto> findGistsByUser(final String login) {
        log.info("Searching gists for username {}", login);
        return GithubApiClient.create().findGistsByUser(login);
    }
}

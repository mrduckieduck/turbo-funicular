package turbo.funicular.service;

import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import turbo.funicular.entity.User;
import turbo.funicular.web.GistDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class GithubApiClient {

    private final GitHub gitHub;

    public GithubApiClient() {
        this.gitHub = fromEnvironment();
    }

    public GithubApiClient(final String accessToken) {
        this.gitHub = createGithubInstance(accessToken);
    }

    public Optional<User> getUser(final String login) {
        try {
            final var ghUser = gitHub.getUser(login);
            final var user = new User();
            user.setName(ghUser.getName());
            user.setGhId(ghUser.getId());
            user.setLogin(ghUser.getLogin());
            user.setBio(ghUser.getBio());
            user.setAvatarUrl(ghUser.getAvatarUrl());
            user.setPublicGistsCount(ghUser.getPublicGistCount());
            return Optional.of(user);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        } finally {
            gitHub.refreshCache();
        }
    }

    public List<GistDto> findGistsByUser(final String login) {
        try {
            final var ghUser = gitHub.getUser(login);
            return ghUser.listGists().toList().stream()
                .map(this::createGistDto)
                .collect(Collectors.toList());
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return List.of();
        }
    }

    private GistDto createGistDto(final GHGist gist) {
        try {
            return GistDto.builder()
                .createdAt(LocalDateTime.ofInstant(gist.getCreatedAt().toInstant(), ZoneId.systemDefault()))
                .updatedAt(LocalDateTime.ofInstant(gist.getUpdatedAt().toInstant(), ZoneId.systemDefault()))
                .commentsCount(gist.getCommentCount())
                .description(gist.getDescription())
                .ghId(gist.getGistId())
                .publicGist(gist.isPublic())
                .build();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    private GitHub createGithubInstance(final String accessToken) {
        try {
            return new GitHubBuilder().withJwtToken(accessToken).build();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private GitHub fromEnvironment() {
        try {
            return GitHubBuilder.fromEnvironment().build();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
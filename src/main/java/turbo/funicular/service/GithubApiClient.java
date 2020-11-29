package turbo.funicular.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.UserService;
import turbo.funicular.entity.User;
import turbo.funicular.web.GistComment;
import turbo.funicular.web.GistContent;
import turbo.funicular.web.GistDto;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class GithubApiClient {

    private final UserService userService;
    private final GistService gistService;

    public static GithubApiClient create() {
        final var githubClient = new GitHubClient();
        final var userService = new UserService(githubClient);
        final var gistService = new GistService(githubClient);
        return new GithubApiClient(userService, gistService);
    }

    public static GithubApiClient create(final String accessToken) {
        final var githubClient = new GitHubClient().setOAuth2Token(accessToken);
        final var userService = new UserService(githubClient);
        final var gistService = new GistService(githubClient);
        return new GithubApiClient(userService, gistService);
    }

    protected GithubApiClient(final UserService userService, final GistService gistService) {
        this.userService = userService;
        this.gistService = gistService;
    }

    public Optional<User> findUser(final String login) {
        try {
            final var ghUser = userService.getUser(login);
            final var user = new User();
            user.setName(ghUser.getName());
            user.setGhId((long) ghUser.getId());
            user.setLogin(ghUser.getLogin());
            user.setBio(ghUser.getCompany());
            user.setAvatarUrl(ghUser.getAvatarUrl());
            user.setPublicGistsCount(ghUser.getPublicGists());
            return Optional.of(user);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    public List<GistDto> findGistsByUser(final String login) {
        try {
            return gistService.getGists(login).stream()
                .map(this::createGistDto)
                .collect(Collectors.toList());
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return List.of();
        }
    }

    public Optional<GistDto> findGistById(final String ghId) {
        try {
            return Optional.ofNullable(gistService.getGist(ghId))
                .map(this::createGistDto);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    public Optional<GistComment> addCommentToGist(final String gistId, final String comment) {
        try {
            return Optional.ofNullable(gistService.createComment(gistId, comment))
                .map(this::createGistComment);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    public void deleteCommentFromGist(final long gistCommentId) {
        try {
            gistService.deleteComment(gistCommentId);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public List<GistComment> topGistComments(final String gistId, final long count) {
        try {
            return gistService.getComments(gistId).stream()
                .limit(count)
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .map(this::createGistComment)
                .collect(Collectors.toList());
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return List.of();
        }
    }

    private GistDto createGistDto(final Gist gist) {
        return GistDto.builder()
            .createdAt(LocalDateTime.ofInstant(gist.getCreatedAt().toInstant(), ZoneId.systemDefault()))
            .updatedAt(LocalDateTime.ofInstant(gist.getUpdatedAt().toInstant(), ZoneId.systemDefault()))
            .commentsCount(gist.getComments())
            .description(gist.getDescription())
            .ghId(gist.getId())
            .publicGist(gist.isPublic())
            .owner(gist.getUser().getLogin())
            .files(createGistContent(gist))
            .build();
    }

    private List<GistContent> createGistContent(final Gist gist) {
        return gist.getFiles().values().stream()
            .map(gistContent -> GistContent.builder()
                .filename(gistContent.getFilename())
                .size(gistContent.getSize())
                .rawUrl(gistContent.getRawUrl())
                .content(gistContent.getContent())
                .build()
            )
            .collect(Collectors.toList());
    }

    private GistComment createGistComment(final Comment gistComment) {
        final var owner = new User();
        owner.setName(gistComment.getUser().getName());
        owner.setLogin(gistComment.getUser().getLogin());
        owner.setAvatarUrl(gistComment.getUser().getAvatarUrl());
        return GistComment.builder()
            .owner(owner)
            .createdAt(LocalDate.ofInstant(gistComment.getCreatedAt().toInstant(), ZoneId.systemDefault()))
            .body(gistComment.getBody())
            .build();
    }
}
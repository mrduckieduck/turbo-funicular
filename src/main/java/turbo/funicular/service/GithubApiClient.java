package turbo.funicular.service;

import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Try;
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
        return Try.ofCallable(() -> userService.getUser(login))
            .map(this::mapUser)
            .onFailure(throwable -> log.error("Error in getting the user {} from Github", login, throwable))
            .toJavaOptional();//For now, in fact should be Either<String, User>
    }

    public Either<List<String>, List<GistDto>> findGistsByUser(final String login) {
        return Try.of(() -> gistService.getGists(login))
            .map(gists -> Stream.ofAll(gists).map(this::createGistDto))
            .map(gists -> gists.collect(Collectors.toList()))
            .onFailure(throwable -> log.error("Can not find gists for user {}", login, throwable))
            .toEither(List.of(String.format("Can not get the gists for %s", login)));
    }

    public Either<List<String>, GistDto> findGistById(final String ghId) {
        return Try.ofCallable(() -> gistService.getGist(ghId))
            .map(this::createGistDto)
            .onFailure(throwable -> log.error("Can not get gist {} from GH", ghId, throwable))
            .toEither(List.of("Gist is either empty or invalid"));
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

    public List<GistComment> topGistComments(final String gistId, final int count) {
        return Try.ofCallable(() -> gistService.getComments(gistId))
            .map(comments -> Stream.ofAll(comments).take(count)
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .map(this::createGistComment)
                .collect(Collectors.toList()))
            .onFailure(throwable -> log.error("Can not get the comments from {} gist", gistId, throwable))
            .getOrElse(List.of());
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
        return gist.getFiles()
            .values()
            .stream()
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
            .createdAt(LocalDateTime.ofInstant(gistComment.getCreatedAt().toInstant(), ZoneId.systemDefault()))
            .body(gistComment.getBody())
            .build();
    }

    private User mapUser(final org.eclipse.egit.github.core.User ghUser) {
        final var user = new User();
        user.setName(ghUser.getName());
        user.setGhId((long) ghUser.getId());
        user.setLogin(ghUser.getLogin());
        user.setBio(ghUser.getCompany());
        user.setAvatarUrl(ghUser.getAvatarUrl());
        user.setPublicGistsCount(ghUser.getPublicGists());
        return user;
    }
}
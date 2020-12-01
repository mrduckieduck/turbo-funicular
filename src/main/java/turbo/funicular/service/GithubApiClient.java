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

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

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
            .toJavaOptional();//For now, in fact could be Either<List<String>, User> to keep track of all the possible errors
    }

    public Either<List<String>, List<GistDto>> findGistsByUser(final String login) {
        return Try.of(() -> gistService.getGists(login))
            .map(gists -> Stream.ofAll(gists).map(this::createGistDto)
                .collect(Collectors.toList()))
            .onFailure(throwable -> log.error("Can not find gists for user {}", login, throwable))
            .toEither(List.of(String.format("Can not get the gists for %s", login)));
    }

    public Either<List<String>, GistDto> findGistById(final String ghId) {
        return Try.ofCallable(() -> gistService.getGist(ghId))
            .onFailure(throwable -> log.error("Can not get gist {} from GH", ghId, throwable))
            .map(this::createGistDto)
            .toEither(List.of(String.format("Can not get the gist with id %s", ghId)));
    }

    public Either<List<String>, GistComment> addCommentToGist(final String gistId, final String comment) {
        return Try.ofCallable(() -> gistService.createComment(gistId, comment))
            .onFailure(throwable -> log.error("Can not create a new comment for gist {}", gistId, throwable))
            .map(this::createGistComment)
            .map(gistComment -> Either.<List<String>, GistComment>right(gistComment))
            .getOrElseGet(throwable -> Either.left(List.of(throwable.getMessage())));
    }

    public Either<String, Void> deleteCommentFromGist(final long gistCommentId) {
        return Try.run(() -> gistService.deleteComment(gistCommentId))
            .onFailure(throwable -> log.error("Can not delete for user {}", gistCommentId, throwable))
            .toEither(String.format("Can not delete for user %s", gistCommentId));
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
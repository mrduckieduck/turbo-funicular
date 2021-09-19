package turbo.funicular.service;

import io.micronaut.security.oauth2.endpoint.token.response.OauthUserDetailsMapper;
import io.micronaut.security.utils.SecurityService;
import io.vavr.CheckedFunction0;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.eclipse.egit.github.core.service.UserService;
import turbo.funicular.entity.Failure;
import turbo.funicular.entity.User;
import turbo.funicular.web.GistComment;
import turbo.funicular.web.GistDto;

import javax.inject.Singleton;
import java.util.List;
import java.util.function.Function;

import static turbo.funicular.service.GistMapper.GIST_MAPPER;
import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Slf4j
@Singleton
public class DefaultGitHubService implements GitHubApiService {

    private static final String PREFIX_FAILURE_CODE = "github.api.failure.%s";
    private final SecurityService securityService;

    public DefaultGitHubService(final SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public Either<Failure, User> findUserByLogin(final String login) {
        return createUseServiceClient()
            .flatMap(userService -> {
                final CheckedFunction0<org.eclipse.egit.github.core.User> getUser =
                    () -> userService.getUser(login);
                return executeRequest(getUser, USERS_MAPPER::githubToEntity,
                    Tuple.of("get-user", "Error in getting the user %s from Github".formatted(login)));
            });
    }

    @Override
    public Either<Failure, List<GistDto>> findGistsByUser(final String login) {
        return createGistServiceClient()
            .flatMap(gistService -> {
                final CheckedFunction0<List<Gist>> gistsByUser = () -> gistService.getGists(login);
                return executeRequestForList(gistsByUser, GIST_MAPPER::githubToGistDto,
                    Tuple.of("find-gists-by-user", "Can not get the gists for %s".formatted(login)));
            });
    }

    @Override
    public Either<Failure, GistDto> findGistById(final String ghId) {
        return createGistServiceClient()
            .flatMap(gistService -> {
                final CheckedFunction0<Gist> getGist = () -> gistService.getGist(ghId);
                return executeRequest(getGist, GIST_MAPPER::githubToGistDto,
                    Tuple.of("find-gist-by-id", "Can not get the gist with id %s".formatted(ghId)));
            });
    }

    @Override
    public Either<Failure, List<GistComment>> topGistComments(final String gistId, final int count) {
        return createGistServiceClient()
            .flatMap(gistService -> {
                final CheckedFunction0<List<Comment>> getGistComments = () -> gistService.getComments(gistId);
                return executeRequestForList(getGistComments,
                    comments -> GIST_MAPPER.takeCommentsAndMap(comments, count),
                    Tuple.of("top-gists-comments", "Can not get the comments from %s gist".formatted(gistId)));
            });
    }

    @Override
    public Either<Failure, GistComment> addCommentToGist(final String gistId, final String comment) {
        return createGistServiceClient()
            .flatMap(gistService -> {
                final CheckedFunction0<Comment> createGist = () -> gistService.createComment(gistId, comment);
                return executeRequest(createGist, GIST_MAPPER::githubToGistComment,
                    Tuple.of("add-gist-comment",
                        "Can not create a new comment for gist %s".formatted(gistId)));
            });
    }

    private <T, V> Either<Failure, V> executeRequest(final CheckedFunction0<T> action,
                                                     final Function<T, V> onSuccess,
                                                     final Tuple2<String, String> failureInfo) {
        return Try.of(action).toEither()
            .map(onSuccess)
            //1. contains the code of the error and 2 contains the reason why could it failed
            .mapLeft(throwable -> Failure.of(throwable, PREFIX_FAILURE_CODE.formatted(failureInfo._1), failureInfo._2));
    }

    private <T, V> Either<Failure, List<V>> executeRequestForList(final CheckedFunction0<List<T>> action,
                                                                  final Function<List<T>, List<V>> onSuccess,
                                                                  final Tuple2<String, String> failureInfo) {
        return Try.of(action).toEither()
            .map(onSuccess)
            //1. contains the code of the error and 2 contains the reason why could it failed
            .mapLeft(throwable -> Failure.of(throwable, PREFIX_FAILURE_CODE.formatted(failureInfo._1), failureInfo._2));
    }

    private Either<Failure, GistService> createGistServiceClient() {
        return Option.ofOptional(securityService.getAuthentication())
            .toEither(Failure.of("github.validation.authentication.empty",
                "For some reason the authentication info is not present!"))
            .map(authentication -> (String) authentication.getAttributes().get(OauthUserDetailsMapper.ACCESS_TOKEN_KEY))
            .map(accessToken -> new GitHubClient().setOAuth2Token(accessToken))
            .map(GistService::new);
    }

    private Either<Failure, UserService> createUseServiceClient() {
        return Option.ofOptional(securityService.getAuthentication())
            .toEither(Failure.of("github.validation.authentication.empty",
                "For some reason the authentication info is not present!"))
            .map(authentication -> (String) authentication.getAttributes().get(OauthUserDetailsMapper.ACCESS_TOKEN_KEY))
            .map(accessToken -> new GitHubClient().setOAuth2Token(accessToken))
            .map(UserService::new);
    }

}
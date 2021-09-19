package turbo.funicular.web;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.RequestAttribute;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.views.View;
import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.service.GitHubApiService;
import turbo.funicular.service.UsersService;

import javax.annotation.Nullable;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;
import static java.lang.String.format;
import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UiController {
    private final UsersService usersService;
    private final GitHubApiService gitHubService;

    @Get
    @View("index")
    @Secured(IS_ANONYMOUS)
    public HttpResponse index() {
        return HttpResponse.ok();
    }

    @SneakyThrows
    @View("start")
    @Get("/getting-started")
    @Secured(IS_ANONYMOUS)
    public HttpResponse start(@Nullable Authentication authentication) {
        if (Objects.nonNull(authentication)) {
            //if the user is authenticated, sent to home
            return HttpResponse.redirect(new URI("/home"));
        }
        return HttpResponse.ok();
    }

    @Get("/home")
    @View("user_home")
    @Secured(IS_AUTHENTICATED)
    public HttpResponse home(Authentication authentication) {
        return HttpResponse.ok(model(authentication));
    }

    @Get("/profile/{login}")
    @View("profile")
    @Secured(IS_AUTHENTICATED)
    public HttpResponse featuredUser(final String login) {
        return Option.ofOptional(usersService.findUser(login))
            .map(user -> gitHubService.findGistsByUser(user.getLogin())
                .fold(errors -> Tuple.of("ghUser", user, "errors", errors), gists -> Tuple.of("ghUser", user, "gists", gists)))
            .map(sequence -> HashMap.of(sequence._1, sequence._2, sequence._3, sequence._4).toJavaMap())
            .map(HttpResponse::ok)
            .getOrElse(() -> HttpResponse.notFound());
    }

    @Get("/profile/{login}/gist/{gistId}")
    @View("gist_detail")
    @Secured(IS_AUTHENTICATED)
    public HttpResponse gist(final String login, final String gistId) {
        final var gistOrError = gitHubService.findGistById(gistId)
            .filter(gist -> gist.getOwner().equals(login))
            .getOrElse(Either.left(List.of(String.format("Gist %s doesn't belong to the given user %s", gistId, login))))
            .fold(errors -> Map.of("errors", errors, "username", login),
                gist -> Map.of("gist", gist,
                    "newComment", GistComment.builder().build(),
                    "username", login,
                    "topComments", gitHubService.topGistComments(gistId)));
        return HttpResponse.ok(gistOrError);
    }

    @Post(value = "/profile/{login}/gist/{gistId}/comment/new", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Secured(IS_AUTHENTICATED)
    public HttpResponse addComment(final String login,
                                   final String gistId,
                                   @RequestAttribute("body") final String comment) {
        return gitHubService.addCommentToGist(gistId, comment)
            .map(newComment -> HttpResponse.redirect(URI.create(format("/profile/%s/gist/%s", login, gistId))))
            .getOrElseGet(errors -> HttpResponse.serverError(Map.of("errors", errors)));
    }

    protected Map<String, Object> model(Authentication authentication) {
        final var username = Optional.ofNullable(authentication)
            .map(Principal::getName)
            .orElse("");

        final var users = usersService.randomTop(5L)
            .stream()
            .filter(user -> !user.getLogin().equals(username))
            .map(USERS_MAPPER::entityToCommand)
            .collect(Collectors.toList());

        final var roles = Optional.ofNullable(authentication)
            .map(auth -> auth.getAttributes().get("roles"))
            .orElse(List.of());

        final var ghUser = Optional.ofNullable(authentication)
            .map(auth -> auth.getAttributes().get("ghUser"))
            .orElse(UserCommand.builder().login(username).build());

        final var gistsOrErrors = gitHubService.findGistsByUser(username)
            .fold(errors -> Tuple.of("errors", errors), gists -> Tuple.of("gists", gists));

        return HashMap.of(
            "isLoggedIn", Objects.nonNull(authentication),
            "username", username,
            "featuredUsers", users,
            "roles", roles,
            "ghUser", ghUser
        ).put(gistsOrErrors).toJavaMap();
    }

}

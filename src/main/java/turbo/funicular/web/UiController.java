package turbo.funicular.web;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.views.View;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import turbo.funicular.service.GitHubService;
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
import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Controller
@RequiredArgsConstructor
public class UiController {
    private final UsersService usersService;
    private final GitHubService gitHubService;

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
        return usersService.findUser(login)
            .map(user -> Map.of("ghUser", user, "gists", gitHubService.findGistsByUser(user.getLogin())))
            .map(HttpResponse::ok)
            .orElse(HttpResponse.notFound());
    }

    @Get("/profile/{login}/gist/{gistId}")
    @View("gist_detail")
    @Secured(IS_AUTHENTICATED)
    public HttpResponse gist(final String login, final String gistId) {
        return gitHubService.findGistById(gistId)
            .filter(gist -> gist.getOwner().equals(login))
            .map(foundGist -> Map.of("gist", foundGist,
                "username", login,
                "topComments", gitHubService.topGistComments(gistId)))
            .map(HttpResponse::ok)
            .orElse(HttpResponse.notFound());
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

        return Map.of(
            "isLoggedIn", Objects.nonNull(authentication),
            "username", username,
            "featuredUsers", users,
            "ghUser", ghUser,
            "gists", gitHubService.findGistsByUser(username),
            "roles", roles
        );
    }

}

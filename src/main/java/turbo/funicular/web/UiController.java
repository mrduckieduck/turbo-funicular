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

    protected Map<String, Object> model(Authentication authentication) {
        final var username = Optional.ofNullable(authentication)
            .map(Principal::getName)
            .orElse("");

        final var users = usersService.randomTop(5l)
            .stream()
            .filter(user -> !user.getLogin().equals(username))
            .map(USERS_MAPPER::entityToCommand)
            .collect(Collectors.toList());

        final var roles = Optional.ofNullable(authentication)
            .map(auth -> auth.getAttributes().get("roles"))
            .map(strings -> (List<String>) strings)
            .orElse(List.of());

        final var ghUser = (Objects.requireNonNull(authentication).getAttributes().get("ghUser"));

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

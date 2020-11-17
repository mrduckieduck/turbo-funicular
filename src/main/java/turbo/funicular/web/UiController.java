package turbo.funicular.web;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.views.View;
import lombok.RequiredArgsConstructor;
import turbo.funicular.service.GitHubService;
import turbo.funicular.service.UsersService;

import java.util.Map;
import java.util.stream.Collectors;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static turbo.funicular.service.UsersMapper.USERS_MAPPER;

@Controller
@RequiredArgsConstructor
public class UiController {
    private final UsersService usersService;
    private final GitHubService gitHubService;

    @Get
    @View("index")
    @Secured(IS_ANONYMOUS)
    public HttpResponse<Map> index() {
        return HttpResponse.ok(Map.of());
    }

    @View("start")
    @Get("/getting-started")
    @Secured(IS_ANONYMOUS)
    public HttpResponse<Map> start() {
        return HttpResponse.ok(Map.of());
    }

    @Get("/home")
    @View("user_home")
    @Secured(IS_ANONYMOUS)
    public HttpResponse<Map> home() {
        final var users = usersService.randomTop(5)
            .stream()
            .map(USERS_MAPPER::entityToCommand)
            .collect(Collectors.toList());

        //TODO: This should be set by the security infra when ready. to get the gists owned by the current user logged
        final String username = "";

        final var model = Map.of(
            "featuredUsers", users,
            "gists", gitHubService.findGistsByUser(username)
        );

        return HttpResponse.ok(model);
    }

}

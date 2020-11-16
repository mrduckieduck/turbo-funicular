package turbo.funicular.web;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.View;

import java.util.Map;

@Controller
public class UiController {

    @Secured(SecurityRule.IS_ANONYMOUS)
    @View("index")
    @Get
    public HttpResponse<Map> index() {
        return HttpResponse.ok(Map.of());
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @View("start")
    @Get("/getting-started")
    public HttpResponse<Map> start() {
        return HttpResponse.ok(Map.of());
    }
}

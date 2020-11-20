package turbo.funicular.web

import io.micronaut.security.authentication.Authentication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import turbo.funicular.service.GitHubService
import turbo.funicular.service.UsersService

import javax.inject.Inject

@MicronautTest
class UiControllerSpecs extends Specification {
    @Inject
    UsersService usersService
    @Inject
    GitHubService gitHubService

    def foo() {
        given:
            def authentication = Stub(Authentication)
            def controller = new UiController(usersService, gitHubService)
        when:
            def model = controller.model(authentication)
        then:
            model
    }
}

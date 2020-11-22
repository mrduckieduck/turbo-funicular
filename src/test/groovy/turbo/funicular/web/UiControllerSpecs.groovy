package turbo.funicular.web

import io.micronaut.security.authentication.Authentication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.kohsuke.github.GHUser
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

    def 'should test the model generation for auth user at home page'() {
        given:
            def ghUser = Stub(GHUser)
            def authentication = Stub(Authentication)
            authentication.attributes.put('ghUser', ghUser)
            def controller = new UiController(usersService, gitHubService)
        when:
            def model = controller.model(authentication)
        then:
            model
    }

    def 'should test the start page for null auth'() {
        given:
            def ghUser = Stub(GHUser)
            def authentication = Stub(Authentication)
            authentication.attributes.put('ghUser', ghUser)

            def controller = new UiController(usersService, gitHubService)
        when:
            def response = controller.start(authentication)
        then:
            response
    }
}

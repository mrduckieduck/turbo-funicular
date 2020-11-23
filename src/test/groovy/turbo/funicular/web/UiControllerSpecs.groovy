package turbo.funicular.web

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.DefaultAuthentication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.kohsuke.github.GHUser
import spock.lang.Specification
import turbo.funicular.service.GitHubService
import turbo.funicular.service.UsersService

import javax.inject.Inject
import java.time.LocalDateTime

@MicronautTest
class UiControllerSpecs extends Specification {
    @Inject
    UsersService usersService

    @Inject
    GitHubService gitHubService

    def 'should test the model generation for auth user at home page'() {
        given:
            def attributes = [ghUser: Stub(GHUser)]
            def authentication = new DefaultAuthentication('username', attributes)
            def controller = new UiController(usersService, gitHubService)

        and: 'Inserting couple dummy users'
            def user = UserCommand.builder()
                .ghId(2L)
                .name('user name')
                .login('foo-login')
                .build()

            usersService.addUser(user)

        expect:
            controller.model(authentication)
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

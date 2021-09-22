package turbo.funicular.web

import io.micronaut.http.HttpStatus
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.DefaultAuthentication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.kohsuke.github.GHUser
import spock.lang.Specification
import turbo.funicular.entity.User
import turbo.funicular.service.GithubService
import turbo.funicular.service.UsersService

import javax.inject.Inject

@MicronautTest
class UiControllerSpecs extends Specification {
    @Inject
    UsersService usersService

    @Inject
    GithubService gitHubService

    def 'should test the model generation for auth user at home page'() {
        given:
            def attributes = [ghUser: Stub(GHUser)]
            def authentication = new DefaultAuthentication('username', attributes)
            def controller = new UiController(usersService, gitHubService)

        and: 'Inserting couple dummy users'
            ['username', 'foo-username'].eachWithIndex { username, idx ->
                def userCommand = UserCommand.builder()
                    .ghId(idx as long)
                    .name(username)
                    .login("$username-login")
                    .build()
                usersService.addUser(userCommand)
            }

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

    def 'should test the profile page'() {
        given:
            def controller = new UiController(usersService, gitHubService)

        and: 'Inserting couple dummy users'
            def userCommand = UserCommand.builder()
                .ghId(12345L)
                .name('username')
                .login("foo-login")
                .build()
            usersService.addUser(userCommand)

        expect:
            controller.featuredUser(userCommand.login).attributes.every {
                it.key in ['ghUser', 'gists']
                it.value.class in [User, List]
            }

        when:
            def notFound = controller.featuredUser('fooUsersServiceSpecs')
        then: 'should fail and return a 404'
            notFound.status().code == HttpStatus.NOT_FOUND.code

    }
}

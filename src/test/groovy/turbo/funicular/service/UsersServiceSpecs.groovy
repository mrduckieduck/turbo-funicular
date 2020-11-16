package turbo.funicular.service

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import turbo.funicular.web.UserCommand

import javax.inject.Inject
import javax.transaction.Transactional

@MicronautTest
@Transactional
class UsersServiceSpecs extends Specification {

    @Inject
    UsersService usersService

    void 'should add a user to the database'() {
        given:
            def userCommand = UserCommand.builder()
                .ghId(1)
                .login('dd')
                .build()
            def user = usersService.addUser(userCommand)
        expect:
            user.present
    }
}

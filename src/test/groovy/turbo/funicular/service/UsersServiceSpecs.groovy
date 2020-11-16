package turbo.funicular.service

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import turbo.funicular.web.UserCommand

import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException

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

    void "should validate the users' properties"() {
        when: 'send a null command'
            usersService.addUser(null)
        then: 'should get a violation error'
            def validationException = thrown(ConstraintViolationException)
            validationException.constraintViolations.size() == 1
        when: 'send a command with no required values set'
            def userCommand = UserCommand.builder()
                .build()
            usersService.addUser(userCommand)
        then: 'should get two violation errors'
            validationException = thrown(ConstraintViolationException)
            validationException.constraintViolations.size() == 2
        when: 'send a empty login'
            userCommand = UserCommand.builder()
                .login('')
                .build()
            usersService.addUser(userCommand)
        then: 'should get two violation errors'
            validationException = thrown(ConstraintViolationException)
            validationException.constraintViolations.size() == 2
        when: 'send a command with only the user name set'
            userCommand = UserCommand.builder()
                .login("foo")
                .build()
            usersService.addUser(userCommand)
        then: 'should get one violation error'
            validationException = thrown(ConstraintViolationException)
            validationException.constraintViolations.size() == 1
        when: 'send a command with only the user id set'
            userCommand = UserCommand.builder()
                .ghId(1)
                .build()
            usersService.addUser(userCommand)
        then: 'should get one violation error'
            validationException = thrown(ConstraintViolationException)
            validationException.constraintViolations.size() == 1
    }

}

package turbo.funicular.service

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import turbo.funicular.entity.UserRepository
import turbo.funicular.service.exceptions.DuplicatedEntityException
import turbo.funicular.web.UserCommand

import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException

@MicronautTest
@Transactional
class UsersServiceSpecs extends Specification {

    @Inject
    UsersService usersService
    @Inject
    UserRepository userRepository

    def 'should add a user to the database'() {
        given:
            def userCommand = UserCommand.builder()
                .ghId(1)
                .login('dd')
                .build()
            def user = usersService.addUser(userCommand)
        expect:
            user.present
    }

    def "should validate the users' properties"() {
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

    def 'should validate duplicated users on add operation'() {
        given: 'Add directly an user to the database, using the repository'
            def userCommand = UserCommand.builder()
                .ghId(1)
                .login('foo')
                .build()
            def user = UsersMapper.USERS_MAPPER.commandToEntity(userCommand)
            userRepository.save(user)
        expect: 'to have only one user'
            userRepository.count() == 1
        when: 'trying to add again the same user, this time using the service'
            usersService.addUser(userCommand)
        then: 'The service should raise an exception to prevent adding a duplicated user'
            def duplicatedEntityException = thrown(DuplicatedEntityException)
            duplicatedEntityException.entityName == 'User'
            duplicatedEntityException.duplicatedIdentifier == userCommand.getLogin() + ", " + userCommand.getGhId()
    }

}

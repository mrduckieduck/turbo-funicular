package turbo.funicular.service

import com.github.javafaker.Faker
import dev.mrpato.failure.entity.Failure
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import turbo.funicular.entity.User
import turbo.funicular.entity.UserRepository
import turbo.funicular.web.UserCommand

import javax.inject.Inject
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException
import java.util.stream.Collectors

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
            user.right
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
            def user = usersService.addUser(userCommand)
            def errors = user.getLeft()
        then: 'should get two violation errors'
            errors.reason == 'Failed validations!'
            errors.code == 'user.failure.constraints-violations'
            errors.details.every {
                it.codeMessage in ['user.failure.field.ghId', 'user.failure.field.login']
                it.type in [Failure.ErrorType.VALIDATION]
                it.path in ['ghId', 'login']
            }
        when: 'send a empty login'
            userCommand = UserCommand.builder()
                .login('')
                .build()
            user = usersService.addUser(userCommand)
            errors = user.getLeft()
        then: 'should get two violation errors'
            errors.reason == 'Failed validations!'
            errors.code == 'user.failure.constraints-violations'
            errors.details.every {
                it.codeMessage in ['user.failure.field.ghId', 'user.failure.field.login']
                it.type in [Failure.ErrorType.VALIDATION]
                it.path in ['ghId', 'login']
            }
        when: 'send a command with only the user name set'
            userCommand = UserCommand.builder()
                .login("foo")
                .build()
            def user1 = usersService.addUser(userCommand)
            errors = user1.getLeft()
        then: 'should get one violation error'
            errors.reason == 'Failed validations!'
            errors.code == 'user.failure.constraints-violations'
            errors.details.every {
                it.codeMessage in ['user.failure.field.ghId']
                it.type in [Failure.ErrorType.VALIDATION]
                it.path in ['ghId']
            }
        when: 'send a command with only the user id set'
            userCommand = UserCommand.builder()
                .ghId(1)
                .build()
            def user2 = usersService.addUser(userCommand)
            errors = user2.getLeft()
        then: 'should get one violation error'
            errors.reason == 'Failed validations!'
            errors.code == 'user.failure.constraints-violations'
            errors.details.every {
                it.codeMessage in ['user.failure.field.login']
                it.type in [Failure.ErrorType.VALIDATION]
                it.path in ['login']
            }
    }

    def 'should validate duplicated users on add operation'() {
        given: 'Add directly an user to the database, using the repository'
            def userCount = userRepository.count()
            def userCommand = UserCommand.builder()
                .ghId(1)
                .login('foo')
                .build()
            def user = UsersMapper.USERS_MAPPER.commandToEntity(userCommand)
            userRepository.save(user)
        expect: 'to have only one user'
            userRepository.count() == userCount + 1
        when: 'trying to add again the same user, this time using the service'
            def either = usersService.addUser(userCommand)
        then: 'The service should return an error to prevent adding a duplicated user'
            either.isLeft()
            with(either.getLeft()) {
                code == 'user.failure.alreadyExists'
                reason == 'User already exists'
            }
    }

    def 'should verify the functionality in the random user selection'() {
        given: 'Create 5 new users'
            def userCount = userRepository.count()
            5.times { usersService.addUser(fakeUser(it)) }
        expect: 'To have only the 5 new users'
            userRepository.count() == 5 + userCount
        when: 'ask for 10 random users'
            def randomTop = usersService.randomTop(10)
        then: 'return only the 5 existing users'
            randomTop.get().size() == 5 + userCount
        when: 'ask for 5 random users'
            randomTop = usersService.randomTop(5 + userCount)
        then: 'return only the 5 existing users'
            randomTop.get().size() == 5 + userCount
        when: 'create another 15 users'
            (5..19).each { usersService.addUserIfMissing(fakeUser(it)) }
        then: 'verify we have 20 users'
            userRepository.count() == 20 + userCount
        when: 'ask for 15 random users'
            def count = 15
            randomTop = usersService.randomTop(count)
        then: 'verify we got the requested random unique users'
            //we generate another list, because the service returns an immutable list.
            def uniqueUsers = randomTop.get().stream()
                .collect(Collectors.toList())
                .unique { User u1, User u2 -> u1.getId() <=> u2.getId() }
            uniqueUsers.size() == count
    }

    def 'should get an user by its login'() {
        given:
            def id = userRepository.count() + 1L
            def user = fakeUser(id)
            usersService.addUser(user)
        expect:
            usersService.findUser(user.login).filter { it.ghId == id }.defined
    }

    static UserCommand fakeUser(Long id) {
        def faker = new Faker()

        return UserCommand.builder()
            .ghId(id)
            .login(faker.name().username())
            .name(faker.name().name())
            .avatarUrl(faker.internet().avatar())
            .bio(faker.superhero().descriptor())
            .publicGistsCount(faker.number().randomDigit())
            .build()
    }
}

package turbo.funicular.entity

import com.github.javafaker.Faker
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import javax.transaction.Transactional

@MicronautTest
@Transactional
class UserRepositorySpecs extends Specification {

    @Inject
    UserRepository userRepository
    Faker faker = new Faker()

    def 'should add a user to the database'() {
        given:
            def userCount = userRepository.count()
            def username = faker.name().username()
            def userId = userCount + 1
            def user = new User(login: username, ghId: userId)
            userRepository.save(user)
        expect:
            userRepository.findAll().size() == (userCount + 1)
        when:
            def userFound = userRepository.findUserWith(username, userId)
        then:
            userFound.present
            def entity = userFound.get()
            entity.createdAt
            entity.lastUpdated
        when:
            def lastUpdated = entity.lastUpdated
            user.avatarUrl = 'bar'
            def updatedUser = userRepository.update(user)
        then:
            updatedUser.lastUpdated != lastUpdated
    }
}

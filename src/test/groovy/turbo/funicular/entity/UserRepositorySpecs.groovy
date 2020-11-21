package turbo.funicular.entity

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import javax.transaction.Transactional

@MicronautTest
@Transactional
class UserRepositorySpecs extends Specification {

    @Inject
    UserRepository userRepository

    def 'should add a user to the database'() {
        given:
            def user = new User(login: 'domix', ghId: 21805)
            userRepository.save(user)
        expect:
            userRepository.findAll().size() == 1
        when:
            def userFound = userRepository.findUserWith('domix', 21805)
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

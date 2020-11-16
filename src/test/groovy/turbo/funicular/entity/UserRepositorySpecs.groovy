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

    void 'should add a user to the database'() {
        given:
            def user = new User(login: 'domix', ghId: 21805)
            userRepository.save(user)
        expect:
            userRepository.findAll().size() == 1
    }
}

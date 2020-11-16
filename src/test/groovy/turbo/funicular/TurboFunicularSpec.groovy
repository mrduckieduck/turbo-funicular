package turbo.funicular

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import turbo.funicular.entity.User
import turbo.funicular.entity.UserRepository

import javax.inject.Inject
import javax.transaction.Transactional

@MicronautTest
@Transactional
class TurboFunicularSpec extends Specification {

    @Inject
    EmbeddedApplication<?> application

    @Inject
    UserRepository userRepository

    void 'test it works'() {
        expect:
            application.running
    }

    void 'ssss'() {
        given:
            def user = new User(username: 'domix')
            userRepository.save(user)
        expect:
            userRepository.findAll().size() == 1
    }

}

package turbo.funicular.service

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import turbo.funicular.entity.User

import javax.inject.Inject

@MicronautTest
class ValidationServiceSpecs extends Specification {
    @Inject
    ValidationService validationService

    def foo() {
        when:
            def persono = new User()
            def foo = validationService.validate(persono)
        then:
            foo.isInvalid()
            def left = foo.getError()
            left.size() == 2
        when:
            persono = new User(login: 'fdf', ghId: 1L)
            foo = validationService.validate(persono)
        then:
            foo.isValid()
            foo.get()
    }
}

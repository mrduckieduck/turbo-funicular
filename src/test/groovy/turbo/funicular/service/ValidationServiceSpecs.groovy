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
            def person = new User()
            def foo = validationService.validate(person, UserValidator.PREFIX_FAILURE_CODE)
        then:
            foo.isInvalid()
            def left = foo.getError()
            left.details.size() == 2
        when:
            person = new User(login: 'fdf', ghId: 1L)
            foo = validationService.validate(person, UserValidator.PREFIX_FAILURE_CODE)
        then:
            foo.isValid()
            foo.get()
    }
}

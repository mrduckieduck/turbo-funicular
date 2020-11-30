package turbo.funicular.service;

import io.micronaut.context.MessageSource;
import io.micronaut.validation.validator.Validator;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;

import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Predicates.is;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

@Singleton
@RequiredArgsConstructor
public class ValidationService {
    private final Validator validator;
    private final MessageSource messageSource;
    private final MessageSource.MessageContext messageContext;

    public <T> Either<List<String>, T> validateFoo(T toValidate) {
        final var violations = validator.validate(toValidate);

        return Match(violations.isEmpty()).of(
            Case($(is(true)), right(toValidate)),
            Case($(is(false)), left(toList(violations)))
        );
    }

    public <T> List<String> toList(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
    }

}

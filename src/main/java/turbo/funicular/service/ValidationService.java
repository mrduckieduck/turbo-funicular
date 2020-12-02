package turbo.funicular.service;

import io.micronaut.validation.validator.Validator;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;

import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vavr.API.*;
import static io.vavr.Predicates.is;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;

@Singleton
@RequiredArgsConstructor
public class ValidationService {
    private final Validator validator;

    public <T> Validation<List<String>, T> validate(T toValidate) {
        final var violations = validator.validate(toValidate);

        return Match(violations.isEmpty()).of(
            Case($(is(true)), valid(toValidate)),
            Case($(is(false)), invalid(toList(violations)))
        );
    }

    public <T> List<String> toList(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
    }

}

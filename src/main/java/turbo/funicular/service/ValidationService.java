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

    /**
     * Validates a Java class that uses Bean Validation annotations.
     *
     * @param toValidate The bean to validate
     * @param <T> The bean generic type
     * @return The validation result, either could be a error list or the bean itself
     */
    public <T> Validation<List<String>, T> validate(T toValidate) {
        final var violations = validator.validate(toValidate);

        return Match(violations.isEmpty()).of(
            Case($(is(true)), valid(toValidate)),
            Case($(is(false)), invalid(toList(violations)))
        );
    }

    /**
     * Converts the bean violations constraints to a "business" error representation.
     *
     * For simplicity, this example only generates a String
     *
     * @param violations The given violations
     * @param <T> The bean generic type
     * @return A List with the "business" error messages.
     */
    public <T> List<String> toList(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
    }

}

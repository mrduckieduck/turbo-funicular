package turbo.funicular.service;

import io.micronaut.context.MessageSource;
import io.micronaut.validation.validator.Validator;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import turbo.funicular.entity.Failure;

import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.is;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;

@Singleton
@RequiredArgsConstructor
public class ValidationService {

    private final Validator validator;
    private final MessageSource messageSource;
    private final MessageSource.MessageContext messageContext;


    /**
     * Validates a Java class that uses Bean Validation annotations.
     *
     * @param toValidate The bean to validate
     * @param <T>        The bean generic type
     * @return The validation result, either could be a error list or the bean itself
     */
    public <T> Validation<Failure, T> validate(final T toValidate,
                                               final String errorCodePrefix) {
        final var violations = validator.validate(toValidate);
        return Match(violations.isEmpty()).of(
            Case($(is(true)), valid(toValidate)),
            Case($(is(false)), invalid(toFailure(violations, errorCodePrefix)))
        );
    }

    public <T> Failure toFailure(final Set<ConstraintViolation<T>> violations,
                                 final String errorCodePrefix) {
        final var details = violations.stream()
            .map(violation -> violationToFailureDetail(violation, errorCodePrefix))
            .collect(Collectors.toList());
        return Failure.builder()
            .reason("Failed validations!")
            .code("%s.constraints-violations".formatted(errorCodePrefix))
            .details(details)
            .build();
    }

    private <T> Failure.Detail violationToFailureDetail(final ConstraintViolation<T> violation,
                                                        final String errorCodePrefix) {
        final var field = violation.getPropertyPath().iterator().next();
        final var codeMessage = "%s.field.%s".formatted(errorCodePrefix, field.getName());
        final var localizedMessage = messageSource.getMessage(codeMessage, messageContext)
            .orElse(StringUtils.EMPTY);
        return Failure.Detail.builder()
            .type(Failure.ErrorType.VALIDATION)
            .path(violation.getPropertyPath().toString())
            .codeMessage(codeMessage)
            .localizedMessage(localizedMessage)
            .build();
    }

}

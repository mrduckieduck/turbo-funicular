package turbo.funicular.service;

import io.micronaut.context.MessageSource;
import io.micronaut.validation.validator.Validator;
import lombok.RequiredArgsConstructor;

import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

@Singleton
@RequiredArgsConstructor
public class ValidationService {
    private final Validator validator;
    private final MessageSource messageSource;
    private final MessageSource.MessageContext messageContext;

    public void validate(Object toValidate) {
        validate(toValidate, "validation.default.message");
    }

    public void validate(Object toValidate, String messageCode) {
        Set<ConstraintViolation<Object>> violations = validator.validate(toValidate);
        if (!violations.isEmpty()) {
            String message = messageSource
                .getMessage(messageCode, messageContext)
                .orElse("Validation error found.");
            throw new ConstraintViolationException(message, violations);
        }
    }
}

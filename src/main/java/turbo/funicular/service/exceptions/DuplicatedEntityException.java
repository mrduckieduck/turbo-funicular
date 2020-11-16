package turbo.funicular.service.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DuplicatedEntityException extends BusinessException {
    private final String entityName;
    private final String duplicatedIdentifier;
}

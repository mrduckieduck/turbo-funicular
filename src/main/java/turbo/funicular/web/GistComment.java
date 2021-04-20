package turbo.funicular.web;

import io.micronaut.core.annotation.Introspected;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import turbo.funicular.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Introspected
public class GistComment {

    private final User owner;
    private final long id;
    private final LocalDateTime createdAt;
    private final String body;

}

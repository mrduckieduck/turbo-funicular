package turbo.funicular.web;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Getter;
import turbo.funicular.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder
@Introspected
public class GistComment {

    private final User owner;
    private final long id;
    private final LocalDateTime createdAt;
    private final String body;

}

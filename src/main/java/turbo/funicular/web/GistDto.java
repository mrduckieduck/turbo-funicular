package turbo.funicular.web;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Introspected
public class GistDto {
    private final Integer commentsCount;
    private final LocalDateTime createdAt;
    private final String description;
    private final String ghId;
    private final LocalDateTime updatedAt;
    private final Boolean publicGist;
}

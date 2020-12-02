package turbo.funicular.web;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Introspected
public class GistDto {
    private final Integer commentsCount;
    private final LocalDateTime createdAt;
    private final String description;
    private final String ghId;
    private final LocalDateTime updatedAt;
    private final Boolean publicGist;
    private final String owner;
    private final List<GistContent> files;
}

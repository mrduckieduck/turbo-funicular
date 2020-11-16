package turbo.funicular.web;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
public class GistDto {
    private Integer commentsCount;
    private LocalDateTime createdAt;
    private String description;
    private String ghId;
    private LocalDateTime updatedAt;
    private Boolean publicGist;
}

package turbo.funicular.web;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Introspected
public class GistContent {
    private final String filename;
    private final String language;
    private final Integer size;
    private final String mimeType;
    private final String rawUrl;
}

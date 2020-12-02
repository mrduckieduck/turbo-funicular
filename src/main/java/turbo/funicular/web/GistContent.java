package turbo.funicular.web;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Introspected
public class GistContent {
    private final String filename;
    private final String language;
    private final Integer size;
    private final String mimeType;
    private final String rawUrl;
    private final String content;
}

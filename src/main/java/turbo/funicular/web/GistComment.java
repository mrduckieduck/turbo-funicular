package turbo.funicular.web;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import turbo.funicular.entity.User;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@ToString
@Getter
@Builder
public class GistComment {

    private final User owner;
    private final long id;
    private final LocalDateTime createdAt;
    private final String body;

}

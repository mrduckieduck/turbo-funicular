package turbo.funicular.web;

import lombok.*;
import turbo.funicular.entity.User;

import java.time.LocalDate;

@RequiredArgsConstructor
@ToString
@Getter
@Builder
public class GistComment {

    private final User owner;
    private final long id;
    private final LocalDate createdAt;
    private final String body;

}

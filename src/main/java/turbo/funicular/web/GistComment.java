package turbo.funicular.web;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@ToString
@Getter
@Builder
public class GistComment {

    private final String login;
    private final long id;
    private final LocalDate createdAt;
    private final String body;

}

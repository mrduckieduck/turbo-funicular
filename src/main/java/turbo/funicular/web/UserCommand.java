package turbo.funicular.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCommand {
    private Long id;
    private String login;
    private String avatarUrl;
    private String bio;
    private Long ghId;
    private String name;
    private Integer publicGistsCount;
}

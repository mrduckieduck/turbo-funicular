package turbo.funicular.web;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
public class UserCommand {
    @NotBlank
    private String login;
    @Size(max = 2000)
    private String avatarUrl;
    @Size(max = 2000)
    private String bio;
    @NotNull
    private Long ghId;
    @Size(max = 200)
    private String name;
    private Integer publicGistsCount;
}

package turbo.funicular.entity;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Setter
@Getter
@Table(name = "users")
@Introspected
public class User {
    @Id
    @GeneratedValue
    private Long id;
    @NotBlank
    @Size(max = 200)
    private String login;
    @Size(max = 2000)
    private String avatarUrl;
    @Size(max = 2000)
    private String bio;
    @NotNull
    private Long ghId;
    @Size(max = 200)
    private String name;
    @Column(name = "public_gists")
    private Integer publicGistsCount;
    private Boolean publicProfile;
}

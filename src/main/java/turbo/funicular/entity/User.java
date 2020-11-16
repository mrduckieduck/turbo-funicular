package turbo.funicular.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Setter
@Getter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String login;
    private String avatarUrl;
    private String bio;
    private Long ghId;
    private String name;
    @Column(name = "public_gists")
    private Integer publicGistsCount;
    private Boolean publicProfile;
}

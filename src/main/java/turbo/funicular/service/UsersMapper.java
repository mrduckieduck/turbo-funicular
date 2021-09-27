package turbo.funicular.service;

import org.kohsuke.github.GHUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import turbo.funicular.entity.User;
import turbo.funicular.web.UserCommand;

import java.io.IOException;

@Mapper
public interface UsersMapper {
    UsersMapper USERS_MAPPER = Mappers.getMapper(UsersMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicProfile", ignore = true)
    User commandToEntity(UserCommand command);

    UserCommand entityToCommand(User entity);

    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(source = "id", target = "ghId")
    @Mapping(source = "publicGistCount", target = "publicGistsCount")
    UserCommand githubToCommand(GHUser user) throws IOException;

    @Mapping(source = "company", target = "bio")
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(source = "id", target = "ghId")
    @Mapping(source = "publicGists", target = "publicGistsCount")
    @Mapping(target = "publicProfile", ignore = true)
    User githubToEntity(org.eclipse.egit.github.core.User user);
}

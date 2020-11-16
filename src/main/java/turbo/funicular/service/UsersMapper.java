package turbo.funicular.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import turbo.funicular.entity.User;
import turbo.funicular.web.UserCommand;

@Mapper
public interface UsersMapper {
    UsersMapper INSTANCE = Mappers.getMapper(UsersMapper.class);

    @Mapping(target = "publicProfile", ignore = true)
    User commandToEntity(UserCommand command);

    UserCommand entityToCommand(User entity);
}

package turbo.funicular.service;

import io.vavr.collection.Stream;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import turbo.funicular.web.GistComment;
import turbo.funicular.web.GistContent;
import turbo.funicular.web.GistDto;

import java.util.List;
import java.util.Map;

@Mapper(uses = {UsersMapper.class})
public interface GistMapper {

    GistMapper GIST_MAPPER = Mappers.getMapper(GistMapper.class);

    @Mapping(target = "commentsCount", source = "comments")
    @Mapping(target = "ghId", source = "id")
    @Mapping(target = "publicGist", source = "public")
    @Mapping(target = "owner", source = "user.login")
    @Mapping(target = "files", qualifiedByName = "filesToListOfGistContent")
    GistDto githubToGistDto(Gist gist);

    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "language", ignore = true)
    GistContent githubToGistContent(GistFile gistFile);

    @Mapping(target = "owner", source = "user")
    GistComment githubToGistComment(Comment comment);

    @Named("filesToListOfGistContent")
    default List<GistContent> filesToGistContentList(Map<String, GistFile> gistFiles) {
        return Stream.ofAll(gistFiles.values())
            .map(this::githubToGistContent)
            .toJavaList();
    }

}
package turbo.funicular.service;

import io.vavr.control.Either;
import turbo.funicular.entity.Failure;
import turbo.funicular.entity.User;
import turbo.funicular.web.GistComment;
import turbo.funicular.web.GistDto;

import java.util.List;

public interface GithubService {

    Either<Failure, List<GistDto>> findGistsByUser(String login);

    Either<Failure, User> findUserByLogin(String login);

    Either<Failure, GistDto> findGistById(String ghId);

    Either<Failure, List<GistComment>> topGistComments(String gistId, int count);

    Either<Failure, GistComment> addCommentToGist(String gistId, String comment);

}
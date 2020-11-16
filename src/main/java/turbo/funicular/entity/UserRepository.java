package turbo.funicular.entity;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

import static io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES;

@JdbcRepository(dialect = POSTGRES)
public interface UserRepository extends CrudRepository<User, Long> {
    @Query("select * from users u where u.login = :login or u.gh_id = :id")
    Optional<User> findUser(String login, Long id);
}

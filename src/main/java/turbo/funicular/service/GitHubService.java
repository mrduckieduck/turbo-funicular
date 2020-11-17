package turbo.funicular.service;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import turbo.funicular.web.GistDto;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class GitHubService {
    private final Faker faker = new Faker();

    public List<GistDto> findGistsByUser(String login) {
        //TODO: we need the GitHub client #7
        return List.of(fake(), fake(), fake());
    }

    private GistDto fake() {
        return GistDto.builder()
            .commentsCount(faker.number().randomDigit())
            .createdAt(convertToLocalDateTimeViaInstant(faker.date().past(200, TimeUnit.DAYS)))
            .description(faker.lorem().sentence(200))
            .ghId(faker.internet().uuid())
            .publicGist(true)
            .updatedAt(convertToLocalDateTimeViaInstant(faker.date().past(100, TimeUnit.DAYS)))
            .build();
    }

    public LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }
}

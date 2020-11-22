package turbo.funicular.security

import com.github.javafaker.Faker
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.reactivex.Flowable
import org.kohsuke.github.GHMyself
import org.kohsuke.github.GitHub
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class UserDetailsMapperSpecs extends Specification {
    @Inject
    UserDetailsMapper userDetailsMapper
    Faker faker = new Faker()

    def 'should test the failed cases'() {
        when:
            def tokenResponse = new TokenResponse()
            def responses = userDetailsMapper.createAuthenticationResponse(tokenResponse, null)

            Flowable.fromPublisher(responses)
                .blockingFirst()
        then:
            thrown(IllegalStateException)
        when:
            def details = userDetailsMapper.createUserDetails(null)
            Flowable.fromPublisher(details)
                .blockingFirst()
        then:
            thrown(UnsupportedOperationException)
    }

    def 'should build the userDetails'() {
        given:
            def username = faker.name().username()
            def github = Stub(GitHub)
            def myself = Stub(GHMyself)
            myself.getId() >> 1l
            myself.getLogin() >> username

            github.getMyself() >> myself
            def accessToken = 'fooo'
            def details = userDetailsMapper.buildDetails(github, accessToken)
        expect:
            details.username == username
            details.getRoles() == UserDetailsMapper.ROLES
    }
}

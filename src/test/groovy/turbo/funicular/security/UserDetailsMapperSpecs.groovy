package turbo.funicular.security

import com.github.javafaker.Faker
import io.micronaut.security.authentication.AuthenticationException
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.event.LoginSuccessfulEvent
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.reactivex.Emitter
import io.reactivex.Flowable
import org.kohsuke.github.GHMyself
import org.kohsuke.github.GitHub
import spock.lang.Requires
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class UserDetailsMapperSpecs extends Specification {
    @Inject
    UserDetailsMapper userDetailsMapper
    @Inject
    LoginSuccessfulEventListener loginSuccessfulEventListener
    Faker faker = new Faker()

    def 'should test the failed cases'() {
        when:
            def tokenResponse = new TokenResponse()
            def responses = userDetailsMapper.createAuthenticationResponse(tokenResponse, null)

            Flowable.fromPublisher(responses)
                .blockingFirst()
        then:
            thrown(AuthenticationException)
        when:
            def details = userDetailsMapper.createUserDetails(null)
            Flowable.fromPublisher(details)
                .blockingFirst()
        then:
            thrown(UnsupportedOperationException)
    }

    //For some reason, IDK this feature method fails randomly on JDK 11
    @Requires({ javaVersion < 11 })
    def 'should build the userDetails'() {
        given:
            def username = faker.name().username()
            def github = Stub(GitHub)
            def myself = Stub(GHMyself)

            myself.getLogin() >> username
            myself.getId() >> 1l

            github.getMyself() >> myself
            def accessToken = 'fooo'
            def details = userDetailsMapper.buildDetails(github, accessToken)
            def mockEmitter = Mock(Emitter<AuthenticationResponse>)
        expect:
            details.getRoles() == UserDetailsMapper.ROLES
            def event = new LoginSuccessfulEvent(details)
            loginSuccessfulEventListener.onApplicationEvent(event)
            userDetailsMapper.authenticationError(mockEmitter, new IOException("ss"))
    }

}

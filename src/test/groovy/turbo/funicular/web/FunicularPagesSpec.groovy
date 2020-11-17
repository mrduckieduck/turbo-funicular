package turbo.funicular.web

import geb.Page
import geb.spock.GebSpec
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest
class FunicularPagesSpec extends GebSpec {
    @Inject
    EmbeddedServer embeddedServer

    def "can access Funicular homepage"() {
        given:
            browser.baseUrl = "http://localhost:${ embeddedServer.port }"
        when:
            to HomePage
        then:
            at HomePage
        when: 'click the start link'
            HomePage homePage = browser.page HomePage
            homePage.start()
        then:
            at StartPage
    }

}


class HomePage extends Page {
    static url = "/"
    static at = { title == "Home" }
    static content = {
        startButton { $('#start-button') }
    }

    void start() {
        startButton.click()
    }
}

class StartPage extends Page {
    static url = "/getting-started"
    static at = { title == "Log In to Funicular" }
    static content = {
    }
}
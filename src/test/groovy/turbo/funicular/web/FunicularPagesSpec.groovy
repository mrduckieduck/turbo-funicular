package turbo.funicular.web

import geb.Page
import geb.spock.GebSpec
import spock.lang.Ignore

@Ignore
class FunicularPagesSpec extends GebSpec {

    def "can access Funicular homepage"() {
        given:
            to HomePage
        when:
            true
            //manualsMenu.open()
            //.links[0].click()
        then:
            true
            //at TheBookOfGebPage
    }

}


class HomePage extends Page {
    static url = "/"
    static at = { title == "Geb - Very Groovy Browser Automation" }
    static content = {
        //manualsMenu { module(ManualsMenuModule) }
    }
}
package turbo.funicular.service


import spock.lang.Specification

class GithubApiClientSpecs extends Specification {

    GithubApiClient githubApiClient = new GithubApiClient()

    void 'Test github operations for user #login'() {
        expect:
            githubApiClient.findGistsByUser(login)

        when:
            def user = githubApiClient.getUser(login)
        then: 'Check user info'
            user.present
            with(user.get()) {
                println(it)
                it.login == login
                it.avatarUrl.contains(ghId.toString())
                it.name == expectedName
                it.ghId
                it.publicGistsCount
            }

        where:
            login          | expectedName
            'mrduckieduck' | 'Daniel Castillo'
            'domix'        | 'Domingo Suarez Torres'
    }

}
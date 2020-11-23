package turbo.funicular.service


import spock.lang.Specification

class GithubApiClientSpecs extends Specification {

    GithubApiClient githubApiClient = GithubApiClient.create()

    void 'Test github operations for user #login'() {
        expect:
            githubApiClient.findGistsByUser(login).every {
                it.createdAt
                it.updatedAt
                it.ghId
                it.files.every {
                    it.content
                    it.rawUrl
                    it.language
                    it.mimeType
                    it.filename
                    it.size
                }
            }

        when:
            def user = githubApiClient.findUser(login)
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
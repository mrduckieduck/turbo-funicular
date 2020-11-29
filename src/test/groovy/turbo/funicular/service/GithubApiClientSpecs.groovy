package turbo.funicular.service

import org.eclipse.egit.github.core.Comment
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.service.GistService
import org.eclipse.egit.github.core.service.UserService
import spock.lang.Specification

class GithubApiClientSpecs extends Specification {

    void 'Test github operations for user #login'() {
        given:
            def githubApiClient = GithubApiClient.create()
        expect:
            githubApiClient.findGistsByUser(login).every {
                it.createdAt
                it.updatedAt
                it.ghId
                it.files.every {
                    it.content
                    it.rawUrl
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
                it.avatarUrl
                it.publicGistsCount
            }

        where:
            login          | expectedName
            'mrduckieduck' | 'Daniel Castillo'
            'domix'        | 'Domingo Suarez Torres'
    }

    void 'Test github operations for gists'() {
        given:
            def githubApiClient = GithubApiClient.create()
            def gists = githubApiClient.findGistsByUser('mrduckieduck')
            def gistId = gists[0].ghId
        expect:
            !gists.empty && gistId

        when:
            def gist = githubApiClient.findGistById(gistId)
        then:
            gist.present
            verifyAll(gist.get()) {
                it.createdAt
                it.updatedAt
                it.ghId
                it.files.every {
                    it.content
                    it.rawUrl
                    it.filename
                    it.size
                }
            }

        when:
            def comments = githubApiClient.topGistComments(gistId, 10)

        then:
            comments.every {
                it.body
                it.createdAt
                it.owner.login
            }

    }

    def 'Test gists comments'() {
        given:
            def gistsService = Stub(GistService)
            def githubApiClient = new GithubApiClient(Stub(UserService), gistsService)
            def gistId = 'fooo'

        and:
            gistsService.createComment(_ as String, _ as String) >> Stub(Comment) {
                it.id >> 1l
                it.body >> 'body'
                it.createdAt >> new Date()
                it.getUser() >> Stub(User) {
                    it.login >> 'mrduckieduck'
                    it.avatarUrl >> 'avatar-url'
                    it.name >> 'name'
                }
            }
            gistsService.deleteComment(1l)
            gistsService.deleteComment(Integer.MAX_VALUE) >> { throw new RuntimeException() }
            gistsService.getComments(_ as String) >> List.of(Stub(Comment))

        when:
            def newComment = githubApiClient.addCommentToGist(gistId, 'Pretty comment!')
        then:
            newComment.present
            verifyAll(newComment.get()) {
                it.body == 'body'
                it.createdAt
                it.owner.login == 'mrduckieduck'
                it.owner.avatarUrl == 'avatar-url'
                it.owner.name == 'name'
            }

        when:
            newComment
                .ifPresent { githubApiClient.deleteCommentFromGist(it.id) }
        then:
            noExceptionThrown()
        and:
            githubApiClient.topGistComments(gistId, 10).size() > 0

        when:
            githubApiClient.deleteCommentFromGist(Integer.MAX_VALUE)

        then:
            thrown(RuntimeException)
    }

}
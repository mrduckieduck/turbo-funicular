package turbo.funicular.service

import com.github.javafaker.Faker
import org.eclipse.egit.github.core.Comment
import org.eclipse.egit.github.core.Gist
import org.eclipse.egit.github.core.GistFile
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.service.GistService
import org.eclipse.egit.github.core.service.UserService
import spock.lang.Specification

class GithubApiClientSpecs extends Specification {

    def faker = new Faker()

    void 'Test github operations for user #login'() {
        given:
            def githubApiClient = GithubApiClient.create()

        when:
            def user = githubApiClient.findUser(login)

        then: 'Check user info'
            user.present
            with(user.get()) {
                it.login == login
                it.avatarUrl.contains(ghId.toString())
                it.name == expectedName
                it.ghId
                it.avatarUrl
                it.publicGistsCount
            }

        when: 'Get all gists of a user'
            def foundGists = githubApiClient.findGistsByUser(login)

        then:
            foundGists.isRight()
            foundGists.get().every {
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

        where:
            login          | expectedName
            'mrduckieduck' | 'Daniel Castillo'
            'domix'        | 'Domingo Suarez Torres'
    }

    void 'Test github operations for gists'() {
        given: 'Stubs, base objects'

            def login = faker.name().username()
            def gistsService = Stub(GistService)
            def githubApiClient = new GithubApiClient(Stub(UserService), gistsService)
            def gistId = faker.crypto().md5()

        and: 'Stubs configuration for gists'

            def file = faker.file()
            def gistFile = Stub(GistFile) {
                it.filename >> file.fileName()
                it.size >> 10
                it.rawUrl >> faker.internet().url()
                it.content >> faker.dune().quote()
            }
            gistsService.getGist(gistId) >> Stub(Gist) {
                it.id >> gistId
                it.createdAt >> new Date()
                it.updatedAt >> new Date()
                it.description >> faker.dune().quote()
                it.files >> Map.of(file.fileName(), gistFile)
                it.comments >> faker.number().randomDigitNotZero()
                it.public >> true
                it.user >> Stub(User) {
                    it.login >> faker.name().username()
                }
            }

        when:
            def gist = githubApiClient.findGistById(gistId)
        then:
            gist.isRight()
            verifyAll(gist.get()) {
                it.ghId == gistId
                it.createdAt
                it.updatedAt
                it.commentsCount
                it.description
                it.publicGist
                it.owner
                it.files.every {
                    it.content
                    it.rawUrl
                    it.filename
                    it.size
                }
            }

        when:
            def comments = githubApiClient.topGistComments(gistId, 1)

        then:
            comments.every {
                it.body
                it.createdAt
                it.owner.login == login
            }

    }

    void 'Test github operations for gist comments'() {
        given:
            def login = faker.name().username()
            def gistsService = Stub(GistService)
            def githubApiClient = new GithubApiClient(Stub(UserService), gistsService)
            def gistId = faker.crypto().md5()

        and:
            def comment = Stub(Comment) {
                it.id >> 1l
                it.body >> faker.dune().quote()
                it.createdAt >> new Date()
                it.getUser() >> Stub(User) {
                    it.login >> login
                    it.avatarUrl >> faker.avatar().image()
                    it.name >> faker.funnyName().name()
                }
            }
            gistsService.getComments(gistId) >> List.of(comment)
            gistsService.createComment(_ as String, _ as String) >> comment
            gistsService.createComment(_ as String, null) >> {
                throw new IllegalArgumentException("Ugly exception that needs to be caught!! (create comment - no comment)")
            }
            gistsService.createComment(null, _ as String) >> {
                throw new IllegalArgumentException("Ugly exception that needs to be caught!! (create comment - no gistId)")
            }
            gistsService.deleteComment(1l)
            gistsService.deleteComment(Integer.MAX_VALUE) >> {
                throw new IOException("Ugly exception that needs to be caught!! (delete comment)")
            }
            gistsService.getComments(_ as String) >> List.of(comment)

        expect:
            githubApiClient.topGistComments(gistId, 1).size() == 1

        when:
            def newComment = githubApiClient.addCommentToGist(gistId, faker.dune().quote())

        then:
            newComment.isRight()
            verifyAll(newComment.get()) {
                it.body
                it.createdAt
                it.owner.login == login
                it.owner.avatarUrl
                it.owner.name
            }

        when:
            def withError = githubApiClient.addCommentToGist(gistId, null)

        then:
            withError.isLeft()
            withError.getLeft().find { "Ugly exception that needs to be caught!! (create comment - no comment)" == it }

        when:
            withError = githubApiClient.addCommentToGist(null, faker.dune().quote())

        then:
            withError.isLeft()
            withError.getLeft().find { "Ugly exception that needs to be caught!! (create comment - no gistId)" == it }

        when:
            def deleteResult = githubApiClient.deleteCommentFromGist(newComment.get().id)
        then:
            deleteResult.isRight()

        when:
            withError = githubApiClient.deleteCommentFromGist(Integer.MAX_VALUE)

        then:
            withError.isLeft()
            withError.getLeft() == "Can not delete for user ${Integer.MAX_VALUE}"
    }

}
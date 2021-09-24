package turbo.funicular.service

import com.github.javafaker.Faker
import io.vavr.control.Either
import org.eclipse.egit.github.core.Comment
import org.eclipse.egit.github.core.Gist
import org.eclipse.egit.github.core.GistFile
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.service.GistService
import org.eclipse.egit.github.core.service.UserService
import spock.lang.Specification

class GithubApiSpecs extends Specification {

    static final def faker = new Faker()

    def userService = Mock(UserService)

    def gistService = Mock(GistService)

    GithubClientFactory githubClientFactory

    def setup() {
        githubClientFactory = Mock(GithubClientFactory) {
            createUserService() >> Either.right(userService)
            createGistService() >> Either.right(gistService)
        }
    }

    void 'Test github operations for user #login'() {
        given:
            def stubbedGithubUser = createGithubUser(login, expectedName)
            def stubbedGistsForUser = createGistsForUser(login)
        and:
            1 * userService.getUser(login) >> stubbedGithubUser
            1 * gistService.getGists(login) >> stubbedGistsForUser

        and:
            def githubApiClient = new GithubApiClient(githubClientFactory)

        when:
            def user = githubApiClient.findUserByLogin(login)

        then: 'Check user info'
            user.right
            with(user.get()) {
                it.login == login
                it.avatarUrl == stubbedGithubUser.avatarUrl
                it.name == expectedName
                it.ghId?.intValue() == stubbedGithubUser.id
                it.publicGistsCount == stubbedGithubUser.publicGists
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
            def gistId = faker.internet().uuid()
            def gistComment = createGistComment(login)

        and: 'Stubs configuration for gists'
            1 * gistService.getGist(gistId) >> createGistForUser(login, gistId)
            1 * gistService.getComments(gistId) >> [gistComment]

        and:
            def githubApiClient = new GithubApiClient(githubClientFactory)

        when:
            def gist = githubApiClient.findGistById(gistId)
        then:
            gist.isRight()
            verifyAll(gist.get()) {
                ghId == gistId
                createdAt
                updatedAt
                commentsCount
                publicGist
                owner
                files.every { file ->
                    file.content
                    file.rawUrl
                    file.filename
                    file.size
                }
            }

        when:
            def comments = githubApiClient.topGistComments(gistId, 1)

        then:
            comments.isRight()
            comments.get().every { comment ->
                comment.id == gistComment.id
                comment.body == gistComment.body
                comment.createdAt
                comment.owner.login == login
            }

    }

    void 'Test github operations for gist comments'() {
        given:
            def login = faker.name().username()
            def gistId = faker.internet().uuid()
            def gistComment = createGistComment(login)
            def noCommentException = new IllegalArgumentException('Ugly exception that needs to be caught!! (create comment - no comment)')
            def noGithubIdException = new IllegalArgumentException('Ugly exception that needs to be caught!! (create comment - no gistId)')

        and: 'Stubs configuration for gists comments'
            1 * gistService.getComments(gistId) >> [gistComment]
            1 * gistService.createComment(_ as String, _ as String) >> gistComment
            1 * gistService.createComment(_ as String, null) >> { throw noCommentException }
            1 * gistService.createComment(null, _ as String) >> { throw noGithubIdException }

        and:
            def githubApiClient = new GithubApiClient(githubClientFactory)

        expect:
            githubApiClient.topGistComments(gistId, 1).get().size() == 1

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
            withError.getLeft().reason == "Can not create a new comment for gist $gistId"
            withError.getLeft().code == 'github.api.failure.add-gist-comment'
            withError.getLeft().cause.defined

        when:
            withError = githubApiClient.addCommentToGist(null, faker.dune().quote())

        then:
            withError.isLeft()
            withError.getLeft().reason == "Can not create a new comment for gist null"
            withError.getLeft().code == 'github.api.failure.add-gist-comment'
            withError.getLeft().cause.defined

    }

    private static def createGithubUser(final String login, final String expectedName) {
        new User(
            id: faker.number().randomNumber(),
            company: faker.company().name(),
            avatarUrl: faker.internet().avatar(),
            publicGists: faker.number().randomDigit(),
            login: login,
            name: expectedName
        )
    }

    private static def createGistForUser(final String login, final String ghId = faker.internet().uuid()) {
        new Gist(
            comments: faker.number().randomDigitNotZero(),
            createdAt: new Date(),
            updatedAt: new Date(),
            id: ghId,
            public: true,
            user: new User(login: login),
            files: Map.of("${ faker.name().title() }", new GistFile(
                size: faker.number().randomDigitNotZero(),
                content: faker.dune().quote(),
                filename: faker.dune().character(),
                rawUrl: faker.internet().url()
            ))
        )
    }

    private static def createGistComment(final String login) {
        new Comment(
            createdAt: new Date(),
            body: faker.dune().quote(),
            id: faker.number().randomNumber(),
            user: new User(login: login, avatarUrl: faker.internet().avatar(), name: faker.dune().character())
        )
    }

    private static def createGistsForUser(final String login) {
        (1..5).collect {
            createGistForUser(login)
        }
    }
}
package com.example.upload

import com.example.upload.domain.member.member.entity.Member
import com.example.upload.domain.member.member.service.MemberService
import com.example.upload.domain.post.post.controller.ApiV1PostController
import com.example.upload.domain.post.post.dto.PostListParamDto
import com.example.upload.domain.post.post.entity.Post
import com.example.upload.domain.post.post.service.PostService
import com.example.upload.standard.search.SearchKeywordType
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostControllerTest {
    @Autowired
    private val mvc: MockMvc? = null

    @Autowired
    private val postService: PostService? = null

    @Autowired
    private val memberService: MemberService? = null

    private var loginedMember: Member? = null
    private var token: String? = null

    @BeforeEach
    fun login() {
        loginedMember = memberService!!.findByUsername("user1").get()
        token = memberService.getAuthToken(loginedMember!!)
    }

    @Throws(Exception::class)
    private fun checkPost(resultActions: ResultActions, post: Post) {
        resultActions
            .andExpect(MockMvcResultMatchers.jsonPath("$.data").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(post.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(post.content))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorId").value(post.author.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorName").value(post.author.nickname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.published").value(post.published))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.listed").value(post.listed))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createdDate")
                    .value(Matchers.matchesPattern(post.createdDate.toString().replace("0+$".toRegex(), "") + ".*"))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifiedDate")
                    .value(Matchers.matchesPattern(post.modifiedDate.toString().replace("0+$".toRegex(), "") + ".*"))
            )
    }


    @Throws(Exception::class)
    private fun checkPosts(posts: List<Post>, resultActions: ResultActions) {
        for (i in posts.indices) {
            val post = posts[i]

            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.items[%d]".formatted(i)).exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.items[%d].id".formatted(i)).value(post.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.items[%d].title".formatted(i)).value(post.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.items[%d].content".formatted(i)).doesNotExist())
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.items[%d].authorId".formatted(i)).value(post.author.id)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.items[%d].authorName".formatted(i))
                        .value(post.author.nickname)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.items[%d].published".formatted(i)).value(post.published)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.items[%d].listed".formatted(i)).value(post.listed))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.items[%d].createdDate".formatted(i))
                        .value(Matchers.matchesPattern(post.createdDate.toString().replace("0+$".toRegex(), "") + ".*"))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.data.items[%d].modifiedDate".formatted(i)).value(
                        Matchers.matchesPattern(
                            post.modifiedDate.toString().replace("0+$".toRegex(), "") + ".*"
                        )
                    )
                )
        }
    }

    @Test
    @DisplayName("글 다건 조회 - 공개 글 목록")
    @Throws(Exception::class)
    fun items1() {
        val pageSize = 10
        val page = 1
        val keyword = ""
        val keywordType = SearchKeywordType.title
        val listed = true

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/api/v1/posts?page=%d&pageSize=%d&keywordType=%s&keyword=%s&listed=%s"
                        .formatted(page, pageSize, keywordType, keyword, listed)
                )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItems"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.items.length()").value(pageSize)) // 한페이지당 보여줄 글 개수
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.currentPageNo").isNumber()) // 현재 페이지
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalPages").isNumber()) // 전체 페이지 개수


        val postListParamDto = PostListParamDto(
            keywordType = keywordType,
            keyword = keyword,
            listed = listed,
            null,
            page = page,
            pageSize = pageSize
        )

        val postPage = postService!!.getItems(postListParamDto)
        val posts = postPage.content
        checkPosts(posts, resultActions)
    }

    @Test
    @DisplayName("글 다건 조회 - 검색 - 제목, 페이징이 되어야 함.")
    @Throws(Exception::class)
    fun items2() {
        val page = 1
        val pageSize = 3

        val keywordType = SearchKeywordType.title
        val keyword = "title"
        val listed = true

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/api/v1/posts?page=%d&pageSize=%d&keywordType=%s&keyword=%s&listed=%s"
                        .formatted(page, pageSize, keywordType, keyword, listed)
                )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItems"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.items.length()").value(pageSize)) // 한페이지당 보여줄 글 개수
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalPages").value(50))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalItems").value(148))

        val postListParamDto = PostListParamDto(
            keywordType = keywordType,
            keyword = keyword,
            listed = listed,
            null,
            page = page,
            pageSize = pageSize
        )

        val postPage = postService!!.getItems(postListParamDto)
        val posts = postPage.content
        checkPosts(posts, resultActions)
    }

    @Test
    @DisplayName("글 다건 조회 - 검색 - 내용, 페이징이 되어야 함.")
    @Throws(Exception::class)
    fun items3() {
        val page = 1
        val pageSize = 3

        val keywordType = SearchKeywordType.content
        val keyword = "content"
        val listed = true

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/api/v1/posts?page=%d&pageSize=%d&keywordType=%s&keyword=%s&listed=%s"
                        .formatted(page, pageSize, keywordType, keyword, listed)
                )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItems"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("글 목록 조회가 완료되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.items.length()").value(pageSize)) // 한페이지당 보여줄 글 개수
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalPages").value(50))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalItems").value(148))

        val postListParamDto = PostListParamDto(
            keywordType = keywordType,
            keyword = keyword,
            listed = listed,
            null,
            page = page,
            pageSize = pageSize
        )

        val postPage = postService!!.getItems(postListParamDto)

        val posts = postPage.content
        checkPosts(posts, resultActions)
    }

    @Test
    @DisplayName("내가 작성한 글 조회 (user1) - 검색, 페이징 되어야 함.")
    @Throws(
        Exception::class
    )
    fun mines() {
        val page = 1
        val pageSize = 3
        // 검색어, 검색 대상
        val keywordType = SearchKeywordType.title
        val keyword = ""

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get(
                    "/api/v1/posts/mine?page=%d&pageSize=%d&keywordType=%s&keyword=%s"
                        .formatted(page, pageSize, keywordType, keyword)
                )
                    .header("Authorization", "Bearer $token")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getMines"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("내 글 목록 조회가 완료되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.items.length()").value(pageSize)) // 한페이지당 보여줄 글 개수
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.currentPageNo").value(page)) // 현재 페이지
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalPages").value(32))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalItems").value(95))


        val postListParamDto = PostListParamDto(
            keywordType = keywordType,
            keyword = keyword,
            null,
            null,
            page = page,
            pageSize = pageSize
        )

        val postPage = postService!!.getMines(
            postListParamDto,
            loginedMember!!
        )
        val posts = postPage.content
        checkPosts(posts, resultActions)
    }


    @Throws(Exception::class)
    private fun itemRequest(postId: Long, apiKey: String?): ResultActions {
        return mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/%d".formatted(postId))
                    .header("Authorization", "Bearer $apiKey")
            )
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("글 단건 조회 1 - 다른 유저의 공개글 조회")
    @Throws(Exception::class)
    fun item1() {
        val postId: Long = 1

        val resultActions = itemRequest(postId, token)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItem"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("%d번 글을 조회하였습니다.".formatted(postId)))

        val post = postService!!.getItem(postId).get()

        checkPost(resultActions, post)
    }

    @Test
    @DisplayName("글 단건 조회 2 - 없는 글 조회")
    @Throws(Exception::class)
    fun item2() {
        val postId: Long = 100000

        val resultActions = itemRequest(postId, token)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItem"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("존재하지 않는 글입니다."))
    }

    @Test
    @DisplayName("글 단건 조회 3 - 다른 유저의 비공개 글 조회")
    @Throws(Exception::class)
    fun item3() {
        val postId: Long = 3

        val resultActions = itemRequest(postId, token)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getItem"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("비공개 설정된 글입니다."))
    }

    @Throws(Exception::class)
    private fun writeRequest(apiKey: String?, title: String, content: String): ResultActions {
        return mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "Bearer $apiKey")
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s",
                                            "published": true,
                                            "listed": true
                                        }
                                        
                                        """
                            .trimIndent()
                            .formatted(title, content)
                            .stripIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("글 작성")
    @WithUserDetails("user3")
    @Throws(Exception::class)
    fun write1() {
        val title = "새로운 글 제목"
        val content = "새로운 글 내용"

        //        String apiKey = loginedMember.getApiKey();
        val resultActions = writeRequest(token, title, content)

        val post = postService!!.getLatestItem().get()

        resultActions
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("write"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("%d번 글 작성이 완료되었습니다.".formatted(post.id)))

        checkPost(resultActions, post)
    }

    @Test
    @DisplayName("글 작성2 - no apiKey")
    @Throws(Exception::class)
    fun write2() {
        val token = "212123"
        val title = "새로운 글 제목"
        val content = "새로운 글 내용"

        val resultActions = writeRequest(token, title, content)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("잘못된 인증키입니다."))
    }

    @Test
    @DisplayName("글 작성3 - no input data")
    @Throws(Exception::class)
    fun write3() {
        val title = ""
        val content = ""

        val resultActions = writeRequest(token, title, content)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("write"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("400-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value(
                    """
                        content : NotBlank : must not be blank
                        title : NotBlank : must not be blank
                        
                        """.trimIndent().trim { it <= ' ' }.stripIndent()
                )
            )
    }


    @Throws(Exception::class)
    private fun modifyRequest(postId: Long, apiKey: String?, title: String, content: String): ResultActions {
        return mvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/posts/%d".formatted(postId))
                    .header("Authorization", "Bearer $apiKey")
                    .content(
                        """
                                        {
                                            "title": "%s",
                                            "content": "%s",
                                            "published": true,
                                            "listed": true
                                        }
                                        
                                        """
                            .trimIndent()
                            .formatted(title, content)
                            .stripIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )

            )
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("글 수정")
    @Rollback(false)
    @Throws(Exception::class)
    fun modify1() {
        val postId: Long = 1
        val title = "수정된 글 제목"
        val content = "수정된 글 내용"

        val resultActions = modifyRequest(postId, token, title, content)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("%d번 글 수정이 완료되었습니다.".formatted(postId)))


        val post = postService!!.getItem(postId).get()

        checkPost(resultActions, post)
    }

    @Test
    @DisplayName("글 수정 2 - no apiKey")
    @Throws(Exception::class)
    fun modify2() {
        val postId: Long = 1
        val token = ""
        val title = "수정된 글 제목"
        val content = "수정된 글 내용"

        val resultActions = modifyRequest(postId, token, title, content)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("잘못된 인증키입니다.".formatted(postId)))
    }

    @Test
    @DisplayName("글 수정 3 - no input data")
    @Throws(Exception::class)
    fun modify3() {
        val postId: Long = 1
        val title = ""
        val content = ""

        val resultActions = modifyRequest(postId, token, title, content)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("400-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value(
                    """
                        content : NotBlank : must not be blank
                        title : NotBlank : must not be blank
                        
                        """.trimIndent().trim { it <= ' ' }.stripIndent()
                )
            )
    }

    @Test
    @DisplayName("글 수정 4 - no permission")
    @Throws(Exception::class)
    fun modify4() {
        val postId: Long = 3
        val title = "다른 유저의 글 제목 수정"
        val content = "다른 유저의 글 내용 수정"

        val resultActions = modifyRequest(postId, token, title, content)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("자신이 작성한 글만 수정 가능합니다."))
    }

    @Throws(Exception::class)
    private fun deleteRequest(postId: Long, apiKey: String?): ResultActions {
        return mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/%d".formatted(postId))
                    .header("Authorization", "Bearer $apiKey")
            )
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    @DisplayName("글 삭제")
    @Throws(Exception::class)
    fun delete1() {
        val postId: Long = 1

        val resultActions = deleteRequest(postId, token)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("%d번 글 삭제가 완료되었습니다.".formatted(postId)))
    }

    @Test
    @DisplayName("글 삭제2 - no apiKey")
    @Throws(Exception::class)
    fun delete2() {
        val postId: Long = 1
        val token = ""

        val resultActions = deleteRequest(postId, token)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("잘못된 인증키입니다.".formatted(postId)))
    }

    @Test
    @DisplayName("글 삭제3 - no permission")
    @Throws(Exception::class)
    fun delete3() {
        val postId: Long = 3

        val resultActions = deleteRequest(postId, token)

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("자신이 작성한 글만 삭제 가능합니다."))
    }

    @Test
    @DisplayName("통계 - 관리자 기능 - 관리자 접근")
    @WithUserDetails("admin")
    @Throws(
        Exception::class
    )
    fun statisticsAdmin() {
        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/v1/posts/statistics")
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("getStatistics"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("통계 조회가 완료되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postCount").value(10))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postPublishedCount").value(10))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postListedCount").value(10))
    }

    @Test
    @DisplayName("통계 - 관리자 기능 - user1 접근")
    @WithUserDetails("user1")
    @Throws(
        Exception::class
    )
    fun statisticsUser() {
        val resultActions = mvc!!.perform(
            MockMvcRequestBuilders.get("/api/v1/posts/statistics")
        )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("접근 권한이 없습니다."))
    }

    @Test
    @DisplayName("임시글 생성")
    @WithUserDetails("user1")
    @Throws(
        Exception::class
    )
    fun writeTemp() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts/temp")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("201-1"))
    }

    @Test
    @DisplayName("임시글 생성, 이미 임시글이 있다면 생성하지 않음")
    @WithUserDetails("user1")
    @Throws(
        Exception::class
    )
    fun AlreadyExistsTemp() {
        val resultActions1 = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts/temp")
            )
            .andDo(MockMvcResultHandlers.print())

        val resultActions2 = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts/temp")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions2
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("200-1"))
    }
}
package dev.limebeck.openconf.domain

import dev.inmo.tgbotapi.types.UserId
import dev.limebeck.openconf.QuestionId
import dev.limebeck.openconf.common.TimeProvider
import java.time.Instant

interface QuestionsRepository {
    fun getUserAnswers(userId: UserId): List<Answer>
    fun getAllAnswers(): List<Answer>
    fun addAnswer(userInfo: UserInfo, questionId: QuestionId, answer: String)
    fun getActiveQuestion(userId: UserId): QuestionId?
    fun setActiveQuestion(userId: UserId, questionId: QuestionId?)
    fun getUserState(userId: UserId): UserState
    fun setUserState(userInfo: UserInfo, state: UserState)
    fun getAllUserStates(): List<UserInfoWithState>
}

data class UserInfoWithState(
    val userInfo: UserInfo,
    val state: UserState,
    val updateTime: Instant
)

data class UserInfo(
    val userId: UserId,
    val username: String
)

enum class UserState {
    NOT_MEMBER,
    AWAIT_SUBSCRIPTION,
    AWAIT_ANSWERS,
    DONE
}

data class Answer(
    val questionId: QuestionId,
    val userInfo: UserInfo,
    val answer: String,
    val dateTime: Instant
)

class QuestionsRepositoryMock(
    private val timeProvider: TimeProvider
) : QuestionsRepository {
    val repo = mutableMapOf<UserId, List<Answer>>()
    val activeQuestions = mutableMapOf<UserId, QuestionId>()
    val userState = mutableMapOf<UserId, UserState>()

    override fun getUserAnswers(userId: UserId): List<Answer> {
        return repo[userId] ?: emptyList()
    }

    override fun getAllAnswers(): List<Answer> {
        return repo.values.flatten()
    }

    override fun addAnswer(userInfo: UserInfo, questionId: QuestionId, answer: String) {
        repo[userInfo.userId] = (repo[userInfo.userId] ?: emptyList()) + Answer(
            questionId,
            userInfo,
            answer,
            timeProvider.getCurrent()
        )
    }

    override fun getActiveQuestion(userId: UserId) = activeQuestions[userId]

    override fun setActiveQuestion(
        userId: UserId,
        questionId: QuestionId?
    ) {
        questionId?.let { activeQuestions[userId] = it } ?: activeQuestions.remove(userId)
    }

    override fun getUserState(userId: UserId): UserState {
        return userState[userId] ?: UserState.AWAIT_SUBSCRIPTION
    }

    override fun setUserState(userInfo: UserInfo, state: UserState) {
        userState[userInfo.userId] = state
    }

    override fun getAllUserStates(): List<UserInfoWithState> {
        return userState.entries.map { (userId, state) ->
            UserInfoWithState(
                userInfo = UserInfo(userId = userId, username = "Unknown"),
                state = state,
                updateTime = timeProvider.getCurrent()
            )
        }
    }
}
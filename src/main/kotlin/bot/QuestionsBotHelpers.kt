package dev.limebeck.openconf.bot

import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.member.ChatMember
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.utils.row
import dev.limebeck.openconf.Question

val Message.userId: UserId
    get() = this.from!!.id

suspend fun BehaviourContext.isMemberOfChat(userId: UserId, chatId: ChatId): Boolean = runCatching {
    val member = getChatMember(chatId, userId).also { println(it) }
    member.status !in listOf(
        ChatMember.Status.Restricted,
        ChatMember.Status.Left,
        ChatMember.Status.Kicked
    )
}.getOrDefault(false)

const val ANSWER_TO_QUESTION_PREFIX = "ANSWER_TO_QUESTION:"

suspend fun BehaviourContext.sendQuestion(userId: UserId, question: Question) {
    when (question) {
        is Question.OpenQuestion -> {
            sendTextMessage(userId, "${question.title}\n${question.description}")
        }

        is Question.QuizWithSingleAnswer -> {
            sendTextMessage(
                userId,
                "${question.title}\n${question.description}",
                replyMarkup = inlineKeyboard {
                    row {
                        question.answers.mapIndexed { i, answer ->
                            dataButton(
                                text = answer,
                                data = ANSWER_TO_QUESTION_PREFIX + answer
                            )
                        }
                    }
                }
            )
        }
    }
}
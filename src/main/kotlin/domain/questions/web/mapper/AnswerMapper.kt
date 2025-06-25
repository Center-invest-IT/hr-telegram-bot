package dev.limebeck.openconf.domain.questions.web.mapper

import dev.limebeck.openconf.domain.Answer
import dev.limebeck.openconf.domain.questions.web.AnswerDTO

fun Answer.toDto(): AnswerDTO = AnswerDTO(
    questionId = this.questionId.value,
    userId = this.userInfo.userId.chatId.toString(),
    username = this.userInfo.username,
    answer = this.answer,
    dateTime = this.dateTime.toString()
)
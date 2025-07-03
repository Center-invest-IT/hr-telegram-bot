package dev.limebeck.openconf.domain.questions.web.mapper

import dev.limebeck.openconf.domain.questions.repository.Question
import dev.limebeck.openconf.domain.questions.web.QuestionDTO

fun Question.toDto(): QuestionDTO {
    return QuestionDTO(
        id = this.id.value,
        question = this.question,
        botId = this.botId.value,
        createdTime = this.createdTime.toString()
    )
}
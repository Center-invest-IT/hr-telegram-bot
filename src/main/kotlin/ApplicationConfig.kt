package dev.limebeck.openconf

data class ApplicationConfig(
    val botToken: String,
    val botReceiver: BotReceiver = BotReceiver.LONGPOLLING,
    val chatId: Long,
    val questions: List<Question>,
    val dbConfig: DbConfig
)

enum class BotReceiver {
    WEBHOOK,
    LONGPOLLING
}

sealed interface DbConfig {
    data object Mock : DbConfig

    data class KtormConfig(
        val url: String,
        val driver: String,
        val username: String,
        val password: String
    ) : DbConfig
}

@JvmInline
value class QuestionId(val value: String)

sealed interface Question {
    val id: QuestionId
    val description: String
    val title: String
    val helpMessage: String

    data class OpenQuestion(
        override val id: QuestionId,
        override val description: String,
        override val title: String,
        override val helpMessage: String
    ) : Question

    data class QuizWithSingleAnswer(
        override val id: QuestionId,
        override val description: String,
        override val title: String,
        override val helpMessage: String,
        val answers: List<String>,
    ) : Question
}
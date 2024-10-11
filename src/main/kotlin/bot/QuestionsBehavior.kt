package dev.limebeck.openconf.bot

import dev.inmo.tgbotapi.extensions.api.chat.get.getChat
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onChatMemberJoined
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMessageCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.utils.asChannelChat
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.data
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.formatting.chatLink
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.updates.hasCommands
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.textsources.link
import dev.inmo.tgbotapi.types.message.textsources.plus
import dev.inmo.tgbotapi.types.message.textsources.regular
import dev.inmo.tgbotapi.types.userLink
import dev.inmo.tgbotapi.utils.RiskFeature
import dev.limebeck.openconf.Question
import dev.limebeck.openconf.domain.QuestionsService
import dev.limebeck.openconf.domain.UserInfo
import dev.limebeck.openconf.domain.UserState

@OptIn(RiskFeature::class)
suspend fun BehaviourContext.createQuestionsBehavior(
    questionsService: QuestionsService,
    chatId: ChatId,
) {
//    val chat = getChat(chatId).asChannelChat()
//        ?: throw RuntimeException("<f0b9648e> Чат не найден")

    onCommand("start") { message ->
        runCatching {
            val userId = message.userId
            val userInfo = UserInfo(
                userId = userId,
                username = message.from!!.username?.username ?: message.from!!.userLink
            )
            if (questionsService.getAll(userId).all { it.completed }) {
                sendTextMessage(
                    userId,
                    """
                        Твоя заявка уже принята! Наш заботливый HR тебе напишет ☺️
                    """.trimIndent()
                )
                return@runCatching
            }
            sendTextMessage(
                userId,
                """
                        Привет! Я бот-помощник банка "Центр-инвест" 💚 

                        Помогу тебе подать заявку на стажировку☺️
                    """.trimIndent()
            )
            sendTextMessage(
                userId,
                """
                        Коротко о главном:
                        
                        💪 Полезный опыт в реальных командах
                        
                        💸 Стажировка оплачивается
                        
                        📆 Гибкий график, чтобы совмещать с учебой
                        
                        📝 Возможность трудоустройства в штат после завершения
                        
                        😋 Бесплатное питание
                    """.trimIndent()
            )
            sendTextMessage(
                userId,
                """
                        Для того, чтобы подать заявку, укажи информацию о себе
                    """.trimIndent()
            )
            val nextQuestion = questionsService.getAll(userId).first { !it.completed }.question
            questionsService.markActiveQuestion(userId, questionId = nextQuestion.id)
            sendQuestion(userId, nextQuestion)
        }.onFailure { it.printStackTrace() }
    }

    onText { message ->
        println("Text message: $message")

        runCatching {
            if (message.hasCommands()) {
                return@runCatching
            }

            val userId = message.from!!.id
            val userInfo = UserInfo(
                userId = userId,
                username = message.from!!.username?.username ?: message.from!!.userLink
            )

//            val isMember = isMemberOfChat(userId, chatId)
//            if (!isMember) {
//                sendTextMessage(
//                    userId,
//                    regular("Сначала подпишись на наш канал: ")
//                        .plus(
//                            link(
//                                chat.title,
//                                chat.chatLink!!
//                            )
//                        )
//
//                )
//                return@runCatching
//            }
            questionsService.setUserState(userInfo, UserState.AWAIT_ANSWERS)

            if (questionsService.getAll(userId).all { it.completed }) {
                questionsService.setUserState(userInfo, UserState.DONE)
                sendTextMessage(
                    userId,
                    regular("""
                                Твоя заявка принята! Наш заботливый HR тебе напишет ☺️
                                 
                                Пока можешь следить за актуальными стажировками в нашем 
                            """.trimIndent()) +
                            link(
                                "Telegram-канале",
                                "https://t.me/ci_jobs"
                            )
                )
                return@runCatching
            }

            val activeQuestion = questionsService.getActive(userId)
            if (activeQuestion != null) {
                when (activeQuestion) {
                    is Question.OpenQuestion -> {
                        questionsService.addAnswer(userInfo, activeQuestion.id, message.content.text)
                        questionsService.markActiveQuestion(userId, null)
//                        reply(message, "Твой ответ принят")

                        if (questionsService.getAll(userId).all { it.completed }) {
                            questionsService.setUserState(userInfo, UserState.DONE)
                            sendTextMessage(
                                userId,
                                regular("""
                                Твоя заявка принята! Наш заботливый HR тебе напишет ☺️
                                 
                                Пока можешь следить за актуальными стажировками в нашем 
                            """.trimIndent()) +
                                        link(
                                            "Telegram-канале",
                                            "https://t.me/ci_jobs"
                                        )
                            )
                        } else {
                            val nextQuestion = questionsService.getAll(userId).first { !it.completed }.question
                            questionsService.markActiveQuestion(userId, questionId = nextQuestion.id)
                            sendQuestion(userId, nextQuestion)
                        }
                    }

                    is Question.QuizWithSingleAnswer -> {
                        reply(message, "Выберите один из вариантов, нажав на кнопку")
                    }
                }
            }
        }.onFailure { it.printStackTrace() }
    }


    onChatMemberJoined(
        initialFilter = {
            it.chat.id.chatId == chatId.chatId
        }
    ) {
        println("Member joined $it")
        runCatching {
            val userId = it.user.id
            val userInfo = UserInfo(
                userId = userId,
                username = it.user.username?.username ?: it.user.userLink
            )
            val state = questionsService.getUserState(userId)
            when (state) {
                UserState.AWAIT_SUBSCRIPTION -> {
                    questionsService.setUserState(userInfo, UserState.AWAIT_ANSWERS)

                    val nextQuestion = questionsService.getAll(userId).first { !it.completed }.question
                    questionsService.markActiveQuestion(userId, questionId = nextQuestion.id)
                    sendQuestion(userId, nextQuestion)
                }

                else -> {
                    //pass
                }
            }
        }.onFailure { it.printStackTrace() }
    }

    onMessageCallbackQuery { message ->
        println("Callback $message")
        runCatching {

            val userId = message.user.id
            val messageData = message.data

            val userInfo = UserInfo(
                userId = userId,
                username = message.from.username?.username ?: message.from.userLink
            )

            if (questionsService.getAll(userId).all { it.completed }) {
                sendTextMessage(
                    userId,
                    """
                        Твоя заявка уже принята! Наш заботливый HR тебе напишет ☺️
                    """.trimIndent()
                )
                return@runCatching
            }

            when {
                messageData == null -> {
                    //pass
                }

                messageData.startsWith(ANSWER_TO_QUESTION_PREFIX) -> {
                    val activeQuestion = questionsService.getActive(userId)
                        ?: throw RuntimeException("<3d0c3390> Нет активного вопрос для участника $userId")

                    questionsService.addAnswer(
                        userInfo,
                        activeQuestion.id,
                        response = messageData.removePrefix(ANSWER_TO_QUESTION_PREFIX)
                    )
                    questionsService.markActiveQuestion(userId, null)

//                    sendTextMessage(userId, "Твой ответ принят")

                    if (questionsService.getAll(userId).all { it.completed }) {
                        questionsService.setUserState(userInfo, UserState.DONE)
                        sendTextMessage(
                            userId,
                            regular("""
                                Твоя заявка принята! Наш заботливый HR тебе напишет ☺️
                                 
                                Пока можешь следить за актуальными стажировками в нашем 
                            """.trimIndent()) +
                                    link(
                                        "Telegram-канале",
                                        "https://t.me/ci_jobs"
                                    )
                        )
                    } else {
                        sendQuestion(userId, questionsService.getAll(userId).first { !it.completed }.question)
                    }
                }
            }
        }.onFailure { it.printStackTrace() }
    }

    onMessageCallbackQuery { message ->
        edit(message.message, replyMarkup = inlineKeyboard { })
    }
}
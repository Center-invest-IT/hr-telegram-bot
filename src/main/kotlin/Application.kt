package dev.limebeck.openconf

import arrow.continuations.SuspendApp
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addCommandLineSource
import com.sksamuel.hoplite.addEnvironmentSource
import com.sksamuel.hoplite.addFileSource
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.utils.updates.flowsUpdatesFilter
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.includeWebhookHandlingInRoute
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.longPolling
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import dev.limebeck.openconf.bot.createQuestionsBehavior
import dev.limebeck.openconf.common.RealTimeProvider
import dev.limebeck.openconf.db.DbConfiguration
import dev.limebeck.openconf.db.FlywayMigrationService
import dev.limebeck.openconf.domain.QuestionsRepositoryKtorm
import dev.limebeck.openconf.domain.QuestionsRepositoryMock
import dev.limebeck.openconf.domain.QuestionsService
import dev.limebeck.openconf.domain.admin.AdminsService
import dev.limebeck.openconf.domain.admin.AdminsRepositoryKtorm
import dev.limebeck.openconf.domain.admins.adminsRouting
import dev.limebeck.openconf.domain.createQuestionRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>) =
    SuspendApp {
        val configLoader = ConfigLoaderBuilder
            .default()
            .addEnvironmentSource()
            .addCommandLineSource(args)
            .addFileSource("config.yaml", optional = true)
            .build()

        val config = configLoader.loadConfigOrThrow<ApplicationConfig>()

        println("<ec76db92> Config loaded: $config")

        val timeProvider = RealTimeProvider

        val questionsRepository = when (config.dbConfig) {
            is DbConfig.Mock -> QuestionsRepositoryMock(timeProvider)
            is DbConfig.KtormConfig -> {
                val dbConfiguration = DbConfiguration(
                    dbUrl = config.dbConfig.url,
                    dbDriver = config.dbConfig.driver,
                    dbUsername = config.dbConfig.username,
                    dbPassword = config.dbConfig.password
                )
                FlywayMigrationService(dbConfiguration).migrate()
                QuestionsRepositoryKtorm(config.dbConfig, timeProvider)
            }
        }

        val questionsService = QuestionsService(config.questions, questionsRepository)

        val bot = telegramBot(config.botToken)
        val chatId = ChatId(RawChatId(config.chatId))

        val adminsService =
            AdminsService(AdminsRepositoryKtorm(config.dbConfig as DbConfig.KtormConfig))

        val filter = flowsUpdatesFilter {}

        bot.buildBehaviour(filter) {
            createQuestionsBehavior(questionsService, chatId)
        }

        embeddedServer(Netty, port = 8080) {
            install(Authentication) {
                basic("auth-basic") {
                    validate { credentials ->
                        val admin = adminsService.login(credentials.name, credentials.password)
                        if (admin != null) {
                            UserIdPrincipal(admin.login)
                        } else {
                            null
                        }
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {})
            }

            routing {
                if (config.botReceiver == BotReceiver.WEBHOOK) {
                    val scope = CoroutineScope(Dispatchers.Default)
                    route("/webhook") {
                        includeWebhookHandlingInRoute(
                            scope,
                            {
                                it.printStackTrace()
                            },
                            block = filter.asUpdateReceiver
                        )
                    }
                }
                authenticate("auth-basic") {
                    createQuestionRoutes(questionsRepository)
                    adminsRouting(adminsService)
                }
            }
        }.start()

        if (config.botReceiver == BotReceiver.LONGPOLLING) {
            bot.longPolling(filter).join()
        }
    }


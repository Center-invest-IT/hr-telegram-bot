package dev.limebeck.openconf.domain.admins

import com.benasher44.uuid.uuid
import dev.limebeck.openconf.domain.admin.AdminsService
import dev.limebeck.openconf.domain.ErrorResponse
import dev.limebeck.openconf.domain.admin.AdminId
import dev.limebeck.openconf.domain.admin.AdminInfo
import dev.limebeck.openconf.domain.admin.AdminsRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import java.util.*

suspend fun ApplicationCall.requireUuidParameter(name: String): UUID? {
    val idParam = parameters[name]
    if (idParam == null) {
        respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                code = "Bad Request",
                message = "Id cannot be empty",
                uid = "400")
        )
        return null
    }
    return try {
        UUID.fromString(idParam)
    } catch (e: IllegalArgumentException) {
        respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(
                code = "Bad Request",
                message = "Invalid UUID",
                uid = "400")
        )
        null
    }
}
    fun Route.adminsRouting(
        adminsService: AdminsService
    ) {
        route("/admins") {
            authenticate("auth-basic") {
                get {
                    val list = adminsService.getAllAdmins().map { admin ->
                        mapOf(
                            "id" to admin.id.uuid.toString(),
                            "login" to admin.login,
                            "password" to admin.passwordHash
                        )
                    }
                    call.respond(list)
                }

                post("/create") {
                    val request = call.receive<AdminCreateRequest>()
                    val result = adminsService.addAdminWithRawPassword(request.password, request.login)
                    call.respond(
                        HttpStatusCode.Created,
                        AdminSuccessResponse(
                            id = result.id.uuid.toString(),
                            login = result.login,
                        )
                    )
                }

                delete("/{id}") {
                    val uuid = call.requireUuidParameter("id") ?: return@delete

                    val admin = adminsService.getAdminById(uuid)
                    if (admin == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(
                                code = "Not Found",
                                message = "Admin with given ID does not exist",
                                uid = "404"
                            )
                        )
                        return@delete
                    }
                    adminsService.deleteAdmin(uuid)
                    call.respond(
                        HttpStatusCode.OK,
                        AdminSuccessResponse(
                            id = admin.id.uuid.toString(),
                            login = admin.login,
                        )
                    )
                }

                get("/{id}/hashpass") {
                    val uuid = call.requireUuidParameter("id") ?: return@get
                    val admin = adminsService.getAdminById(uuid)
                    if (admin == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(
                                code = "Not Found",
                                message = "Id doesn't exist",
                                uid = "404")
                        )
                    } else {
                        call.respond(
                            mapOf(
                                "id" to admin.id.uuid.toString(),
                                "login" to admin.login,
                                "HashPassword" to admin.passwordHash
                            )
                        )
                    }
                }

                get("/{login}/id") {
                    val login = call.parameters["login"]
                    if (login.isNullOrBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                code = "Bad Request",
                                message = "Login cannot be empty",
                                uid = "400"
                            )
                        )
                        return@get
                    }
                    val id = adminsService.getAdminId(login)
                    if (id == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(
                                code = "Not Found",
                                message = "Admin with login '$login' not found",
                                uid = "404"
                            )
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            mapOf(
                                "id" to id.toString(),
                                "login" to login
                            )
                        )
                    }
                }
                post("/update") {
                    val request = call.receive<AdminUpdateRequest>()

                    val uuid = try {
                        UUID.fromString(request.id)
                    } catch (e: IllegalArgumentException) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                code = "Bad Request",
                                message = "Invalid UUID format",
                                uid = "400"
                            )
                        )
                        return@post
                    }
                    val admin = AdminInfo(
                        id = AdminId(uuid),
                        login = request.login,
                        passwordHash = request.password
                    )

                    adminsService.updateAdmin(admin)

                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "id" to admin.id.uuid.toString(),
                            "login" to admin.login,
                        )
                    )
                }
            }
        }
    }

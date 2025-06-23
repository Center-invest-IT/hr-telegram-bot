package dev.limebeck.openconf.domain.admins

import dev.limebeck.openconf.domain.admin.AdminsService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.util.*

suspend fun ApplicationCall.requireUuidParameter(name: String): UUID? {
    val idParam = parameters[name]
    if (idParam == null) {
        respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(error = "Missing parameter", details = "The '$name' parameter is required.")
        )
        return null
    }
    return try {
        UUID.fromString(idParam)
    } catch (e: IllegalArgumentException) {
        respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(error = "Invalid UUID", details = "The '$name' parameter has the wrong format.")
        )
        null
    }
}

fun Route.adminsRouting(
    adminsService: AdminsService
) {

    route("/admins") {

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
            call.respond(HttpStatusCode.Created)
        }

        delete("/{id}") {
            val uuid = call.requireUuidParameter("id") ?:
            return@delete
            adminsService.deleteAdmin(uuid)
            call.respond(HttpStatusCode.OK)
        }

        post("/login") {
            val request = call.receive<AdminLoginRequest>()
            val admin = adminsService.login(request.login, request.password)

            if (admin != null) {
                call.respond(HttpStatusCode.OK,mapOf(
                    "id" to admin.id.toString(),
                    "login" to admin.login,
                    "HashPassword" to admin.passwordHash
                ) )
            } else {
                call.respond(HttpStatusCode.Unauthorized,
                    ErrorResponse(error = "Access is denied", details = "Invalid Login or Password"))
            }
        }

        get("/{id}/hashpass") {
            val uuid = call.requireUuidParameter("id") ?:
            return@get
            val admin = adminsService.getAdminById(uuid)
            if (admin == null) {
                call.respond(HttpStatusCode.NotFound,
                    ErrorResponse(error = "Admin not found", details = "Id doesn't exist"))
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
    }
}
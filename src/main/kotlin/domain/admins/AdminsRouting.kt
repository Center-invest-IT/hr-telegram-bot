package dev.limebeck.openconf.domain.admins

import dev.limebeck.openconf.domain.admin.AdminsService
import dev.limebeck.openconf.domain.admin.AdminInfo
import dev.limebeck.openconf.domain.admins.AdminCreateRequest
import dev.limebeck.openconf.domain.admins.AdminLoginRequest
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.util.*

fun Route.adminsRouting(
    adminsService: AdminsService
) {

    route("/admins") {

        get {
            val admins = adminsService.getAllAdmins()
            call.respond(admins)
        }

        post("/create") {
            val request = call.receive<AdminCreateRequest>()
            val result = adminsService.addAdminWithRawPassword(UUID.randomUUID(),request.login, request.password)
            call.respond(HttpStatusCode.Created, result)
        }

        delete("/{id}") {
            val idParam = call.parameters["id"]
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing id")
                return@delete
            }
            val uuid = try {
                UUID.fromString(idParam)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@delete
            }
            adminsService.deleteAdmin(uuid)
            call.respond(HttpStatusCode.OK, "Admin deleted")
        }

        post("/admin/login") {
            val request = call.receive<AdminLoginRequest>()
            val success = adminsService.login(request.login, request.password)

            if (success) {
                call.respond(HttpStatusCode.OK, "Login successful")
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid login or password")
            }
        }

        get("/{id}/hashpass") {
            val idParam = call.parameters["id"]
            if (idParam == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing id")
                return@get
            }

            val uuid = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@get
            }

            val admin = adminsService.getAdminById(uuid)
            if (admin == null) {
                call.respond(HttpStatusCode.NotFound, "Admin not found")
            } else {
                call.respond(admin)
            }
        }
    }
}
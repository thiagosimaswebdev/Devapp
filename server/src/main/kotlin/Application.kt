import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080) {
        routing {
            get("/") {
                call.respondText("Devapp Web rodando no Render!")
            }
        }
    }.start(wait = true)
}

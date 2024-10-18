package org.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*

val dotenv = dotenv()
val apiGatewayURL = dotenv["API_GATEWAY_URL"] ?: "http://localhost:4000"
val serverPort = dotenv["SERVER_PORT"] ?: "8082"

fun main() {
    embeddedServer(Netty, port = serverPort.toInt(), host = "0.0.0.0") {
        install(CORS) {
            anyHost()
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
        }

        routing {
            get("/") {
                call.respondText(
                    "Netty server deployed by Mujahid in Kotlin",
                    ContentType.Text.Plain
                )
            }

            // Route to proxy all requests to the API Gateway
            route("/{...}") {
                handle {
                    proxyRequest(call)
                }
            }
        }
    }.start(wait = true)
}

// Function to handle proxy requests and stream large files correctly
suspend fun proxyRequest(call: ApplicationCall) {
    val client = HttpClient(CIO) {
        expectSuccess = false  // Allow non-2xx responses
    }

    try {
        // Forward the request to the API Gateway
        val response = client.request("$apiGatewayURL${call.request.uri}") {
            method = call.request.httpMethod
            headers {
                call.request.headers.forEach { key, values ->
                    if (!isCorsHeader(key)) {
                        values.forEach { value ->
                            append(key, value)
                        }
                    }
                }
            }
        }

        // Stream the response body to the client
        call.respondBytesWriter(
            status = response.status,
            contentType = response.contentType()
        ) {
            response.bodyAsChannel().copyTo(this)  // Use the ByteWriteChannel directly
        }
    } catch (e: Exception) {
        call.respond(HttpStatusCode.ServiceUnavailable, "Service unavailable: ${e.localizedMessage}")
    } finally {
        client.close()
    }
}

// Helper function to check for CORS headers
fun isCorsHeader(header: String): Boolean {
    return header.equals(HttpHeaders.AccessControlAllowOrigin, ignoreCase = true) ||
           header.equals(HttpHeaders.AccessControlAllowMethods, ignoreCase = true) ||
           header.equals(HttpHeaders.AccessControlAllowHeaders, ignoreCase = true)
}

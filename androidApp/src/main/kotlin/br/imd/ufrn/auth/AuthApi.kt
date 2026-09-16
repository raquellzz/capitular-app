package br.imd.ufrn.auth

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun defaultApiBaseUrl(): String = "http://10.0.2.2:8000"

class AuthApi(
    private val baseUrl: String = defaultApiBaseUrl(),
) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private val client = createHttpClient().config {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun login(
        identifier: String,
        password: String,
    ): AuthSession =
        request {
            client.post("$baseUrl/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(identifier.trim(), password))
            }
        }

    suspend fun register(
        email: String,
        username: String,
        displayName: String,
        password: String,
    ): AuthSession =
        request {
            client.post("$baseUrl/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterRequest(
                        email = email.trim().lowercase(),
                        username = username.trim().lowercase(),
                        displayName = displayName.trim(),
                        password = password,
                    ),
                )
            }
        }

    suspend fun logout(refreshToken: String) {
        try {
            client.post("$baseUrl/api/v1/auth/logout") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken))
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // Logout is best-effort: the local session has already been cleared.
        }
    }

    fun close() {
        client.close()
    }

    private suspend inline fun <reified T> request(
        crossinline block: suspend () -> io.ktor.client.statement.HttpResponse,
    ): T =
        try {
            val response = block()
            if (!response.status.isSuccess()) {
                val body = response.body<String>()
                throw AuthApiException(errorMessage(body, response.status.value))
            }
            response.body()
        } catch (error: AuthApiException) {
            throw error
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            throw AuthApiException(
                "Não foi possível acessar o Capitular. Verifique se o servidor está ligado.",
            )
        }

    private fun errorMessage(
        responseBody: String,
        statusCode: Int,
    ): String {
        val detail =
            runCatching {
                json.parseToJsonElement(responseBody).jsonObject["detail"]
            }.getOrNull()
        return detail.readableDetail()
            ?: if (statusCode >= 500) {
                "O servidor encontrou um problema. Tente novamente."
            } else {
                "Não foi possível concluir a solicitação."
            }
    }
}

class AuthApiException(
    override val message: String,
) : Exception(message)

private fun JsonElement?.readableDetail(): String? =
    when (this) {
        is JsonPrimitive -> contentOrNull
        is JsonArray ->
            firstOrNull()
                ?.let { it as? JsonObject }
                ?.get("msg")
                ?.jsonPrimitive
                ?.contentOrNull

        else -> null
    }

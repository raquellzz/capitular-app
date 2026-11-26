package br.imd.ufrn.data.remoto

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Fornece o token de acesso da sessão atual, ou `null` se ninguém entrou. */
fun interface ProvedorDeToken {
    fun tokenDeAcesso(): String?
}

/** JSON da API: campos novos no backend não quebram o app. */
val JsonDaApi =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

/**
 * Cliente HTTP compartilhado por todos os repositórios remotos.
 *
 * - `expectSuccess = true`: respostas 4xx e 5xx viram exceção (traduzidas por `chamadaDeRede`).
 * - Base `.../api/v1/`: nos repositórios, use caminhos **sem barra inicial**, ex.: `get("groups/$id/feed")`.
 * - Em POST/PUT com JSON, declare `contentType(ContentType.Application.Json)` na requisição.
 * - O token é lido a cada requisição, então trocar de sessão não exige recriar o cliente.
 */
fun criarClienteHttp(
    engine: HttpClientEngine,
    baseUrl: String,
    provedorDeToken: ProvedorDeToken,
): HttpClient =
    HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(JsonDaApi)
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 20_000
        }
        defaultRequest {
            url(baseUrl.trimEnd('/') + "/api/v1/")
            provedorDeToken.tokenDeAcesso()?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }

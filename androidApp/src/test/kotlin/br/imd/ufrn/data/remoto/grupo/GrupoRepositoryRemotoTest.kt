package br.imd.ufrn.data.remoto.grupo

import br.imd.ufrn.data.remoto.SessaoEmMemoria
import br.imd.ufrn.data.remoto.criarClienteHttp
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GrupoRepositoryRemotoTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val requisicoes = mutableListOf<HttpRequestData>()

    private fun repositorio(handler: MockRequestHandler) =
        GrupoRepositoryRemoto(
            criarClienteHttp(
                engine =
                    MockEngine { requisicao ->
                        requisicoes += requisicao
                        handler(requisicao)
                    },
                baseUrl = "http://servidor:8000",
                provedorDeToken = SessaoEmMemoria(),
            ),
        )

    private val grupoJson =
        """
        {"id":"g1","name":"Clube do Livro","description":null,"invite_code":"ABCD2345",
         "owner_id":"u1","my_role":"owner","member_count":3,"created_at":"2026-11-20T10:00:00Z"}
        """.trimIndent()

    @Test
    fun meusGruposConverteODtoParaODominio() =
        runTest {
            val repositorio = repositorio { respond("[$grupoJson]", HttpStatusCode.OK, jsonHeaders) }

            val grupos = repositorio.meusGrupos()

            assertEquals(listOf(Grupo("g1", "Clube do Livro", null, "ABCD2345", 3)), grupos)
            assertEquals("/api/v1/groups", requisicoes.single().url.encodedPath)
        }

    @Test
    fun entrarEnviaOCodigoEmMaiusculas() =
        runTest {
            val repositorio = repositorio { respond(grupoJson, HttpStatusCode.OK, jsonHeaders) }

            repositorio.entrar(" abcd2345 ")

            val requisicao = requisicoes.single()
            assertEquals(HttpMethod.Post, requisicao.method)
            assertEquals("/api/v1/groups/join", requisicao.url.encodedPath)
            assertEquals("""{"invite_code":"ABCD2345"}""", requisicao.body.toByteArray().decodeToString())
        }

    @Test
    fun conviteInexistenteViraRespostaInvalida404() =
        runTest {
            val repositorio =
                repositorio { respond("""{"detail":"Convite inválido"}""", HttpStatusCode.NotFound, jsonHeaders) }

            val falha = assertFailsWith<FalhaNoRepositorio.RespostaInvalida> { repositorio.entrar("ZZZZ9999") }
            assertEquals(404, falha.status)
        }
}

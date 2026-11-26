package br.imd.ufrn.data.remoto

import br.imd.ufrn.domain.FalhaNoRepositorio
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ChamadaDeRedeTest {
    @Serializable
    private data class ItemDto(
        val id: String,
    )

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val requisicoes = mutableListOf<HttpRequestData>()
    private val sessao = SessaoEmMemoria()

    private fun cliente(handler: MockRequestHandler) =
        criarClienteHttp(
            engine =
                MockEngine { requisicao ->
                    requisicoes += requisicao
                    handler(requisicao)
                },
            baseUrl = "http://servidor:8000/",
            provedorDeToken = sessao,
        )

    @Test
    fun respostaValidaEhConvertidaEIgnoraCamposDesconhecidos() =
        runTest {
            val cliente = cliente { respond("""{"id":"1","campo_novo":true}""", HttpStatusCode.OK, jsonHeaders) }

            val item = chamadaDeRede { cliente.get("itens/1").body<ItemDto>() }

            assertEquals(ItemDto("1"), item)
            assertEquals("http://servidor:8000/api/v1/itens/1", requisicoes.single().url.toString())
        }

    @Test
    fun tokenDaSessaoEhEnviadoEmCadaRequisicao() =
        runTest {
            val cliente = cliente { respond("""{"id":"1"}""", HttpStatusCode.OK, jsonHeaders) }

            cliente.get("itens/1")
            sessao.token = "abc"
            cliente.get("itens/1")

            assertNull(requisicoes[0].headers[HttpHeaders.Authorization])
            assertEquals("Bearer abc", requisicoes[1].headers[HttpHeaders.Authorization])
        }

    @Test
    fun erroDoServidorViraRespostaInvalida() =
        runTest {
            val cliente = cliente { respond("""{"detail":"erro"}""", HttpStatusCode.InternalServerError, jsonHeaders) }

            val falha =
                assertFailsWith<FalhaNoRepositorio.RespostaInvalida> {
                    chamadaDeRede { cliente.get("itens/1").body<ItemDto>() }
                }
            assertEquals(500, falha.status)
        }

    @Test
    fun jsonForaDoFormatoViraRespostaInvalida() =
        runTest {
            val cliente = cliente { respond("""{"outro":1}""", HttpStatusCode.OK, jsonHeaders) }

            assertFailsWith<FalhaNoRepositorio.RespostaInvalida> {
                chamadaDeRede { cliente.get("itens/1").body<ItemDto>() }
            }
        }

    @Test
    fun naoAutorizadoViraNaoAutenticado() =
        runTest {
            val cliente = cliente { respond("""{"detail":"token"}""", HttpStatusCode.Unauthorized, jsonHeaders) }

            assertFailsWith<FalhaNoRepositorio.NaoAutenticado> {
                chamadaDeRede { cliente.get("itens/1").body<ItemDto>() }
            }
        }

    @Test
    fun falhaDeRedeViraSemConexao() =
        runTest {
            val cliente = cliente { throw IOException("Connection refused") }

            assertFailsWith<FalhaNoRepositorio.SemConexao> {
                chamadaDeRede { cliente.get("itens/1").body<ItemDto>() }
            }
        }
}

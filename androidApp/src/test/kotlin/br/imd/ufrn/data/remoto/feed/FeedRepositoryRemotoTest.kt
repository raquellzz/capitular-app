package br.imd.ufrn.data.remoto.feed

import br.imd.ufrn.data.remoto.SessaoEmMemoria
import br.imd.ufrn.data.remoto.criarClienteHttp
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.feed.FotoDaLeitura
import br.imd.ufrn.domain.feed.Leitor
import br.imd.ufrn.domain.feed.NovoCheckIn
import br.imd.ufrn.domain.feed.RegrasDoCheckIn
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeedRepositoryRemotoTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val requisicoes = mutableListOf<HttpRequestData>()

    private fun repositorio(handler: MockRequestHandler) =
        FeedRepositoryRemoto(
            cliente =
                criarClienteHttp(
                    engine =
                        MockEngine { requisicao ->
                            requisicoes += requisicao
                            handler(requisicao)
                        },
                    baseUrl = "http://servidor:8000",
                    provedorDeToken = SessaoEmMemoria(),
                ),
            baseUrl = "http://servidor:8000",
        )

    private val checkInJson =
        """
        {"id":"c1","user":{"id":"u1","username":"raquels","display_name":"Raquel Freire",
          "timezone":"America/Fortaleza","created_at":"2026-10-14T10:00:00Z"},
         "group_id":"g1",
         "photo":{"id":"p1","content_type":"image/webp","byte_size":1000,"width":800,"height":600,
                  "content_url":"/api/v1/photos/p1/content","created_at":"2026-11-26T12:00:00Z"},
         "book_title":"Torto Arado","page_start":84,"page_end":112,"pages_read":29,
         "note":"Belonísia","read_at":"2026-11-26T09:30:00-03:00","created_at":"2026-11-26T12:31:05.123456Z"}
        """.trimIndent()

    private val novo =
        NovoCheckIn(
            grupoId = "g1",
            livro = "  Torto Arado ",
            paginaInicial = 84,
            paginaFinal = 112,
            nota = "  ",
            lidoEm = Instant.parse("2026-11-26T12:30:00Z"),
            foto = FotoDaLeitura(byteArrayOf(1, 2, 3), "image/jpeg"),
            id = "c1",
            fotoId = "p1",
        )

    @Test
    fun feedConverteParaODominioComFotoCompletaEDatas() =
        runTest {
            val repositorio =
                repositorio { respond("""{"items":[$checkInJson],"next_cursor":"abc"}""", HttpStatusCode.OK, jsonHeaders) }

            val pagina = repositorio.feed("g1")

            val checkIn = pagina.checkIns.single()
            assertEquals(Leitor("u1", "Raquel Freire", "raquels"), checkIn.leitor)
            assertEquals("Torto Arado", checkIn.livro)
            assertEquals(29, checkIn.paginasLidas)
            assertEquals("http://servidor:8000/api/v1/photos/p1/content", checkIn.fotoUrl)
            assertEquals(Instant.parse("2026-11-26T12:30:00Z"), checkIn.lidoEm)
            assertEquals("abc", pagina.proximoCursor)
            assertEquals("/api/v1/groups/g1/feed", requisicoes.single().url.encodedPath)
        }

    @Test
    fun feedEnviaOCursorDaProximaPagina() =
        runTest {
            val repositorio = repositorio { respond("""{"items":[],"next_cursor":null}""", HttpStatusCode.OK, jsonHeaders) }

            val pagina = repositorio.feed("g1", cursor = "abc")

            assertEquals("abc", requisicoes.single().url.parameters["cursor"])
            assertTrue(pagina.checkIns.isEmpty())
            assertNull(pagina.proximoCursor)
        }

    @Test
    fun registrarEnviaAFotoEDepoisOCheckIn() =
        runTest {
            val repositorio =
                repositorio { requisicao ->
                    if (requisicao.method == HttpMethod.Put) {
                        respond("""{"id":"p1"}""", HttpStatusCode.OK, jsonHeaders)
                    } else {
                        respond(checkInJson, HttpStatusCode.Created, jsonHeaders)
                    }
                }

            val salvo = repositorio.registrarCheckIn(novo)

            val (foto, checkIn) = requisicoes
            assertEquals(HttpMethod.Put, foto.method)
            assertEquals("/api/v1/photos/p1", foto.url.encodedPath)
            assertTrue(foto.body.contentType.toString().startsWith("multipart/form-data"))
            assertTrue(foto.body is OutgoingContent.WriteChannelContent || foto.body is OutgoingContent.ByteArrayContent)

            assertEquals(HttpMethod.Post, checkIn.method)
            assertEquals("/api/v1/check-ins", checkIn.url.encodedPath)
            val corpo = Json.parseToJsonElement(checkIn.body.toByteArray().decodeToString()).jsonObject
            assertEquals("c1", corpo["id"]!!.jsonPrimitive.content)
            assertEquals("p1", corpo["photo_id"]!!.jsonPrimitive.content)
            assertEquals("Torto Arado", corpo["book_title"]!!.jsonPrimitive.content)
            assertEquals(84, corpo["page_start"]!!.jsonPrimitive.int)
            assertEquals("2026-11-26T12:30:00Z", corpo["read_at"]!!.jsonPrimitive.content)
            assertTrue(corpo["note"] == null || corpo["note"].toString() == "null", "nota em branco não é enviada")

            assertEquals("c1", salvo.id)
        }

    @Test
    fun fotoRecusadaNaoCriaOCheckIn() =
        runTest {
            val repositorio =
                repositorio { respond("""{"detail":"Use uma imagem JPEG, PNG ou WebP"}""", HttpStatusCode.UnprocessableEntity, jsonHeaders) }

            val falha = assertFailsWith<FalhaNoRepositorio.RespostaInvalida> { repositorio.registrarCheckIn(novo) }

            assertEquals(422, falha.status)
            assertEquals(1, requisicoes.size, "parou no envio da foto")
        }

    @Test
    fun erroDoServidorViraRespostaInvalida() =
        runTest {
            val repositorio = repositorio { respond("erro", HttpStatusCode.InternalServerError) }

            val falha = assertFailsWith<FalhaNoRepositorio.RespostaInvalida> { repositorio.feed("g1") }
            assertEquals(500, falha.status)
        }

    @Test
    fun dataForaDoFormatoViraRespostaInvalida() =
        runTest {
            val semFuso = checkInJson.replace("2026-11-26T09:30:00-03:00", "ontem")
            val repositorio = repositorio { respond("""{"items":[$semFuso]}""", HttpStatusCode.OK, jsonHeaders) }

            assertFailsWith<FalhaNoRepositorio.RespostaInvalida> { repositorio.feed("g1") }
        }

    @Test
    fun semRedeViraSemConexao() =
        runTest {
            val repositorio = repositorio { throw IOException("Network is unreachable") }

            assertFailsWith<FalhaNoRepositorio.SemConexao> { repositorio.feed("g1") }
            assertFailsWith<FalhaNoRepositorio.SemConexao> { repositorio.registrarCheckIn(novo) }
        }

    @Test
    fun regrasDoCheckInIguaisAsDoBackend() {
        assertEquals("Informe o livro que você leu.", RegrasDoCheckIn.validarLivro("  "))
        assertNull(RegrasDoCheckIn.validarPaginas(10, 10))
        assertEquals("A página final não pode ser anterior à inicial.", RegrasDoCheckIn.validarPaginas(20, 10))
        assertEquals("As páginas começam em 1.", RegrasDoCheckIn.validarPaginas(0, 10))
        assertEquals("Escolha uma foto da sua leitura.", RegrasDoCheckIn.validarFoto(null))
        assertEquals("Use uma foto JPEG, PNG ou WebP.", RegrasDoCheckIn.validarFoto(FotoDaLeitura(byteArrayOf(), "image/gif")))
    }
}

class EstatisticasRepositoryRemotoTest {
    @Test
    fun converteOsNumerosDoUsuario() =
        runTest {
            val cliente =
                criarClienteHttp(
                    engine =
                        MockEngine {
                            respond(
                                """{"pages_read":340,"checkins":12,"current_streak":7,"longest_streak":9}""",
                                HttpStatusCode.OK,
                                headersOf(HttpHeaders.ContentType, "application/json"),
                            )
                        },
                    baseUrl = "http://servidor:8000",
                    provedorDeToken = SessaoEmMemoria(),
                )

            val numeros = EstatisticasRepositoryRemoto(cliente).minhas()

            assertEquals(br.imd.ufrn.domain.feed.Estatisticas(340, 12, 7, 9), numeros)
        }
}

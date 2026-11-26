package br.imd.ufrn.data.remoto.feed

import br.imd.ufrn.data.remoto.chamadaDeRede
import br.imd.ufrn.domain.feed.CheckIn
import br.imd.ufrn.domain.feed.FeedRepository
import br.imd.ufrn.domain.feed.Leitor
import br.imd.ufrn.domain.feed.NovoCheckIn
import br.imd.ufrn.domain.feed.PaginaDoFeed
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
internal data class LeitorDto(
    val id: String,
    val username: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
internal data class FotoDto(
    val id: String,
    @SerialName("content_url") val contentUrl: String,
)

@Serializable
internal data class CheckInDto(
    val id: String,
    val user: LeitorDto,
    @SerialName("group_id") val groupId: String,
    val photo: FotoDto,
    @SerialName("book_title") val bookTitle: String,
    @SerialName("page_start") val pageStart: Int,
    @SerialName("page_end") val pageEnd: Int,
    @SerialName("pages_read") val pagesRead: Int,
    val note: String? = null,
    @SerialName("read_at") val readAt: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
internal data class FeedPageDto(
    val items: List<CheckInDto>,
    @SerialName("next_cursor") val nextCursor: String? = null,
)

@Serializable
internal data class CriarCheckInRequest(
    val id: String,
    @SerialName("group_id") val groupId: String,
    @SerialName("photo_id") val photoId: String,
    @SerialName("book_title") val bookTitle: String,
    @SerialName("page_start") val pageStart: Int,
    @SerialName("page_end") val pageEnd: Int,
    val note: String?,
    /** ISO-8601 em UTC (`...Z`): o backend exige data com fuso horário. */
    @SerialName("read_at") val readAt: String,
)

/** A API devolve a foto como caminho (`/api/v1/photos/...`); o domínio recebe o endereço completo. */
internal fun CheckInDto.paraDominio(baseUrl: String) =
    CheckIn(
        id = id,
        leitor = Leitor(id = user.id, nome = user.displayName, usuario = user.username),
        grupoId = groupId,
        livro = bookTitle,
        paginaInicial = pageStart,
        paginaFinal = pageEnd,
        paginasLidas = pagesRead,
        nota = note,
        fotoUrl = baseUrl.trimEnd('/') + photo.contentUrl,
        lidoEm = paraInstante(readAt),
        criadoEm = paraInstante(createdAt),
    )

/** Aceita `2026-11-26T10:00:00Z` e também com deslocamento (`-03:00`). */
private fun paraInstante(texto: String): Instant = java.time.OffsetDateTime.parse(texto).toInstant()

internal fun NovoCheckIn.paraRequisicao() =
    CriarCheckInRequest(
        id = id,
        groupId = grupoId,
        photoId = fotoId,
        bookTitle = livro.trim(),
        pageStart = paginaInicial,
        pageEnd = paginaFinal,
        note = nota?.trim()?.ifBlank { null },
        readAt = lidoEm.toString(),
    )

class FeedRepositoryRemoto(
    private val cliente: HttpClient,
    private val baseUrl: String,
) : FeedRepository {
    override suspend fun feed(
        grupoId: String,
        cursor: String?,
    ): PaginaDoFeed =
        chamadaDeRede {
            val pagina =
                cliente
                    .get("groups/$grupoId/feed") {
                        cursor?.let { parameter("cursor", it) }
                    }.body<FeedPageDto>()
            PaginaDoFeed(
                checkIns = pagina.items.map { it.paraDominio(baseUrl) },
                proximoCursor = pagina.nextCursor,
            )
        }

    /**
     * Dois passos: 1) envia a foto (`PUT photos/{fotoId}`, multipart); 2) cria o
     * check-in apontando para ela (`POST check-ins`). Os dois são idempotentes no
     * backend pelos ids, então repetir depois de uma falha não duplica nada.
     */
    override suspend fun registrarCheckIn(novo: NovoCheckIn): CheckIn =
        chamadaDeRede {
            cliente.put("photos/${novo.fotoId}") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = novo.foto.bytes,
                                headers =
                                    Headers.build {
                                        append(HttpHeaders.ContentType, novo.foto.tipo)
                                        append(HttpHeaders.ContentDisposition, "filename=\"leitura\"")
                                    },
                            )
                        },
                    ),
                )
            }
            cliente
                .post("check-ins") {
                    contentType(ContentType.Application.Json)
                    setBody(novo.paraRequisicao())
                }.body<CheckInDto>()
                .paraDominio(baseUrl)
        }
}

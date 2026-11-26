package br.imd.ufrn.data.remoto.grupo

import br.imd.ufrn.data.remoto.chamadaDeRede
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.GrupoRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GrupoDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("member_count") val memberCount: Int,
)

@Serializable
internal data class CriarGrupoRequest(
    val name: String,
    val description: String?,
)

@Serializable
internal data class EntrarNoGrupoRequest(
    @SerialName("invite_code") val inviteCode: String,
)

internal fun GrupoDto.paraDominio() =
    Grupo(
        id = id,
        nome = name,
        descricao = description,
        codigoConvite = inviteCode,
        totalMembros = memberCount,
    )

class GrupoRepositoryRemoto(
    private val cliente: HttpClient,
) : GrupoRepository {
    override suspend fun meusGrupos(): List<Grupo> =
        chamadaDeRede {
            cliente.get("groups").body<List<GrupoDto>>().map { it.paraDominio() }
        }

    override suspend fun criar(
        nome: String,
        descricao: String?,
    ): Grupo =
        chamadaDeRede {
            cliente
                .post("groups") {
                    contentType(ContentType.Application.Json)
                    setBody(CriarGrupoRequest(nome.trim(), descricao?.trim()?.ifBlank { null }))
                }.body<GrupoDto>()
                .paraDominio()
        }

    override suspend fun entrar(codigoConvite: String): Grupo =
        chamadaDeRede {
            cliente
                .post("groups/join") {
                    contentType(ContentType.Application.Json)
                    setBody(EntrarNoGrupoRequest(codigoConvite.trim().uppercase()))
                }.body<GrupoDto>()
                .paraDominio()
        }
}

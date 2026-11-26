package br.imd.ufrn.data.remoto.grupo

import br.imd.ufrn.data.remoto.chamadaDeRede
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.domain.grupo.Membro
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
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
    @SerialName("my_role") val myRole: String = "member",
)

@Serializable
internal data class UsuarioDto(
    val id: String,
    val username: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
internal data class MembroDto(
    val user: UsuarioDto,
    val role: String,
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

private const val PAPEL_DONO = "owner"

internal fun GrupoDto.paraDominio() =
    Grupo(
        id = id,
        nome = name,
        descricao = description,
        codigoConvite = inviteCode,
        totalMembros = memberCount,
        souAdmin = myRole == PAPEL_DONO,
    )

internal fun MembroDto.paraDominio() =
    Membro(
        id = user.id,
        nome = user.displayName,
        usuario = user.username,
        admin = role == PAPEL_DONO,
    )

class GrupoRepositoryRemoto(
    private val cliente: HttpClient,
) : GrupoRepository {
    override suspend fun meusGrupos(): List<Grupo> =
        chamadaDeRede {
            cliente.get("groups").body<List<GrupoDto>>().map { it.paraDominio() }
        }

    override suspend fun membros(grupoId: String): List<Membro> =
        chamadaDeRede {
            cliente.get("groups/$grupoId/members").body<List<MembroDto>>().map { it.paraDominio() }
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

    override suspend fun sair(grupoId: String) {
        chamadaDeRede { cliente.delete("groups/$grupoId/members/me") }
    }
}

package br.imd.ufrn.data.local

import br.imd.ufrn.domain.feed.CheckIn
import br.imd.ufrn.domain.feed.Estatisticas
import br.imd.ufrn.domain.feed.Leitor
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.Membro
import kotlinx.serialization.Serializable
import java.time.Instant

// Formatos guardados no aparelho. Ficam separados do domínio (que não conhece
// serialização) e dos DTOs da API (que mudam com o backend).

@Serializable
internal data class CheckInGuardado(
    val id: String,
    val leitorId: String,
    val leitorNome: String,
    val leitorUsuario: String,
    val grupoId: String,
    val livro: String,
    val paginaInicial: Int,
    val paginaFinal: Int,
    val paginasLidas: Int,
    val nota: String? = null,
    val fotoUrl: String,
    val lidoEmMillis: Long,
    val criadoEmMillis: Long,
)

internal fun CheckIn.paraGuardar() =
    CheckInGuardado(
        id = id,
        leitorId = leitor.id,
        leitorNome = leitor.nome,
        leitorUsuario = leitor.usuario,
        grupoId = grupoId,
        livro = livro,
        paginaInicial = paginaInicial,
        paginaFinal = paginaFinal,
        paginasLidas = paginasLidas,
        nota = nota,
        fotoUrl = fotoUrl,
        lidoEmMillis = lidoEm.toEpochMilli(),
        criadoEmMillis = criadoEm.toEpochMilli(),
    )

internal fun CheckInGuardado.paraDominio() =
    CheckIn(
        id = id,
        leitor = Leitor(leitorId, leitorNome, leitorUsuario),
        grupoId = grupoId,
        livro = livro,
        paginaInicial = paginaInicial,
        paginaFinal = paginaFinal,
        paginasLidas = paginasLidas,
        nota = nota,
        fotoUrl = fotoUrl,
        lidoEm = Instant.ofEpochMilli(lidoEmMillis),
        criadoEm = Instant.ofEpochMilli(criadoEmMillis),
    )

@Serializable
internal data class GrupoGuardado(
    val id: String,
    val nome: String,
    val descricao: String? = null,
    val codigoConvite: String,
    val totalMembros: Int,
    val souAdmin: Boolean,
)

internal fun Grupo.paraGuardar() = GrupoGuardado(id, nome, descricao, codigoConvite, totalMembros, souAdmin)

internal fun GrupoGuardado.paraDominio() = Grupo(id, nome, descricao, codigoConvite, totalMembros, souAdmin)

@Serializable
internal data class MembroGuardado(
    val id: String,
    val nome: String,
    val usuario: String,
    val admin: Boolean,
)

internal fun Membro.paraGuardar() = MembroGuardado(id, nome, usuario, admin)

internal fun MembroGuardado.paraDominio() = Membro(id, nome, usuario, admin)

@Serializable
internal data class EstatisticasGuardadas(
    val paginasLidas: Int,
    val checkIns: Int,
    val sequenciaAtual: Int,
    val maiorSequencia: Int,
)

internal fun Estatisticas.paraGuardar() = EstatisticasGuardadas(paginasLidas, checkIns, sequenciaAtual, maiorSequencia)

internal fun EstatisticasGuardadas.paraDominio() = Estatisticas(paginasLidas, checkIns, sequenciaAtual, maiorSequencia)

package br.imd.ufrn.data.remoto.feed

import br.imd.ufrn.data.remoto.chamadaDeRede
import br.imd.ufrn.domain.feed.Estatisticas
import br.imd.ufrn.domain.feed.EstatisticasRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class EstatisticasDto(
    @SerialName("pages_read") val pagesRead: Int,
    val checkins: Int,
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("longest_streak") val longestStreak: Int,
)

class EstatisticasRepositoryRemoto(
    private val cliente: HttpClient,
) : EstatisticasRepository {
    override suspend fun minhas(): Estatisticas =
        chamadaDeRede {
            val dto = cliente.get("users/me/stats").body<EstatisticasDto>()
            Estatisticas(
                paginasLidas = dto.pagesRead,
                checkIns = dto.checkins,
                sequenciaAtual = dto.currentStreak,
                maiorSequencia = dto.longestStreak,
            )
        }
}

package br.imd.ufrn.presentation.feed

import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.feed.CheckIn
import br.imd.ufrn.domain.feed.Estatisticas
import br.imd.ufrn.domain.feed.EstatisticasRepository
import br.imd.ufrn.domain.feed.FeedRepository
import br.imd.ufrn.domain.feed.Leitor
import br.imd.ufrn.domain.feed.NovoCheckIn
import br.imd.ufrn.domain.feed.PaginaDoFeed
import kotlinx.coroutines.delay
import java.time.Instant

fun checkInDeExemplo(
    id: String = "c1",
    livro: String = "Torto Arado",
) = CheckIn(
    id = id,
    leitor = Leitor("u1", "Bia Nunes", "bia"),
    grupoId = "g1",
    livro = livro,
    paginaInicial = 84,
    paginaFinal = 112,
    paginasLidas = 29,
    nota = null,
    fotoUrl = "http://servidor/api/v1/photos/p1/content",
    lidoEm = Instant.parse("2026-11-26T12:00:00Z"),
    criadoEm = Instant.parse("2026-11-26T12:00:00Z"),
)

/** Dublê do feed, com demora em tempo virtual e respostas configuráveis. */
class FeedRepositoryFalso : FeedRepository {
    var checkIns: List<CheckIn> = emptyList()
    var falha: FalhaNoRepositorio? = null
    var falhaAoRegistrar: FalhaNoRepositorio? = null
    val registrados = mutableListOf<NovoCheckIn>()

    override suspend fun feed(
        grupoId: String,
        cursor: String?,
    ): PaginaDoFeed {
        delay(100)
        falha?.let { throw it }
        return PaginaDoFeed(checkIns, proximoCursor = null)
    }

    override suspend fun registrarCheckIn(novo: NovoCheckIn): CheckIn {
        delay(100)
        registrados += novo
        falhaAoRegistrar?.let { throw it }
        return checkInDeExemplo(id = novo.id, livro = novo.livro)
    }
}

class EstatisticasRepositoryFalso(
    var numeros: Estatisticas = Estatisticas(paginasLidas = 340, checkIns = 12, sequenciaAtual = 7, maiorSequencia = 9),
    var falha: FalhaNoRepositorio? = null,
) : EstatisticasRepository {
    override suspend fun minhas(): Estatisticas {
        delay(50)
        falha?.let { throw it }
        return numeros
    }
}

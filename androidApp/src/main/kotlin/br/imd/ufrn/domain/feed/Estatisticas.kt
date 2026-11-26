package br.imd.ufrn.domain.feed

/** Números de leitura do usuário, somando todos os grupos. */
data class Estatisticas(
    val paginasLidas: Int,
    val checkIns: Int,
    val sequenciaAtual: Int,
    val maiorSequencia: Int,
)

/** Lança [br.imd.ufrn.domain.FalhaNoRepositorio] em caso de erro. */
interface EstatisticasRepository {
    suspend fun minhas(): Estatisticas
}

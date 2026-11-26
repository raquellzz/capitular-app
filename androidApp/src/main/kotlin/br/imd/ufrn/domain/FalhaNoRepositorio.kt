package br.imd.ufrn.domain

/**
 * Falhas com nome que os repositórios lançam. O domínio não conhece Ktor:
 * a camada de dados traduz as exceções da biblioteca nestas (ver `chamadaDeRede`).
 *
 * É selada para que um `when` sobre ela, sem `else`, deixe de compilar
 * se uma falha nova for acrescentada sem tratamento.
 */
sealed class FalhaNoRepositorio(
    mensagem: String,
    causa: Throwable? = null,
) : Exception(mensagem, causa) {
    /** O servidor não respondeu: sem rede, endereço errado, servidor desligado ou tempo esgotado. */
    class SemConexao(
        causa: Throwable? = null,
    ) : FalhaNoRepositorio("Sem conexão com o servidor", causa)

    /** O servidor respondeu, mas com status de erro (4xx/5xx) ou com um corpo fora do formato. */
    class RespostaInvalida(
        val status: Int? = null,
        causa: Throwable? = null,
    ) : FalhaNoRepositorio("Resposta inválida do servidor${status?.let { " ($it)" }.orEmpty()}", causa)

    /** O token expirou ou é inválido (HTTP 401): é preciso entrar de novo. */
    class NaoAutenticado(
        causa: Throwable? = null,
    ) : FalhaNoRepositorio("Sessão expirada", causa)
}

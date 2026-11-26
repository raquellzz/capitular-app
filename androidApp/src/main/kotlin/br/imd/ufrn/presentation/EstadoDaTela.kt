package br.imd.ufrn.presentation

import br.imd.ufrn.domain.FalhaNoRepositorio

/**
 * Os quatro estados de uma tela que carrega dados. Na tela, use um `when`
 * **sem `else`**: um estado novo deixa de compilar até ser tratado.
 *
 * ```
 * when (val estado = estado.collectAsStateWithLifecycle().value) {
 *     EstadoDaTela.Carregando -> Carregando()
 *     EstadoDaTela.Vazio -> ListaVazia()
 *     is EstadoDaTela.Sucesso -> Lista(estado.dados, aviso = estado.desatualizado)
 *     is EstadoDaTela.Erro -> Erro(estado.mensagem, onTentarDeNovo = vm::carregar)
 * }
 * ```
 */
sealed interface EstadoDaTela<out T> {
    data object Carregando : EstadoDaTela<Nothing>

    data object Vazio : EstadoDaTela<Nothing>

    /** [desatualizado] é `true` quando os dados vieram do cache local por falta de rede. */
    data class Sucesso<T>(
        val dados: T,
        val desatualizado: Boolean = false,
    ) : EstadoDaTela<T>

    data class Erro(
        val mensagem: String,
    ) : EstadoDaTela<Nothing>
}

/** Mensagem para o usuário, uma por falha. */
fun FalhaNoRepositorio.mensagemParaUsuario(): String =
    when (this) {
        is FalhaNoRepositorio.SemConexao ->
            "Sem conexão com o Capitular. Verifique sua internet e tente de novo."
        is FalhaNoRepositorio.RespostaInvalida ->
            "O servidor respondeu de um jeito inesperado. Tente de novo em instantes."
        is FalhaNoRepositorio.NaoAutenticado ->
            "Sua sessão expirou. Entre de novo para continuar."
    }

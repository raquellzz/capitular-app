package br.imd.ufrn.domain.grupo

/** Grupo de leitura do qual o usuário participa. */
data class Grupo(
    val id: String,
    val nome: String,
    val descricao: String?,
    val codigoConvite: String,
    val totalMembros: Int,
)

/**
 * Acesso aos grupos do usuário. As funções lançam
 * [br.imd.ufrn.domain.FalhaNoRepositorio] em caso de erro.
 */
interface GrupoRepository {
    /** Grupos do usuário, do mais recente para o mais antigo. */
    suspend fun meusGrupos(): List<Grupo>

    suspend fun criar(
        nome: String,
        descricao: String?,
    ): Grupo

    /** Entra num grupo pelo código de convite; código inexistente lança `RespostaInvalida(404)`. */
    suspend fun entrar(codigoConvite: String): Grupo
}

/** Regras do formulário, iguais às do backend. Devolvem a mensagem de erro, ou `null` se válido. */
object RegrasDoGrupo {
    fun validarNome(nome: String): String? =
        when (nome.trim().length) {
            in 0..1 -> "O nome precisa ter ao menos 2 caracteres."
            in 2..80 -> null
            else -> "O nome pode ter no máximo 80 caracteres."
        }

    fun validarCodigo(codigo: String): String? =
        if (codigo.trim().length in 6..12) null else "O código de convite tem de 6 a 12 caracteres."
}

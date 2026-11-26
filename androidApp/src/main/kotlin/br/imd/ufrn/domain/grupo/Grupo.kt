package br.imd.ufrn.domain.grupo

/** Grupo de leitura do qual o usuário participa. */
data class Grupo(
    val id: String,
    val nome: String,
    val descricao: String?,
    val codigoConvite: String,
    val totalMembros: Int,
    /** `true` se o usuário é o dono (admin) do grupo. O dono não pode sair dele. */
    val souAdmin: Boolean = false,
)

/** Participante de um grupo. */
data class Membro(
    val id: String,
    val nome: String,
    val usuario: String,
    val admin: Boolean,
)

/**
 * Acesso aos grupos do usuário. As funções lançam
 * [br.imd.ufrn.domain.FalhaNoRepositorio] em caso de erro.
 */
interface GrupoRepository {
    /** Grupos do usuário, do mais recente para o mais antigo. */
    suspend fun meusGrupos(): List<Grupo>

    suspend fun membros(grupoId: String): List<Membro>

    suspend fun criar(
        nome: String,
        descricao: String?,
    ): Grupo

    /** Entra num grupo pelo código de convite; código inexistente lança `RespostaInvalida(404)`. */
    suspend fun entrar(codigoConvite: String): Grupo

    /** Sai do grupo. O dono não pode sair: a API responde `RespostaInvalida(409)`. */
    suspend fun sair(grupoId: String)
}

/** Lembra qual grupo o usuário abriu por último, para reabrir o mesmo. */
interface PreferenciaDeGrupo {
    suspend fun grupoAtualId(): String?

    suspend fun salvarGrupoAtual(grupoId: String?)
}

/**
 * Escolhe o grupo a abrir: o último aberto, se o usuário ainda participa dele;
 * senão, o mais recente.
 */
fun escolherGrupoAtual(
    grupos: List<Grupo>,
    ultimoAbertoId: String?,
): Grupo? = grupos.firstOrNull { it.id == ultimoAbertoId } ?: grupos.firstOrNull()

/** Regras do formulário, iguais às do backend. Devolvem a mensagem de erro, ou `null` se válido. */
object RegrasDoGrupo {
    const val MAXIMO_DESCRICAO = 300

    fun validarNome(nome: String): String? =
        when (nome.trim().length) {
            in 0..1 -> "O nome precisa ter ao menos 2 caracteres."
            in 2..80 -> null
            else -> "O nome pode ter no máximo 80 caracteres."
        }

    fun validarDescricao(descricao: String): String? =
        if (descricao.trim().length <= MAXIMO_DESCRICAO) {
            null
        } else {
            "A descrição pode ter no máximo $MAXIMO_DESCRICAO caracteres."
        }

    fun validarCodigo(codigo: String): String? =
        if (codigo.trim().length in 6..12) null else "O código de convite tem de 6 a 12 caracteres."
}

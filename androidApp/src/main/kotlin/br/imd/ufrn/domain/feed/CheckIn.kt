package br.imd.ufrn.domain.feed

import java.time.Instant
import java.util.UUID

/** Quem registrou a leitura. */
data class Leitor(
    val id: String,
    val nome: String,
    val usuario: String,
)

/** Uma leitura registrada no grupo: páginas lidas de um livro, com foto. */
data class CheckIn(
    val id: String,
    val leitor: Leitor,
    val grupoId: String,
    val livro: String,
    val paginaInicial: Int,
    val paginaFinal: Int,
    val paginasLidas: Int,
    val nota: String?,
    /** Endereço completo da foto; exige o token de acesso para baixar. */
    val fotoUrl: String,
    val lidoEm: Instant,
    val criadoEm: Instant,
)

/**
 * Um trecho do feed, do mais recente para o mais antigo. [proximoCursor] é `null` na última página.
 * [desatualizada] é `true` quando, sem conexão, a página veio da cópia guardada no aparelho,
 * e [guardadaEm] diz quando essa cópia foi recebida do servidor.
 */
data class PaginaDoFeed(
    val checkIns: List<CheckIn>,
    val proximoCursor: String?,
    val desatualizada: Boolean = false,
    val guardadaEm: Instant? = null,
)

/** Foto escolhida pelo usuário, já lida em bytes. [tipo] é o MIME: `image/jpeg`, `image/png` ou `image/webp`. */
class FotoDaLeitura(
    val bytes: ByteArray,
    val tipo: String,
)

/**
 * Dados de um check-in a registrar. [id] e [fotoId] são gerados no app uma única vez:
 * se o envio falhar e o usuário tentar de novo com o **mesmo** objeto, o backend
 * reconhece os ids e não duplica a leitura.
 */
data class NovoCheckIn(
    val grupoId: String,
    val livro: String,
    val paginaInicial: Int,
    val paginaFinal: Int,
    val nota: String?,
    val lidoEm: Instant,
    val foto: FotoDaLeitura,
    val id: String = UUID.randomUUID().toString(),
    val fotoId: String = UUID.randomUUID().toString(),
)

/**
 * Acesso ao feed do grupo e ao registro de leituras. As funções lançam
 * [br.imd.ufrn.domain.FalhaNoRepositorio] em caso de erro.
 */
interface FeedRepository {
    suspend fun feed(
        grupoId: String,
        cursor: String? = null,
    ): PaginaDoFeed

    /** Envia a foto e registra o check-in; devolve o check-in como ficou salvo no servidor. */
    suspend fun registrarCheckIn(novo: NovoCheckIn): CheckIn
}

/** Regras do check-in, iguais às do backend. Devolvem a mensagem de erro, ou `null` se válido. */
object RegrasDoCheckIn {
    const val MAXIMO_LIVRO = 160
    const val MAXIMO_NOTA = 500
    const val MAXIMO_PAGINA = 1_000_000
    val TIPOS_DE_FOTO = setOf("image/jpeg", "image/png", "image/webp")

    fun validarLivro(livro: String): String? =
        when (livro.trim().length) {
            0 -> "Informe o livro que você leu."
            in 1..MAXIMO_LIVRO -> null
            else -> "O título pode ter no máximo $MAXIMO_LIVRO caracteres."
        }

    fun validarPaginas(
        inicial: Int?,
        final: Int?,
    ): String? =
        when {
            inicial == null || final == null -> "Informe a página inicial e a final."
            inicial < 1 || final < 1 -> "As páginas começam em 1."
            inicial > MAXIMO_PAGINA || final > MAXIMO_PAGINA -> "Número de página grande demais."
            final < inicial -> "A página final não pode ser anterior à inicial."
            else -> null
        }

    fun validarNota(nota: String): String? =
        if (nota.trim().length <= MAXIMO_NOTA) null else "A nota pode ter no máximo $MAXIMO_NOTA caracteres."

    fun validarFoto(foto: FotoDaLeitura?): String? =
        when {
            foto == null -> "Escolha uma foto da sua leitura."
            foto.tipo !in TIPOS_DE_FOTO -> "Use uma foto JPEG, PNG ou WebP."
            else -> null
        }
}

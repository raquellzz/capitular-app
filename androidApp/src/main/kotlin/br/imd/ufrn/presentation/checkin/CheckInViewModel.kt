package br.imd.ufrn.presentation.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.feed.FeedRepository
import br.imd.ufrn.domain.feed.FotoDaLeitura
import br.imd.ufrn.domain.feed.NovoCheckIn
import br.imd.ufrn.domain.feed.RegrasDoCheckIn
import br.imd.ufrn.domain.grupo.PreferenciaDeGrupo
import br.imd.ufrn.presentation.mensagemParaUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

/** Campos do formulário de check-in. As páginas ficam como texto, do jeito que foram digitadas. */
data class FormularioDeCheckIn(
    val livro: String = "",
    val paginaInicial: String = "",
    val paginaFinal: String = "",
    val nota: String = "",
    val foto: FotoDaLeitura? = null,
    val lendoFoto: Boolean = false,
    val enviando: Boolean = false,
    /** Motivo de uma falha ao salvar (rede, servidor, foto recusada). */
    val erro: String? = null,
) {
    /** Primeiro problema de preenchimento, para mostrar enquanto o usuário digita. */
    val problema: String?
        get() =
            if (livro.isBlank() && paginaInicial.isBlank() && paginaFinal.isBlank()) {
                null
            } else {
                RegrasDoCheckIn.validarLivro(livro)
                    ?: RegrasDoCheckIn.validarPaginas(paginaInicial.toIntOrNull(), paginaFinal.toIntOrNull())
                    ?: RegrasDoCheckIn.validarNota(nota)
            }

    /** Páginas que serão somadas, contando a inicial e a final (como o servidor). */
    val paginasLidas: Int?
        get() {
            val inicial = paginaInicial.toIntOrNull() ?: return null
            val final = paginaFinal.toIntOrNull() ?: return null
            return (final - inicial + 1).takeIf { RegrasDoCheckIn.validarPaginas(inicial, final) == null }
        }

    val podeSalvar: Boolean
        get() =
            !enviando &&
                !lendoFoto &&
                livro.isNotBlank() &&
                problema == null &&
                paginasLidas != null &&
                RegrasDoCheckIn.validarFoto(foto) == null
}

/**
 * Formulário de check-in. O grupo vem da rota; pelo deep link (`capitular://check-in`)
 * ele não vem, e o ViewModel usa o último grupo aberto.
 *
 * Se o envio falhar, o formulário fica preenchido e "Salvar" tenta de novo com o
 * **mesmo** [NovoCheckIn] (mesmos ids), para o servidor não duplicar a leitura.
 * Mudar qualquer campo descarta esse pendente.
 */
class CheckInViewModel(
    private val grupoIdDaRota: String?,
    private val feed: FeedRepository,
    private val preferencia: PreferenciaDeGrupo,
    private val agora: () -> Instant = Instant::now,
) : ViewModel() {
    private val _formulario = MutableStateFlow(FormularioDeCheckIn())
    val formulario: StateFlow<FormularioDeCheckIn> = _formulario.asStateFlow()

    /** Vira `true` quando a leitura foi salva; a tela então volta para o feed. */
    private val _salvo = MutableStateFlow(false)
    val salvo: StateFlow<Boolean> = _salvo.asStateFlow()

    private var pendente: NovoCheckIn? = null

    fun alterarLivro(livro: String) = editar { it.copy(livro = livro) }

    fun alterarPaginaInicial(pagina: String) = editar { it.copy(paginaInicial = pagina.filter(Char::isDigit)) }

    fun alterarPaginaFinal(pagina: String) = editar { it.copy(paginaFinal = pagina.filter(Char::isDigit)) }

    fun alterarNota(nota: String) = editar { it.copy(nota = nota) }

    fun lendoFoto() = _formulario.update { it.copy(lendoFoto = true, erro = null) }

    fun escolherFoto(foto: FotoDaLeitura?) =
        editar {
            it.copy(
                foto = foto ?: it.foto,
                lendoFoto = false,
                erro = if (foto == null) "Não foi possível abrir essa foto. Tente outra." else null,
            )
        }

    fun salvar() {
        val atual = _formulario.value
        if (!atual.podeSalvar) {
            _formulario.update {
                it.copy(erro = it.problema ?: RegrasDoCheckIn.validarFoto(it.foto) ?: "Preencha o livro e as páginas.")
            }
            return
        }
        viewModelScope.launch {
            _formulario.update { it.copy(enviando = true, erro = null) }
            val grupoId = grupoIdDaRota ?: preferencia.grupoAtualId()
            if (grupoId == null) {
                _formulario.update { it.copy(enviando = false, erro = "Abra um grupo antes de registrar a leitura.") }
                return@launch
            }
            val novo = pendente ?: montar(atual, grupoId).also { pendente = it }
            try {
                feed.registrarCheckIn(novo)
                pendente = null
                _salvo.value = true
            } catch (falha: FalhaNoRepositorio) {
                _formulario.update { it.copy(enviando = false, erro = mensagemDoEnvio(falha)) }
            }
        }
    }

    private fun montar(
        formulario: FormularioDeCheckIn,
        grupoId: String,
    ) = NovoCheckIn(
        grupoId = grupoId,
        livro = formulario.livro.trim(),
        paginaInicial = formulario.paginaInicial.toInt(),
        paginaFinal = formulario.paginaFinal.toInt(),
        nota = formulario.nota.trim().ifBlank { null },
        lidoEm = agora(),
        foto = requireNotNull(formulario.foto),
    )

    private fun editar(mudanca: (FormularioDeCheckIn) -> FormularioDeCheckIn) {
        pendente = null
        _formulario.update { mudanca(it.copy(erro = null)) }
    }

    private fun mensagemDoEnvio(falha: FalhaNoRepositorio): String =
        if (falha is FalhaNoRepositorio.RespostaInvalida && falha.status == 422) {
            "O servidor recusou a leitura. Confira a foto e os campos."
        } else {
            "Não foi possível salvar. ${falha.mensagemParaUsuario()}"
        }
}

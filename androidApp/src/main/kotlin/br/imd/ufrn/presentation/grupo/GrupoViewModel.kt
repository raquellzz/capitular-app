package br.imd.ufrn.presentation.grupo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.domain.grupo.RegrasDoGrupo
import br.imd.ufrn.presentation.EstadoDaTela
import br.imd.ufrn.presentation.mensagemParaUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Campos de "criar grupo" e "entrar com convite". */
data class FormularioDeGrupo(
    val nome: String = "",
    val codigo: String = "",
    val enviando: Boolean = false,
    val erro: String? = null,
) {
    val podeCriar: Boolean get() = !enviando && RegrasDoGrupo.validarNome(nome) == null
    val podeEntrar: Boolean get() = !enviando && RegrasDoGrupo.validarCodigo(codigo) == null
}

/**
 * Decide o grupo atual depois do login:
 * - `Sucesso(grupo)`: o usuário tem grupo (o mais recente) e a tela segue para o feed;
 * - `Vazio`: ainda não tem grupo, e a tela oferece criar um ou entrar com convite;
 * - `Erro`: falhou ao buscar, com "Tentar de novo".
 */
class GrupoViewModel(
    private val repositorio: GrupoRepository,
) : ViewModel() {
    private val _estado = MutableStateFlow<EstadoDaTela<Grupo>>(EstadoDaTela.Carregando)
    val estado: StateFlow<EstadoDaTela<Grupo>> = _estado.asStateFlow()

    private val _formulario = MutableStateFlow(FormularioDeGrupo())
    val formulario: StateFlow<FormularioDeGrupo> = _formulario.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.value = EstadoDaTela.Carregando
            _estado.value =
                try {
                    repositorio.meusGrupos().firstOrNull()
                        ?.let { EstadoDaTela.Sucesso(it) }
                        ?: EstadoDaTela.Vazio
                } catch (falha: FalhaNoRepositorio) {
                    EstadoDaTela.Erro(falha.mensagemParaUsuario())
                }
        }
    }

    fun alterarNome(nome: String) = _formulario.update { it.copy(nome = nome, erro = null) }

    fun alterarCodigo(codigo: String) = _formulario.update { it.copy(codigo = codigo, erro = null) }

    fun criarGrupo() {
        val nome = _formulario.value.nome
        RegrasDoGrupo.validarNome(nome)?.let { erro -> return _formulario.update { it.copy(erro = erro) } }
        enviar { repositorio.criar(nome = nome, descricao = null) }
    }

    fun entrarNoGrupo() {
        val codigo = _formulario.value.codigo
        RegrasDoGrupo.validarCodigo(codigo)?.let { erro -> return _formulario.update { it.copy(erro = erro) } }
        enviar { repositorio.entrar(codigo) }
    }

    private fun enviar(acao: suspend () -> Grupo) {
        if (_formulario.value.enviando) return
        viewModelScope.launch {
            _formulario.update { it.copy(enviando = true, erro = null) }
            try {
                val grupo = acao()
                _formulario.value = FormularioDeGrupo()
                _estado.value = EstadoDaTela.Sucesso(grupo)
            } catch (falha: FalhaNoRepositorio) {
                // Falhou ao salvar: mantém o que foi digitado e mostra o motivo.
                _formulario.update { it.copy(enviando = false, erro = mensagemDoEnvio(falha)) }
            }
        }
    }

    private fun mensagemDoEnvio(falha: FalhaNoRepositorio): String =
        if (falha is FalhaNoRepositorio.RespostaInvalida && falha.status == 404) {
            "Convite não encontrado. Confira o código com quem te convidou."
        } else {
            falha.mensagemParaUsuario()
        }
}

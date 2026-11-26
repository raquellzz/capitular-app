package br.imd.ufrn.presentation.grupo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.domain.grupo.Membro
import br.imd.ufrn.domain.grupo.PreferenciaDeGrupo
import br.imd.ufrn.domain.grupo.escolherGrupoAtual
import br.imd.ufrn.presentation.EstadoDaTela
import br.imd.ufrn.presentation.mensagemParaUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** O grupo aberto, com seus membros, e os demais grupos do usuário. */
data class MeusGrupos(
    val atual: Grupo,
    val membros: List<Membro>,
    val outros: List<Grupo>,
)

/** Para onde a aba Grupos manda o usuário depois de uma ação. */
sealed interface DestinoDosGrupos {
    data class AbrirFeed(
        val grupo: Grupo,
    ) : DestinoDosGrupos

    /** Saiu do único grupo: volta para a tela de criar ou entrar. */
    data object EscolherGrupo : DestinoDosGrupos
}

/**
 * Aba "Grupos": detalhes do grupo atual (convite, membros, papel), lista dos
 * outros grupos para trocar, criar ou entrar em outro, e sair do grupo.
 */
class MeusGruposViewModel(
    private val grupoAtualId: String,
    private val repositorio: GrupoRepository,
    private val preferencia: PreferenciaDeGrupo,
) : ViewModel() {
    private val _estado = MutableStateFlow<EstadoDaTela<MeusGrupos>>(EstadoDaTela.Carregando)
    val estado: StateFlow<EstadoDaTela<MeusGrupos>> = _estado.asStateFlow()

    /** Aviso de uma ação que falhou (ex.: sair sem conexão), sem esconder os dados da tela. */
    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private val _saindo = MutableStateFlow(false)
    val saindo: StateFlow<Boolean> = _saindo.asStateFlow()

    /** Navegação pendente; a tela navega e chama [navegacaoConcluida]. */
    private val _destino = MutableStateFlow<DestinoDosGrupos?>(null)
    val destino: StateFlow<DestinoDosGrupos?> = _destino.asStateFlow()

    val acoes =
        AcoesDoFormularioDeGrupo(repositorio, viewModelScope) { grupo -> abrir(grupo) }
    val formulario: StateFlow<FormularioDeGrupo> = acoes.formulario

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.value = EstadoDaTela.Carregando
            _estado.value =
                try {
                    val grupos = repositorio.meusGrupos()
                    val atual = escolherGrupoAtual(grupos, grupoAtualId)
                    if (atual == null) {
                        EstadoDaTela.Vazio
                    } else {
                        EstadoDaTela.Sucesso(
                            MeusGrupos(
                                atual = atual,
                                membros = repositorio.membros(atual.id),
                                outros = grupos - atual,
                            ),
                        )
                    }
                } catch (falha: FalhaNoRepositorio) {
                    EstadoDaTela.Erro(falha.mensagemParaUsuario())
                }
        }
    }

    fun trocarPara(grupo: Grupo) {
        viewModelScope.launch { abrir(grupo) }
    }

    fun sairDoGrupo() {
        val grupos = (_estado.value as? EstadoDaTela.Sucesso)?.dados ?: return
        if (_saindo.value || grupos.atual.souAdmin) return
        viewModelScope.launch {
            _saindo.value = true
            _aviso.value = null
            try {
                repositorio.sair(grupos.atual.id)
                val proximo = grupos.outros.firstOrNull()
                if (proximo != null) {
                    abrir(proximo)
                } else {
                    preferencia.salvarGrupoAtual(null)
                    _destino.value = DestinoDosGrupos.EscolherGrupo
                }
            } catch (falha: FalhaNoRepositorio) {
                _aviso.value = "Não foi possível sair do grupo. ${falha.mensagemParaUsuario()}"
            } finally {
                _saindo.value = false
            }
        }
    }

    fun avisoVisto() {
        _aviso.value = null
    }

    fun navegacaoConcluida() {
        _destino.value = null
    }

    private suspend fun abrir(grupo: Grupo) {
        preferencia.salvarGrupoAtual(grupo.id)
        _destino.value = DestinoDosGrupos.AbrirFeed(grupo)
    }
}

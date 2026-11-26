package br.imd.ufrn.presentation.grupo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.domain.grupo.PreferenciaDeGrupo
import br.imd.ufrn.domain.grupo.escolherGrupoAtual
import br.imd.ufrn.presentation.EstadoDaTela
import br.imd.ufrn.presentation.mensagemParaUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Decide o grupo atual depois do login:
 * - `Sucesso(grupo)`: o usuário tem grupo (o último aberto, ou o mais recente) e a tela segue para o feed;
 * - `Vazio`: ainda não tem grupo, e a tela oferece criar um ou entrar com convite;
 * - `Erro`: falhou ao buscar, com "Tentar de novo".
 */
class GrupoViewModel(
    private val repositorio: GrupoRepository,
    private val preferencia: PreferenciaDeGrupo,
) : ViewModel() {
    private val _estado = MutableStateFlow<EstadoDaTela<Grupo>>(EstadoDaTela.Carregando)
    val estado: StateFlow<EstadoDaTela<Grupo>> = _estado.asStateFlow()

    val acoes =
        AcoesDoFormularioDeGrupo(repositorio, viewModelScope) { grupo ->
            preferencia.salvarGrupoAtual(grupo.id)
            _estado.value = EstadoDaTela.Sucesso(grupo)
        }
    val formulario: StateFlow<FormularioDeGrupo> = acoes.formulario

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.value = EstadoDaTela.Carregando
            _estado.value =
                try {
                    escolherGrupoAtual(repositorio.meusGrupos(), preferencia.grupoAtualId())
                        ?.let { EstadoDaTela.Sucesso(it) }
                        ?: EstadoDaTela.Vazio
                } catch (falha: FalhaNoRepositorio) {
                    EstadoDaTela.Erro(falha.mensagemParaUsuario())
                }
        }
    }
}

package br.imd.ufrn.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.feed.CheckIn
import br.imd.ufrn.domain.feed.Estatisticas
import br.imd.ufrn.domain.feed.EstatisticasRepository
import br.imd.ufrn.domain.feed.FeedRepository
import br.imd.ufrn.presentation.EstadoDaTela
import br.imd.ufrn.presentation.mensagemParaUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Feed do grupo: os check-ins do backend nos quatro estados, e os números do
 * usuário para o cartão "Sua sequência".
 *
 * A tela chama [atualizar] sempre que volta a aparecer (ex.: depois de registrar
 * uma leitura). Se já há check-ins na tela, a atualização acontece sem voltar
 * para "Carregando"; se ela falhar, os dados ficam e aparece um [aviso].
 */
class FeedViewModel(
    private val grupoId: String,
    private val feed: FeedRepository,
    private val estatisticas: EstatisticasRepository,
) : ViewModel() {
    private val _estado = MutableStateFlow<EstadoDaTela<List<CheckIn>>>(EstadoDaTela.Carregando)
    val estado: StateFlow<EstadoDaTela<List<CheckIn>>> = _estado.asStateFlow()

    /** `null` enquanto não carregou ou se falhou: o cartão some, o feed continua. */
    private val _resumo = MutableStateFlow<Estatisticas?>(null)
    val resumo: StateFlow<Estatisticas?> = _resumo.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    /** Quando a cópia mostrada (sem conexão) chegou do servidor; `null` com dados frescos. */
    private val _guardadoEm = MutableStateFlow<Instant?>(null)
    val guardadoEm: StateFlow<Instant?> = _guardadoEm.asStateFlow()

    fun atualizar() {
        viewModelScope.launch {
            val anterior = _estado.value
            if (anterior !is EstadoDaTela.Sucesso) _estado.value = EstadoDaTela.Carregando
            try {
                val pagina = feed.feed(grupoId)
                _estado.value =
                    if (pagina.checkIns.isEmpty()) {
                        EstadoDaTela.Vazio
                    } else {
                        EstadoDaTela.Sucesso(pagina.checkIns, desatualizado = pagina.desatualizada)
                    }
                _guardadoEm.value = pagina.guardadaEm.takeIf { pagina.desatualizada }
                _aviso.value = null
            } catch (falha: FalhaNoRepositorio) {
                if (anterior is EstadoDaTela.Sucesso) {
                    _aviso.value = "Não foi possível atualizar. ${falha.mensagemParaUsuario()}"
                } else {
                    _estado.value = EstadoDaTela.Erro(falha.mensagemParaUsuario())
                }
            }
        }
        viewModelScope.launch {
            try {
                _resumo.value = estatisticas.minhas()
            } catch (_: FalhaNoRepositorio) {
                // O resumo é secundário: sem ele, o feed segue normalmente.
            }
        }
    }

    fun avisoVisto() {
        _aviso.value = null
    }
}

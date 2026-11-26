package br.imd.ufrn.presentation.grupo

import app.cash.turbine.test
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.presentation.EstadoDaTela
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

/** Dublê com demora em tempo virtual e respostas configuráveis. */
private class GrupoRepositoryFalso : GrupoRepository {
    var grupos: List<Grupo> = emptyList()
    var falha: FalhaNoRepositorio? = null
    var falhaAoEntrar: FalhaNoRepositorio? = null

    override suspend fun meusGrupos(): List<Grupo> {
        delay(100)
        falha?.let { throw it }
        return grupos
    }

    override suspend fun criar(
        nome: String,
        descricao: String?,
    ): Grupo {
        delay(100)
        return Grupo("novo", nome, descricao, "NOVO2345", 1)
    }

    override suspend fun entrar(codigoConvite: String): Grupo {
        delay(100)
        falhaAoEntrar?.let { throw it }
        return Grupo("g2", "Grupo do convite", null, codigoConvite, 4)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GrupoViewModelTest {
    private val clube = Grupo("g1", "Clube", null, "ABCD2345", 3)
    private val repositorio = GrupoRepositoryFalso()

    private fun TestScope.criarViewModel(): GrupoViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return GrupoViewModel(repositorio)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun comGrupoVaiDeCarregandoParaSucesso() =
        runTest {
            repositorio.grupos = listOf(clube)
            val viewModel = criarViewModel()

            viewModel.estado.test {
                assertEquals(EstadoDaTela.Carregando, awaitItem())
                assertEquals(EstadoDaTela.Sucesso(clube), awaitItem())
            }
        }

    @Test
    fun semGrupoFicaVazio() =
        runTest {
            val viewModel = criarViewModel()

            viewModel.estado.test {
                assertEquals(EstadoDaTela.Carregando, awaitItem())
                assertEquals(EstadoDaTela.Vazio, awaitItem())
            }
        }

    @Test
    fun erroETentarDeNovo() =
        runTest {
            repositorio.falha = FalhaNoRepositorio.SemConexao()
            val viewModel = criarViewModel()

            viewModel.estado.test {
                assertEquals(EstadoDaTela.Carregando, awaitItem())
                assertIs<EstadoDaTela.Erro>(awaitItem())

                repositorio.falha = null
                repositorio.grupos = listOf(clube)
                viewModel.carregar()

                assertEquals(EstadoDaTela.Carregando, awaitItem())
                assertEquals(EstadoDaTela.Sucesso(clube), awaitItem())
            }
        }

    @Test
    fun criarGrupoLevaAoSucesso() =
        runTest {
            val viewModel = criarViewModel()
            advanceUntilIdle()

            viewModel.alterarNome("Leitoras da UFRN")
            viewModel.criarGrupo()
            advanceUntilIdle()

            val estado = assertIs<EstadoDaTela.Sucesso<Grupo>>(viewModel.estado.value)
            assertEquals("Leitoras da UFRN", estado.dados.nome)
        }

    @Test
    fun conviteInexistenteMantemOCodigoEMostraOMotivo() =
        runTest {
            repositorio.falhaAoEntrar = FalhaNoRepositorio.RespostaInvalida(status = 404)
            val viewModel = criarViewModel()
            advanceUntilIdle()

            viewModel.alterarCodigo("ZZZZ9999")
            viewModel.entrarNoGrupo()
            advanceUntilIdle()

            val formulario = viewModel.formulario.value
            assertEquals("ZZZZ9999", formulario.codigo)
            assertFalse(formulario.enviando)
            assertEquals("Convite não encontrado. Confira o código com quem te convidou.", formulario.erro)
            assertEquals(EstadoDaTela.Vazio, viewModel.estado.value)
        }
}

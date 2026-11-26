package br.imd.ufrn.presentation.grupo

import app.cash.turbine.test
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.presentation.EstadoDaTela
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class GrupoViewModelTest {
    private val clube = Grupo("g1", "Clube", null, "ABCD2345", 3)
    private val leituras = Grupo("g2", "Leituras", null, "QWER5678", 5)
    private val repositorio = GrupoRepositoryFalso()
    private val preferencia = PreferenciaDeGrupoFalsa()

    private fun TestScope.criarViewModel(): GrupoViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return GrupoViewModel(repositorio, preferencia)
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
    fun reabreOUltimoGrupoAberto() =
        runTest {
            repositorio.grupos = listOf(clube, leituras)
            preferencia.grupoId = leituras.id
            val viewModel = criarViewModel()
            advanceUntilIdle()

            assertEquals(EstadoDaTela.Sucesso(leituras), viewModel.estado.value)
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
    fun criarGrupoComDescricaoLevaAoSucessoEGuardaOGrupo() =
        runTest {
            val viewModel = criarViewModel()
            advanceUntilIdle()

            viewModel.acoes.alterarNome("Leitoras da UFRN")
            viewModel.acoes.alterarDescricao("Um capítulo por dia")
            viewModel.acoes.criar()
            advanceUntilIdle()

            val estado = assertIs<EstadoDaTela.Sucesso<Grupo>>(viewModel.estado.value)
            assertEquals("Leitoras da UFRN", estado.dados.nome)
            assertEquals("Leitoras da UFRN" to "Um capítulo por dia", repositorio.ultimoCriado)
            assertEquals("novo", preferencia.grupoId)
        }

    @Test
    fun conviteInexistenteMantemOCodigoEMostraOMotivo() =
        runTest {
            repositorio.falhaAoEntrar = FalhaNoRepositorio.RespostaInvalida(status = 404)
            val viewModel = criarViewModel()
            advanceUntilIdle()

            viewModel.acoes.alterarCodigo("ZZZZ9999")
            viewModel.acoes.entrar()
            advanceUntilIdle()

            val formulario = viewModel.formulario.value
            assertEquals("ZZZZ9999", formulario.codigo)
            assertFalse(formulario.enviando)
            assertEquals("Convite não encontrado. Confira o código com quem te convidou.", formulario.erro)
            assertEquals(EstadoDaTela.Vazio, viewModel.estado.value)
        }
}

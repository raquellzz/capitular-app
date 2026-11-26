package br.imd.ufrn.presentation.feed

import app.cash.turbine.test
import br.imd.ufrn.domain.FalhaNoRepositorio
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    private val feed = FeedRepositoryFalso()
    private val estatisticas = EstatisticasRepositoryFalso()

    private fun TestScope.criarViewModel(): FeedViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return FeedViewModel("g1", feed, estatisticas)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun carregandoDepoisSucessoComOsCheckInsDoBackend() =
        runTest {
            feed.checkIns = listOf(checkInDeExemplo())
            val viewModel = criarViewModel()

            viewModel.estado.test {
                assertEquals(EstadoDaTela.Carregando, awaitItem())
                viewModel.atualizar()
                assertEquals(EstadoDaTela.Sucesso(listOf(checkInDeExemplo())), awaitItem())
            }
        }

    @Test
    fun semLeiturasFicaVazio() =
        runTest {
            val viewModel = criarViewModel()

            viewModel.estado.test {
                assertEquals(EstadoDaTela.Carregando, awaitItem())
                viewModel.atualizar()
                assertEquals(EstadoDaTela.Vazio, awaitItem())
            }
        }

    @Test
    fun erroComMensagemETentarDeNovo() =
        runTest {
            feed.falha = FalhaNoRepositorio.SemConexao()
            val viewModel = criarViewModel()

            viewModel.estado.test {
                assertEquals(EstadoDaTela.Carregando, awaitItem())
                viewModel.atualizar()
                val erro = assertIs<EstadoDaTela.Erro>(awaitItem())
                assertEquals("Sem conexão com o Capitular. Verifique sua internet e tente de novo.", erro.mensagem)

                feed.falha = null
                feed.checkIns = listOf(checkInDeExemplo())
                viewModel.atualizar()
                assertEquals(EstadoDaTela.Carregando, awaitItem())
                assertIs<EstadoDaTela.Sucesso<*>>(awaitItem())
            }
        }

    @Test
    fun respostaInvalidaTemMensagemPropria() =
        runTest {
            feed.falha = FalhaNoRepositorio.RespostaInvalida(500)
            val viewModel = criarViewModel()

            viewModel.atualizar()
            advanceUntilIdle()

            val erro = assertIs<EstadoDaTela.Erro>(viewModel.estado.value)
            assertEquals("O servidor respondeu de um jeito inesperado. Tente de novo em instantes.", erro.mensagem)
        }

    @Test
    fun atualizarComDadosNaTelaNaoVoltaParaCarregandoEFalhaViraAviso() =
        runTest {
            feed.checkIns = listOf(checkInDeExemplo())
            val viewModel = criarViewModel()
            viewModel.atualizar()
            advanceUntilIdle()

            viewModel.estado.test {
                assertIs<EstadoDaTela.Sucesso<*>>(awaitItem())
                feed.falha = FalhaNoRepositorio.SemConexao()
                viewModel.atualizar()
                advanceUntilIdle()
                expectNoEvents()
            }
            assertNotNull(viewModel.aviso.value)
        }

    @Test
    fun feedGuardadoSemConexaoViraSucessoDesatualizadoComAData() =
        runTest {
            val guardadaEm = java.time.Instant.parse("2026-11-26T09:00:00Z")
            val viewModel =
                run {
                    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
                    FeedViewModel("g1", FeedGuardado(guardadaEm), estatisticas)
                }

            viewModel.atualizar()
            advanceUntilIdle()

            assertEquals(EstadoDaTela.Sucesso(listOf(checkInDeExemplo()), desatualizado = true), viewModel.estado.value)
            assertEquals(guardadaEm, viewModel.guardadoEm.value)
        }

    /** Repositório que responde como o cache responde sem conexão. */
    private class FeedGuardado(
        private val guardadaEm: java.time.Instant,
    ) : br.imd.ufrn.domain.feed.FeedRepository {
        override suspend fun feed(
            grupoId: String,
            cursor: String?,
        ) = br.imd.ufrn.domain.feed.PaginaDoFeed(listOf(checkInDeExemplo()), null, desatualizada = true, guardadaEm = guardadaEm)

        override suspend fun registrarCheckIn(novo: br.imd.ufrn.domain.feed.NovoCheckIn) = throw UnsupportedOperationException()
    }

    @Test
    fun resumoVemDasEstatisticasESomeSeElasFalham() =
        runTest {
            val viewModel = criarViewModel()
            viewModel.atualizar()
            advanceUntilIdle()
            assertEquals(7, viewModel.resumo.value?.sequenciaAtual)

            val semResumo = criarViewModel().also { estatisticas.falha = FalhaNoRepositorio.SemConexao() }
            semResumo.atualizar()
            advanceUntilIdle()
            assertNull(semResumo.resumo.value)
            assertEquals(EstadoDaTela.Vazio, semResumo.estado.value)
        }
}

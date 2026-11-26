package br.imd.ufrn.presentation.grupo

import app.cash.turbine.test
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.Membro
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MeusGruposViewModelTest {
    private val clube = Grupo("g1", "Clube", "Um capítulo por dia", "ABCD2345", 2)
    private val leituras = Grupo("g2", "Leituras", null, "QWER5678", 5, souAdmin = true)
    private val raquel = Membro("u1", "Raquel", "raquels", admin = false)
    private val repositorio = GrupoRepositoryFalso()
    private val preferencia = PreferenciaDeGrupoFalsa()

    private fun TestScope.criarViewModel(grupoAtualId: String): MeusGruposViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return MeusGruposViewModel(grupoAtualId, repositorio, preferencia)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun mostraOGrupoAtualSeusMembrosEOsOutros() =
        runTest {
            repositorio.grupos = listOf(clube, leituras)
            repositorio.membros = listOf(raquel)
            val viewModel = criarViewModel(grupoAtualId = "g2")

            viewModel.estado.test {
                assertEquals(EstadoDaTela.Carregando, awaitItem())
                assertEquals(
                    EstadoDaTela.Sucesso(MeusGrupos(atual = leituras, membros = listOf(raquel), outros = listOf(clube))),
                    awaitItem(),
                )
            }
        }

    @Test
    fun trocarDeGrupoGuardaEAbreOFeedDele() =
        runTest {
            repositorio.grupos = listOf(clube, leituras)
            val viewModel = criarViewModel(grupoAtualId = "g1")
            advanceUntilIdle()

            viewModel.trocarPara(leituras)
            advanceUntilIdle()

            assertEquals(DestinoDosGrupos.AbrirFeed("g2"), viewModel.destino.value)
            assertEquals("g2", preferencia.grupoId)
        }

    @Test
    fun sairDoUnicoGrupoVoltaParaEscolherGrupo() =
        runTest {
            repositorio.grupos = listOf(clube)
            preferencia.grupoId = "g1"
            val viewModel = criarViewModel(grupoAtualId = "g1")
            advanceUntilIdle()

            viewModel.sairDoGrupo()
            advanceUntilIdle()

            assertEquals(listOf("g1"), repositorio.saiuDe)
            assertEquals(DestinoDosGrupos.EscolherGrupo, viewModel.destino.value)
            assertNull(preferencia.grupoId)
        }

    @Test
    fun adminNaoSaiDoGrupo() =
        runTest {
            repositorio.grupos = listOf(leituras)
            val viewModel = criarViewModel(grupoAtualId = "g2")
            advanceUntilIdle()

            viewModel.sairDoGrupo()
            advanceUntilIdle()

            assertTrue(repositorio.saiuDe.isEmpty())
            assertNull(viewModel.destino.value)
        }

    @Test
    fun falhaAoSairMantemOsDadosEMostraAviso() =
        runTest {
            repositorio.grupos = listOf(clube, leituras)
            repositorio.falhaAoSair = FalhaNoRepositorio.SemConexao()
            val viewModel = criarViewModel(grupoAtualId = "g1")
            advanceUntilIdle()

            viewModel.sairDoGrupo()
            advanceUntilIdle()

            assertIs<EstadoDaTela.Sucesso<MeusGrupos>>(viewModel.estado.value)
            assertNotNull(viewModel.aviso.value)
            assertNull(viewModel.destino.value)
        }
}

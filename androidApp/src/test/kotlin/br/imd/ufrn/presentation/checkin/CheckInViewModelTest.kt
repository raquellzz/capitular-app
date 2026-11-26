package br.imd.ufrn.presentation.checkin

import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.feed.FotoDaLeitura
import br.imd.ufrn.presentation.feed.FeedRepositoryFalso
import br.imd.ufrn.presentation.grupo.PreferenciaDeGrupoFalsa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CheckInViewModelTest {
    private val feed = FeedRepositoryFalso()
    private val preferencia = PreferenciaDeGrupoFalsa()
    private val foto = FotoDaLeitura(byteArrayOf(1, 2, 3), "image/jpeg")
    private val instante = Instant.parse("2026-11-26T12:00:00Z")

    private fun TestScope.criarViewModel(grupoId: String? = "g1"): CheckInViewModel {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        return CheckInViewModel(grupoId, feed, preferencia, agora = { instante })
    }

    private fun CheckInViewModel.preencher() {
        alterarLivro("Torto Arado")
        alterarPaginaInicial("84")
        alterarPaginaFinal("112")
        alterarNota("Belonísia!")
        escolherFoto(foto)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun formularioValidoSalvaNoGrupoEAvisaQueTerminou() =
        runTest {
            val viewModel = criarViewModel()
            viewModel.preencher()
            assertEquals(29, viewModel.formulario.value.paginasLidas)

            viewModel.salvar()
            advanceUntilIdle()

            val enviado = feed.registrados.single()
            assertEquals("g1", enviado.grupoId)
            assertEquals("Torto Arado", enviado.livro)
            assertEquals(84 to 112, enviado.paginaInicial to enviado.paginaFinal)
            assertEquals("Belonísia!", enviado.nota)
            assertEquals(instante, enviado.lidoEm)
            assertTrue(viewModel.salvo.value)
        }

    @Test
    fun semFotoNaoEnviaEPedeAFoto() =
        runTest {
            val viewModel = criarViewModel()
            viewModel.alterarLivro("Torto Arado")
            viewModel.alterarPaginaInicial("1")
            viewModel.alterarPaginaFinal("10")

            viewModel.salvar()
            advanceUntilIdle()

            assertTrue(feed.registrados.isEmpty())
            assertEquals("Escolha uma foto da sua leitura.", viewModel.formulario.value.erro)
        }

    @Test
    fun paginasInvalidasNaoPodemSerSalvas() =
        runTest {
            val viewModel = criarViewModel()
            viewModel.preencher()
            viewModel.alterarPaginaFinal("20")

            assertFalse(viewModel.formulario.value.podeSalvar)
            assertEquals("A página final não pode ser anterior à inicial.", viewModel.formulario.value.problema)
        }

    @Test
    fun falhaAoSalvarMantemOFormularioETentarDeNovoNaoDuplica() =
        runTest {
            feed.falhaAoRegistrar = FalhaNoRepositorio.SemConexao()
            val viewModel = criarViewModel()
            viewModel.preencher()

            viewModel.salvar()
            advanceUntilIdle()
            assertEquals("Torto Arado", viewModel.formulario.value.livro)
            assertFalse(viewModel.formulario.value.enviando)
            assertTrue(viewModel.formulario.value.erro!!.startsWith("Não foi possível salvar."))
            assertFalse(viewModel.salvo.value)

            feed.falhaAoRegistrar = null
            viewModel.salvar()
            advanceUntilIdle()

            val (primeira, segunda) = feed.registrados
            assertEquals(primeira.id, segunda.id, "mesmo id: o servidor não duplica")
            assertEquals(primeira.fotoId, segunda.fotoId)
            assertTrue(viewModel.salvo.value)
        }

    @Test
    fun mudarUmCampoDepoisDaFalhaGeraUmCheckInNovo() =
        runTest {
            feed.falhaAoRegistrar = FalhaNoRepositorio.SemConexao()
            val viewModel = criarViewModel()
            viewModel.preencher()
            viewModel.salvar()
            advanceUntilIdle()

            feed.falhaAoRegistrar = null
            viewModel.alterarPaginaFinal("120")
            viewModel.salvar()
            advanceUntilIdle()

            val (primeira, segunda) = feed.registrados
            assertNotEquals(primeira.id, segunda.id)
            assertEquals(120, segunda.paginaFinal)
        }

    @Test
    fun peloDeepLinkUsaOUltimoGrupoAberto() =
        runTest {
            preferencia.grupoId = "g9"
            val viewModel = criarViewModel(grupoId = null)
            viewModel.preencher()

            viewModel.salvar()
            advanceUntilIdle()

            assertEquals("g9", feed.registrados.single().grupoId)
        }
}

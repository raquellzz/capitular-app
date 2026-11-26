package br.imd.ufrn.presentation

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import br.imd.ufrn.domain.feed.FotoDaLeitura
import br.imd.ufrn.presentation.checkin.FormularioDeCheckIn
import br.imd.ufrn.presentation.model.CheckIn
import br.imd.ufrn.presentation.model.Ranking
import br.imd.ufrn.presentation.screens.CheckInScreen
import br.imd.ufrn.presentation.screens.FeedScreen
import br.imd.ufrn.presentation.screens.RankingScreen
import br.imd.ufrn.presentation.theme.CapitularTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

// Application simples: as telas recebem estado e lambdas, sem Koin.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE, application = Application::class)
class CapitularUiTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun mostrarFeed(
        estado: EstadoDaTela<List<CheckIn>>,
        onNovoCheckIn: () -> Unit = {},
        onTentarDeNovo: () -> Unit = {},
    ) = rule.setContent {
        CapitularTheme {
            FeedScreen(
                userName = "Ana",
                nomeDoGrupo = "Clube do Livro",
                estado = estado,
                onAbrirGrupos = {},
                onLogout = {},
                onNovoCheckInClick = onNovoCheckIn,
                onTentarDeNovo = onTentarDeNovo,
            )
        }
    }

    private fun mostrarCheckIn(formulario: FormularioDeCheckIn) =
        rule.setContent {
            CapitularTheme {
                CheckInScreen(
                    formulario = formulario,
                    onLivroChange = {},
                    onPaginaInicialChange = {},
                    onPaginaFinalChange = {},
                    onNotaChange = {},
                    onEscolherFoto = {},
                    onSalvar = {},
                    onVoltar = {},
                )
            }
        }

    @Test
    fun feedComLeiturasMostraOsCartoes() {
        mostrarFeed(
            EstadoDaTela.Sucesso(
                listOf(
                    CheckIn(
                        id = "1",
                        friendName = "Bia Nunes",
                        bookTitle = "Torto Arado",
                        pageRange = "pág. 84 a 112",
                        timeElapsed = "há 18 min",
                        pagesRead = 29,
                        note = "Ótimo livro",
                    ),
                ),
            ),
        )

        // A lista rola: desce até o check-in, como o usuário faria.
        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Bia Nunes"))
        rule.onNodeWithText("Bia Nunes").assertExists()
        rule.onNodeWithText("+29 pág.").assertExists()
    }

    @Test
    fun feedVazioConvidaARegistrarALeitura() {
        var registrou = false
        mostrarFeed(EstadoDaTela.Vazio, onNovoCheckIn = { registrou = true })

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Nenhuma leitura ainda"))
        rule.onNodeWithText("Registrar leitura").performClick()
        assertTrue(registrou)
    }

    @Test
    fun feedComErroMostraMotivoETentarDeNovo() {
        var tentou = false
        mostrarFeed(EstadoDaTela.Erro("Sem conexão com o Capitular."), onTentarDeNovo = { tentou = true })

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Sem conexão com o Capitular."))
        rule.onNodeWithText("Tentar de novo").performClick()
        assertTrue(tentou)
    }

    @Test
    fun botaoFlutuanteAbreORegistroDeLeitura() {
        var clicou = false
        mostrarFeed(EstadoDaTela.Vazio, onNovoCheckIn = { clicou = true })

        rule.onNodeWithText("＋  Registrar leitura").performClick()
        assertTrue(clicou)
    }

    @Test
    fun checkInComPaginasInvalidasMostraOMotivoENaoSalva() {
        mostrarCheckIn(
            FormularioDeCheckIn(
                livro = "Torto Arado",
                paginaInicial = "50",
                paginaFinal = "20",
                foto = FotoDaLeitura(byteArrayOf(1), "image/jpeg"),
            ),
        )

        rule.onNodeWithText("A página final não pode ser anterior à inicial.").assertExists()
        rule.onNodeWithText("Salvar Check-in").assertIsNotEnabled()
    }

    @Test
    fun checkInSemFotoNaoSalva() {
        mostrarCheckIn(FormularioDeCheckIn(livro = "Torto Arado", paginaInicial = "120", paginaFinal = "145"))

        rule.onNodeWithText("Adicionar foto da leitura").assertExists()
        rule.onNodeWithText("Salvar Check-in").assertIsNotEnabled()
    }

    @Test
    fun checkInCompletoHabilitaSalvarEMostraAsPaginas() {
        mostrarCheckIn(
            FormularioDeCheckIn(
                livro = "Torto Arado",
                paginaInicial = "120",
                paginaFinal = "145",
                foto = FotoDaLeitura(byteArrayOf(1), "image/jpeg"),
            ),
        )

        rule.onNodeWithText("26 páginas lidas").assertExists()
        rule.onNodeWithText("Salvar Check-in").assertIsEnabled()
    }

    @Test
    fun rankingMostraAOrdemEAsPaginas() {
        rule.setContent {
            CapitularTheme {
                RankingScreen(
                    rankings =
                        listOf(
                            Ranking(position = 1, name = "Bia Nunes", totalPages = 340),
                            Ranking(position = 2, name = "Ana Lima", totalPages = 285),
                        ),
                )
            }
        }

        rule.onNode(hasScrollAction()).performScrollToNode(hasText("Bia Nunes"))
        rule.onNodeWithText("Bia Nunes").assertExists()
        rule.onNodeWithText("340 pág.").assertExists()
        rule.onNodeWithText("Ana Lima").assertExists()
        rule.onNodeWithText("285 pág.").assertExists()
    }
}

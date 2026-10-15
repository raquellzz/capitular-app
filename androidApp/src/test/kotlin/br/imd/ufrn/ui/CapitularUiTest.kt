package br.imd.ufrn.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.imd.ufrn.ui.models.CheckIn
import br.imd.ufrn.ui.models.Ranking
import br.imd.ufrn.ui.screens.CheckInScreen
import br.imd.ufrn.ui.screens.FeedScreen
import br.imd.ufrn.ui.screens.RankingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE)
class CapitularUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testFeedScreenRendersItems() {
        val sampleCheckIns = listOf(
            CheckIn(
                id = "1",
                friendName = "Bia Nunes",
                bookTitle = "Torto Arado",
                pageRange = "pág. 84 a 112",
                timeElapsed = "há 18 min",
                pagesRead = 29,
                note = "Ótimo livro",
            )
        )

        rule.setContent {
            CapitularTheme {
                FeedScreen(
                    userName = "Ana",
                    onLogout = {},
                    checkIns = sampleCheckIns,
                    onNovoCheckInClick = {},
                )
            }
        }

        rule.onNodeWithText("Bia Nunes").assertExists()
        rule.onAllNodesWithText("Torto Arado")[0].assertExists()
    }

    @Test
    fun testCheckInFormInvalidDisablesButton() {
        rule.setContent {
            CapitularTheme {
                CheckInScreen(
                    paginaInicial = "50",
                    paginaFinal = "20",
                    onInicioChange = {},
                    onFimChange = {},
                    onSalvarCheckIn = { _, _ -> },
                    onVoltar = {},
                    bookTitle = "Torto Arado",
                )
            }
        }

        rule.onNodeWithText("Salvar Check-in").assertIsNotEnabled()
    }

    @Test
    fun testCheckInFormValidEnablesButton() {
        rule.setContent {
            CapitularTheme {
                CheckInScreen(
                    paginaInicial = "120",
                    paginaFinal = "145",
                    onInicioChange = {},
                    onFimChange = {},
                    onSalvarCheckIn = { _, _ -> },
                    onVoltar = {},
                    bookTitle = "Torto Arado",
                )
            }
        }

        rule.onNodeWithText("Salvar Check-in").assertIsEnabled()
    }

    @Test
    fun testFeedScreenNewCheckInClickTriggersCallback() {
        var clicked = false

        rule.setContent {
            CapitularTheme {
                FeedScreen(
                    userName = "Ana",
                    onLogout = {},
                    checkIns = emptyList(),
                    onNovoCheckInClick = { clicked = true },
                )
            }
        }

        rule.onNodeWithText("＋  Registrar leitura").performClick()
        assertTrue(clicked)
    }

    @Test
    fun testRankingScreenRendersOrder() {
        val sampleRankings = listOf(
            Ranking(position = 1, name = "Bia Nunes", totalPages = 340),
            Ranking(position = 2, name = "Ana Lima", totalPages = 285)
        )

        rule.setContent {
            CapitularTheme {
                RankingScreen(rankings = sampleRankings)
            }
        }

        rule.onNodeWithText("Bia Nunes").assertExists()
        rule.onNodeWithText("340 pág.").assertExists()
        rule.onNodeWithText("Ana Lima").assertExists()
        rule.onNodeWithText("285 pág.").assertExists()
    }
}

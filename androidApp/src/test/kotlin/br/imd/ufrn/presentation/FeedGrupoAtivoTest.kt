package br.imd.ufrn.presentation

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.imd.ufrn.presentation.screens.FeedScreen
import br.imd.ufrn.presentation.theme.CapitularTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE, application = Application::class)
class FeedGrupoAtivoTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun feedMostraOGrupoAtivoETocarAbreOsGrupos() {
        var abriuGrupos = false
        rule.setContent {
            CapitularTheme {
                FeedScreen(
                    userName = "Raquel",
                    nomeDoGrupo = "Clube do Torto Arado",
                    onAbrirGrupos = { abriuGrupos = true },
                    estado = EstadoDaTela.Vazio,
                    onLogout = {},
                    onNovoCheckInClick = {},
                    onTentarDeNovo = {},
                )
            }
        }

        rule.onNodeWithText("Clube do Torto Arado", useUnmergedTree = true).assertExists()
        rule.onNodeWithText("GRUPO ATIVO", useUnmergedTree = true).performClick()
        assertTrue(abriuGrupos)
    }
}

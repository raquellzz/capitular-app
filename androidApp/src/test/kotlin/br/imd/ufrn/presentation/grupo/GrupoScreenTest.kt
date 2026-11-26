package br.imd.ufrn.presentation.grupo

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.presentation.EstadoDaTela
import br.imd.ufrn.presentation.theme.CapitularTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], manifest = Config.NONE, application = Application::class)
class GrupoScreenTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun mostrar(
        estado: EstadoDaTela<Grupo>,
        formulario: FormularioDeGrupo = FormularioDeGrupo(),
        onTentarDeNovo: () -> Unit = {},
    ) = rule.setContent {
        CapitularTheme {
            GrupoScreen(
                estado = estado,
                formulario = formulario,
                eventos = EventosDoFormularioDeGrupo(),
                onTentarDeNovo = onTentarDeNovo,
                onSair = {},
            )
        }
    }

    @Test
    fun semGrupoOCriarSoHabilitaComNomeValido() {
        mostrar(EstadoDaTela.Vazio, FormularioDeGrupo(nome = "A"))

        rule.onNodeWithText("Criar grupo").assertIsNotEnabled()
        rule.onNodeWithText("Entrar no grupo").assertIsNotEnabled()
    }

    @Test
    fun semGrupoComNomeValidoHabilitaCriar() {
        mostrar(EstadoDaTela.Vazio, FormularioDeGrupo(nome = "Clube do Livro"))

        rule.onNodeWithText("Criar grupo").assertIsEnabled()
    }

    @Test
    fun descricaoLongaDesabilitaCriar() {
        mostrar(EstadoDaTela.Vazio, FormularioDeGrupo(nome = "Clube do Livro", descricao = "a".repeat(301)))

        rule.onNodeWithText("Criar grupo").assertIsNotEnabled()
    }

    @Test
    fun erroMostraMensagemETentarDeNovo() {
        var tentou = false
        mostrar(EstadoDaTela.Erro("Sem conexão com o Capitular."), onTentarDeNovo = { tentou = true })

        rule.onNodeWithText("Sem conexão com o Capitular.").assertExists()
        rule.onNodeWithText("Tentar de novo").performClick()
        assertTrue(tentou)
    }
}

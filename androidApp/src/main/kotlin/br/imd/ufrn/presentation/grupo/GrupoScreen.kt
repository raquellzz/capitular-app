package br.imd.ufrn.presentation.grupo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.presentation.EstadoDaTela
import br.imd.ufrn.presentation.theme.CapitularTheme

/** Liga a [GrupoScreen] ao [GrupoViewModel] e avisa quando há um grupo para abrir. */
@Composable
fun GrupoRota(
    viewModel: GrupoViewModel,
    onGrupoPronto: (Grupo) -> Unit,
    onSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()

    val grupoPronto = (estado as? EstadoDaTela.Sucesso)?.dados
    LaunchedEffect(grupoPronto?.id) {
        grupoPronto?.let { onGrupoPronto(it) }
    }

    GrupoScreen(
        estado = estado,
        formulario = formulario,
        eventos = viewModel.acoes.eventos(),
        onTentarDeNovo = viewModel::carregar,
        onSair = onSair,
        modifier = modifier,
    )
}

@Composable
fun GrupoScreen(
    estado: EstadoDaTela<Grupo>,
    formulario: FormularioDeGrupo,
    eventos: EventosDoFormularioDeGrupo,
    onTentarDeNovo: () -> Unit,
    onSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        when (estado) {
            // Com grupo, a rota já está navegando para o feed: mantém o indicador.
            EstadoDaTela.Carregando, is EstadoDaTela.Sucesso -> Carregando()
            EstadoDaTela.Vazio ->
                EscolherGrupo(
                    formulario = formulario,
                    eventos = eventos,
                    onSair = onSair,
                )
            is EstadoDaTela.Erro ->
                ErroAoCarregar(
                    mensagem = estado.mensagem,
                    onTentarDeNovo = onTentarDeNovo,
                    onSair = onSair,
                )
        }
    }
}

@Composable
private fun Carregando() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = "Carregando seus grupos" },
        )
    }
}

@Composable
private fun ErroAoCarregar(
    mensagem: String,
    onTentarDeNovo: () -> Unit,
    onSair: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().widthIn(max = 480.dp).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Não deu para abrir seus grupos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = mensagem,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onTentarDeNovo, shape = RoundedCornerShape(14.dp)) {
            Text("Tentar de novo", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onSair) { Text("Sair") }
    }
}

@Composable
private fun EscolherGrupo(
    formulario: FormularioDeGrupo,
    eventos: EventosDoFormularioDeGrupo,
    onSair: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "SEU GRUPO DE LEITURA",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Text(
                    text = "Leia junto com alguém",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
            }
            TextButton(
                onClick = onSair,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) { Text("Sair") }
        }
        Text(
            text = "Você ainda não participa de nenhum grupo. Crie o seu ou entre com o código de convite de um amigo.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        FormulariosDeGrupo(formulario = formulario, eventos = eventos)
    }
}

@Preview(showBackground = true)
@Composable
private fun GrupoVazioPreview() {
    CapitularTheme {
        GrupoScreen(
            estado = EstadoDaTela.Vazio,
            formulario = FormularioDeGrupo(nome = "Clube do Torto Arado", descricao = "Um capítulo por dia."),
            eventos = EventosDoFormularioDeGrupo(),
            onTentarDeNovo = {},
            onSair = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GrupoErroPreview() {
    CapitularTheme {
        GrupoScreen(
            estado = EstadoDaTela.Erro("Sem conexão com o Capitular. Verifique sua internet e tente de novo."),
            formulario = FormularioDeGrupo(),
            eventos = EventosDoFormularioDeGrupo(),
            onTentarDeNovo = {},
            onSair = {},
        )
    }
}

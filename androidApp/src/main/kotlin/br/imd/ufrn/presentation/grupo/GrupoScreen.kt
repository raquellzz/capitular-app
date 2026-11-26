package br.imd.ufrn.presentation.grupo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
    onGrupoPronto: (grupoId: String) -> Unit,
    onSair: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()

    val grupoPronto = (estado as? EstadoDaTela.Sucesso)?.dados
    LaunchedEffect(grupoPronto?.id) {
        grupoPronto?.let { onGrupoPronto(it.id) }
    }

    GrupoScreen(
        estado = estado,
        formulario = formulario,
        onNomeChange = viewModel::alterarNome,
        onCodigoChange = viewModel::alterarCodigo,
        onCriar = viewModel::criarGrupo,
        onEntrar = viewModel::entrarNoGrupo,
        onTentarDeNovo = viewModel::carregar,
        onSair = onSair,
        modifier = modifier,
    )
}

@Composable
fun GrupoScreen(
    estado: EstadoDaTela<Grupo>,
    formulario: FormularioDeGrupo,
    onNomeChange: (String) -> Unit,
    onCodigoChange: (String) -> Unit,
    onCriar: () -> Unit,
    onEntrar: () -> Unit,
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
                    onNomeChange = onNomeChange,
                    onCodigoChange = onCodigoChange,
                    onCriar = onCriar,
                    onEntrar = onEntrar,
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
    onNomeChange: (String) -> Unit,
    onCodigoChange: (String) -> Unit,
    onCriar: () -> Unit,
    onEntrar: () -> Unit,
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

        formulario.erro?.let { erro ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(text = erro, modifier = Modifier.fillMaxWidth().padding(12.dp))
            }
        }

        CartaoDeAcao(titulo = "Criar um grupo") {
            OutlinedTextField(
                value = formulario.nome,
                onValueChange = onNomeChange,
                label = { Text("Nome do grupo") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formulario.enviando,
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(12.dp),
            )
            Button(
                onClick = onCriar,
                enabled = formulario.podeCriar,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Criar grupo", fontWeight = FontWeight.Bold) }
        }

        CartaoDeAcao(titulo = "Entrar com convite") {
            OutlinedTextField(
                value = formulario.codigo,
                onValueChange = onCodigoChange,
                label = { Text("Código de convite") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formulario.enviando,
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                shape = RoundedCornerShape(12.dp),
            )
            OutlinedButton(
                onClick = onEntrar,
                enabled = formulario.podeEntrar,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
            ) { Text("Entrar no grupo", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun CartaoDeAcao(
    titulo: String,
    conteudo: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.semantics { heading() },
            )
            conteudo()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GrupoVazioPreview() {
    CapitularTheme {
        GrupoScreen(
            estado = EstadoDaTela.Vazio,
            formulario = FormularioDeGrupo(nome = "Clube do Torto Arado"),
            onNomeChange = {},
            onCodigoChange = {},
            onCriar = {},
            onEntrar = {},
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
            onNomeChange = {},
            onCodigoChange = {},
            onCriar = {},
            onEntrar = {},
            onTentarDeNovo = {},
            onSair = {},
        )
    }
}

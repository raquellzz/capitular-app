package br.imd.ufrn.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.imd.ufrn.R
import br.imd.ufrn.domain.feed.Estatisticas
import br.imd.ufrn.presentation.EstadoDaTela
import br.imd.ufrn.presentation.components.CheckInCard
import br.imd.ufrn.presentation.feed.FeedViewModel
import br.imd.ufrn.presentation.feed.paraCartao
import br.imd.ufrn.presentation.feed.tempoRelativo
import br.imd.ufrn.presentation.model.CheckIn
import br.imd.ufrn.presentation.theme.CapitularTheme
import java.time.Instant

/**
 * Liga a [FeedScreen] ao [FeedViewModel]. O feed é recarregado sempre que a
 * tela volta a aparecer, por exemplo depois de registrar uma leitura.
 */
@Composable
fun FeedRota(
    viewModel: FeedViewModel,
    userName: String,
    nomeDoGrupo: String,
    onAbrirGrupos: () -> Unit,
    onLogout: () -> Unit,
    onNovoCheckInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LifecycleResumeEffect(viewModel) {
        viewModel.atualizar()
        onPauseOrDispose {}
    }
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val resumo by viewModel.resumo.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val guardadoEm by viewModel.guardadoEm.collectAsStateWithLifecycle()
    val agora = Instant.now()

    FeedScreen(
        userName = userName,
        nomeDoGrupo = nomeDoGrupo,
        estado =
            when (val atual = estado) {
                EstadoDaTela.Carregando -> EstadoDaTela.Carregando
                EstadoDaTela.Vazio -> EstadoDaTela.Vazio
                is EstadoDaTela.Erro -> atual
                is EstadoDaTela.Sucesso -> EstadoDaTela.Sucesso(atual.dados.map { it.paraCartao(agora) }, atual.desatualizado)
            },
        resumo = resumo,
        aviso = aviso,
        salvoHa = guardadoEm?.let { tempoRelativo(it, agora) },
        onAbrirGrupos = onAbrirGrupos,
        onLogout = onLogout,
        onNovoCheckInClick = onNovoCheckInClick,
        onTentarDeNovo = viewModel::atualizar,
        onFecharAviso = viewModel::avisoVisto,
        modifier = modifier,
    )
}

@Composable
fun FeedScreen(
    userName: String,
    nomeDoGrupo: String,
    estado: EstadoDaTela<List<CheckIn>>,
    onAbrirGrupos: () -> Unit,
    onLogout: () -> Unit,
    onNovoCheckInClick: () -> Unit,
    onTentarDeNovo: () -> Unit,
    modifier: Modifier = Modifier,
    resumo: Estatisticas? = null,
    aviso: String? = null,
    salvoHa: String? = null,
    onFecharAviso: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp).fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { FeedHeader(userName = userName, onLogout = onLogout) }
            item { GrupoAtivo(nome = nomeDoGrupo, onClick = onAbrirGrupos) }
            if (estado is EstadoDaTela.Sucesso && estado.desatualizado) {
                item { AvisoSemConexao(salvoHa = salvoHa, onTentarDeNovo = onTentarDeNovo) }
            }
            resumo?.let { numeros -> item { ResumoDeLeitura(numeros) } }
            aviso?.let { mensagem -> item { AvisoDoFeed(mensagem, onFecharAviso) } }
            item {
                Column(Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "ATIVIDADE DO GRUPO",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        text = "Leituras recentes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                }
            }

            when (estado) {
                EstadoDaTela.Carregando ->
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.semantics { contentDescription = "Carregando as leituras do grupo" },
                            )
                        }
                    }
                EstadoDaTela.Vazio ->
                    item {
                        MensagemDoFeed(
                            titulo = "Nenhuma leitura ainda",
                            texto = "Seja a primeira pessoa do grupo a registrar uma leitura hoje!",
                            acao = "Registrar leitura",
                            onAcao = onNovoCheckInClick,
                        )
                    }
                is EstadoDaTela.Erro ->
                    item {
                        MensagemDoFeed(
                            titulo = "Não deu para carregar o feed",
                            texto = estado.mensagem,
                            acao = "Tentar de novo",
                            onAcao = onTentarDeNovo,
                        )
                    }
                is EstadoDaTela.Sucesso ->
                    items(estado.dados, key = { it.id }) { checkIn ->
                        CheckInCard(
                            friendName = checkIn.friendName,
                            bookTitle = checkIn.bookTitle,
                            pageRange = checkIn.pageRange,
                            timeElapsed = checkIn.timeElapsed,
                            photoUrl = checkIn.photoUrl,
                            avatarUrl = checkIn.avatarUrl,
                            pagesRead = checkIn.pagesRead,
                            note = checkIn.note,
                        )
                    }
            }
        }

        Button(
            onClick = onNovoCheckInClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .height(54.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("＋  Registrar leitura", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FeedHeader(
    userName: String,
    onLogout: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            ) {
                Image(
                    painter = painterResource(R.drawable.capitular_logo),
                    contentDescription = "Logo do Capitular",
                    modifier = Modifier.fillMaxSize().padding(3.dp),
                )
            }
            Column {
                Text(
                    text = "Olá, ${userName.substringBefore(' ')}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "Qual página marcou seu dia?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        TextButton(
            onClick = onLogout,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        ) {
            Text("Sair")
        }
    }
}

/** Mostra qual grupo está ativo; tocar abre a aba Grupos (trocar, convidar, ver membros). */
@Composable
private fun GrupoAtivo(
    nome: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClickLabel = "Ver ou trocar de grupo", role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("👥", fontSize = 22.sp, modifier = Modifier.clearAndSetSemantics {})
            Column(Modifier.weight(1f)) {
                Text(
                    text = "GRUPO ATIVO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                )
                Text(
                    text = nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text("Trocar ›", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Sequência de dias lendo e totais do usuário, vindos de `users/me/stats`. */
@Composable
private fun ResumoDeLeitura(numeros: Estatisticas) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "SUA SEQUÊNCIA",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.64f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                    )
                    Text(
                        text =
                            when (numeros.sequenciaAtual) {
                                0 -> "Comece hoje!"
                                1 -> "1 dia lendo"
                                else -> "${numeros.sequenciaAtual} dias lendo"
                            },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                Surface(color = MaterialTheme.colorScheme.surface, shape = CircleShape) {
                    Text(
                        text = numeros.sequenciaAtual.toString(),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp).clearAndSetSemantics {},
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Numero(valor = numeros.paginasLidas, rotulo = "páginas lidas")
                Numero(valor = numeros.checkIns, rotulo = "leituras")
                Numero(valor = numeros.maiorSequencia, rotulo = "maior sequência")
            }
        }
    }
}

@Composable
private fun Numero(
    valor: Int,
    rotulo: String,
) {
    Column(Modifier.semantics(mergeDescendants = true) {}) {
        Text(valor.toString(), fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(rotulo, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

@Composable
private fun MensagemDoFeed(
    titulo: String,
    texto: String,
    acao: String,
    onAcao: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = onAcao) { Text(acao, fontWeight = FontWeight.Bold) }
        }
    }
}

/** Sem conexão: o feed mostrado é a última cópia guardada no aparelho. */
@Composable
private fun AvisoSemConexao(
    salvoHa: String?,
    onTentarDeNovo: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("📡", fontSize = 20.sp, modifier = Modifier.clearAndSetSemantics {})
            Column(Modifier.weight(1f)) {
                Text("Você está sem conexão", fontWeight = FontWeight.Bold)
                Text(
                    text =
                        "Mostrando as leituras salvas no aparelho" +
                            (salvoHa?.let { " (atualizadas $it)" } ?: "") + ". Podem estar desatualizadas.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onTentarDeNovo) { Text("Atualizar", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun AvisoDoFeed(
    mensagem: String,
    onFechar: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(mensagem, modifier = Modifier.weight(1f))
            TextButton(onClick = onFechar) { Text("OK") }
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun FeedComLeiturasPreview() {
    CapitularTheme {
        FeedScreen(
            userName = "Ana Lima",
            nomeDoGrupo = "Clube do Torto Arado",
            estado =
                EstadoDaTela.Sucesso(
                    listOf(
                        CheckIn(
                            id = "1",
                            friendName = "Bia Nunes",
                            bookTitle = "Torto Arado",
                            pageRange = "pág. 84 a 112",
                            timeElapsed = "há 18 min",
                            pagesRead = 29,
                            note = "A história da Belonísia não sai da cabeça.",
                        ),
                    ),
                ),
            resumo = Estatisticas(paginasLidas = 340, checkIns = 12, sequenciaAtual = 7, maiorSequencia = 9),
            onAbrirGrupos = {},
            onLogout = {},
            onNovoCheckInClick = {},
            onTentarDeNovo = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedVazioPreview() {
    CapitularTheme {
        FeedScreen(
            userName = "Ana Lima",
            nomeDoGrupo = "Clube do Torto Arado",
            estado = EstadoDaTela.Vazio,
            onAbrirGrupos = {},
            onLogout = {},
            onNovoCheckInClick = {},
            onTentarDeNovo = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedErroPreview() {
    CapitularTheme {
        FeedScreen(
            userName = "Ana Lima",
            nomeDoGrupo = "Clube do Torto Arado",
            estado = EstadoDaTela.Erro("Sem conexão com o Capitular. Verifique sua internet e tente de novo."),
            onAbrirGrupos = {},
            onLogout = {},
            onNovoCheckInClick = {},
            onTentarDeNovo = {},
        )
    }
}

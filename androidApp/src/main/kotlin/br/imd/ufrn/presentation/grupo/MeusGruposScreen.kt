package br.imd.ufrn.presentation.grupo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.Membro
import br.imd.ufrn.presentation.EstadoDaTela
import br.imd.ufrn.presentation.theme.CapitularTheme

/** Liga a [MeusGruposScreen] ao [MeusGruposViewModel], ao compartilhamento e à navegação. */
@Composable
fun MeusGruposRota(
    viewModel: MeusGruposViewModel,
    onAbrirFeed: (Grupo) -> Unit,
    onEscolherGrupo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    val aviso by viewModel.aviso.collectAsStateWithLifecycle()
    val saindo by viewModel.saindo.collectAsStateWithLifecycle()
    val destino by viewModel.destino.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(destino) {
        when (val atual = destino) {
            null -> return@LaunchedEffect
            is DestinoDosGrupos.AbrirFeed -> onAbrirFeed(atual.grupo)
            DestinoDosGrupos.EscolherGrupo -> onEscolherGrupo()
        }
        viewModel.navegacaoConcluida()
    }

    MeusGruposScreen(
        estado = estado,
        formulario = formulario,
        eventos = viewModel.acoes.eventos(),
        aviso = aviso,
        saindo = saindo,
        onTentarDeNovo = viewModel::carregar,
        onAbrirGrupo = viewModel::trocarPara,
        onSairDoGrupo = viewModel::sairDoGrupo,
        onFecharAviso = viewModel::avisoVisto,
        onCopiarConvite = { grupo -> copiarConvite(context, grupo) },
        onCompartilharConvite = { grupo -> compartilharConvite(context, grupo) },
        modifier = modifier,
    )
}

private fun copiarConvite(
    context: Context,
    grupo: Grupo,
) {
    context
        .getSystemService(ClipboardManager::class.java)
        .setPrimaryClip(ClipData.newPlainText("Código de convite", grupo.codigoConvite))
    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
}

private fun compartilharConvite(
    context: Context,
    grupo: Grupo,
) {
    val texto =
        "Bora ler junto? Entra no meu grupo \"${grupo.nome}\" no Capitular " +
            "com o código de convite ${grupo.codigoConvite}."
    val envio =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }
    context.startActivity(Intent.createChooser(envio, "Compartilhar convite"))
}

@Composable
fun MeusGruposScreen(
    estado: EstadoDaTela<MeusGrupos>,
    formulario: FormularioDeGrupo,
    eventos: EventosDoFormularioDeGrupo,
    aviso: String?,
    saindo: Boolean,
    onTentarDeNovo: () -> Unit,
    onAbrirGrupo: (Grupo) -> Unit,
    onSairDoGrupo: () -> Unit,
    onFecharAviso: () -> Unit,
    onCopiarConvite: (Grupo) -> Unit,
    onCompartilharConvite: (Grupo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                Text(
                    text = "SEUS GRUPOS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Text(
                    text = "Grupos de leitura",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
            }

            aviso?.let { Aviso(mensagem = it, onFechar = onFecharAviso) }

            when (estado) {
                EstadoDaTela.Carregando ->
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.semantics { contentDescription = "Carregando seus grupos" },
                        )
                    }
                EstadoDaTela.Vazio -> FormulariosDeGrupo(formulario = formulario, eventos = eventos)
                is EstadoDaTela.Erro ->
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(estado.mensagem, textAlign = TextAlign.Center)
                        Button(onClick = onTentarDeNovo) { Text("Tentar de novo") }
                    }
                is EstadoDaTela.Sucesso ->
                    ConteudoDosGrupos(
                        grupos = estado.dados,
                        formulario = formulario,
                        eventos = eventos,
                        saindo = saindo,
                        onAbrirGrupo = onAbrirGrupo,
                        onSairDoGrupo = onSairDoGrupo,
                        onCopiarConvite = onCopiarConvite,
                        onCompartilharConvite = onCompartilharConvite,
                    )
            }
        }
    }
}

@Composable
private fun ConteudoDosGrupos(
    grupos: MeusGrupos,
    formulario: FormularioDeGrupo,
    eventos: EventosDoFormularioDeGrupo,
    saindo: Boolean,
    onAbrirGrupo: (Grupo) -> Unit,
    onSairDoGrupo: () -> Unit,
    onCopiarConvite: (Grupo) -> Unit,
    onCompartilharConvite: (Grupo) -> Unit,
) {
    var mostrarFormularios by rememberSaveable { mutableStateOf(false) }

    GrupoAtual(
        grupo = grupos.atual,
        membros = grupos.membros,
        saindo = saindo,
        onSairDoGrupo = onSairDoGrupo,
        onCopiarConvite = onCopiarConvite,
        onCompartilharConvite = onCompartilharConvite,
    )

    if (grupos.outros.isNotEmpty()) {
        Text(
            text = "Outros grupos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        grupos.outros.forEach { grupo -> OutroGrupo(grupo = grupo, onAbrir = { onAbrirGrupo(grupo) }) }
    }

    if (mostrarFormularios) {
        FormulariosDeGrupo(formulario = formulario, eventos = eventos)
        TextButton(onClick = { mostrarFormularios = false }, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    } else {
        OutlinedButton(
            onClick = { mostrarFormularios = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        ) { Text("＋  Criar ou entrar em outro grupo", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun GrupoAtual(
    grupo: Grupo,
    membros: List<Membro>,
    saindo: Boolean,
    onSairDoGrupo: () -> Unit,
    onCopiarConvite: (Grupo) -> Unit,
    onCompartilharConvite: (Grupo) -> Unit,
) {
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }

    CartaoDeGrupo(titulo = grupo.nome) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Etiqueta("Grupo atual")
            if (grupo.souAdmin) Etiqueta("Você é admin")
            Etiqueta(if (grupo.totalMembros == 1) "1 membro" else "${grupo.totalMembros} membros")
        }
        Text(
            text = grupo.descricao ?: "Sem descrição.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text("Convide para o grupo", fontWeight = FontWeight.Bold)
        Text(
            text = grupo.codigoConvite,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
            modifier = Modifier.semantics { contentDescription = "Código de convite: ${grupo.codigoConvite}" },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onCopiarConvite(grupo) }) { Text("Copiar código") }
            Button(onClick = { onCompartilharConvite(grupo) }) { Text("Compartilhar") }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text("Membros", fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
        membros.forEach { membro -> LinhaDeMembro(membro) }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (grupo.souAdmin) {
            Text(
                text = "Como admin, você não pode sair deste grupo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            TextButton(
                onClick = { confirmarSaida = true },
                enabled = !saindo,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(if (saindo) "Saindo…" else "Sair do grupo") }
        }
    }

    if (confirmarSaida) {
        AlertDialog(
            onDismissRequest = { confirmarSaida = false },
            title = { Text("Sair de \"${grupo.nome}\"?") },
            text = { Text("Você deixa de ver as leituras deste grupo. Para voltar, vai precisar do código de convite.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSaida = false
                    onSairDoGrupo()
                }) { Text("Sair") }
            },
            dismissButton = { TextButton(onClick = { confirmarSaida = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun LinhaDeMembro(membro: Membro) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(iniciais(membro.nome), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(membro.nome, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = "@${membro.usuario}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (membro.admin) Etiqueta("Admin")
    }
}

@Composable
private fun OutroGrupo(
    grupo: Grupo,
    onAbrir: () -> Unit,
) {
    Surface(
        onClick = onAbrir,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(grupo.nome, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text =
                        buildString {
                            append(if (grupo.totalMembros == 1) "1 membro" else "${grupo.totalMembros} membros")
                            if (grupo.souAdmin) append(" · você é admin")
                        },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text("Abrir", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun Aviso(
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

private fun iniciais(nome: String): String =
    nome
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .let { partes -> if (partes.size >= 2) "${partes.first().first()}${partes.last().first()}" else nome.take(2) }
        .uppercase()

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun MeusGruposPreview() {
    CapitularTheme {
        MeusGruposScreen(
            estado =
                EstadoDaTela.Sucesso(
                    MeusGrupos(
                        atual = Grupo("g1", "Clube do Torto Arado", "Um capítulo por dia, sem pressa.", "ABCD2345", 3, true),
                        membros =
                            listOf(
                                Membro("u1", "Raquel Freire", "raquels", admin = true),
                                Membro("u2", "Pablo Paiva", "pablo", admin = false),
                            ),
                        outros = listOf(Grupo("g2", "Leituras da UFRN", null, "QWER5678", 12)),
                    ),
                ),
            formulario = FormularioDeGrupo(),
            eventos = EventosDoFormularioDeGrupo(),
            aviso = null,
            saindo = false,
            onTentarDeNovo = {},
            onAbrirGrupo = {},
            onSairDoGrupo = {},
            onFecharAviso = {},
            onCopiarConvite = {},
            onCompartilharConvite = {},
        )
    }
}

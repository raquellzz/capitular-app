package br.imd.ufrn.presentation.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.imd.ufrn.domain.feed.FotoDaLeitura
import br.imd.ufrn.domain.feed.RegrasDoCheckIn
import br.imd.ufrn.presentation.checkin.CheckInViewModel
import br.imd.ufrn.presentation.checkin.FormularioDeCheckIn
import br.imd.ufrn.presentation.checkin.lerFotoParaEnvio
import br.imd.ufrn.presentation.theme.CapitularTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Liga a [CheckInScreen] ao [CheckInViewModel] e ao seletor de fotos do Android. */
@Composable
fun CheckInRota(
    viewModel: CheckInViewModel,
    onVoltar: () -> Unit,
    onSalvo: () -> Unit,
) {
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()
    val salvo by viewModel.salvo.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    // Seletor de fotos do sistema: não pede permissão e só mostra imagens.
    val seletorDeFoto =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.lendoFoto()
                escopo.launch { viewModel.escolherFoto(lerFotoParaEnvio(context, uri)) }
            }
        }

    LaunchedEffect(salvo) { if (salvo) onSalvo() }

    CheckInScreen(
        formulario = formulario,
        onLivroChange = viewModel::alterarLivro,
        onPaginaInicialChange = viewModel::alterarPaginaInicial,
        onPaginaFinalChange = viewModel::alterarPaginaFinal,
        onNotaChange = viewModel::alterarNota,
        onEscolherFoto = {
            seletorDeFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onSalvar = viewModel::salvar,
        onVoltar = onVoltar,
    )
}

@Composable
fun CheckInScreen(
    formulario: FormularioDeCheckIn,
    onLivroChange: (String) -> Unit,
    onPaginaInicialChange: (String) -> Unit,
    onPaginaFinalChange: (String) -> Unit,
    onNotaChange: (String) -> Unit,
    onEscolherFoto: () -> Unit,
    onSalvar: () -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val habilitado = !formulario.enviando
    val mensagem = formulario.erro ?: formulario.problema

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextButton(
                onClick = onVoltar,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) {
                Text("← Voltar", fontWeight = FontWeight.Bold)
            }

            Column {
                Text(
                    text = "REGISTRAR LEITURA",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Text(
                    text = "O que você leu hoje?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
            }

            SeletorDeFoto(
                foto = formulario.foto,
                lendo = formulario.lendoFoto,
                habilitado = habilitado,
                onEscolher = onEscolherFoto,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    OutlinedTextField(
                        value = formulario.livro,
                        onValueChange = onLivroChange,
                        label = { Text("Livro") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = habilitado,
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = formulario.paginaInicial,
                            onValueChange = onPaginaInicialChange,
                            label = { Text("Página inicial") },
                            modifier = Modifier.weight(1f),
                            enabled = habilitado,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                        )
                        OutlinedTextField(
                            value = formulario.paginaFinal,
                            onValueChange = onPaginaFinalChange,
                            label = { Text("Página final") },
                            modifier = Modifier.weight(1f),
                            enabled = habilitado,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                    formulario.paginasLidas?.let { paginas ->
                        Text(
                            text = if (paginas == 1) "1 página lida" else "$paginas páginas lidas",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    OutlinedTextField(
                        value = formulario.nota,
                        onValueChange = onNotaChange,
                        label = { Text("O que marcou essa leitura? (opcional)") },
                        supportingText = { Text("${formulario.nota.trim().length}/${RegrasDoCheckIn.MAXIMO_NOTA}") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = habilitado,
                        minLines = 2,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        shape = RoundedCornerShape(12.dp),
                    )

                    mensagem?.let {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(text = it, modifier = Modifier.fillMaxWidth().padding(12.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Button(
                        onClick = onSalvar,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = formulario.podeSalvar,
                    ) {
                        if (formulario.enviando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp).semantics { contentDescription = "Salvando a leitura" },
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Salvar Check-in", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/** Área da foto: convite para escolher, indicador enquanto lê, ou a prévia com "Trocar foto". */
@Composable
private fun SeletorDeFoto(
    foto: FotoDaLeitura?,
    lendo: Boolean,
    habilitado: Boolean,
    onEscolher: () -> Unit,
) {
    val previa by produceState<ImageBitmap?>(null, foto) {
        value =
            foto?.let {
                withContext(Dispatchers.Default) {
                    BitmapFactory.decodeByteArray(it.bytes, 0, it.bytes.size)?.asImageBitmap()
                }
            }
    }
    val formato = RoundedCornerShape(24.dp)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(formato)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, formato)
                .clickable(
                    enabled = habilitado && !lendo,
                    onClickLabel = if (foto == null) "Escolher foto da leitura" else "Trocar foto da leitura",
                    role = Role.Button,
                    onClick = onEscolher,
                ),
        contentAlignment = Alignment.Center,
    ) {
        val imagem = previa
        when {
            lendo -> CircularProgressIndicator(Modifier.semantics { contentDescription = "Preparando a foto" })
            imagem != null -> {
                Image(
                    bitmap = imagem,
                    contentDescription = "Foto escolhida para a leitura",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Trocar foto", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold)
                }
            }
            else ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📷", fontSize = 36.sp)
                    Text("Adicionar foto da leitura", fontWeight = FontWeight.Bold)
                    Text(
                        "A página, a capa ou o cantinho onde você leu",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CheckInVazioPreview() {
    CapitularTheme {
        CheckInScreen(
            formulario = FormularioDeCheckIn(),
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun CheckInInvalidoPreview() {
    CapitularTheme {
        CheckInScreen(
            formulario = FormularioDeCheckIn(livro = "Torto Arado", paginaInicial = "50", paginaFinal = "20"),
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

package br.imd.ufrn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.imd.ufrn.ui.CapitularTheme

fun isFormularioValido(pagInicio: String, pagFim: String): Boolean {
    if (pagInicio.isBlank() || pagFim.isBlank()) return false
    if (!pagInicio.all { it.isDigit() } || !pagFim.all { it.isDigit() }) return false
    val start = pagInicio.toIntOrNull() ?: return false
    val end = pagFim.toIntOrNull() ?: return false
    return start > 0 && end > 0 && end > start
}

fun getFormularioValidationMessage(pagInicio: String, pagFim: String): String? {
    if (pagInicio.isBlank() && pagFim.isBlank()) return null
    if (!pagInicio.all { it.isDigit() } || !pagFim.all { it.isDigit() }) {
        return "Digite apenas números positivos."
    }
    val start = pagInicio.toIntOrNull()
    val end = pagFim.toIntOrNull()
    if (start != null && start <= 0) {
        return "A página inicial deve ser maior que zero."
    }
    if (end != null && end <= 0) {
        return "A página final deve ser maior que zero."
    }
    if (start != null && end != null && end <= start) {
        return "A página final não pode ser anterior ou igual à inicial."
    }
    return null
}

@Composable
fun CheckInScreen(
    paginaInicial: String,
    paginaFinal: String,
    onInicioChange: (String) -> Unit,
    onFimChange: (String) -> Unit,
    onSalvarCheckIn: (inicio: String, fim: String) -> Unit,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier,
    bookTitle: String = "Livro Atual",
    errorMessage: String? = null,
) {
    val validationWarning = getFormularioValidationMessage(paginaInicial, paginaFinal)
    val displayError = errorMessage ?: validationWarning

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                TextButton(
                    onClick = onVoltar,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) {
                    Text("← Voltar", fontWeight = FontWeight.Bold)
                }
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
                    text = "Atualize suas páginas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = bookTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.semantics { heading() },
                    )

                    OutlinedTextField(
                        value = paginaInicial,
                        onValueChange = onInicioChange,
                        label = { Text("Página Inicial") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                    )

                    OutlinedTextField(
                        value = paginaFinal,
                        onValueChange = onFimChange,
                        label = { Text("Página Final") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                    )

                    if (!displayError.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = displayError,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    Button(
                        onClick = { onSalvarCheckIn(paginaInicial, paginaFinal) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = isFormularioValido(paginaInicial, paginaFinal),
                    ) {
                        Text("Salvar Check-in", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckInScreenEmptyPreview() {
    CapitularTheme {
        CheckInScreen(
            paginaInicial = "",
            paginaFinal = "",
            onInicioChange = {},
            onFimChange = {},
            onSalvarCheckIn = { _, _ -> },
            onVoltar = {},
            bookTitle = "Torto Arado",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckInScreenInvalidPreview() {
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

@Preview(showBackground = true)
@Composable
private fun CheckInScreenFilledPreview() {
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

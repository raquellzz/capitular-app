package br.imd.ufrn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.imd.ufrn.R
import br.imd.ufrn.auth.AuthFormState
import br.imd.ufrn.auth.AuthMode

@Composable
fun AuthScreen(
    state: AuthFormState,
    onModeChange: (AuthMode) -> Unit,
    onIdentifierChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onSubmit: () -> Unit,
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
        Column(
            modifier =
                Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            BrandHeader()
            Spacer(Modifier.height(28.dp))
            AuthModeSelector(
                selectedMode = state.mode,
                enabled = !state.isSubmitting,
                onModeChange = onModeChange,
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = if (state.mode == AuthMode.LOGIN) "Continue sua leitura" else "Crie sua conta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text =
                    if (state.mode == AuthMode.LOGIN) {
                        "Entre para acompanhar seu grupo e manter a sequência."
                    } else {
                        "Junte seus amigos e transforme capítulos em consistência."
                    },
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(22.dp))

            if (state.mode == AuthMode.REGISTER) {
                AuthField(
                    value = state.displayName,
                    onValueChange = onDisplayNameChange,
                    label = "Seu nome",
                    enabled = !state.isSubmitting,
                )
                Spacer(Modifier.height(12.dp))
                AuthField(
                    value = state.username,
                    onValueChange = onUsernameChange,
                    label = "Nome de usuário",
                    supportingText = "Letras, números e sublinhado",
                    enabled = !state.isSubmitting,
                )
                Spacer(Modifier.height(12.dp))
                AuthField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = "E-mail",
                    enabled = !state.isSubmitting,
                )
            } else {
                AuthField(
                    value = state.identifier,
                    onValueChange = onIdentifierChange,
                    label = "E-mail ou nome de usuário",
                    enabled = !state.isSubmitting,
                )
            }

            Spacer(Modifier.height(12.dp))
            AuthField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Senha",
                enabled = !state.isSubmitting,
                isPassword = true,
                imeAction =
                    if (state.mode == AuthMode.LOGIN) {
                        ImeAction.Done
                    } else {
                        ImeAction.Next
                    },
                onDone = if (state.mode == AuthMode.LOGIN) onSubmit else null,
            )
            if (state.mode == AuthMode.REGISTER) {
                Spacer(Modifier.height(12.dp))
                AuthField(
                    value = state.passwordConfirmation,
                    onValueChange = onPasswordConfirmationChange,
                    label = "Confirme a senha",
                    enabled = !state.isSubmitting,
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onDone = onSubmit,
                )
            }

            state.errorMessage?.let { message ->
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !state.isSubmitting,
                shape = RoundedCornerShape(16.dp),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = if (state.mode == AuthMode.LOGIN) "Entrar" else "Criar conta",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Text(
                text = "A leitura do mundo precede a leitura da palavra",
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun BrandHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = CapitularLavender,
        ) {
            Image(
                painter = painterResource(R.drawable.capitular_logo),
                contentDescription = "Logo do Capitular",
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(4.dp),
            )
        }
        Column {
            Text(
                text = "CAPITULAR",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp,
            )
            Text(
                text = "leitura é melhor em grupo",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AuthModeSelector(
    selectedMode: AuthMode,
    enabled: Boolean,
    onModeChange: (AuthMode) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(4.dp)) {
            AuthModeButton(
                text = "Entrar",
                selected = selectedMode == AuthMode.LOGIN,
                enabled = enabled,
                onClick = { onModeChange(AuthMode.LOGIN) },
                modifier = Modifier.weight(1f),
            )
            AuthModeButton(
                text = "Cadastrar",
                selected = selectedMode == AuthMode.REGISTER,
                enabled = enabled,
                onClick = { onModeChange(AuthMode.REGISTER) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AuthModeButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 1.dp,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(vertical = 11.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    } else {
        TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Text(text)
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    supportingText: String? = null,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = true,
        visualTransformation =
            if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions =
            KeyboardActions(
                onDone = {
                    onDone?.invoke()
                },
            ),
        shape = RoundedCornerShape(16.dp),
    )
}

@Preview
@Composable
private fun LoginScreenPreview() {
    CapitularTheme {
        AuthScreen(
            state = AuthFormState(identifier = "ana"),
            onModeChange = {},
            onIdentifierChange = {},
            onEmailChange = {},
            onUsernameChange = {},
            onDisplayNameChange = {},
            onPasswordChange = {},
            onPasswordConfirmationChange = {},
            onSubmit = {},
        )
    }
}

@Preview
@Composable
private fun RegisterScreenPreview() {
    CapitularTheme {
        AuthScreen(
            state = AuthFormState(mode = AuthMode.REGISTER),
            onModeChange = {},
            onIdentifierChange = {},
            onEmailChange = {},
            onUsernameChange = {},
            onDisplayNameChange = {},
            onPasswordChange = {},
            onPasswordConfirmationChange = {},
            onSubmit = {},
        )
    }
}

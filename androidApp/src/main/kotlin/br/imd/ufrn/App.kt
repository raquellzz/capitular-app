package br.imd.ufrn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import br.imd.ufrn.auth.AuthApi
import br.imd.ufrn.auth.AuthFormState
import br.imd.ufrn.auth.AuthMode
import br.imd.ufrn.auth.AuthSession
import br.imd.ufrn.ui.AuthScreen
import br.imd.ufrn.ui.CapitularTheme
import br.imd.ufrn.ui.FeedScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun App() {
    CapitularTheme {
        val scope = rememberCoroutineScope()
        val authApi = remember { AuthApi() }
        var authForm by remember { mutableStateOf(AuthFormState()) }
        var session by remember { mutableStateOf<AuthSession?>(null) }

        DisposableEffect(authApi) {
            onDispose(authApi::close)
        }

        val submit = {
            val validationMessage = authForm.validationMessage()
            if (validationMessage != null) {
                authForm = authForm.copy(errorMessage = validationMessage)
            } else if (!authForm.isSubmitting) {
                scope.launch {
                    authForm = authForm.copy(isSubmitting = true, errorMessage = null)
                    try {
                        session =
                            when (authForm.mode) {
                                AuthMode.LOGIN ->
                                    authApi.login(
                                        identifier = authForm.identifier,
                                        password = authForm.password,
                                    )

                                AuthMode.REGISTER ->
                                    authApi.register(
                                        email = authForm.email,
                                        username = authForm.username,
                                        displayName = authForm.displayName,
                                        password = authForm.password,
                                    )
                            }
                        authForm = AuthFormState()
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        authForm =
                            authForm.copy(
                                isSubmitting = false,
                                errorMessage = error.message ?: "Não foi possível entrar.",
                            )
                    }
                }
            }
        }

        val activeSession = session
        if (activeSession == null) {
            AuthScreen(
                state = authForm,
                onModeChange = { mode ->
                    authForm = AuthFormState(mode = mode)
                },
                onIdentifierChange = { authForm = authForm.copy(identifier = it, errorMessage = null) },
                onEmailChange = { authForm = authForm.copy(email = it, errorMessage = null) },
                onUsernameChange = { authForm = authForm.copy(username = it, errorMessage = null) },
                onDisplayNameChange = {
                    authForm = authForm.copy(displayName = it, errorMessage = null)
                },
                onPasswordChange = { authForm = authForm.copy(password = it, errorMessage = null) },
                onPasswordConfirmationChange = {
                    authForm = authForm.copy(passwordConfirmation = it, errorMessage = null)
                },
                onSubmit = submit,
            )
        } else {
            FeedScreen(
                user = activeSession.user,
                onLogout = {
                    session = null
                    scope.launch {
                        authApi.logout(activeSession.tokens.refreshToken)
                    }
                },
                onNewCheckIn = {},
            )
        }
    }
}

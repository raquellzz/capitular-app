package br.imd.ufrn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import br.imd.ufrn.auth.AuthApi
import br.imd.ufrn.auth.AuthFormState
import br.imd.ufrn.auth.AuthMode
import br.imd.ufrn.auth.AuthSessionViewModel
import br.imd.ufrn.data.remoto.SessaoEmMemoria
import br.imd.ufrn.presentation.navigation.AuthDestination
import br.imd.ufrn.presentation.navigation.CapitularNavHost
import br.imd.ufrn.presentation.navigation.GrupoDestination
import br.imd.ufrn.presentation.theme.CapitularTheme
import br.imd.ufrn.presentation.model.Ranking
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.runtime.CompositionLocalProvider
import br.imd.ufrn.data.remoto.CarregadorDeFotos
import br.imd.ufrn.presentation.components.LocalCarregadorDeFotos

@Composable
fun App() {
    CapitularTheme {
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        val authApi = remember { AuthApi() }
        val authSessionViewModel = viewModel<AuthSessionViewModel>()
        val sessaoEmMemoria = koinInject<SessaoEmMemoria>()
        var authForm by remember { mutableStateOf(AuthFormState()) }
        val session = authSessionViewModel.session
        // Ranking ainda fixo: vem do backend na issue de Auth e Ranking (Pablo).
        val rankings =
            remember {
                listOf(
                    Ranking(position = 1, name = "Bia Nunes", totalPages = 340),
                    Ranking(position = 2, name = "Você", totalPages = 285),
                    Ranking(position = 3, name = "Ravi Melo", totalPages = 210),
                    Ranking(position = 4, name = "Lia Costa", totalPages = 155),
                )
            }

        DisposableEffect(authApi) {
            onDispose(authApi::close)
        }

        val submitAuth: () -> Unit = {
            val validationMessage = authForm.validationMessage()
            if (validationMessage != null) {
                authForm = authForm.copy(errorMessage = validationMessage)
            } else if (!authForm.isSubmitting) {
                scope.launch {
                    authForm = authForm.copy(isSubmitting = true, errorMessage = null)
                    try {
                        val authenticatedSession =
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
                        authSessionViewModel.authenticate(authenticatedSession)
                        sessaoEmMemoria.token = authenticatedSession.tokens.accessToken
                        authForm = AuthFormState()
                        navController.navigate(GrupoDestination) {
                            popUpTo<AuthDestination> { inclusive = true }
                            launchSingleTop = true
                        }
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

        CompositionLocalProvider(LocalCarregadorDeFotos provides koinInject<CarregadorDeFotos>()) {
        CapitularNavHost(
            navController = navController,
            authForm = authForm,
            session = session,
            rankings = rankings,
            onAuthModeChange = { mode -> authForm = AuthFormState(mode = mode) },
            onIdentifierChange = { authForm = authForm.copy(identifier = it, errorMessage = null) },
            onEmailChange = { authForm = authForm.copy(email = it, errorMessage = null) },
            onUsernameChange = { authForm = authForm.copy(username = it, errorMessage = null) },
            onDisplayNameChange = { authForm = authForm.copy(displayName = it, errorMessage = null) },
            onPasswordChange = { authForm = authForm.copy(password = it, errorMessage = null) },
            onPasswordConfirmationChange = {
                authForm = authForm.copy(passwordConfirmation = it, errorMessage = null)
            },
            onSubmitAuth = submitAuth,
            onLogout = {
                val activeSession = session
                authSessionViewModel.clearSession()
                sessaoEmMemoria.token = null
                authForm = AuthFormState()
                if (activeSession != null) {
                    scope.launch {
                        authApi.logout(activeSession.tokens.refreshToken)
                    }
                }
            },
        )
        }
    }
}

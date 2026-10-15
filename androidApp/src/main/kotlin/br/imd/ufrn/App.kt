package br.imd.ufrn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import br.imd.ufrn.auth.AuthApi
import br.imd.ufrn.auth.AuthFormState
import br.imd.ufrn.auth.AuthMode
import br.imd.ufrn.auth.AuthSession
import br.imd.ufrn.ui.AuthScreen
import br.imd.ufrn.ui.CapitularTheme
import br.imd.ufrn.ui.models.CheckIn
import br.imd.ufrn.ui.models.Ranking
import br.imd.ufrn.ui.screens.CheckInScreen
import br.imd.ufrn.ui.screens.FeedScreen
import br.imd.ufrn.ui.screens.RankingScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

sealed class MainTab {
    object Feed : MainTab()
    object Ranking : MainTab()
}

@Composable
fun App() {
    CapitularTheme {
        val scope = rememberCoroutineScope()
        val authApi = remember { AuthApi() }
        var authForm by remember { mutableStateOf(AuthFormState()) }
        var session by remember { mutableStateOf<AuthSession?>(null) }

        // App navigation & state after login
        var currentTab by remember { mutableStateOf<MainTab>(MainTab.Feed) }
        var isCheckInOpen by remember { mutableStateOf(false) }

        // Form state for CheckInScreen
        var paginaInicial by remember { mutableStateOf("") }
        var paginaFinal by remember { mutableStateOf("") }
        var checkInError by remember { mutableStateOf<String?>(null) }

        // Mock/live lists
        var checkIns by remember {
            mutableStateOf(
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
                    CheckIn(
                        id = "2",
                        friendName = "Ravi Melo",
                        bookTitle = "A vida invisível de Addie LaRue",
                        pageRange = "pág. 201 a 224",
                        timeElapsed = "há 2 h",
                        pagesRead = 24,
                        note = "Leitura antes da aula. Sequência mantida!",
                    ),
                )
            )
        }

        var rankings by remember {
            mutableStateOf(
                listOf(
                    Ranking(position = 1, name = "Bia Nunes", totalPages = 340),
                    Ranking(position = 2, name = "Você", totalPages = 285),
                    Ranking(position = 3, name = "Ravi Melo", totalPages = 210),
                    Ranking(position = 4, name = "Lia Costa", totalPages = 155),
                )
            )
        }

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
        } else if (isCheckInOpen) {
            CheckInScreen(
                paginaInicial = paginaInicial,
                paginaFinal = paginaFinal,
                onInicioChange = { paginaInicial = it; checkInError = null },
                onFimChange = { paginaFinal = it; checkInError = null },
                onSalvarCheckIn = { inicio, fim ->
                    val start = inicio.toIntOrNull()
                    val end = fim.toIntOrNull()
                    if (start == null || end == null || end <= start) {
                        checkInError = "Informe páginas válidas (fim deve ser maior que o início)."
                    } else {
                        val pages = end - start
                        val newCheckIn = CheckIn(
                            id = System.currentTimeMillis().toString(),
                            friendName = activeSession.user.displayName,
                            bookTitle = "Leitura Atual",
                            pageRange = "pág. $start a $end",
                            timeElapsed = "agora",
                            pagesRead = pages,
                            note = "Novo check-in registrado!",
                        )
                        checkIns = listOf(newCheckIn) + checkIns
                        paginaInicial = ""
                        paginaFinal = ""
                        checkInError = null
                        isCheckInOpen = false
                    }
                },
                onVoltar = { isCheckInOpen = false },
                bookTitle = "Leitura Atual",
                errorMessage = checkInError,
            )
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Text("📖", fontSize = 18.sp) },
                            label = { Text("Feed") },
                            selected = currentTab is MainTab.Feed,
                            onClick = { currentTab = MainTab.Feed },
                        )
                        NavigationBarItem(
                            icon = { Text("🏆", fontSize = 18.sp) },
                            label = { Text("Ranking") },
                            selected = currentTab is MainTab.Ranking,
                            onClick = { currentTab = MainTab.Ranking },
                        )
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    when (currentTab) {
                        MainTab.Feed -> {
                            FeedScreen(
                                userName = activeSession.user.displayName,
                                onLogout = {
                                    session = null
                                    scope.launch {
                                        authApi.logout(activeSession.tokens.refreshToken)
                                    }
                                },
                                checkIns = checkIns,
                                onNovoCheckInClick = { isCheckInOpen = true },
                            )
                        }
                        MainTab.Ranking -> {
                            RankingScreen(
                                rankings = rankings,
                            )
                        }
                    }
                }
            }
        }
    }
}

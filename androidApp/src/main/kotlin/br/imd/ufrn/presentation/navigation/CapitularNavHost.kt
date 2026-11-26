package br.imd.ufrn.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import br.imd.ufrn.auth.AuthFormState
import br.imd.ufrn.auth.AuthMode
import br.imd.ufrn.auth.AuthSession
import br.imd.ufrn.presentation.screens.AuthScreen
import br.imd.ufrn.presentation.model.CheckIn
import br.imd.ufrn.presentation.model.Ranking
import br.imd.ufrn.presentation.screens.CheckInScreen
import br.imd.ufrn.presentation.screens.FeedScreen
import br.imd.ufrn.presentation.screens.RankingScreen
import br.imd.ufrn.presentation.grupo.GrupoRota
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CapitularNavHost(
    navController: NavHostController,
    authForm: AuthFormState,
    session: AuthSession?,
    checkIns: List<CheckIn>,
    rankings: List<Ranking>,
    paginaInicial: String,
    paginaFinal: String,
    checkInError: String?,
    onAuthModeChange: (AuthMode) -> Unit,
    onIdentifierChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onSubmitAuth: () -> Unit,
    onLogout: () -> Unit,
    onPaginaInicialChange: (String) -> Unit,
    onPaginaFinalChange: (String) -> Unit,
    onSalvarCheckIn: (inicio: String, fim: String) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = AuthDestination,
    ) {
        composable<AuthDestination> {
            AuthScreen(
                state = authForm,
                onModeChange = onAuthModeChange,
                onIdentifierChange = onIdentifierChange,
                onEmailChange = onEmailChange,
                onUsernameChange = onUsernameChange,
                onDisplayNameChange = onDisplayNameChange,
                onPasswordChange = onPasswordChange,
                onPasswordConfirmationChange = onPasswordConfirmationChange,
                onSubmit = onSubmitAuth,
            )
        }

        composable<GrupoDestination> {
            GrupoRota(
                viewModel = koinViewModel(),
                onGrupoPronto = { grupoId ->
                    navController.navigate(FeedDestination(grupoId)) {
                        popUpTo<GrupoDestination> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSair = {
                    onLogout()
                    navController.navigate(AuthDestination) {
                        popUpTo<GrupoDestination> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<FeedDestination> { entrada ->
            val feed = entrada.toRoute<FeedDestination>()
            AdaptiveMainScaffold(
                selectedDestination = MainDestination.FEED,
                onFeedClick = {},
                onRankingClick = {
                    navController.navigate(RankingDestination(feed.grupoId)) {
                        launchSingleTop = true
                    }
                },
            ) { contentModifier ->
                FeedScreen(
                    userName = session?.user?.displayName ?: "Leitor",
                    onLogout = {
                        onLogout()
                        navController.navigate(AuthDestination) {
                            popUpTo<FeedDestination> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    checkIns = checkIns,
                    onNovoCheckInClick = { navController.navigate(CheckInDestination) },
                    modifier = contentModifier,
                )
            }
        }

        composable<RankingDestination> { entrada ->
            val ranking = entrada.toRoute<RankingDestination>()
            AdaptiveMainScaffold(
                selectedDestination = MainDestination.RANKING,
                onFeedClick = {
                    if (!navController.popBackStack<FeedDestination>(inclusive = false)) {
                        navController.navigate(FeedDestination(ranking.grupoId)) {
                            launchSingleTop = true
                        }
                    }
                },
                onRankingClick = {},
            ) { contentModifier ->
                RankingScreen(
                    rankings = rankings,
                    modifier = contentModifier,
                )
            }
        }

        composable<CheckInDestination>(
            deepLinks =
                listOf(
                    navDeepLink<CheckInDestination>(basePath = CHECK_IN_DEEP_LINK),
                ),
        ) {
            CheckInScreen(
                paginaInicial = paginaInicial,
                paginaFinal = paginaFinal,
                onInicioChange = onPaginaInicialChange,
                onFimChange = onPaginaFinalChange,
                onSalvarCheckIn = onSalvarCheckIn,
                onVoltar = { navController.popBackStack() },
                bookTitle = "Leitura Atual",
                errorMessage = checkInError,
            )
        }
    }
}

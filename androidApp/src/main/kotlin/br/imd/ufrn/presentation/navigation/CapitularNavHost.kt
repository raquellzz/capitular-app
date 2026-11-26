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
import br.imd.ufrn.presentation.grupo.MeusGruposRota
import org.koin.core.parameter.parametersOf
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
    // Abas (Feed, Ranking, Grupos): a pilha fica sempre Feed -> aba, sem crescer.
    fun abrirAba(destino: Any) =
        navController.navigate(destino) {
            popUpTo<FeedDestination>()
            launchSingleTop = true
        }

    fun voltarAoFeed(grupoId: String) {
        if (!navController.popBackStack<FeedDestination>(inclusive = false)) {
            navController.navigate(FeedDestination(grupoId)) { launchSingleTop = true }
        }
    }

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
                onRankingClick = { abrirAba(RankingDestination(feed.grupoId)) },
                onGruposClick = { abrirAba(GruposDestination(feed.grupoId)) },
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
                onFeedClick = { voltarAoFeed(ranking.grupoId) },
                onRankingClick = {},
                onGruposClick = { abrirAba(GruposDestination(ranking.grupoId)) },
            ) { contentModifier ->
                RankingScreen(
                    rankings = rankings,
                    modifier = contentModifier,
                )
            }
        }

        composable<GruposDestination> { entrada ->
            val grupos = entrada.toRoute<GruposDestination>()
            AdaptiveMainScaffold(
                selectedDestination = MainDestination.GRUPOS,
                onFeedClick = { voltarAoFeed(grupos.grupoId) },
                onRankingClick = { abrirAba(RankingDestination(grupos.grupoId)) },
                onGruposClick = {},
            ) { contentModifier ->
                MeusGruposRota(
                    viewModel = koinViewModel { parametersOf(grupos.grupoId) },
                    onAbrirFeed = { grupoId ->
                        navController.navigate(FeedDestination(grupoId)) {
                            popUpTo<FeedDestination> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onEscolherGrupo = {
                        navController.navigate(GrupoDestination) {
                            popUpTo<FeedDestination> { inclusive = true }
                        }
                    },
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

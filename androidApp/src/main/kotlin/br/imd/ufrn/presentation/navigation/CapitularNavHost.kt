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
import br.imd.ufrn.presentation.model.Ranking
import br.imd.ufrn.presentation.screens.CheckInRota
import br.imd.ufrn.presentation.screens.FeedRota
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
    rankings: List<Ranking>,
    onAuthModeChange: (AuthMode) -> Unit,
    onIdentifierChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onSubmitAuth: () -> Unit,
    onLogout: () -> Unit,
) {
    // Abas (Feed, Ranking, Grupos): a pilha fica sempre Feed -> aba, sem crescer.
    fun abrirAba(destino: Any) =
        navController.navigate(destino) {
            popUpTo<FeedDestination>()
            launchSingleTop = true
        }

    // Sem o feed na pilha (não deveria acontecer), refaz a escolha do grupo.
    fun voltarAoFeed() {
        if (!navController.popBackStack<FeedDestination>(inclusive = false)) {
            navController.navigate(GrupoDestination) { launchSingleTop = true }
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
                onGrupoPronto = { grupo ->
                    navController.navigate(FeedDestination(grupo.id, grupo.nome)) {
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
                FeedRota(
                    viewModel = koinViewModel { parametersOf(feed.grupoId) },
                    userName = session?.user?.displayName ?: "Leitor",
                    nomeDoGrupo = feed.grupoNome,
                    onAbrirGrupos = { abrirAba(GruposDestination(feed.grupoId)) },
                    onLogout = {
                        onLogout()
                        navController.navigate(AuthDestination) {
                            popUpTo<FeedDestination> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNovoCheckInClick = { navController.navigate(CheckInDestination(feed.grupoId)) },
                    modifier = contentModifier,
                )
            }
        }

        composable<RankingDestination> { entrada ->
            val ranking = entrada.toRoute<RankingDestination>()
            AdaptiveMainScaffold(
                selectedDestination = MainDestination.RANKING,
                onFeedClick = { voltarAoFeed() },
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
                onFeedClick = { voltarAoFeed() },
                onRankingClick = { abrirAba(RankingDestination(grupos.grupoId)) },
                onGruposClick = {},
            ) { contentModifier ->
                MeusGruposRota(
                    viewModel = koinViewModel { parametersOf(grupos.grupoId) },
                    onAbrirFeed = { grupo ->
                        navController.navigate(FeedDestination(grupo.id, grupo.nome)) {
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
        ) { entrada ->
            val checkIn = entrada.toRoute<CheckInDestination>()
            CheckInRota(
                // Pelo deep link o grupo não vem; o ViewModel usa o último grupo aberto.
                viewModel = koinViewModel { parametersOf(checkIn.grupoId) },
                onVoltar = { navController.popBackStack() },
                // Ao voltar, o feed recarrega sozinho e mostra a leitura nova.
                onSalvo = { navController.popBackStack() },
            )
        }
    }
}

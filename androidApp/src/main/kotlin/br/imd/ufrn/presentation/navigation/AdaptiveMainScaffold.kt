package br.imd.ufrn.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

enum class MainDestination {
    FEED,
    RANKING,
}

@Composable
fun AdaptiveMainScaffold(
    selectedDestination: MainDestination,
    onFeedClick: () -> Unit,
    onRankingClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val useNavigationRail =
        windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

    if (useNavigationRail) {
        Row(
            modifier =
                modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            NavigationRail(modifier = Modifier.testTag("wide-navigation")) {
                MainNavigationRailItem(
                    icon = "📖",
                    label = "Feed",
                    selected = selectedDestination == MainDestination.FEED,
                    onClick = onFeedClick,
                )
                MainNavigationRailItem(
                    icon = "🏆",
                    label = "Ranking",
                    selected = selectedDestination == MainDestination.RANKING,
                    onClick = onRankingClick,
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                content(Modifier.fillMaxSize())
            }
        }
    } else {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                NavigationBar(modifier = Modifier.testTag("compact-navigation")) {
                    MainNavigationBarItem(
                        icon = "📖",
                        label = "Feed",
                        selected = selectedDestination == MainDestination.FEED,
                        onClick = onFeedClick,
                    )
                    MainNavigationBarItem(
                        icon = "🏆",
                        label = "Ranking",
                        selected = selectedDestination == MainDestination.RANKING,
                        onClick = onRankingClick,
                    )
                }
            },
        ) { paddingValues ->
            content(Modifier.fillMaxSize().padding(paddingValues))
        }
    }
}

@Composable
private fun RowScope.MainNavigationBarItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        icon = { Text(icon, fontSize = 18.sp) },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
    )
}

@Composable
private fun MainNavigationRailItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationRailItem(
        icon = { Text(icon, fontSize = 20.sp) },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
    )
}

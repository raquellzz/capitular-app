package br.imd.ufrn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.imd.ufrn.ui.CapitularTheme
import br.imd.ufrn.ui.components.RankingItem
import br.imd.ufrn.ui.models.Ranking

@Composable
fun RankingScreen(
    rankings: List<Ranking>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        if (rankings.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Nenhum ranking disponível",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "As pontuações aparecerão aqui assim que os membros começarem a registrar leituras.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp).fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "PLACAR DA SEMANA",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        )
                        Text(
                            text = "Ranking de leitores",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                items(rankings) { ranking ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp,
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            RankingItem(
                                position = "${ranking.position}º",
                                name = ranking.name,
                                scorePages = "${ranking.totalPages} pág.",
                                avatarUrl = ranking.avatarUrl,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingScreenEmptyPreview() {
    CapitularTheme {
        RankingScreen(rankings = emptyList())
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingScreenFilledPreview() {
    val sampleRankings =
        listOf(
            Ranking(position = 1, name = "Bia Nunes", totalPages = 340),
            Ranking(position = 2, name = "Ana Lima", totalPages = 285),
            Ranking(position = 3, name = "Ravi Melo", totalPages = 210),
            Ranking(position = 4, name = "Lia Costa", totalPages = 155),
        )

    CapitularTheme {
        RankingScreen(rankings = sampleRankings)
    }
}

package br.imd.ufrn.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.imd.ufrn.R
import br.imd.ufrn.ui.CapitularTheme
import br.imd.ufrn.ui.components.CheckInCard
import br.imd.ufrn.ui.components.RankingItem
import br.imd.ufrn.ui.models.CheckIn

@Composable
fun FeedScreen(
    userName: String,
    onLogout: () -> Unit,
    checkIns: List<CheckIn>,
    onNovoCheckInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp).fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                FeedHeader(userName = userName, onLogout = onLogout)
            }
            item {
                WeeklySummary()
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Text(
                            text = "ATIVIDADE DO GRUPO",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        )
                        Text(
                            text = "Leituras recentes",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                    Text(
                        text = "${checkIns.size + 10} membros",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (checkIns.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Nenhum check-in recente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Seja o primeiro do grupo a registrar sua leitura hoje!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(checkIns) { checkIn ->
                    CheckInCard(
                        friendName = checkIn.friendName,
                        bookTitle = checkIn.bookTitle,
                        pageRange = checkIn.pageRange,
                        timeElapsed = checkIn.timeElapsed,
                        photoUrl = checkIn.photoUrl,
                        avatarUrl = checkIn.avatarUrl,
                        pagesRead = checkIn.pagesRead,
                        note = checkIn.note,
                    )
                }
            }
        }

        Button(
            onClick = onNovoCheckInClick,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
                    .height(54.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("＋  Registrar leitura", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FeedHeader(
    userName: String,
    onLogout: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
            ) {
                Image(
                    painter = painterResource(R.drawable.capitular_logo),
                    contentDescription = "Logo do Capitular",
                    modifier = Modifier.fillMaxSize().padding(3.dp),
                )
            }
            Column {
                Text(
                    text = "Olá, ${userName.substringBefore(' ')}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = "Qual página marcou seu dia?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        TextButton(
            onClick = onLogout,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        ) {
            Text("Sair")
        }
    }
}

@Composable
private fun WeeklySummary() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "SUA SEQUÊNCIA",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.64f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                    )
                    Text(
                        text = "7 dias lendo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                ) {
                    Text(
                        text = "7",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                RankingItem(position = "1º", name = "Bia", scorePages = "187 pág.")
                RankingItem(position = "2º", name = "Você", scorePages = "143 pág.")
                RankingItem(position = "3º", name = "Ravi", scorePages = "121 pág.")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedScreenFilledPreview() {
    val sampleCheckIns =
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

    CapitularTheme {
        FeedScreen(
            userName = "Ana Lima",
            onLogout = {},
            checkIns = sampleCheckIns,
            onNovoCheckInClick = {},
        )
    }
}

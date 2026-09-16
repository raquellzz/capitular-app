package br.imd.ufrn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.imd.ufrn.R
import br.imd.ufrn.auth.CapitularUser

data class FeedCheckIn(
    val username: String,
    val initials: String,
    val bookTitle: String,
    val pageRange: String,
    val pagesRead: Int,
    val timeLabel: String,
    val note: String,
    val photoColor: Color,
)

private val sampleCheckIns =
    listOf(
        FeedCheckIn(
            username = "Bia Nunes",
            initials = "BN",
            bookTitle = "Torto Arado",
            pageRange = "páginas 84–112",
            pagesRead = 29,
            timeLabel = "há 18 min",
            note = "A história da Belonísia não sai da cabeça.",
            photoColor = CapitularSuccess,
        ),
        FeedCheckIn(
            username = "Ravi Melo",
            initials = "RM",
            bookTitle = "A vida invisível de Addie LaRue",
            pageRange = "páginas 201–224",
            pagesRead = 24,
            timeLabel = "há 2 h",
            note = "Leitura antes da aula. Sequência mantida!",
            photoColor = CapitularViolet,
        ),
        FeedCheckIn(
            username = "Lia Costa",
            initials = "LC",
            bookTitle = "O avesso da pele",
            pageRange = "páginas 35–51",
            pagesRead = 17,
            timeLabel = "ontem",
            note = "Poucas páginas, muita coisa para pensar.",
            photoColor = CapitularCoral,
        ),
    )

@Composable
fun FeedScreen(
    user: CapitularUser,
    onLogout: () -> Unit,
    onNewCheckIn: () -> Unit,
    modifier: Modifier = Modifier,
    checkIns: List<FeedCheckIn> = sampleCheckIns,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        LazyColumn(
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 720.dp).fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                FeedHeader(user = user, onLogout = onLogout)
            }
            item {
                WeeklySummary()
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        )
                    }
                    Text(
                        text = "12 membros",
                        color = CapitularSuccess,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            items(checkIns) { checkIn ->
                CheckInCard(checkIn)
            }
        }

        Button(
            onClick = onNewCheckIn,
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
    user: CapitularUser,
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
                modifier = Modifier.size(54.dp),
                color = CapitularLavender,
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
                    text = "Olá, ${user.displayName.substringBefore(' ')}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Qual página marcou seu dia?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextButton(onClick = onLogout) {
            Text("Sair")
        }
    }
}

@Composable
private fun WeeklySummary() {
    Surface(
        color = CapitularDeepPurple,
        contentColor = Color.White,
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
                        color = Color.White.copy(alpha = 0.64f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                    )
                    Text(
                        text = "7 dias lendo",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Surface(
                    color = CapitularWarmYellow,
                    shape = CircleShape,
                ) {
                    Text(
                        text = "7",
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                        color = CapitularDeepPurple,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.White.copy(alpha = 0.16f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                RankingItem(position = "1", name = "Bia", pages = "187 pág.")
                RankingItem(position = "2", name = "Você", pages = "143 pág.")
                RankingItem(position = "3", name = "Ravi", pages = "121 pág.")
            }
        }
    }
}

@Composable
private fun RankingItem(
    position: String,
    name: String,
    pages: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = position,
            color = CapitularWarmYellow,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(pages, color = Color.White.copy(alpha = 0.58f), fontSize = 11.sp)
        }
    }
}

@Composable
fun CheckInCard(
    checkIn: FeedCheckIn,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    color = checkIn.photoColor.copy(alpha = 0.16f),
                    contentColor = checkIn.photoColor,
                    shape = CircleShape,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = checkIn.initials,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(checkIn.username, fontWeight = FontWeight.Bold)
                    Text(
                        text = checkIn.timeLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "+${checkIn.pagesRead} pág.",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                    )
                }
            }
            ReadingPhotoPlaceholder(
                bookTitle = checkIn.bookTitle,
                color = checkIn.photoColor,
            )
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = checkIn.bookTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = checkIn.pageRange,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = "“${checkIn.note}”",
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}

@Composable
private fun ReadingPhotoPlaceholder(
    bookTitle: String,
    color: Color,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.8f)
                .background(color.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 210.dp).fillMaxWidth(0.55f).rotate(-4f),
            color = CapitularSurface,
            contentColor = CapitularTextPrimary,
            shape = RoundedCornerShape(4.dp),
            shadowElevation = 10.dp,
        ) {
            Column(
                modifier = Modifier.aspectRatio(0.72f).padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "CAPITULAR",
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                )
                Text(
                    text = bookTitle,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "LEITURA DO DIA",
                    color = CapitularTextPrimary.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Preview
@Composable
private fun FeedPreview() {
    CapitularTheme {
        FeedScreen(
            user =
                CapitularUser(
                    id = "preview",
                    username = "ana",
                    displayName = "Ana Lima",
                    timezone = "America/Fortaleza",
                    createdAt = "",
                ),
            onLogout = {},
            onNewCheckIn = {},
        )
    }
}

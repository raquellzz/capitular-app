package br.imd.ufrn.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun RankingItem(
    position: String,
    name: String,
    scorePages: String,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
) {
    val initials = name.split(" ").let { parts ->
        if (parts.size >= 2) "${parts.first().take(1)}${parts.last().take(1)}"
        else name.take(2)
    }.uppercase()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = position,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
        )

        Surface(
            modifier = Modifier.size(36.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }

        Column {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = scorePages,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingItemPreview() {
    CapitularTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RankingItem(
                position = "1º",
                name = "Ana Lima",
                scorePages = "340 pág.",
            )
        }
    }
}

package br.imd.ufrn.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import br.imd.ufrn.data.remoto.CarregadorDeFotos

/**
 * Carregador das fotos remotas. O app fornece o real (com token) em `App`;
 * em testes e previews fica `null` e os cartões mostram o ilustrativo.
 */
val LocalCarregadorDeFotos = staticCompositionLocalOf<CarregadorDeFotos?> { null }

private sealed interface EstadoDaFoto {
    data object Carregando : EstadoDaFoto

    data class Pronta(
        val imagem: ImageBitmap,
    ) : EstadoDaFoto

    data object Falhou : EstadoDaFoto
}

/**
 * Foto vinda da API. Enquanto baixa, mostra um indicador; se não houver
 * carregador ou o download falhar, mostra [semFoto].
 */
@Composable
fun FotoRemota(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    semFoto: @Composable () -> Unit,
) {
    val carregador = LocalCarregadorDeFotos.current
    if (carregador == null) {
        semFoto()
        return
    }
    val estado by produceState<EstadoDaFoto>(EstadoDaFoto.Carregando, url) {
        value = carregador.carregar(url)?.let { EstadoDaFoto.Pronta(it.asImageBitmap()) } ?: EstadoDaFoto.Falhou
    }
    when (val atual = estado) {
        EstadoDaFoto.Carregando ->
            Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        is EstadoDaFoto.Pronta ->
            Image(
                bitmap = atual.imagem,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        EstadoDaFoto.Falhou -> semFoto()
    }
}

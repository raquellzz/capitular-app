package br.imd.ufrn.presentation.checkin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import br.imd.ufrn.domain.feed.FotoDaLeitura
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val LADO_MAXIMO = 2048
private const val QUALIDADE_JPEG = 85

/**
 * Lê a foto escolhida no seletor e a prepara para envio: reduz para no máximo
 * 2048 px no maior lado e converte para JPEG. Assim fotos HEIC do celular
 * viram um formato que o backend aceita, e o upload fica bem abaixo do limite
 * de 10 MB. Devolve `null` se a imagem não puder ser lida.
 */
suspend fun lerFotoParaEnvio(
    context: Context,
    uri: Uri,
): FotoDaLeitura? =
    withContext(Dispatchers.IO) {
        try {
            val bitmap = decodificarReduzido(context, uri) ?: return@withContext null
            val saida = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, saida)
            FotoDaLeitura(bytes = saida.toByteArray(), tipo = "image/jpeg")
        } catch (_: Exception) {
            null
        }
    }

private fun decodificarReduzido(
    context: Context,
    uri: Uri,
): Bitmap? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val maior = maxOf(info.size.width, info.size.height)
            if (maior > LADO_MAXIMO) {
                val escala = LADO_MAXIMO.toFloat() / maior
                decoder.setTargetSize((info.size.width * escala).toInt(), (info.size.height * escala).toInt())
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        var amostra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / amostra > LADO_MAXIMO) amostra *= 2
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = amostra })
        }
    }

package br.imd.ufrn.data.remoto

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Baixa as fotos dos check-ins pelo cliente HTTP do app, que já envia o token
 * (a API não serve as fotos sem autenticação), e guarda as últimas em memória
 * para a lista não baixar de novo ao rolar.
 */
class CarregadorDeFotos(
    private val cliente: HttpClient,
) {
    // ~1/8 da memória do app, medido em KB.
    private val cache =
        object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()) {
            override fun sizeOf(
                key: String,
                value: Bitmap,
            ) = value.byteCount / 1024
        }

    /** Devolve a foto reduzida para no máximo [larguraMaxima] px, ou `null` se não deu para baixar. */
    suspend fun carregar(
        url: String,
        larguraMaxima: Int = 1080,
    ): Bitmap? {
        cache.get(url)?.let { return it }
        return try {
            val bytes = cliente.get(url).body<ByteArray>()
            withContext(Dispatchers.Default) { decodificar(bytes, larguraMaxima) }
                ?.also { cache.put(url, it) }
        } catch (erro: kotlinx.coroutines.CancellationException) {
            throw erro
        } catch (_: Exception) {
            null
        }
    }

    private fun decodificar(
        bytes: ByteArray,
        larguraMaxima: Int,
    ): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
        var amostra = 1
        while (limites.outWidth / (amostra * 2) >= larguraMaxima) amostra *= 2
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = amostra })
    }
}

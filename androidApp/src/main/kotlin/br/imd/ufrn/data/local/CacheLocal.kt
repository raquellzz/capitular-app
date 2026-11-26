package br.imd.ufrn.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Instant

/** Um valor guardado e o momento em que ele chegou do servidor. */
data class Guardado<T>(
    val valor: T,
    val guardadoEm: Instant,
)

@Serializable
private data class Envelope(
    val guardadoEmMillis: Long,
    val json: String,
)

/**
 * Cópia local das respostas do backend, no DataStore, em JSON.
 *
 * Todas as chaves desta frente começam com [PREFIXO] (`feed.`), o que permite
 * apagar tudo de uma vez ao sair da conta ([limparTudo]): assim a pessoa que
 * entrar depois no mesmo aparelho não vê os grupos e leituras da anterior.
 */
class CacheLocal(
    private val dataStore: DataStore<Preferences>,
    private val agora: () -> Instant = Instant::now,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun <T> salvar(
        chave: String,
        valor: T,
        serializer: KSerializer<T>,
    ) {
        val envelope = Envelope(agora().toEpochMilli(), json.encodeToString(serializer, valor))
        dataStore.edit { it[stringPreferencesKey(PREFIXO + chave)] = json.encodeToString(Envelope.serializer(), envelope) }
    }

    /** O valor guardado, ou `null` se não há nada (ou se o formato mudou numa versão nova do app). */
    suspend fun <T> ler(
        chave: String,
        serializer: KSerializer<T>,
    ): Guardado<T>? {
        val texto = dataStore.data.first()[stringPreferencesKey(PREFIXO + chave)] ?: return null
        return try {
            val envelope = json.decodeFromString(Envelope.serializer(), texto)
            Guardado(json.decodeFromString(serializer, envelope.json), Instant.ofEpochMilli(envelope.guardadoEmMillis))
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    suspend fun remover(chave: String) {
        dataStore.edit { it.remove(stringPreferencesKey(PREFIXO + chave)) }
    }

    /** Apaga tudo o que a frente de feed e grupos guardou (inclui o último grupo aberto). */
    suspend fun limparTudo() {
        dataStore.edit { preferencias ->
            preferencias.asMap().keys
                .filter { it.name.startsWith(PREFIXO) }
                .forEach { preferencias.remove(it) }
        }
    }

    companion object {
        const val PREFIXO = "feed."
    }
}

package br.imd.ufrn.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import br.imd.ufrn.domain.grupo.PreferenciaDeGrupo
import kotlinx.coroutines.flow.first

/** Guarda no DataStore o id do último grupo aberto (chave `feed.grupoAtual`). */
class PreferenciaDeGrupoLocal(
    private val dataStore: DataStore<Preferences>,
) : PreferenciaDeGrupo {
    private val chave = stringPreferencesKey("feed.grupoAtual")

    override suspend fun grupoAtualId(): String? = dataStore.data.first()[chave]

    override suspend fun salvarGrupoAtual(grupoId: String?) {
        dataStore.edit { preferencias ->
            if (grupoId == null) preferencias.remove(chave) else preferencias[chave] = grupoId
        }
    }
}

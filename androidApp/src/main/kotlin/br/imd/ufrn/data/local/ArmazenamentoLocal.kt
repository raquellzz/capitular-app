package br.imd.ufrn.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

/**
 * Um único DataStore de preferências para o app inteiro (só pode haver uma
 * instância por arquivo; o Koin garante isso com `single`). A pasta vem da
 * plataforma (`filesDir` do Android).
 *
 * Cada frente usa as suas próprias chaves, com prefixo, para não colidir:
 * `feed.*` (Feed e check-in), `sessao.*` e `ranking.*` (Auth e Ranking).
 * Listas e objetos são guardados como JSON em `stringPreferencesKey`.
 */
fun criarDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("capitular") },
    )

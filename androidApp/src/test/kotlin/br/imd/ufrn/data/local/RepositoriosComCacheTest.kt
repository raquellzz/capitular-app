package br.imd.ufrn.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.presentation.feed.EstatisticasRepositoryFalso
import br.imd.ufrn.presentation.feed.FeedRepositoryFalso
import br.imd.ufrn.presentation.feed.checkInDeExemplo
import br.imd.ufrn.presentation.grupo.GrupoRepositoryFalso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Usa um DataStore real num arquivo temporário: testa a gravação de verdade, não um dublê. */
class RepositoriosComCacheTest {
    private val pasta: File = Files.createTempDirectory("capitular-cache").toFile()
    private val escopoDoDataStore = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dataStore =
        PreferenceDataStoreFactory.create(scope = escopoDoDataStore) { File(pasta, "teste.preferences_pb") }
    private val momento = Instant.parse("2026-11-26T12:00:00Z")
    private val cache = CacheLocal(dataStore, agora = { momento })

    private val feedRemoto = FeedRepositoryFalso()
    private val feed = FeedRepositoryComCache(feedRemoto, cache)

    @AfterTest
    fun limpar() {
        escopoDoDataStore.cancel()
        pasta.deleteRecursively()
    }

    @Test
    fun comConexaoGuardaEDevolveOsDadosFrescos() =
        runTest {
            feedRemoto.checkIns = listOf(checkInDeExemplo())

            val pagina = feed.feed("g1")

            assertFalse(pagina.desatualizada)
            assertEquals(listOf(checkInDeExemplo()), pagina.checkIns)
            assertEquals(listOf(checkInDeExemplo()), cache.ler("checkins.g1", formatoDoFeed())?.valor?.map { it.paraDominio() })
        }

    @Test
    fun semConexaoDevolveOUltimoFeedGuardadoMarcadoComoDesatualizado() =
        runTest {
            feedRemoto.checkIns = listOf(checkInDeExemplo())
            feed.feed("g1")

            feedRemoto.falha = FalhaNoRepositorio.SemConexao()
            val pagina = feed.feed("g1")

            assertTrue(pagina.desatualizada)
            assertEquals(momento, pagina.guardadaEm)
            assertEquals(listOf(checkInDeExemplo()), pagina.checkIns)
        }

    @Test
    fun semConexaoESemNadaGuardadoAFalhaContinua() =
        runTest {
            feedRemoto.falha = FalhaNoRepositorio.SemConexao()

            assertFailsWith<FalhaNoRepositorio.SemConexao> { feed.feed("g1") }
        }

    @Test
    fun erroDoServidorNaoUsaOCache() =
        runTest {
            feedRemoto.checkIns = listOf(checkInDeExemplo())
            feed.feed("g1")

            feedRemoto.falha = FalhaNoRepositorio.RespostaInvalida(500)

            assertFailsWith<FalhaNoRepositorio.RespostaInvalida> { feed.feed("g1") }
        }

    @Test
    fun cadaGrupoTemSuaCopia() =
        runTest {
            feedRemoto.checkIns = listOf(checkInDeExemplo())
            feed.feed("g1")

            feedRemoto.falha = FalhaNoRepositorio.SemConexao()

            assertFailsWith<FalhaNoRepositorio.SemConexao> { feed.feed("g2") }
        }

    @Test
    fun gruposEEstatisticasTambemAbremSemRede() =
        runTest {
            val gruposRemoto = GrupoRepositoryFalso().apply { grupos = listOf(Grupo("g1", "Clube", null, "ABCD2345", 3)) }
            val grupos = GrupoRepositoryComCache(gruposRemoto, cache)
            val numerosRemoto = EstatisticasRepositoryFalso()
            val numeros = EstatisticasRepositoryComCache(numerosRemoto, cache)
            grupos.meusGrupos()
            numeros.minhas()

            gruposRemoto.falha = FalhaNoRepositorio.SemConexao()
            numerosRemoto.falha = FalhaNoRepositorio.SemConexao()

            assertEquals("Clube", grupos.meusGrupos().single().nome)
            assertEquals(7, numeros.minhas().sequenciaAtual)
        }

    @Test
    fun sairDaContaApagaTudoQueFoiGuardado() =
        runTest {
            feedRemoto.checkIns = listOf(checkInDeExemplo())
            feed.feed("g1")
            PreferenciaDeGrupoLocal(dataStore).salvarGrupoAtual("g1")

            cache.limparTudo()

            assertNull(cache.ler("checkins.g1", formatoDoFeed()))
            assertNull(PreferenciaDeGrupoLocal(dataStore).grupoAtualId())
        }

    private fun formatoDoFeed() = kotlinx.serialization.builtins.ListSerializer(CheckInGuardado.serializer())
}

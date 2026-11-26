package br.imd.ufrn.presentation.feed

import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

class CartaoDoFeedTest {
    private val agora = Instant.parse("2026-11-26T15:00:00Z")
    private val fuso = ZoneId.of("America/Fortaleza")

    private fun antes(segundos: Long) = tempoRelativo(agora.minusSeconds(segundos), agora, fuso)

    @Test
    fun tempoRelativoEmPortugues() {
        assertEquals("agora", antes(30))
        assertEquals("há 18 min", antes(18 * 60))
        assertEquals("há 2 h", antes(2 * 3600))
        assertEquals("ontem", antes(30 * 3600))
        assertEquals("há 3 dias", antes(3 * 86_400))
        assertEquals("12 de nov.", tempoRelativo(Instant.parse("2026-11-12T15:00:00Z"), agora, fuso))
    }

    @Test
    fun cartaoUsaAsPaginasDoServidor() {
        val cartao = checkInDeExemplo().paraCartao(agora)

        assertEquals("Bia Nunes", cartao.friendName)
        assertEquals("pág. 84 a 112", cartao.pageRange)
        assertEquals(29, cartao.pagesRead)
        assertEquals("http://servidor/api/v1/photos/p1/content", cartao.photoUrl)
        assertEquals("há 3 h", cartao.timeElapsed)
    }
}

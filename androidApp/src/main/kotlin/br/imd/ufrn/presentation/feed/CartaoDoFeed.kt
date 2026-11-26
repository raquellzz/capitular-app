package br.imd.ufrn.presentation.feed

import br.imd.ufrn.domain.feed.CheckIn
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import br.imd.ufrn.presentation.model.CheckIn as CartaoDeCheckIn

private val formatoDeData = DateTimeFormatter.ofPattern("d 'de' MMM", Locale.forLanguageTag("pt-BR"))

/** "agora", "há 18 min", "há 2 h", "ontem", "há 3 dias" ou a data ("12 de nov."). */
fun tempoRelativo(
    instante: Instant,
    agora: Instant,
    fuso: ZoneId = ZoneId.systemDefault(),
): String {
    val decorrido = Duration.between(instante, agora)
    return when {
        decorrido.toMinutes() < 1 -> "agora"
        decorrido.toMinutes() < 60 -> "há ${decorrido.toMinutes()} min"
        decorrido.toHours() < 24 -> "há ${decorrido.toHours()} h"
        decorrido.toDays() < 2 -> "ontem"
        decorrido.toDays() < 7 -> "há ${decorrido.toDays()} dias"
        else -> formatoDeData.format(instante.atZone(fuso))
    }
}

/** Converte o check-in do domínio no que o cartão do feed mostra. */
fun CheckIn.paraCartao(agora: Instant): CartaoDeCheckIn =
    CartaoDeCheckIn(
        id = id,
        friendName = leitor.nome,
        bookTitle = livro,
        pageRange = if (paginaInicial == paginaFinal) "pág. $paginaInicial" else "pág. $paginaInicial a $paginaFinal",
        timeElapsed = tempoRelativo(lidoEm, agora),
        photoUrl = fotoUrl,
        pagesRead = paginasLidas,
        note = nota,
    )

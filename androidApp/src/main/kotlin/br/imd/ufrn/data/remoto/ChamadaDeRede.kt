package br.imd.ufrn.data.remoto

import br.imd.ufrn.domain.FalhaNoRepositorio
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.time.format.DateTimeParseException
import kotlinx.serialization.SerializationException

/**
 * Executa uma chamada ao backend e traduz as exceções do Ktor em [FalhaNoRepositorio].
 * Todo repositório remoto deve envolver suas requisições nela:
 *
 * ```
 * override suspend fun feed(grupoId: String) = chamadaDeRede {
 *     cliente.get("groups/$grupoId/feed").body<FeedPageDto>().paraDominio()
 * }
 * ```
 */
suspend fun <T> chamadaDeRede(bloco: suspend () -> T): T =
    try {
        bloco()
    } catch (erro: CancellationException) {
        throw erro
    } catch (erro: FalhaNoRepositorio) {
        throw erro
    } catch (erro: ResponseException) {
        // expectSuccess = true: status 3xx, 4xx e 5xx chegam aqui.
        val status = erro.response.status
        throw if (status == HttpStatusCode.Unauthorized) {
            FalhaNoRepositorio.NaoAutenticado(erro)
        } else {
            FalhaNoRepositorio.RespostaInvalida(status.value, erro)
        }
    } catch (erro: ContentConvertException) {
        throw FalhaNoRepositorio.RespostaInvalida(causa = erro)
    } catch (erro: NoTransformationFoundException) {
        throw FalhaNoRepositorio.RespostaInvalida(causa = erro)
    } catch (erro: SerializationException) {
        throw FalhaNoRepositorio.RespostaInvalida(causa = erro)
    } catch (erro: DateTimeParseException) {
        // Data fora do formato ISO-8601 na resposta.
        throw FalhaNoRepositorio.RespostaInvalida(causa = erro)
    } catch (erro: IOException) {
        // Inclui sem rede, host desconhecido, conexão recusada e tempo esgotado.
        throw FalhaNoRepositorio.SemConexao(erro)
    }

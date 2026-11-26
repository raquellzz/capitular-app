package br.imd.ufrn.data.local

import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.feed.CheckIn
import br.imd.ufrn.domain.feed.Estatisticas
import br.imd.ufrn.domain.feed.EstatisticasRepository
import br.imd.ufrn.domain.feed.FeedRepository
import br.imd.ufrn.domain.feed.NovoCheckIn
import br.imd.ufrn.domain.feed.PaginaDoFeed
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.domain.grupo.Membro
import kotlinx.serialization.builtins.ListSerializer

/*
 * Decorators de cache: cada um envolve o repositório remoto com a MESMA interface
 * do domínio, então o ViewModel não sabe (nem precisa saber) se há cache.
 *
 * Regra: com conexão, guarda cada resposta; com SemConexao, devolve a última cópia
 * guardada; sem cópia, deixa a falha seguir. Outras falhas (ex.: servidor com
 * erro 500) não usam o cache, para a tela mostrar o erro certo.
 */

/** Feed: guarda a primeira página de cada grupo (chave `feed.checkins.<grupoId>`). */
class FeedRepositoryComCache(
    private val remoto: FeedRepository,
    private val cache: CacheLocal,
) : FeedRepository {
    private val formato = ListSerializer(CheckInGuardado.serializer())

    override suspend fun feed(
        grupoId: String,
        cursor: String?,
    ): PaginaDoFeed {
        // Só a primeira página é guardada; as seguintes vão sempre ao servidor.
        if (cursor != null) return remoto.feed(grupoId, cursor)
        val chave = "checkins.$grupoId"
        return try {
            remoto.feed(grupoId).also { pagina ->
                cache.salvar(chave, pagina.checkIns.map { it.paraGuardar() }, formato)
            }
        } catch (falha: FalhaNoRepositorio.SemConexao) {
            val guardado = cache.ler(chave, formato) ?: throw falha
            PaginaDoFeed(
                checkIns = guardado.valor.map { it.paraDominio() },
                proximoCursor = null,
                desatualizada = true,
                guardadaEm = guardado.guardadoEm,
            )
        }
    }

    /** Registrar exige conexão: sem rede, a falha chega à tela, que mantém o formulário. */
    override suspend fun registrarCheckIn(novo: NovoCheckIn): CheckIn = remoto.registrarCheckIn(novo)
}

/** Grupos e membros: para o app achar o grupo ativo e abrir o feed mesmo sem rede. */
class GrupoRepositoryComCache(
    private val remoto: GrupoRepository,
    private val cache: CacheLocal,
) : GrupoRepository {
    private val formatoGrupos = ListSerializer(GrupoGuardado.serializer())
    private val formatoMembros = ListSerializer(MembroGuardado.serializer())

    override suspend fun meusGrupos(): List<Grupo> =
        try {
            remoto.meusGrupos().also { grupos -> cache.salvar(CHAVE_GRUPOS, grupos.map { it.paraGuardar() }, formatoGrupos) }
        } catch (falha: FalhaNoRepositorio.SemConexao) {
            cache.ler(CHAVE_GRUPOS, formatoGrupos)?.valor?.map { it.paraDominio() } ?: throw falha
        }

    override suspend fun membros(grupoId: String): List<Membro> {
        val chave = "membros.$grupoId"
        return try {
            remoto.membros(grupoId).also { membros -> cache.salvar(chave, membros.map { it.paraGuardar() }, formatoMembros) }
        } catch (falha: FalhaNoRepositorio.SemConexao) {
            cache.ler(chave, formatoMembros)?.valor?.map { it.paraDominio() } ?: throw falha
        }
    }

    // Ações que mudam a lista de grupos: a cópia antiga deixa de valer.
    override suspend fun criar(
        nome: String,
        descricao: String?,
    ): Grupo = remoto.criar(nome, descricao).also { cache.remover(CHAVE_GRUPOS) }

    override suspend fun entrar(codigoConvite: String): Grupo = remoto.entrar(codigoConvite).also { cache.remover(CHAVE_GRUPOS) }

    override suspend fun sair(grupoId: String) {
        remoto.sair(grupoId)
        cache.remover(CHAVE_GRUPOS)
    }

    private companion object {
        const val CHAVE_GRUPOS = "grupos"
    }
}

/** Números do cartão "Sua sequência". */
class EstatisticasRepositoryComCache(
    private val remoto: EstatisticasRepository,
    private val cache: CacheLocal,
) : EstatisticasRepository {
    override suspend fun minhas(): Estatisticas =
        try {
            remoto.minhas().also { cache.salvar(CHAVE, it.paraGuardar(), EstatisticasGuardadas.serializer()) }
        } catch (falha: FalhaNoRepositorio.SemConexao) {
            cache.ler(CHAVE, EstatisticasGuardadas.serializer())?.valor?.paraDominio() ?: throw falha
        }

    private companion object {
        const val CHAVE = "estatisticas"
    }
}

package br.imd.ufrn.presentation.grupo

import br.imd.ufrn.domain.FalhaNoRepositorio
import br.imd.ufrn.domain.grupo.Grupo
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.domain.grupo.Membro
import br.imd.ufrn.domain.grupo.PreferenciaDeGrupo
import kotlinx.coroutines.delay

/** Dublê do repositório de grupos, com demora em tempo virtual e respostas configuráveis. */
class GrupoRepositoryFalso : GrupoRepository {
    var grupos: List<Grupo> = emptyList()
    var membros: List<Membro> = emptyList()
    var falha: FalhaNoRepositorio? = null
    var falhaAoEntrar: FalhaNoRepositorio? = null
    var falhaAoSair: FalhaNoRepositorio? = null
    val saiuDe = mutableListOf<String>()
    var ultimoCriado: Pair<String, String?>? = null

    override suspend fun meusGrupos(): List<Grupo> {
        delay(100)
        falha?.let { throw it }
        return grupos
    }

    override suspend fun membros(grupoId: String): List<Membro> {
        delay(100)
        return membros
    }

    override suspend fun criar(
        nome: String,
        descricao: String?,
    ): Grupo {
        delay(100)
        ultimoCriado = nome to descricao
        return Grupo("novo", nome, descricao, "NOVO2345", 1, souAdmin = true)
    }

    override suspend fun entrar(codigoConvite: String): Grupo {
        delay(100)
        falhaAoEntrar?.let { throw it }
        return Grupo("g2", "Grupo do convite", null, codigoConvite, 4)
    }

    override suspend fun sair(grupoId: String) {
        delay(100)
        falhaAoSair?.let { throw it }
        saiuDe += grupoId
    }
}

class PreferenciaDeGrupoFalsa(
    var grupoId: String? = null,
) : PreferenciaDeGrupo {
    override suspend fun grupoAtualId(): String? = grupoId

    override suspend fun salvarGrupoAtual(grupoId: String?) {
        this.grupoId = grupoId
    }
}

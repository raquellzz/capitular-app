package br.imd.ufrn.presentation.navigation

import kotlinx.serialization.Serializable

const val CHECK_IN_DEEP_LINK = "capitular://check-in"

@Serializable
data object AuthDestination

/** Depois do login: descobre o grupo atual ou oferece criar/entrar em um. */
@Serializable
data object GrupoDestination

/** O nome vai na rota para o feed mostrar o grupo ativo na hora, mesmo sem rede. */
@Serializable
data class FeedDestination(
    val grupoId: String,
    val grupoNome: String,
)

@Serializable
data class RankingDestination(
    val grupoId: String,
)

/** Aba "Grupos": detalhes do grupo atual e os demais grupos do usuário. */
@Serializable
data class GruposDestination(
    val grupoId: String,
)

/** Registrar leitura. [grupoId] é `null` quando se chega pelo deep link `capitular://check-in`. */
@Serializable
data class CheckInDestination(
    val grupoId: String? = null,
)

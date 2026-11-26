package br.imd.ufrn.presentation.navigation

import kotlinx.serialization.Serializable

const val CHECK_IN_DEEP_LINK = "capitular://check-in"

@Serializable
data object AuthDestination

/** Depois do login: descobre o grupo atual ou oferece criar/entrar em um. */
@Serializable
data object GrupoDestination

@Serializable
data class FeedDestination(
    val grupoId: String,
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

@Serializable
data object CheckInDestination

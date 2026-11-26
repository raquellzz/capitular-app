package br.imd.ufrn.presentation.navigation

import kotlinx.serialization.Serializable

const val CHECK_IN_DEEP_LINK = "capitular://check-in"

@Serializable
data object AuthDestination

@Serializable
data object FeedDestination

@Serializable
data object RankingDestination

@Serializable
data object CheckInDestination

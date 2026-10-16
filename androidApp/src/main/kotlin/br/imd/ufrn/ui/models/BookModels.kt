package br.imd.ufrn.ui.models

data class CheckIn(
    val id: String = "",
    val friendName: String,
    val bookTitle: String,
    val pageRange: String,
    val timeElapsed: String,
    val photoUrl: String? = null,
    val avatarUrl: String? = null,
    val pagesRead: Int = 0,
    val note: String? = null,
)

data class Ranking(
    val position: Int,
    val name: String,
    val totalPages: Int,
    val avatarUrl: String? = null,
)

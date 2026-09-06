package br.imd.ufrn

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
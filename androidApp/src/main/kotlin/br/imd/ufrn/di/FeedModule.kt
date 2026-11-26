package br.imd.ufrn.di

import org.koin.dsl.module

/**
 * Frente A (Raquel): grupo atual, feed e check-in.
 * Repositórios (interface do domínio -> implementação em data) e ViewModels, ex.:
 *
 * ```
 * single<FeedRepository> { FeedRepositoryComCache(FeedRepositoryRemoto(get()), get()) }
 * viewModelOf(::FeedViewModel)
 * ```
 */
val feedModule =
    module {
    }

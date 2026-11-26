package br.imd.ufrn.di

import br.imd.ufrn.data.remoto.grupo.GrupoRepositoryRemoto
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.presentation.grupo.GrupoViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Frente A (Raquel): grupo atual, feed e check-in. */
val feedModule =
    module {
        single<GrupoRepository> { GrupoRepositoryRemoto(get()) }
        viewModelOf(::GrupoViewModel)
    }

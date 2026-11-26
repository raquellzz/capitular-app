package br.imd.ufrn.di

import br.imd.ufrn.data.local.PreferenciaDeGrupoLocal
import br.imd.ufrn.data.remoto.grupo.GrupoRepositoryRemoto
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.domain.grupo.PreferenciaDeGrupo
import br.imd.ufrn.presentation.grupo.GrupoViewModel
import br.imd.ufrn.presentation.grupo.MeusGruposViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Frente A (Raquel): grupo atual, feed e check-in. */
val feedModule =
    module {
        single<GrupoRepository> { GrupoRepositoryRemoto(get()) }
        single<PreferenciaDeGrupo> { PreferenciaDeGrupoLocal(get()) }
        viewModelOf(::GrupoViewModel)
        viewModel { (grupoId: String) -> MeusGruposViewModel(grupoId, get(), get()) }
    }

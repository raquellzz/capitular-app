package br.imd.ufrn.di

import br.imd.ufrn.BuildConfig
import br.imd.ufrn.data.local.PreferenciaDeGrupoLocal
import br.imd.ufrn.data.remoto.feed.EstatisticasRepositoryRemoto
import br.imd.ufrn.data.remoto.feed.FeedRepositoryRemoto
import br.imd.ufrn.data.remoto.grupo.GrupoRepositoryRemoto
import br.imd.ufrn.domain.feed.EstatisticasRepository
import br.imd.ufrn.domain.feed.FeedRepository
import br.imd.ufrn.domain.grupo.GrupoRepository
import br.imd.ufrn.domain.grupo.PreferenciaDeGrupo
import br.imd.ufrn.presentation.checkin.CheckInViewModel
import br.imd.ufrn.presentation.feed.FeedViewModel
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
        single<FeedRepository> { FeedRepositoryRemoto(get(), BuildConfig.API_BASE_URL) }
        single<EstatisticasRepository> { EstatisticasRepositoryRemoto(get()) }
        viewModelOf(::GrupoViewModel)
        viewModel { (grupoId: String) -> MeusGruposViewModel(grupoId, get(), get()) }
        viewModel { (grupoId: String) -> FeedViewModel(grupoId, get(), get()) }
        // Pelo deep link o grupo vem nulo: o ViewModel usa o último grupo aberto.
        viewModel { parametros -> CheckInViewModel(parametros.getOrNull<String>(), get(), get()) }
    }

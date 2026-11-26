package br.imd.ufrn.di

import br.imd.ufrn.BuildConfig
import br.imd.ufrn.data.local.criarDataStore
import br.imd.ufrn.data.remoto.ProvedorDeToken
import br.imd.ufrn.data.remoto.SessaoEmMemoria
import br.imd.ufrn.data.remoto.criarClienteHttp
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

/** Infraestrutura compartilhada pelas duas frentes: cliente HTTP, sessão e DataStore. */
val dadosModule =
    module {
        single { SessaoEmMemoria() } bind ProvedorDeToken::class
        single<HttpClientEngine> { OkHttp.create() }
        single { criarClienteHttp(engine = get(), baseUrl = BuildConfig.API_BASE_URL, provedorDeToken = get()) }
        single { criarDataStore(androidContext()) }
    }

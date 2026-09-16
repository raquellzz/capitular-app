package br.imd.ufrn.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

internal fun createHttpClient(): HttpClient = HttpClient(OkHttp)

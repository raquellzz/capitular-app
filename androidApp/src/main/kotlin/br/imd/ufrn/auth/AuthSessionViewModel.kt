package br.imd.ufrn.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AuthSessionViewModel : ViewModel() {
    var session by mutableStateOf<AuthSession?>(null)
        private set

    fun authenticate(authSession: AuthSession) {
        session = authSession
    }

    fun clearSession() {
        session = null
    }
}

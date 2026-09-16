package br.imd.ufrn

import br.imd.ufrn.auth.AuthFormState
import br.imd.ufrn.auth.AuthMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthFormStateTest {
    @Test
    fun loginRequiresIdentifierAndPassword() {
        assertEquals(
            "Informe seu e-mail ou nome de usuário.",
            AuthFormState().validationMessage(),
        )
        assertNull(
            AuthFormState(identifier = "ana", password = "segredo").validationMessage(),
        )
    }

    @Test
    fun registrationValidatesConfirmation() {
        val state =
            AuthFormState(
                mode = AuthMode.REGISTER,
                displayName = "Ana Lima",
                username = "ana_lima",
                email = "ana@example.com",
                password = "uma-senha-segura",
                passwordConfirmation = "outra-senha",
            )

        assertEquals("As senhas não coincidem.", state.validationMessage())
        assertNull(state.copy(passwordConfirmation = state.password).validationMessage())
    }
}

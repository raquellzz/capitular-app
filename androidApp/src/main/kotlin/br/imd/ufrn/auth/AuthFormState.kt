package br.imd.ufrn.auth

enum class AuthMode {
    LOGIN,
    REGISTER,
}

data class AuthFormState(
    val mode: AuthMode = AuthMode.LOGIN,
    val identifier: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    fun validationMessage(): String? =
        when (mode) {
            AuthMode.LOGIN ->
                when {
                    identifier.isBlank() -> "Informe seu e-mail ou nome de usuário."
                    password.isBlank() -> "Informe sua senha."
                    else -> null
                }

            AuthMode.REGISTER ->
                when {
                    displayName.trim().length < 2 -> "Informe seu nome."
                    username.trim().length < 3 -> "O nome de usuário precisa ter ao menos 3 caracteres."
                    !username.trim().matches(Regex("[a-zA-Z0-9_]+")) ->
                        "Use apenas letras, números e sublinhado no usuário."

                    !email.contains("@") -> "Informe um e-mail válido."
                    password.length < 8 -> "A senha precisa ter ao menos 8 caracteres."
                    password != passwordConfirmation -> "As senhas não coincidem."
                    else -> null
                }
        }
}

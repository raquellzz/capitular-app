package br.imd.ufrn.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CapitularUser(
    val id: String,
    val username: String,
    @SerialName("display_name") val displayName: String,
    val timezone: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class TokenPair(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("access_token_expires_at") val accessTokenExpiresAt: String,
)

@Serializable
data class AuthSession(
    val user: CapitularUser,
    val tokens: TokenPair,
)

@Serializable
internal data class LoginRequest(
    val identifier: String,
    val password: String,
)

@Serializable
internal data class RegisterRequest(
    val email: String,
    val username: String,
    @SerialName("display_name") val displayName: String,
    val password: String,
    val timezone: String = "America/Fortaleza",
)

@Serializable
internal data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

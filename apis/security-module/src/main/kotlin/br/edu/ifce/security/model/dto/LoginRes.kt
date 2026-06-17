package br.edu.ifce.security.model.dto

data class LoginRes(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

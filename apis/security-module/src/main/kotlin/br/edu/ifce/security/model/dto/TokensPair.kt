package br.edu.ifce.security.model.dto

data class TokensPair(
    val accessToken: String,
    val refreshToken: String,
)

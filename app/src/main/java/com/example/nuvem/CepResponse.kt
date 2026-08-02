package com.example.nuvem

data class CepResponse(
    val cep: String,
    val logradouro: String,
    val bairro: String,
    val localidade: String,
    val uf: String
)

package com.example.nuvem

data class ClienteAivenRequest(
    val codigo: String,
    val nome: String,
    val endereco: String,
    val cpf: String,
    val bairro: String,
    val estado: String,
    val municipio: String,
    val cep: String,
    val email: String,
    val niver: String,
    val telefone_1: String,
    val telefone_2: String? = null,
    val telefone_3: String? = null,
    val obs: String? = null
)

package com.example.nuvem

import android.icu.math.BigDecimal

data class ItemCarrinho(
    val produtoId: String,
    val nomeProduto: String,
    val precoUnitario: BigDecimal,
    var quantidade: Int
) {
    val totalItem: BigDecimal
        get() = precoUnitario.multiply(BigDecimal(quantidade))
}

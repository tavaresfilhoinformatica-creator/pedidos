package com.example.nuvem

import android.icu.math.BigDecimal

object CarrinhoManager {
    val itens = mutableListOf<ItemCarrinho>()

    fun adicionarOuAtualizar(item: ItemCarrinho) {
        val existente = itens.find { it.produtoId == item.produtoId }
        if (existente != null) {
            existente.quantidade += item.quantidade
        } else {
            itens.add(item)
        }
    }

    fun calcularTotal(): BigDecimal {
        return itens.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalItem) }
    }

    fun limpar() {
        itens.clear()
    }
}
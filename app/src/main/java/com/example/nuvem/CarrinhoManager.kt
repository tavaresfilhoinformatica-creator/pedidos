package com.example.nuvem

import java.math.BigDecimal

object CarrinhoManager {
    val itens = mutableListOf<ItemCarrinho>()

    fun adicionarOuAtualizar(novoItem: ItemCarrinho) {
        val index = itens.indexOfFirst { it.produtoId == novoItem.produtoId }
        if (index != -1) {
            // SUBSTITUI o item antigo pelo novo com a quantidade exata escolhida
            itens[index] = novoItem
        } else {
            // Se não existia, adiciona na lista
            itens.add(novoItem)
        }
    }

    fun calcularTotal(): BigDecimal {
        return itens.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalItem) }
    }

    fun limpar() {
        itens.clear()
    }
    fun removerItem(produtoId: String) {
        itens.removeAll { it.produtoId == produtoId }
    }
}
package com.example.nuvem

data class PedidoAivenRequest(
    val cpf: String,
    val numero: Int,
    val data_pedido: String,
    val formaPagamento: String,
    val totalPedido: Double,
    val enderecoEntrega: String,
    val bairroEntrega: String,
    val telefoneEntrega: String,
    val obs: String?,
    val cep_Entrega: String?,
    val itens: List<ItemPedidoAivenRequest>
)

data class ItemPedidoAivenRequest(
    val cpf: String,
    val numero: Int,
    val produtoId: Long,
    val descricao: String?,
    val quantidade: Int,
    val precoVenda: Double,
    val totalItem: Double
)

data class RespostaAiven(
    val sucesso: Boolean,
    val mensagem: String,
    val idPedidoNuvem: Long?
)
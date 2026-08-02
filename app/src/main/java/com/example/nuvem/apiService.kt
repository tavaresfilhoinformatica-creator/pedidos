package com.example.nuvem

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // Carrega a lista de grupos
    @GET("grupos")
    suspend fun atualizarTabelaRemota(): List<Grupo>

    // Carrega a lista de produtos (já com as URLs do Supabase em web_imagem)
    @GET("produtos")
    suspend fun obterProdutos(): List<Produto>

    // Carrega as formas de pagamento
    @GET("pagamentos")
   // suspend fun obterFormasPagamento(): List<Pagamento>
    suspend fun obterTodas(): List<Pagamento>

    // Envia o pedido feito pelo cliente no app de volta para a nuvem
    @POST("pedidos")
    suspend fun enviarPedido(@Body pedido: Pedido): Response<Void>
}


package com.example.nuvem

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var edtEndereco: EditText
    private lateinit var edtBairro: EditText
    private lateinit var edtCep: EditText
    private lateinit var edtTelefone: EditText
    private lateinit var edtObs: EditText
    private lateinit var spinnerPagamento: Spinner
    private lateinit var txtTotalCheckout: TextView

    private var cpfCliente: String = "" // Guardará o CPF retornado dos dados pessoais

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Referências da Interface
        val rvItens = findViewById<RecyclerView>(R.id.rvItensCheckout)
        rvItens.adapter = CarrinhoCheckoutAdapter(CarrinhoManager.itens)
        txtTotalCheckout = findViewById(R.id.txtTotalCheckout)
        edtEndereco = findViewById(R.id.edtEndereco)
        edtBairro = findViewById(R.id.edtBairro)
        edtCep = findViewById(R.id.edtCep)
        edtTelefone = findViewById(R.id.edtTelefone)
        edtObs = findViewById(R.id.edtObs)
        spinnerPagamento = findViewById(R.id.spinnerPagamento)
        val btnEnviarPedido = findViewById<Button>(R.id.btnEnviarPedido)
        val btnContinuarComprando = findViewById<Button>(R.id.btnContinuarComprando)

        // Configuração da Lista do Carrinho
        rvItens.layoutManager = LinearLayoutManager(this)

        atualizarTotal()

        // Carrega dados locais do Room
        carregarDadosPessoais()
        carregarFormasPagamento()

        // Botão Continuar Comprando
        btnContinuarComprando.setOnClickListener {
            finish() // Simplesmente volta para a tela anterior sem limpar o carrinho
        }

        // Botão Enviar Pedido
        btnEnviarPedido.setOnClickListener {
            finalizarEGravarPedido()
        }
    }

    private fun atualizarTotal() {
        val total = CarrinhoManager.calcularTotal()
        txtTotalCheckout.text = String.format("Total: R$ %.2f", total)
    }

    private fun carregarDadosPessoais() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            // Busca o primeiro registro de dados pessoais salvo no Room
            val dados = db.DadosPessoaisDao().obterDadosPessoais()
            withContext(Dispatchers.Main) {
                dados?.let {
                    cpfCliente = it.cpf
                    edtEndereco.setText(it.endereco)
                    edtBairro.setText(it.bairro)
                    edtCep.setText(it.cep)
                    edtTelefone.setText(it.telefone)
                }
            }
        }
    }

    private fun carregarFormasPagamento() {
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val pagamentosApi = RetrofitClient.apiService.obterTodas()
                if (pagamentosApi.isNotEmpty()) {
                    db.PagamentoDao().inserirPagamentos(pagamentosApi)
                }
            } catch (e: Exception) {
                Log.e("PAGAMENTO_TESTE", "Erro ao buscar do Retrofit: ${e.message}", e)
            }

            // Busca os objetos completos do Room
            val formas = db.PagamentoDao().obterTodas()

            withContext(Dispatchers.Main) {
                if (formas.isNotEmpty()) {
                    val adapter = object : ArrayAdapter<Pagamento>(
                        this@CheckoutActivity,
                        android.R.layout.simple_spinner_item,
                        formas
                    ) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getView(position, convertView, parent) as TextView
                            view.setTextColor(Color.WHITE)
                            view.text = getItem(position)?.descricao // Exibe a descrição no Spinner
                            return view
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getDropDownView(position, convertView, parent) as TextView
                            view.setTextColor(Color.WHITE)
                            view.text = getItem(position)?.descricao // Exibe a descrição no dropdown
                            return view
                        }
                    }
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerPagamento.adapter = adapter
                } else {
                    Toast.makeText(this@CheckoutActivity, "Nenhuma forma de pagamento cadastrada.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun finalizarEGravarPedido() {
        if (CarrinhoManager.itens.isEmpty()) {
            Toast.makeText(this, "Seu carrinho está vazio!", Toast.LENGTH_SHORT).show()
            return
        }

        if (cpfCliente.isBlank()) {
            Toast.makeText(this, "CPF do cliente não encontrado nos Dados Pessoais.", Toast.LENGTH_LONG).show()
            return
        }

        val endereco = edtEndereco.text.toString().trim()
        val bairro = edtBairro.text.toString().trim()
        val cep = edtCep.text.toString().trim()
        val telefone = edtTelefone.text.toString().trim()
        val observacaoDigitada = edtObs.text.toString().trim()

        if (endereco.isEmpty() || telefone.isEmpty()) {
            Toast.makeText(this, "Preencha o endereço e telefone para entrega.", Toast.LENGTH_SHORT).show()
            return
        }

        // Resgata o objeto Pagamento selecionado no Spinner
        val pagamentoSelecionado = spinnerPagamento.selectedItem as? Pagamento

        // Código de 2 caracteres enviado para a API/Aiven (ex: "01")
        val codigoPagamento = pagamentoSelecionado?.codigo ?: "01"

        // Descrição completa usada na observação
        val descricaoPagamento = pagamentoSelecionado?.descricao ?: "Não informado"

        // Monta a string de observação usando a descrição detalhada
        val obsFinal = if (observacaoDigitada.isNotEmpty()) {
            "$observacaoDigitada | Pagamento: $descricaoPagamento"
        } else {
            "Pagamento: $descricaoPagamento"
        }

        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Gera o próximo número de pedido para este CPF
            val ultimoNumero = db.pedidoDao().obterUltimoNumeroPedidoPorCpf(cpfCliente) ?: 0
            val novoNumeroPedido = ultimoNumero + 1

            val totalPedido = CarrinhoManager.calcularTotal()

            // 2. Instancia a Entity Pedido gravando a Observação
            val pedido = Pedido(
                cpf = cpfCliente,
                numero = novoNumeroPedido,
                total_pedido = totalPedido,
                endereco_entrega = endereco,
                bairro_entrega = bairro,
                cep_entrega = cep,
                telefone_entrega = telefone,
                obs = obsFinal
            )

            // 3. Mapeia os itens do carrinho para ItemPedido
            val listaItensPedido = CarrinhoManager.itens.map { item ->
                ItemPedido(
                    cpf = cpfCliente,
                    pedido = novoNumeroPedido.toString(),
                    produto = item.produtoId,
                    descricao = item.nomeProduto,
                    quantidade = item.quantidade,
                    preco_venda = item.precoUnitario,
                    total_item = item.totalItem
                )
            }

            // 4. Salva no Room local primeiro
            db.pedidoDao().inserirPedido(pedido)
            db.pedidoDao().inserirItensPedido(listaItensPedido)

            // 5. AGUARDA o envio para o Aiven terminar na rede passando o codigoPagamento (2 dígitos)
            enviarPedidoParaAiven(pedido, listaItensPedido, codigoPagamento.toString())

            // 6. Transição de tela executada somente após a sincronização
            withContext(Dispatchers.Main) {
                CarrinhoManager.limpar()
                Toast.makeText(this@CheckoutActivity, "Pedido #$novoNumeroPedido realizado com sucesso!", Toast.LENGTH_LONG).show()

                // Redireciona para a tela inicial/cardápio
                val intent = Intent(this@CheckoutActivity, cardapio::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private suspend fun enviarPedidoParaAiven(pedido: Pedido, itens: List<ItemPedido>, formaPagamento: String) {
        try {
            // Data atual formatada no padrão DD-MM-YYYY HH:mm:ss
            val dataFormatada = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())

            val requestAiven = PedidoAivenRequest(
                cpf = pedido.cpf,
                numero = pedido.numero,
                data_pedido = dataFormatada,
                formaPagamento = formaPagamento, // Envia o código de 2 caracteres
                totalPedido = pedido.total_pedido.toDouble(),
                enderecoEntrega = pedido.endereco_entrega,
                bairroEntrega = pedido.bairro_entrega,
                telefoneEntrega = pedido.telefone_entrega,
                obs = pedido.obs,
                cep_Entrega = pedido.cep_entrega,
                itens = itens.map { item ->
                    ItemPedidoAivenRequest(
                        cpf = item.cpf,
                        numero = item.pedido.toIntOrNull() ?: pedido.numero,
                        produtoId = item.produto.toLongOrNull() ?: 0L,
                        descricao = item.descricao,
                        quantidade = item.quantidade,
                        precoVenda = item.preco_venda.toDouble(),
                        totalItem = item.total_item.toDouble()
                    )
                }
            )

            val resposta = RetrofitClient.apiService.enviarPedidoAiven(requestAiven)

            withContext(Dispatchers.Main) {
                if (resposta.isSuccessful && resposta.body()?.sucesso == true) {
                    Log.d("AIVEN_SYNC", "Pedido #${pedido.numero} sincronizado com o Aiven com sucesso!")
                } else {
                    Log.e("AIVEN_SYNC", "Falha ao sincronizar com Aiven: ${resposta.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("AIVEN_SYNC", "Erro de conexão ao enviar para o Aiven: ${e.message}", e)
        }
    }
}
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
import java.math.BigDecimal

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
                Log.d("PAGAMENTO_TESTE", "Tentando buscar pagamentos do Retrofit...")
                val pagamentosApi = RetrofitClient.apiService.obterTodas()
                Log.d("PAGAMENTO_TESTE", "Veio da API: ${pagamentosApi.size} itens")

                if (pagamentosApi.isNotEmpty()) {
                    db.PagamentoDao().inserirPagamentos(pagamentosApi)
                    Log.d("PAGAMENTO_TESTE", "Pagamentos inseridos no Room com sucesso.")
                }
            } catch (e: Exception) {
                Log.e("PAGAMENTO_TESTE", "Erro ao buscar do Retrofit: ${e.message}", e)
            }

            // Consulta o banco local Room
            val formas = db.PagamentoDao().obterTodas()
            Log.d("PAGAMENTO_TESTE", "Total no Room local: ${formas.size} registros")

            val nomesFormas = formas.map { it.descricao }

            withContext(Dispatchers.Main) {
                if (nomesFormas.isNotEmpty()) {
                    // Customização do Adapter para renderizar o texto do Spinner na cor BRANCA
                    val adapter = object : ArrayAdapter<String>(
                        this@CheckoutActivity,
                        android.R.layout.simple_spinner_item,
                        nomesFormas
                    ) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getView(position, convertView, parent) as TextView
                            view.setTextColor(Color.WHITE)
                            return view
                        }

                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getDropDownView(position, convertView, parent) as TextView
                            view.setTextColor(Color.WHITE)
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

        val formaPagamento = spinnerPagamento.selectedItem ?: "Não informado"

        // Monta a string de observação (ou salva só o texto da obs se não quiser concatenar o pagamento)
        val obsFinal = if (observacaoDigitada.isNotEmpty()) {
            "$observacaoDigitada | Pagamento: $formaPagamento"
        } else {
            "Pagamento: $formaPagamento"
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
                    pedido = novoNumeroPedido,
                    produto = item.produtoId,
                    descricao = item.nomeProduto,
                    quantidade = item.quantidade,
                    preco_venda = item.precoUnitario,
                    total_item = item.totalItem
                )
            }

            // 4. Salva no Room
            db.pedidoDao().inserirPedido(pedido)
            db.pedidoDao().inserirItensPedido(listaItensPedido)

            // 5. Volta para a thread principal para limpar carrinho e avisar o usuário
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
}
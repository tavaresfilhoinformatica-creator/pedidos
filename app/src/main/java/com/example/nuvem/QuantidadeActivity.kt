package com.example.nuvem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal // 👈 IMPORT CORRIGIDO PARA JAVA.MATH

class QuantidadeActivity : AppCompatActivity() {
    private var quantidadeAtual = 1
    private lateinit var produtoId: String
    private lateinit var nomeProduto: String
    private lateinit var precoUnitario: BigDecimal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quantidade)

        // Recebe os dados passados pelo CardView
        produtoId = intent.getStringExtra("PRODUTO_ID") ?: ""
        nomeProduto = intent.getStringExtra("PRODUTO_NOME") ?: ""

        val precoString = intent.getStringExtra("PRODUTO_PRECO") ?: "0.00"
        precoUnitario = BigDecimal(precoString)

        // Referências das Views do Layout
        val txtNome = findViewById<TextView>(R.id.txtNomeProduto)
        val txtPreco = findViewById<TextView>(R.id.txtPrecoProduto)
        val txtQuantidade = findViewById<TextView>(R.id.txtQuantidade)
        val btnMais = findViewById<Button>(R.id.btnMais)
        val btnMenos = findViewById<Button>(R.id.btnMenos)
        val btnContinuar = findViewById<Button>(R.id.btnContinuarSelecionando)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizarPedido)

        // Preenche os dados do produto na tela
        txtNome.text = nomeProduto
        txtPreco.text = String.format("R$ %.2f", precoUnitario)

        btnMais.setOnClickListener {
            quantidadeAtual++
            txtQuantidade.text = quantidadeAtual.toString()
        }

        btnMenos.setOnClickListener {
            if (quantidadeAtual > 1) {
                quantidadeAtual--
                txtQuantidade.text = quantidadeAtual.toString()
            }
        }

        // Opção: Continuar Selecionando
        btnContinuar.setOnClickListener {
            salvarNoCarrinho()
            finish() // Volta para a tela anterior (CardView)
        }

        // Opção: Finalizar Pedido
        btnFinalizar.setOnClickListener {
            salvarNoCarrinho()
            val intent = Intent(this, CheckoutActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun salvarNoCarrinho() {
        val item = ItemCarrinho(
            produtoId = produtoId,
            nomeProduto = nomeProduto,
            precoUnitario = precoUnitario,
            quantidade = quantidadeAtual
        )
        CarrinhoManager.adicionarOuAtualizar(item)
    }
}
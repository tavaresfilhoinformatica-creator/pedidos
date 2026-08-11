package com.example.nuvem

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal

class QuantidadeActivity : AppCompatActivity() {
    private var quantidadeAtual = 1
    private lateinit var produtoId: String
    private lateinit var nomeProduto: String
    private lateinit var precoUnitario: BigDecimal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quantidade)

        // Recebe os dados passados pelo CardView/Cardápio
        produtoId = intent.getStringExtra("PRODUTO_ID") ?: ""
        nomeProduto = intent.getStringExtra("PRODUTO_NOME") ?: ""
        val precoString = intent.getStringExtra("PRODUTO_PRECO") ?: "0.00"
        precoUnitario = BigDecimal(precoString)

        // Referências das Views
        val txtNome = findViewById<TextView>(R.id.txtNomeProduto)
        val txtPreco = findViewById<TextView>(R.id.txtPrecoProduto)
        val txtQuantidade = findViewById<TextView>(R.id.txtQuantidade)
        val btnMais = findViewById<Button>(R.id.btnMais)
        val btnMenos = findViewById<Button>(R.id.btnMenos)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarItem)
        val btnContinuarComprando = findViewById<Button>(R.id.btnContinuarComprando)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizarPedido)
        val btnExcluir = findViewById<Button>(R.id.btnExcluirItem)

        // Preenche dados visuais básicos
        txtNome.text = nomeProduto
        txtPreco.text = String.format("R$ %.2f", precoUnitario)

        // REQUISITO 3: Verifica se o produto já está no carrinho
        val itemExistente = CarrinhoManager.itens.find { it.produtoId == produtoId }

        if (itemExistente != null) {
            // Se já existe, carrega a quantidade atual salva no carrinho
            quantidadeAtual = itemExistente.quantidade
            // REQUISITO 4: Exibe o botão de exclusão
            btnExcluir.visibility = View.VISIBLE
        } else {
            // Se é novo, garante que o botão de excluir fica escondido
            btnExcluir.visibility = View.GONE
        }

        txtQuantidade.text = quantidadeAtual.toString()

        // Incremento
        btnMais.setOnClickListener {
            quantidadeAtual++
            txtQuantidade.text = quantidadeAtual.toString()
        }

        // Decremento
        btnMenos.setOnClickListener {
            if (quantidadeAtual > 1) {
                quantidadeAtual--
                txtQuantidade.text = quantidadeAtual.toString()
            }
        }

        // REQUISITO 1: Só adiciona/atualiza no carrinho ao clicar em "Confirmar Item"
        btnConfirmar.setOnClickListener {
            salvarNoCarrinho()
            Toast.makeText(this, "Item atualizado no carrinho!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // REQUISITO 2: Volta sem adicionar/alterar nada no carrinho
        btnContinuarComprando.setOnClickListener {
            finish()
        }

        // Ir para a tela do Carrinho/Checkout
        btnFinalizar.setOnClickListener {
            val intent = Intent(this, CheckoutActivity::class.java)
            startActivity(intent)
            finish()
        }

        // REQUISITO 4: Exclui o item do carrinho se o usuário clicar no botão
        btnExcluir.setOnClickListener {
            CarrinhoManager.removerItem(produtoId)
            Toast.makeText(this, "Item removido do carrinho!", Toast.LENGTH_SHORT).show()
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
package com.example.nuvem

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class activity_historico_pedidos : AppCompatActivity() {

    private lateinit var rvHistorico: RecyclerView
    private lateinit var txtSemPedidos: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico_pedidos)

        rvHistorico = findViewById(R.id.rvHistoricoPedidos)
        txtSemPedidos = findViewById(R.id.txtSemPedidos)

        rvHistorico.layoutManager = LinearLayoutManager(this)

        carregarHistorico()
    }

    private fun carregarHistorico() {
        val db = AppDatabase.getDatabase(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val pedidos = db.pedidoDao().obterTodosPedidos()

            withContext(Dispatchers.Main) {
                if (pedidos.isNotEmpty()) {
                    txtSemPedidos.visibility = View.GONE
                    rvHistorico.visibility = View.VISIBLE

                    // Passamos a ação de clique para abrir os detalhes
                    rvHistorico.adapter = HistoricoAdapter(pedidos) { pedidoSelecionado ->
                        exibirDialogDetalhes(pedidoSelecionado)
                    }
                } else {
                    txtSemPedidos.visibility = View.VISIBLE
                    rvHistorico.visibility = View.GONE
                }
            }
        }
    }

    private fun exibirDialogDetalhes(pedido: Pedido) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog_detalhes_pedido, null)

        val txtTitulo = dialogView.findViewById<TextView>(R.id.txtTituloDetalhes)
        val containerItens = dialogView.findViewById<LinearLayout>(R.id.containerItens)
        val txtTotal = dialogView.findViewById<TextView>(R.id.txtTotalDetalhes)
        val btnFechar = dialogView.findViewById<Button>(R.id.btnFecharDetalhes)

        txtTitulo.text = "Pedido #${pedido.numero}"
        txtTotal.text = String.format("Total: R$ %.2f", pedido.total_pedido)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnFechar.setOnClickListener { alertDialog.dismiss() }

        // Busca os itens do pedido no Room de forma assíncrona
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val itens = db.pedidoDao().obterItensDoPedido(pedido.numero)

            withContext(Dispatchers.Main) {
                containerItens.removeAllViews()

                for (item in itens) {
                    val tvItem = TextView(this@activity_historico_pedidos).apply {
                        text = "${item.quantidade}x ${item.descricao} - R$ %.2f (Total: R$ %.2f)"
                            .format(item.preco_venda, item.total_item)
                        setTextColor(android.graphics.Color.WHITE)
                        textSize = 15f
                        setPadding(0, 6, 0, 6)
                    }
                    containerItens.addView(tvItem)
                }
            }
        }

        alertDialog.show()
    }
}
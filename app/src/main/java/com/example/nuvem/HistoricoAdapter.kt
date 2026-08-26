package com.example.nuvem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoricoAdapter(
    private val listaPedidos: List<Pedido>,
    private val onItemClick: (Pedido) -> Unit
) : RecyclerView.Adapter<HistoricoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNumero: TextView? = view.findViewById(R.id.txtNumeroPedido)
        val txtData: TextView? = view.findViewById(R.id.txtDataPedido)
        val txtEndereco: TextView? = view.findViewById(R.id.txtEnderecoEntrega)
        val txtObs: TextView? = view.findViewById(R.id.txtObsPagamento)
        val txtTotal: TextView? = view.findViewById(R.id.txtTotalPedido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico_pedido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pedido = listaPedidos[position]

        holder.txtNumero?.text = "Pedido #${pedido.numero}"
        holder.txtData?.text = pedido.data_pedido
        holder.txtEndereco?.text = "Entrega: ${pedido.endereco_entrega}, ${pedido.bairro_entrega}"
        holder.txtObs?.text = pedido.obs
        holder.txtTotal?.text = String.format("Total: R$ %.2f", pedido.total_pedido)

        // Ao clicar em qualquer lugar do card, executa o callback
        holder.itemView.setOnClickListener {
            onItemClick(pedido)
        }
    }

    override fun getItemCount(): Int = listaPedidos.size
}
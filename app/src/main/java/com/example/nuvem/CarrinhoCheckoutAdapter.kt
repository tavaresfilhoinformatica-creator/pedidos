package com.example.nuvem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// O construtor com os parênteses ( ) deve vir logo após o nome da classe
class CarrinhoCheckoutAdapter(
    private val itens: List<ItemCarrinho> // Se sua data class tiver outro nome, ajuste aqui
) : RecyclerView.Adapter<CarrinhoCheckoutAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeProduto)

        val txtQtdPreco: TextView = view.findViewById(R.id.txtQtdPreco)
        val txtTotalItem: TextView = view.findViewById(R.id.txtTotalItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_carrinho_checkout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]

        // Ajuste os nomes das propriedades (ex: nomeProduto, descricao, etc) se forem diferentes no seu model
        holder.txtNome.text = item.nomeProduto
        holder.txtQtdPreco.text = "${item.quantidade}x R$ %.2f".format(item.precoUnitario)
        holder.txtTotalItem.text = "R$ %.2f".format(item.totalItem)
    }

    override fun getItemCount(): Int = itens.size
}
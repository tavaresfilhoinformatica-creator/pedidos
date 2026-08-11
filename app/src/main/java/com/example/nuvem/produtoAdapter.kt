package com.example.nuvem

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class ProdutoAdapter : ListAdapter<Produto, ProdutoAdapter.ProdutoViewHolder>(DiffCallback) {

    // 1. DECLARAÇÃO DO CALLBACK DE CLIQUE
    var onItemClick: ((Produto) -> Unit)? = null

    class ProdutoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeProduto)
        val txtPreco: TextView = view.findViewById(R.id.txtPrecoProduto)
        val imgView: ImageView = view.findViewById(R.id.imgProduto)
        val txtObs: TextView = view.findViewById(R.id.txtObsProduto)

        fun bind(produto: Produto, onItemClick: ((Produto) -> Unit)?) {
            txtNome.text = produto.descricao
            txtPreco.text = String.format("R$ %.2f", produto.venda)
            txtObs.text = produto.obs

            // Carregamento de imagem com Glide
            Glide.with(itemView.context)
                .load(produto.imagem_web)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(imgView)

            // 2. DISPARO DO CLIQUE AO TOCAR NO ITEM
            itemView.setOnClickListener {
                onItemClick?.invoke(produto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        // Passa o onItemClick para a função bind do ViewHolder
        holder.bind(getItem(position), onItemClick)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Produto>() {
        override fun areItemsTheSame(oldItem: Produto, newItem: Produto) = oldItem.codigo == newItem.codigo
        override fun areContentsTheSame(oldItem: Produto, newItem: Produto) = oldItem == newItem
    }
}
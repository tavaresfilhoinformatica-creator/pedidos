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
import com.bumptech.glide.request.target.Target // 👈 IMPORT QUE FALTAVA

class ProdutoAdapter : ListAdapter<Produto, ProdutoAdapter.ProdutoViewHolder>(DiffCallback) {

    class ProdutoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeProduto)
        val txtPreco: TextView = view.findViewById(R.id.txtPrecoProduto)
        val imgView: ImageView = view.findViewById(R.id.imgProduto)
        val txtObs: TextView = view.findViewById(R.id.txtObsProduto)

        fun bind(produto: Produto) {
            txtNome.text = produto.descricao
            txtPreco.text = String.format("R$ %.2f", produto.venda)
            txtObs.text = produto.obs

            // Carregamento direto otimizado para Android 16
            Glide.with(itemView.context)
                .load(produto.imagem_web)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Garante o cache correto da imagem
                .placeholder(android.R.drawable.ic_menu_gallery) // Mostra mentras carrega
                .error(android.R.drawable.ic_delete)             // Mostra se a URL/Rede falhar
                .into(imgView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Produto>() {
        override fun areItemsTheSame(oldItem: Produto, newItem: Produto) = oldItem.codigo == newItem.codigo
        override fun areContentsTheSame(oldItem: Produto, newItem: Produto) = oldItem == newItem
    }
}
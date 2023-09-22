package br.com.igorbag.githubsearch.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.domain.Repository

class RepositoryAdapter(private val repositories: List<Repository>) :
    RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    var ItemLister: (Repository) -> Unit = {}
    var btnShareLister: (Repository) -> Unit = {}

    // Cria uma nova view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.repository_item, parent, false)
        return ViewHolder(view)
    }

    // Pega o conteudo da view e troca pela informacao de item de uma lista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.nomeRepositorio.text = repositories[position].name
        holder.btnCompartilhar.setOnClickListener{
            btnShareLister(repositories[position])
        }
        holder.cardRepositorio.setOnClickListener{
            //Log.d("Cliquei", holder.cardRepositorio.toString())
            ItemLister(repositories[position])
        }
    }
    override fun getItemCount(): Int = repositories.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nomeRepositorio : TextView
        val btnCompartilhar : ImageView
        val cardRepositorio : ConstraintLayout
        init {
            view.apply {
                nomeRepositorio = findViewById(R.id.tv_name)
                btnCompartilhar = findViewById(R.id.iv_share)
                cardRepositorio = findViewById(R.id.cl_card_content)
            }
        }

    }
}



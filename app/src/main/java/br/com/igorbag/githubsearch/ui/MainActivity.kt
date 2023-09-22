package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    lateinit var nomeUsuario: EditText
    lateinit var btnConfirmar: Button
    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var barra_Progresso : ProgressBar
    lateinit var noConnection_Wifi : ImageView
    lateinit var noConnection_Text : TextView
    lateinit var noRepoFound_Text : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRetrofit()
        setupView()
        setupListeners()
        showUserName()
    }

    override fun onResume() {
        super.onResume()
        if(nomeUsuario.text.isNotEmpty()){
            if(CheckNetwork(this)){
                getAllReposByUserName()
            }
            else{
                setupEmptyState()
            }
        }
        else{
            setupEmptyStateNoUserName()
        }

    }

    fun setupView() {
        nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        barra_Progresso = findViewById(R.id.Pb_Loader)
        noConnection_Wifi = findViewById(R.id.Iv_SemInternet)
        noConnection_Text = findViewById(R.id.Tv_SemInternet_Text)
        noRepoFound_Text = findViewById(R.id.Tv_SemRepositorios)
    }

    fun setupEmptyState(){
        barra_Progresso.visibility = View.GONE
        listaRepositories.visibility = View.GONE
        noRepoFound_Text.visibility = View.GONE
        noConnection_Wifi.visibility = View.VISIBLE
        noConnection_Text.visibility = View.VISIBLE
    }

    fun setupEmptyStateNoUserName(){
        barra_Progresso.visibility = View.GONE
        listaRepositories.visibility = View.GONE
        noRepoFound_Text.visibility = View.GONE
    }

    fun EmptyStateNull(){
        barra_Progresso.visibility = View.GONE
        listaRepositories.visibility = View.GONE
        noRepoFound_Text.visibility = View.VISIBLE
    }

    fun setupEmptyStateUserFound(){
        barra_Progresso.visibility = View.GONE
        noConnection_Wifi.visibility = View.GONE
        noConnection_Text.visibility = View.GONE
        noRepoFound_Text.visibility = View.GONE
    }

    private fun setupListeners() {
        btnConfirmar.setOnClickListener{
            saveUserLocal()
        }
    }


    fun setupRetrofit() {
        val builder = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        githubApi = builder.create(GitHubService::class.java)
    }


    private fun saveUserLocal() {
        if(nomeUsuario.text.isNotEmpty()){
            SaveSharedPref()
            getAllReposByUserName()
        }
        else{
            setupEmptyStateNoUserName()
            Toast.makeText(this, R.string.string_NoText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUserName() {
        val userName = GetSharedPreference()
        val editable = Editable.Factory.getInstance().newEditable(userName)
        nomeUsuario.text = editable
    }

    fun getAllReposByUserName(){
        barra_Progresso.visibility = View.VISIBLE
        val context = this
        try {
            githubApi.getAllRepositoriesByUser(user = "${nomeUsuario.text}").enqueue(object :
                Callback<List<Repository>> {
                override fun onResponse(
                    call: Call<List<Repository>>,
                    response: Response<List<Repository>>
                ) {

                    if(response.isSuccessful){
                        setupEmptyStateUserFound()
                        val repositories = response.body()
                        if (repositories != null) {
                            if(repositories.isNotEmpty()){
                                setupAdapter(repositories)
                            }
                            else{
                                EmptyStateNull()
                            }
                        } else {
                            EmptyStateNull()
                        }
                    }
                    else{
                        if(nomeUsuario.text.isNotEmpty()){
                            EmptyStateNull()
                        }
                    }
                }

                override fun onFailure(call: Call<List<Repository>>, t: Throwable) {
                    Toast.makeText(context, R.string.string_Response_Error, Toast.LENGTH_LONG).show()
                }
            })
        }
        catch (ex : Exception){
            Toast.makeText(context, R.string.string_Response_Error, Toast.LENGTH_LONG).show()
        }

    }

    fun setupAdapter(list: List<Repository>) {
        val listaAdapter = RepositoryAdapter(list)
        listaRepositories.apply {
            visibility = View.VISIBLE
            adapter = listaAdapter
        }
        listaAdapter.btnShareLister = { repo ->
            shareRepositoryLink(repo.htmlUrl)
        }
        listaAdapter.ItemLister = { repo ->
            openBrowser(repo.htmlUrl)
        }
    }

    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(urlRepository)
            )
        )

    }

    fun CheckNetwork(context: Context?) : Boolean{
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val network = connectivityManager.activeNetwork ?: return  false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return  when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        }
        else{
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }

    fun SaveSharedPref() {
        if(nomeUsuario.text.isNotEmpty()){
            val name = nomeUsuario.text
            val sharedPref = getPreferences(MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString("Saved", name.toString())
                apply()
            }
        }
    }

    fun GetSharedPreference() : String?
    {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        return sharedPref.getString("Saved", "")
    }

}
package com.example.rotacerta
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rotacerta.adapters.ViewPagerAdapter
import com.example.rotacerta.databinding.ActivityCadastroBinding
import com.example.rotacerta.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buscarNomeUsuarioFirestore()
        setContentView(binding.root)
        inicializarNavegacaoAbas()

    }



    private fun inicializarNavegacaoAbas() {
        val tabLayout = binding.tabLayoutPricipal
        val viewPager = binding.viewPagerPricipal

        //adpter
        val abas = listOf("HOME","CADASTRAR","PROCURAR","ROTAS")
        viewPager.adapter = ViewPagerAdapter(
            abas,supportFragmentManager,lifecycle
        )
        tabLayout.isTabIndicatorFullWidth = true
        TabLayoutMediator(tabLayout,viewPager){aba, posicao ->
            aba.text = abas[posicao]

        }.attach()
    }

    private fun inicializarToolbar(nomeUsuario: String?) {
        val toolbar = binding.includeMainToolbar.tbPricipal
        setSupportActionBar(toolbar)

        //Salva Nome do usuario logado
        supportActionBar?.apply {
          title = "Bem vindo ${nomeUsuario ?: "Ao APP Rota Certa"}" // Use "Rota Certa" como fallback se o nome for nulo
        }
        addMenuProvider(
            object : MenuProvider{
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_pricipal,menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when(menuItem.itemId){
                        R.id.item_perfil -> {
                            startActivity(Intent(applicationContext,PerfilActivity::class.java))

                        }
                        R.id.item_sair ->{
                            deslogarUsuario()
                        }
                    }
                    return true
                }

            }
        )

    }

    private fun buscarNomeUsuarioFirestore() {

        val userId = firebaseAuth.currentUser?.uid ?: return // Lidar com usuário não autenticado

        val userDocRef = db.collection("usuarios").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = document.data
                    val nomeUsuario = userData?.get("nomeMotorista") as? String
                    inicializarToolbar(nomeUsuario)
                } else {
                    inicializarToolbar(null)
                }
            }
            .addOnFailureListener { exception ->
                inicializarToolbar(null)
            }

    }

    private fun deslogarUsuario() {
        AlertDialog.Builder(this)
            .setTitle("Deslogar")
            .setMessage("Deseja Realmente Sair?")
            .setNegativeButton("Nao"){dialog,posicao ->}
            .setPositiveButton("Sim"){dialog,posicao ->
                firebaseAuth.signOut()
                startActivity(Intent(applicationContext,LoginActivity::class.java)
                )
            }
            .create()
            .show()
    }
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
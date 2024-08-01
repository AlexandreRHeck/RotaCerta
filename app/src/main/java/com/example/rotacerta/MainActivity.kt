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

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
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

    private fun inicializarToolbar() {
        val toolbar = binding.includeMainToolbar.tbPricipal
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Rota Certa"
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
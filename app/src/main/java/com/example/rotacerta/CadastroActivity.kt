package com.example.rotacerta

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rotacerta.databinding.ActivityCadastroBinding
import com.example.rotacerta.databinding.ActivityLoginBinding
import com.example.rotacerta.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class CadastroActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    private  lateinit var nome: String
    private lateinit var email: String
    private lateinit var senha: String

    //fireBase
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarToolbar()
        inicializarEventosClique()


    }

    private fun inicializarEventosClique() {

        binding.btnCadastrar.setOnClickListener{
            if(validarCampos()){
                //Cria sometente se for validados os campos
                cadastrarUsuario(nome,email,senha)

            }
        }

    }

    private fun cadastrarUsuario(nome: String, email: String, senha: String) {

        firebaseAuth.createUserWithEmailAndPassword(
            email,senha
        ).addOnCompleteListener{resultado ->
            if(resultado.isSuccessful){
                exibirMensagem("Sucesso ao fazer se Cadastro")
                startActivity(
                    Intent(applicationContext,MainActivity::class.java)
                )
            }
        }.addOnFailureListener{erro ->
            try{
                throw erro
            }catch (erroCredenciaisIvalidas: FirebaseAuthInvalidCredentialsException){
                erroCredenciaisIvalidas.printStackTrace()
                exibirMensagem("E-mail invalido, Digite seu E-mail correto!")
            }catch (erroUsuarioExistente: FirebaseAuthUserCollisionException){
                erroUsuarioExistente.printStackTrace()
                exibirMensagem("Ja existe uma conta Cadastrada com esse E-mail")
            }catch (erroSenhaFraca: FirebaseAuthWeakPasswordException){
                erroSenhaFraca.printStackTrace()
                exibirMensagem("Senha Fraca")
            }

        }
    }

    private fun validarCampos(): Boolean {

        nome = binding.editNome.text.toString()
        email = binding.editEmail.text.toString()
        senha = binding.editSenha.text.toString()

        if(nome.isNotEmpty()){
            binding.textInputNome.error = null

            if(email.isNotEmpty()){ // se nao estiver vazil cai no if
                binding.textInputEmail.error = null

                if(senha.isNotEmpty()){
                    binding.textInputSenha.error = null
                    return true
                }else{
                    binding.textInputSenha.error = "Preencha sua Senha!"
                    return false
                }

            }else{
                binding.textInputEmail.error = "Preencha seu E-mail!"
                return false
            }


        }else{
            binding.textInputNome.error = "Preencha seu nome!"
            return false
        }
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.tbPricipal
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "faca seu cadastro"
            setDisplayUseLogoEnabled(true)
        }
    }
}
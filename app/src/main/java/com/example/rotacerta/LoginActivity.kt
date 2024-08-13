package com.example.rotacerta

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rotacerta.databinding.ActivityLoginBinding
import com.example.rotacerta.databinding.ActivityMainBinding
import com.example.rotacerta.utils.exibirMensagem
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {


    companion object {
        private const val REQUEST_CHECK_SETTINGS = 1001
    }

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var email: String
    private lateinit var senha: String

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        inicializarEventosClique()
        firebaseAuth.signOut()

    }

    override fun onStart() {
        super.onStart()
        verificarUsuarioLogado()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // O usuário ligou o GPS, prosseguir para a MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // O usuário não ligou o GPS, exibir uma mensagem ou tomar outra ação
                exibirMensagem("O GPS é necessário para o funcionamento do aplicativo.")
            }
        }
    }


    private fun verificarUsuarioLogado() {
        val usuarioAtual = firebaseAuth.currentUser
        if (usuarioAtual != null) {
            // Verificar se o GPS está ligado
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client: SettingsClient = LocationServices.getSettingsClient(this)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

            task.addOnSuccessListener {
                // GPS está ligado, prosseguir para a MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // GPS está desligado, solicitar ao usuário que o ligue
                    try {
                        exception.startResolutionForResult(this@LoginActivity, REQUEST_CHECK_SETTINGS)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignorar o erro ou exibir uma mensagem ao usuário
                    }
                }
            }
        }
    }


    private fun inicializarEventosClique() {
        binding.textCadastro.setOnClickListener{
            startActivity(
                Intent(this,CadastroActivity::class.java)
            )
        }
        binding.textEsqueceuSenha.setOnClickListener {
            recuperarSenha()
        }
        binding.btnLogar.setOnClickListener{
            if(validarCampos()){
                logarUsuario()
                
            }
        }
    }

    private fun recuperarSenha() {
        val email = binding.editLoginEmail.text.toString()

        if (email.isEmpty()) {
            binding.textInputLayoutLoginEmail.error = "Digite seu e-mail para recuperar a senha"
            return
        }

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    exibirMensagem("Um e-mail de redefinição de senha foi enviado para $email")
                } else {
                    exibirMensagem("Erro ao enviar e-mail de redefinição de senha: ${task.exception?.message}")
                    // Lide com o erro de forma mais apropriada aqui (log, etc.)
                }
            }
    }

    private fun logarUsuario() {
        firebaseAuth.signInWithEmailAndPassword(
            email,senha
        ).addOnSuccessListener {
            //exibirMensagem("Logado")
            startActivity(
                Intent(this,MainActivity::class.java)
            )
        }.addOnFailureListener{erro ->
            try{
                throw erro
            }catch (erroUsuarioInvalido: FirebaseAuthInvalidUserException){
                erroUsuarioInvalido.printStackTrace()
                exibirMensagem("Email nao cadastrado")
            }catch (erroCredenciaisIvalidas: FirebaseAuthInvalidCredentialsException){
                erroCredenciaisIvalidas.printStackTrace()
                exibirMensagem("Email ou Senha estao incorretas!")
            }
        }
    }

    private fun validarCampos(): Boolean {
        email = binding.editLoginEmail.text.toString()
        senha = binding.editLoginSenha.text.toString()

        if(email.isNotEmpty()){ // se nao ta vazio
            binding.textInputLayoutLoginEmail.error = null
            if(senha.isNotEmpty()){
                binding.textInputLayoutLoginSenha.error = null
                return true
            }else {
                binding.textInputLayoutLoginEmail.error = "Preencha E-mail"
                return false
            }
        }else{
            binding.textInputLayoutLoginEmail.error = "Preecha seu email e senha"
            return false
        }
    }
}
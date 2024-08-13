package com.example.rotacerta

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rotacerta.databinding.ActivityCadastroBinding
import com.example.rotacerta.model.Usuario
import com.example.rotacerta.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class CadastroActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCadastroBinding.inflate(layoutInflater)
    }

    // Firebase
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
        binding.btnCadastrar.setOnClickListener {
            if (validarCampos()) {
                cadastrarUsuario()
            }
        }
    }

    private fun cadastrarUsuario() {
        val email = binding.editEmail.text.toString()
        val senha = binding.editSenha.text.toString()

        firebaseAuth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { resultado ->
                if (resultado.isSuccessful) {
                    val idUsuario = resultado.result.user?.uid
                    if (idUsuario != null) {
                        val usuario = Usuario(
                            documentId = idUsuario,
                            nomeMotorista = binding.editNome.text.toString(),
                            emailMotorista = email,
                            cpfMotorista = binding.editCPF.text.toString(),
                            telefoneMotorista = binding.editTelefone.text.toString(),
                            tipoVeiculo = binding.editTipoVeiculo.text.toString(),
                            placaVeiculo = binding.editPlacaVeiculo.text.toString(),
                            nomeEmpresa = binding.editEmpresa.text.toString(),
                            enderecoMotorista = binding.editEndereco.text.toString(),
                            cidadeMotorista = binding.editCidade.text.toString(),
                            estadoMotorista = binding.editEstado.text.toString(),
                            cepMotorista = binding.editCEP.text.toString()
                        )
                        salvarUsuarioFireStore(usuario)

                        // Navega para a MainActivity após o cadastro bem-sucedido
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    }
                }
            }
            .addOnFailureListener { erro ->
                try {
                    throw erro
                } catch (erroCredenciaisIvalidas: FirebaseAuthInvalidCredentialsException) {
                    exibirMensagem("E-mail inválido, Digite seu E-mail correto!")
                } catch (erroUsuarioExistente: FirebaseAuthUserCollisionException) {
                    exibirMensagem("Já existe uma conta Cadastrada com esse E-mail")
                } catch (erroSenhaFraca: FirebaseAuthWeakPasswordException) {
                    exibirMensagem("Senha Fraca")
                }
            }
    }

    private fun salvarUsuarioFireStore(usuario: Usuario) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(usuario.documentId)
            .set(usuario)
            .addOnSuccessListener {
                exibirMensagem("Sucesso ao fazer seu Cadastro")
            }
            .addOnFailureListener {
                exibirMensagem("Erro ao fazer seu Cadastro")
            }
    }

    private fun validarCampos(): Boolean {
        var camposValidos = true

        // Validar nome
        if (binding.editNome.text.toString().isEmpty()) {
            binding.textInputNome.error = "Preencha seu nome!"
            camposValidos = false
        } else {
            binding.textInputNome.error = null
        }

        // Validar email
        if (binding.editEmail.text.toString().isEmpty()) {
            binding.textInputEmail.error = "Preencha seu E-mail!"
            camposValidos = false
        } else {
            binding.textInputEmail.error = null
        }

        // Validar senha
        if (binding.editSenha.text.toString().isEmpty()) {
            binding.textInputSenha.error = "Preencha sua Senha!"
            camposValidos = false
        } else {
            binding.textInputSenha.error = null
        }

        // Adicione validações para outros campos conforme necessário

        return camposValidos
    }

    private fun inicializarToolbar() {
        val toolbar = binding.includeToolbar.tbPricipal
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "Faça seu cadastro"
            setDisplayHomeAsUpEnabled(true) // Adicionar botão de voltar
        }
    }
}

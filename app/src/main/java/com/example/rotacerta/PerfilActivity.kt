package com.example.rotacerta

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rotacerta.databinding.ActivityPerfilBinding
import com.example.rotacerta.utils.exibirMensagem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        enableEdgeToEdge() // Assuming you have this function for edge-to-edge display
        setContentView(binding.root)

        // ... (código para edge-to-edge, se necessário)

        buscarDadosUsuario()

        binding.btnSalvarPerfil.setOnClickListener {
            salvarDadosUsuario()
        }
    }

    private fun buscarDadosUsuario() {
        val userId = firebaseAuth.currentUser?.uid ?: return // Lidar com usuário não autenticado

        val userDocRef = db.collection("usuarios").document(userId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = document.data

                    // Preencher os campos com os dados do usuário
                    binding.editNomePerfil.setText(userData?.get("nomeMotorista") as? String)
                    binding.editCpfPerfil.setText(userData?.get("cpfMotorista") as? String)
                    binding.editTelefonePerfil.setText(userData?.get("telefoneMotorista") as? String)
                    binding.editTipoVeiculoPerfil.setText(userData?.get("tipoVeiculo") as? String)
                    binding.editPlacaVeiculoPerfil.setText(userData?.get("placaVeiculo") as? String)
                    binding.editEmpresaPerfil.setText(userData?.get("nomeEmpresa") as? String)
                    binding.editEnderecoPerfil.setText(userData?.get("enderecoMotorista") as? String)
                    binding.editCidadePerfil.setText(userData?.get("cidadeMotorista") as? String)
                    binding.editEstadoPerfil.setText(userData?.get("estadoMotorista") as? String)
                    binding.editCepPerfil.setText(userData?.get("cepMotorista") as? String)

                } else {
                    exibirMensagem("Usuário não encontrado")
                }
            }
            .addOnFailureListener { exception ->
                // Lidar com o erro de forma mais apropriada, talvez logando a exceção
                exibirMensagem("Erro ao buscar dados do usuário")
            }
    }

    private fun salvarDadosUsuario() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        val updatedData = hashMapOf(
            "nomeMotorista" to binding.editNomePerfil.text.toString(),
            "cpfMotorista" to binding.editCpfPerfil.text.toString(),
            "telefoneMotorista" to binding.editTelefonePerfil.text.toString(),
            "tipoVeiculo" to binding.editTipoVeiculoPerfil.text.toString(),
            "placaVeiculo" to binding.editPlacaVeiculoPerfil.text.toString(),
            "nomeEmpresa" to binding.editEmpresaPerfil.text.toString(),
            "enderecoMotorista" to binding.editEnderecoPerfil.text.toString(),
            "cidadeMotorista" to binding.editCidadePerfil.text.toString(),
            "estadoMotorista" to binding.editEstadoPerfil.text.toString(),
            "cepMotorista" to binding.editCepPerfil.text.toString()
        )

        // Explicitly cast to MutableMap<String, Any>
        db.collection("usuarios").document(userId)
            .update(updatedData as MutableMap<String, Any>)
            .addOnSuccessListener {
                exibirMensagem("Dados salvos com sucesso")
            }
            .addOnFailureListener { exception ->
                // Lidar com o erro de forma mais apropriada
                exibirMensagem("Erro ao salvar dados")
            }
    }

    // ... (restante do seu código, incluindo a função enableEdgeToEdge, se aplicável)
}

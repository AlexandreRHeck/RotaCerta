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
        enableEdgeToEdge()
        setContentView(binding.root)

        // ... (código para edge-to-edge)

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
                    binding.editNomePerfil.setText(userData?.get("nomeMotorista") as? String)
                    binding.editEmailPerfil.setText(userData?.get("emailMotorista") as? String)
                    // ... (preencher outros campos)
                } else {
                    exibirMensagem("Usuário não encontrado")
                }
            }
            .addOnFailureListener { exception ->
                exibirMensagem("Erro ao buscar dados do usuário")
                // Lidar com o erro de forma mais apropriada, talvez logando a exceção
            }
    }

    private fun salvarDadosUsuario() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        val updatedData = hashMapOf(
            "nomeMotorista" to binding.editNomePerfil.text.toString(),
            "emailMotorista" to binding.editEmailPerfil.text.toString()
            // ... (atualizar outros campos)
        )

        db.collection("usuarios").document(userId)
            .update(updatedData as MutableMap<String, Any>) // Explicit cast here
            .addOnSuccessListener {
                exibirMensagem("Dados salvos com sucesso")
            }
            .addOnFailureListener { exception ->
                exibirMensagem("Erro ao salvar dados")
                // Lidar com o erro de forma mais apropriada
            }
    }
}

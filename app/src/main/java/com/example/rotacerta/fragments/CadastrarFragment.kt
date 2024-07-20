package com.example.rotacerta.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rotacerta.databinding.FragmentCadastrarBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastrarFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Inicialização tardia do binding
    private val binding by lazy {
        FragmentCadastrarBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance() //acesso ao banco

        // Verificar se o usuário está logado
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            // Redirecionar para a tela de login ou mostrar uma mensagem de erro
            Log.e("CadastrarFragment", "Usuário não está logado")
            return binding.root // Agora você pode usar 'binding' aqui
        }

        binding.btnCadastrar.setOnClickListener {
            val nomeCompleto = binding.cadastrarNomeCompleto.editText?.text.toString()
            val rua = binding.cadastrarNomeCompleto.editText?.text.toString()
            val numero = binding.cadastrarNumero.editText?.text.toString()
            val cep = binding.cadastrarCEP.editText?.text.toString()
            val cidade = binding.cadastrarCidade.editText?.text.toString()
            val estado = binding.cadastrarEstado.editText?.text.toString()
            val ddd = binding.cadastrarDDD.editText?.text.toString()
            val telefone = binding.cadastrarTelefone.editText?.text.toString()

            // ... (Obter os valores dos outros campos)

            // Gerar um UID único para o novo cadastro
            val novoCadastroUid = firestore.collection("cadastros").document().id

            val dadosUsuario = hashMapOf(
                "uid" to novoCadastroUid,
                "nomeCompleto" to nomeCompleto,
                "rua" to rua,
                "numero" to numero,
                "cep" to cep,
                "cidade" to cidade,
                "estado" to estado,
                "ddd" to ddd,
                "telefone" to telefone
                // ... (Adicionar os outros campos)
            )

            firestore.collection("usuarios")
                .document(currentUser!!.uid)
                .collection("cadastros")
                .add(dadosUsuario)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cadastro realizado com sucesso! ID: $novoCadastroUid", Toast.LENGTH_SHORT).show()
                    // ... (Limpar os campos do formulário) ...
                    binding.cadastrarNomeCompleto.editText?.setText("")
                    binding.cadastrarRua.editText?.setText("")
                    binding.cadastrarNumero.editText?.setText("")
                    binding.cadastrarCEP.editText?.setText("")
                    binding.cadastrarCidade.editText?.setText("")
                    binding.cadastrarEstado.editText?.setText("")
                    binding.cadastrarDDD.editText?.setText("")
                    binding.cadastrarTelefone.editText?.setText("")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Erro ao cadastrar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return binding.root
    }


}

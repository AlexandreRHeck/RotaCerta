package com.example.rotacerta.fragments

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rotacerta.databinding.FragmentCadastrarBinding
import com.example.rotacerta.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Spinner

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

        // Set up Spinner for "Turno" (shift)
        val spinnerTurno = binding.spinnerTurno // Get reference to the Spinner
        val turnos = listOf("Manhã", "Tarde", "Noite")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, turnos) // Note: use 'R.layout...'
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinnerTurno.adapter = adapter

        binding.btnCadastrar.setOnClickListener {
            //DADOS RESPONSAVEL
            var email = binding.cadastrarEmail.editText?.text.toString()
            var cpf = binding.cadastrarCPF.editText?.text.toString()
            var nomeCompleto = binding.cadastrarNomeCompletoResponsavel.editText?.text.toString()
            var rua = binding.cadastrarRua.editText?.text.toString()
            var numero = binding.cadastrarNumero.editText?.text.toString()
            var cep = binding.cadastrarCEP.editText?.text.toString()
            var cidade = binding.cadastrarCidade.editText?.text.toString()
            var estado = binding.cadastrarEstado.editText?.text.toString()
            var ddd = binding.cadastrarDDD.editText?.text.toString()
            val telefone = binding.cadastrarTelefone.editText?.text.toString()
            //dados do aluno
            var nomeCompletoAluno = binding.cadastrarNomeCompletoAluno.editText?.text.toString()
            var escola = binding.cadastrarEscola.editText?.text.toString()
            var turno = binding.spinnerTurno.selectedItem.toString()
            var pontoReferencia = binding.cadastrarPontoReferencia.editText?.text.toString()
            var observacoes = binding.cadastrarObservacoes.editText?.text.toString()
            // ... (Obter os valores dos outros campos)


            // Gerar um UID único para o novo cadastro
            val novoCadastroUid = firestore.collection("cadastros").document().id
            val dadosUsuario = hashMapOf(
                "uid" to novoCadastroUid,
                "email" to email,
                "cpf" to cpf,
                "nomeCompleto" to nomeCompleto,
                "rua" to rua,
                "numero" to numero,
                "cep" to cep,
                "cidade" to cidade,
                "estado" to estado,
                "ddd" to ddd,
                "telefone" to telefone,
                "nomeCompletoAluno" to nomeCompletoAluno,
                "escola" to escola,
                "turno" to turno,
                "pontoReferencia" to pontoReferencia,
                "observacoes" to observacoes,
                // ... (Adicionar os outros campos)
            )

            firestore.collection("usuarios")
                .document(currentUser!!.uid)
                .collection("cadastros")
                .add(dadosUsuario)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()

                    // ... (Limpar os campos do formulário) ...
                    binding.cadastrarEmail.editText?.setText("")
                    binding.cadastrarCPF.editText?.setText("")
                    binding.cadastrarNomeCompletoResponsavel.editText?.setText("")
                    binding.cadastrarRua.editText?.setText("")
                    binding.cadastrarNumero.editText?.setText("")
                    binding.cadastrarCEP.editText?.setText("")
                    binding.cadastrarCidade.editText?.setText("")
                    binding.cadastrarEstado.editText?.setText("")
                    binding.cadastrarDDD.editText?.setText("")
                    binding.cadastrarTelefone.editText?.setText("")
                    //dados do aluno
                    binding.cadastrarNomeCompletoAluno.editText?.setText("")
                    binding.cadastrarEscola.editText?.setText("")
                    binding.spinnerTurno.setSelection(0)
                    binding.cadastrarPontoReferencia.editText?.setText("")
                    binding.cadastrarObservacoes.editText?.setText("")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Erro ao cadastrar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return binding.root
    }


}

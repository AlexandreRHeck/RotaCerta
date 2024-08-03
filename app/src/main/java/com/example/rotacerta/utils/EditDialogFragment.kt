import android.R
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.rotacerta.databinding.FragmentEditDialogBinding
import com.example.rotacerta.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditDialogFragment : DialogFragment() {

    private var _binding: FragmentEditDialogBinding? = null
    private val binding get() = _binding!!

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDialogBinding.inflate(inflater, container, false)
        val task = arguments?.getParcelable<Task>("task")


        val spinnerTurno: Spinner = binding.editspinnerTurno // Use the correct ID from your XML
        val turnos = listOf("Manhã", "Tarde", "Noite")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, turnos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTurno.adapter = adapter

        if (task != null) {

            binding.editEmail.setText(task.email)
            binding.editCpf.setText(task.cpf)
            binding.editNomeCompleto.setText(task.nomeCompleto)
            binding.editRua.setText(task.rua)
            binding.editNumero.setText(task.numero)
            binding.editCep.setText(task.cep)
            binding.editCidade.setText(task.cidade)
            binding.editEstado.setText(task.estado)
            binding.editDDD.setText(task.ddd)
            binding.editTelefone.setText(task.telefone)
            binding.editNomeCompletoAluno.setText(task.nomeCompletoAluno)
            binding.editEscola.setText(task.escola)
            val turnoIndex = turnos.indexOf(task.turno)
            if (turnoIndex >= 0) {
                spinnerTurno.setSelection(turnoIndex)
            } else {
                // Handle case where task.turno is not in the list of options (optional)
                Log.w("EditDialogFragment", "Turno '${task.turno}' not found in options")
            }
            binding.editPontoReferencia.setText(task.pontoReferencia)
            binding.editObservacoes.setText(task.observacoes)
        } else {
            // Lidar com o caso em que a tarefa é nula (opcional)
        }

        binding.btnSave.setOnClickListener { saveTask(task) }
        return binding.root
    }

    private fun saveTask(task: Task?) {
       /*
                 = binding.editNomeCompleto.text.toString(),
                 = binding.editRua.text.toString(),
                 = binding..text.toString(),
                cep = binding.editCep.text.toString(),
                cidade = binding.editCidade.text.toString(),
                estado = binding.editEstado.text.toString(),
                ddd = binding.editDDD.text.toString(),
                telefone = binding.editTelefone?.text.toString(),
                //dados do aluno
                nomeCompletoAluno = binding.editNomeCompletoAluno.text.toString(),
                escola = binding.editEscola.text.toString(),
                turno = binding.editspinnerTurno.selectedItem.toString(),
                pontoReferencia = binding.editPontoReferencia.text.toString(),
                observacoes = binding.editObservacoes.text.toString(),
                // Consider security practices for handling passwords
    */

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null || task == null || task.documentId == null) {
            Toast.makeText(requireContext(), "Erro ao atualizar cadastro", Toast.LENGTH_SHORT).show()
            return
        }

        // Cria um mapa dos campos a serem atualizados
        val updates = mutableMapOf<String, Any>()

        // Verifique se cada campo foi alterado e adicione-o ao mapa de atualizações, se houver
        // Email, CPF, nomeCompleto, rua, numero (already included in your previous request)
        if (binding.editEmail.text.toString() != task.email) {
            updates["email"] = binding.editEmail.text.toString()
        }
        if (binding.editCpf.text.toString() != task.cpf) {
            updates["cpf"] = binding.editCpf.text.toString()
        }
        if (binding.editNomeCompleto.text.toString() != task.nomeCompleto) {
            updates["nomeCompleto"] = binding.editNomeCompleto.text.toString()
        }
        if (binding.editRua.text.toString() != task.rua) {
            updates["rua"] = binding.editRua.text.toString()
        }
        if (binding.editNumero.text.toString() != task.numero) {
            updates["numero"] = binding.editNumero.text.toString()
        }
        // Remaining fields
        if (binding.editCep.text.toString() != task.cep) {
            updates["cep"] = binding.editCep.text.toString()
        }
        if (binding.editCidade.text.toString() != task.cidade) {
            updates["cidade"] = binding.editCidade.text.toString()
        }
        if (binding.editEstado.text.toString() != task.estado) {
            updates["estado"] = binding.editEstado.text.toString()
        }
        if (binding.editDDD.text.toString() != task.ddd) {
            updates["ddd"] = binding.editDDD.text.toString()
        }
        if (binding.editTelefone?.text.toString() != task.telefone) {
            updates["telefone"] = binding.editTelefone?.text.toString()
        }
        if (binding.editNomeCompletoAluno.text.toString() != task.nomeCompletoAluno) {
            updates["nomeCompletoAluno"] = binding.editNomeCompletoAluno.text.toString()
        }
        if (binding.editEscola.text.toString() != task.escola) {
            updates["escola"] = binding.editEscola.text.toString()
        }
        if (binding.editspinnerTurno.selectedItem.toString() != task.turno) {
            updates["turno"] = binding.editspinnerTurno.selectedItem.toString()
        }
        if (binding.editPontoReferencia.text.toString() != task.pontoReferencia) {
            updates["pontoReferencia"] = binding.editPontoReferencia.text.toString()
        }
        if (binding.editObservacoes.text.toString() != task.observacoes) {
            updates["observacoes"] = binding.editObservacoes.text.toString()
        }

        // Atualizar somente se houver alterações
        if (updates.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .collection("cadastros")
                .document(task.documentId)
                .update(updates)
                .addOnSuccessListener {
                    dismiss()
                    Toast.makeText(requireContext(), "Cadastro atualizado com sucesso", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Erro ao atualizar cadastro", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Nenhuma alteração detectada", Toast.LENGTH_SHORT).show()
        }
    }
}


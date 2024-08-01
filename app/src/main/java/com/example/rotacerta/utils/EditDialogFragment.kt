import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        //
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
            binding.editTurno.setText(task.turno)
            binding.editPontoReferencia.setText(task.pontoReferencia)
            binding.editObservacoes.setText(task.observacoes)
            //binding.editNomeCompleto.setText(task.nomeCompleto)
            //binding.editEmail.setText(task.email)
           // binding.editSenha.setText(task.senha) // Consider security practices for handling passwords
        } else {
            // Lidar com o caso em que a tarefa é nula (opcional)
        }

        binding.btnSave.setOnClickListener { saveTask(task) }
        return binding.root
    }

    private fun saveTask(task: Task?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null && task != null) { // Verifica se task não é nulo


            fun String?.toIntOrNullSafe(): Int? {
                return this?.replace(Regex("[^\\d]"), "")?.toIntOrNull()
            }

            fun String?.toLongOrNullSafe(): Long? {
                return this?.replace(Regex("[^\\d]"), "")?.toLongOrNull()
            }
            val updatedTask = Task(
                documentId = userId, // Mantém o documentId original
                email = binding.editEmail.text.toString(),
                cpf = binding.editCpf.text.toString(),
                nomeCompleto = binding.editNomeCompleto.text.toString(),
                rua = binding.editRua.text.toString(),
                numero = binding.editNumero.text.toString(),
                cep = binding.editCep.text.toString(),
                cidade = binding.editCidade.text.toString(),
                estado = binding.editEstado.text.toString(),
                ddd = binding.editDDD.text.toString(),
                telefone = binding.editTelefone?.text.toString(),
                //dados do aluno
                nomeCompletoAluno = binding.editNomeCompletoAluno.text.toString(),
                escola = binding.editEscola.text.toString(),
                turno = binding.editTurno.text.toString(),
                pontoReferencia = binding.editPontoReferencia.text.toString(),
                observacoes = binding.editObservacoes.text.toString(),
                // Consider security practices for handling passwords
            )

            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .collection("cadastros")
                .document(task.documentId!!) // Usa !! para garantir que não é nulo
                .set(updatedTask)
                .addOnSuccessListener {
                    dismiss()
                    Toast.makeText(requireContext(), "Cadastro atualizado com sucesso", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Lidar com erro ao atualizar o cadastro
                    Toast.makeText(requireContext(), "Erro ao atualizar cadastro", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Erro ao atualizar cadastro", Toast.LENGTH_SHORT).show()
        }
    }

    // ... rest of your code (onCreateDialog, onStart, onDestroyView) ...
}


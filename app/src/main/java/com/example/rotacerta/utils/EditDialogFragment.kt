import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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

        if (task != null) {
            binding.editNomeCompleto.setText(task.nomeCompleto)
            binding.editCpf.setText(task.cpf)
            binding.editTelefone.setText(task.telefone)
            binding.editEmail.setText(task.email)
            binding.editSenha.setText(task.senha) // Consider security practices for handling passwords
        } else {
            // Lidar com o caso em que a tarefa é nula (opcional)
        }

        binding.btnSave.setOnClickListener { saveTask(task) }
        return binding.root
    }

    private fun saveTask(task: Task?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null && task != null) { // Verifica se task não é nulo
            val updatedTask = Task(
                documentId = task.documentId, // Mantém o documentId original
                nomeCompleto = binding.editNomeCompleto.text.toString(),
                cpf = binding.editCpf.text.toString(),
                telefone = binding.editTelefone.text.toString(),
                email = binding.editEmail.text.toString(),
                senha = binding.editSenha.text.toString() // Consider security practices for handling passwords
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


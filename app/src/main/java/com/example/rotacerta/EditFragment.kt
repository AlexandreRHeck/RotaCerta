import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rotacerta.databinding.FragmentEditBinding
import com.example.rotacerta.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditFragment : Fragment() {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var task: Task

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)

        val task = arguments?.getParcelable<Task>("task")
        if (task != null) {
            binding.editNomeCompleto.setText(task.nomeCompleto)
            binding.editCpf.setText(task.cpf)
            binding.editTelefone.setText(task.telefone)
            binding.editEmail.setText(task.email)
            binding.editSenha.setText(task.senha) // Consider security practices for handling passwords
        }

        binding.btnSave.setOnClickListener { saveTask() }
        return binding.root
    }

    private fun saveTask() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val updatedTask = Task(
                nomeCompleto = binding.editNomeCompleto.text.toString(),
            )

            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .collection("cadastros")
                .document(task.documentId)
                .set(updatedTask)
                .addOnSuccessListener {
                    Log.d("EditFragment", "Task updated successfully")
                    findNavController().popBackStack() // Navigate back after saving
                }
                .addOnFailureListener { e ->
                    Log.e("EditFragment", "Error updating task: ${e.message}")
                    // Handle the error appropriately (e.g., show a toast)
                }
        } else {
            Log.e("EditFragment", "User ID or document ID is null. Cannot update task.")
        }
    }

    // ... (saveTask function as before)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


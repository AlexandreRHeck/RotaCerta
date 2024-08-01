import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.rotacerta.databinding.ItemCadastradosAdapterBinding
import com.example.rotacerta.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.rotacerta.R


class TaskAdapter(
    private val activity: FragmentActivity,
    private val onEditClickListener: (Task, MyViewHolder) -> Unit // Modified to accept both Task and ViewHolder
) : RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    private val taskList = mutableListOf<Task>()
    private var firestoreListener: ListenerRegistration? = null



    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            firestoreListener = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .collection("cadastros")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("TaskAdapter", "Error fetching tasks: ${error.message}")
                        return@addSnapshotListener
                    }

                    taskList.clear()
                    snapshot?.forEach { document ->
                        val task = document.toObject(Task::class.java)
                        task?.documentId = document.id // Set the documentId from Firestore
                        task?.let { taskList.add(it) }
                    }
                    notifyDataSetChanged()
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            ItemCadastradosAdapterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val task = taskList[position]
        holder.binding.textViewcardNome.text = task.nomeCompleto
        holder.binding.textViewcardDescricao.text = task.nomeCompletoAluno

        holder.binding.btnCardDelite.setOnClickListener {
            deleteTask(task, position) // Pass position for efficient removal
        }

        holder.binding.btnCardEdit.setOnClickListener {
            onEditClickListener(task,holder)
        }
        
    }




    private fun deleteTask(task: Task, position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val taskRef = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(userId)
                .collection("cadastros")
                .document(task.documentId)

            taskRef.delete()
                .addOnSuccessListener {
                    Log.d("TaskAdapter", "Tarefa excluída com sucesso")

                    // Check if the position is valid before removing
                    if (position in 0 until taskList.size) { // Check if position is within bounds
                        taskList.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        // Handle the case where the position is out of bounds
                        Log.e("TaskAdapter", "Posição inválida para remoção de tarefa.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("TaskAdapter", "Erro ao excluir tarefa: ${e.message}")
                }
        } else {
            Log.e("TaskAdapter", "O ID do usuário ou ID do documento é nulo. Não é possível excluir a tarefa.")
        }
       }


    override fun getItemCount() = taskList.size

    inner class MyViewHolder(val binding: ItemCadastradosAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun cleanup() {
        firestoreListener?.remove()
    }
}

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rotacerta.databinding.ItemCadastradosAdapterBinding
import com.example.rotacerta.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TaskAdapter(
    val taskSelected: (Task, Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    companion object {
        val SELECT_REMOVE: Int = 1
        val SELECT_EDIT: Int = 2
    }

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

        holder.binding.btnCardDelite.setOnClickListener { taskSelected(task, SELECT_REMOVE) }
        holder.binding.btnCardEdit.setOnClickListener { taskSelected(task, SELECT_EDIT) }
    }

    override fun getItemCount() = taskList.size

    inner class MyViewHolder(val binding: ItemCadastradosAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun cleanup() {
        firestoreListener?.remove()
    }
}

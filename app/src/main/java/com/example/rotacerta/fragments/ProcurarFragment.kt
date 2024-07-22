import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rotacerta.R
import com.example.rotacerta.databinding.FragmentProcurarBinding
import com.example.rotacerta.model.Task
import com.google.android.material.textfield.TextInputEditText

class ProcurarFragment : Fragment() {

    private var _binding: FragmentProcurarBinding? = null
    private var currentEditViewHolder: TaskAdapter.MyViewHolder? =
        null // Track the currently edited ViewHolder

    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProcurarBinding.inflate(inflater, container, false)

        taskAdapter = TaskAdapter(requireActivity()) { task, viewHolder -> // Add viewHolder parameter
            navigateToEditFragment(task, viewHolder)
        }

        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        return binding.root
    }

    private fun navigateToEditFragment(task: Task, viewHolder: TaskAdapter.MyViewHolder) {
        val bundle = Bundle()
        bundle.putParcelable("task", task)
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container_edit, EditFragment::class.java, bundle)
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun saveTask(task: Task, editView: View) {
        // ... (your existing saveTask logic to update Firestore)

        // After successful update:
        binding.root.removeView(editView)  // Remove the editView
        binding.rvLista.visibility = View.VISIBLE // Show the RecyclerView again
        //Optionally update your RecyclerView's adapter data
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

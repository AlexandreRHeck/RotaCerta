import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rotacerta.databinding.FragmentProcurarBinding

class ProcurarFragment : Fragment() {

    private var _binding: FragmentProcurarBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProcurarBinding.inflate(inflater, container, false)

        taskAdapter = TaskAdapter { task, action ->
            // Lida com a seleção de tarefas (editar ou excluir)
        }

        binding.rvLista.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        taskAdapter.cleanup() // Limpa o ouvinte do Firestore
        _binding = null
    }
}

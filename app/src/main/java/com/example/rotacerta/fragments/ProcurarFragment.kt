import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rotacerta.R
import com.example.rotacerta.databinding.FragmentProcurarBinding
import com.example.rotacerta.model.Task
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Route
import java.io.IOException



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
        val editDialogFragment = EditDialogFragment()
        editDialogFragment.arguments = bundle
        editDialogFragment.show(childFragmentManager, "EditDialogFragment")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

        //gerar rotas

    }


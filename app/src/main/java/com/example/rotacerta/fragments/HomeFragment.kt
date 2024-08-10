package com.example.rotacerta.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.rotacerta.R
import com.example.rotacerta.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null // Declare googleMap as a nullable property

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private val iconeTempoMap = mapOf(
        "céu limpo" to R.raw.sol_animado,
        "céu limpo-noite" to R.raw.sol_noite_animado,
        "poucas nuvens" to R.raw.poucas_nuvens_aniamdo,
        "algumas nuvens" to R.raw.nublado_animado,

        // "céu limpo-dia" to R.raw.sol_dia_animado,
        // "céu limpo-noite" to R.raw.sol_noite_animado,
        // "poucas nuvens-dia" to R.raw.poucas_nuvens_dia_animado,
        // "poucas nuvens-noite" to R.raw.poucas_nuvens_noite_animado,
        // "nuvens dispersas-dia" to R.raw.nuvens_dispersas_dia_animado,
        // "nuvens dispersas-noite" to R.raw.nuvens_dispersas_noite_animado,

    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        //val nomeUsuario = obterNomeUsuario()
       // binding.textBoasVindas.text = "Bem-vindo, $nomeUsuario!"

        obterLocalizacaoEBuscarTempo()
    }

    //private fun obterNomeUsuario(): String {// (your existing code to get the user's name) }

    private fun obterLocalizacaoEBuscarTempo() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Lidar com o caso em que as permissões não foram concedidas
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    buscarDadosTempo(latitude, longitude)
                } ?: run {
                    // Lidar com o caso em que a localização é nula
                    binding.textSituacaoTempo.text = "Erro ao obter a localização (nula)"
                }
            }
            .addOnFailureListener { exception ->
                // Lidar com erros ao obter a localização
                binding.textSituacaoTempo.text = "Erro ao obter a localização: ${exception.message}"
            }
    }

    private fun buscarDadosTempo(latitude: Double, longitude: Double) {
        // Use a viewModelScope ou lifecycleScope para gerenciar a coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=6601b16013c44333071e70f296e346ea&units=metric&lang=pt_br")
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    val jsonData = response.body?.string()

                    if (jsonData != null) {
                        val jsonObject = JSONObject(jsonData)
                        val descricaoTempo = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description")
                        val temperatura = jsonObject.getJSONObject("main").getDouble("temp")
                        val nascerSol = jsonObject.getJSONObject("sys").getLong("sunrise") * 1000 // Converter para milissegundos
                        val porSol = jsonObject.getJSONObject("sys").getLong("sunset") * 1000 // Converter para milissegundos

                        withContext(Dispatchers.Main) {
                            val horarioAtual = System.currentTimeMillis()
                            val periodo = if (horarioAtual in nascerSol..porSol) "dia" else "noite"

                            val iconeResourceId = iconeTempoMap["$descricaoTempo-$periodo"]
                                ?: iconeTempoMap[descricaoTempo]
                                ?: R.raw.padrao_animado

                            (binding.iconeTempo as LottieAnimationView).setAnimation(iconeResourceId)
                            binding.iconeTempo.playAnimation()

                            binding.textSituacaoTempo.text = "Situação do tempo: $descricaoTempo (${temperatura.toInt()}°C)"
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.textSituacaoTempo.text = "Erro ao buscar dados do tempo (resposta nula)"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.textSituacaoTempo.text = "Erro ao buscar dados do tempo: ${e.message}"
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable My Location and fetch weather data
                enableMyLocationOnMap()
                obterLocalizacaoEBuscarTempo() // Fetch weather data after permission is granted
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(requireContext(), "Permissão de localização negada. A funcionalidade de localização não estará disponível.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun enableMyLocationOnMap() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.rotacerta.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.rotacerta.R
import com.example.rotacerta.databinding.FragmentRotaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import android.location.Location

// ... (imports para o adaptador do RecyclerView, etc.)

class RotaFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentRotaBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var db: FirebaseFirestore
    private var googleMap: GoogleMap? = null

    // Class-level variable for waypoints
    private var waypoints: List<LatLng> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentRotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        db = FirebaseFirestore.getInstance()

        // Configurar o Spinner de turnos (substitua pelos seus turnos reais)
        val turnos = listOf("Manhã", "Tarde", "Noite")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, turnos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerTurno.adapter = adapter

        binding.buttonGerarRota.setOnClickListener { gerarRota() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        ) {
            googleMap.isMyLocationEnabled = true

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude,it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }
    private fun gerarRota() {
        val enderecoDestino = binding.editTextEnderecoDestino.text.toString()
        val turnoSelecionado = binding.spinnerTurno.selectedItem.toString()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val origin = LatLng(it.latitude, it.longitude)
                    db.collection("alunos")
                        .whereEqualTo("turno", turnoSelecionado)
                        .get()
                        .addOnSuccessListener { documents ->
                            var waypoints = documents.mapNotNull { document ->
                                val latitude = document.getDouble("latitude")
                                val longitude = document.getDouble("longitude")
                                if (latitude != null && longitude != null) {
                                    LatLng(latitude, longitude)
                                } else {
                                    null
                                }
                            }

                            // Chamar exibirRotaNoMapa aqui, dentro do addOnSuccessListener
                            GlobalScope.launch(Dispatchers.Main) {
                                val resultadoRota = withContext(Dispatchers.IO) {
                                    obterRota(origin, enderecoDestino, waypoints)
                                }
                                exibirRotaNoMapa(resultadoRota)
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Lidar com o erro de acesso ao Firestore
                        }
                }
            }
        }else {
            // Solicitar permissões de localização
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            // ...
        }
    }

    // ... (restante do código: obterRota(), exibirRotaNoMapa(), gerarLinkRota(), etc.)
    private suspend fun obterRota(
        origem: LatLng,
        destino: String,
        waypoints: List<LatLng>
    ): DirectionsResult? = withContext(Dispatchers.IO) {
        try {
            val context = GeoApiContext.Builder()
                .apiKey("AIzaSyBl563BA1QkQ78JZo-0gpm4d-Wik5Qa5B8")
                .build()

            DirectionsApi.newRequest(context)
                .mode(TravelMode.DRIVING)
                .origin(com.google.maps.model.LatLng(origem.latitude, origem.longitude))
                .destination(destino)
                .waypoints(*waypoints.map { com.google.maps.model.LatLng(it.latitude, it.longitude) }.toTypedArray())
                .optimizeWaypoints(true)
                .await()
        } catch (e: Exception) {
            // Lidar com erros na obtenção da rota
            null
        }
    }


    private fun exibirRotaNoMapa(resultado: DirectionsResult?) {
        googleMap?.clear() // Limpa o mapa antes de exibir a nova rota

        resultado?.routes?.firstOrNull()?.let { route ->
            val decodedPath = com.google.maps.android.PolyUtil.decode(route.overviewPolyline.encodedPath)
            googleMap?.addPolyline(com.google.android.gms.maps.model.PolylineOptions().addAll(decodedPath))

            // Adiciona marcadores para origem, destino e waypoints
            googleMap?.addMarker(MarkerOptions().position(decodedPath.first()).title("Origem"))
            googleMap?.addMarker(MarkerOptions().position(decodedPath.last()).title("Destino"))
            waypoints.forEach { googleMap?.addMarker(MarkerOptions().position(it)) }

            // Ajusta a câmera para mostrar toda a rota
            val bounds = com.google.android.gms.maps.model.LatLngBounds.Builder()
                .include(decodedPath.first())
                .include(decodedPath.last())
                .build()
            googleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

            // Cria o link compartilhável
            val link = gerarLinkRota(decodedPath)
            binding.textViewLinkRota.text = "Link da Rota: $link"
        }
    }

    private fun gerarLinkRota(pontos: List<LatLng>): String {
        val waypointsStr = pontos.joinToString("|") { "${it.latitude},${it.longitude}" }
        return "https://www.google.com/maps/dir/?api=1&origin=${pontos.first().latitude},${pontos.first().longitude}&destination=${pontos.last().latitude},${pontos.last().longitude}&waypoints=$waypointsStr"
    }
}
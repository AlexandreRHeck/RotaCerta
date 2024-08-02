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
import com.example.rotacerta.databinding.FragmentRotaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
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
import android.location.Location
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.GeocodingApi
import com.google.maps.model.GeocodingResult
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil

// ... (imports para o adaptador do RecyclerView, etc.)

class RotaFragment : Fragment(), OnMapReadyCallback {

    // Binding para acessar os elementos da interface
    private var _binding: FragmentRotaBinding? = null
    private val binding get() = _binding!!

    // Cliente para obter a localização do usuário
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // Instância do Firestore para acessar o banco de dados
    private lateinit var db: FirebaseFirestore

    // Objeto do GoogleMap que será inicializado quando o mapa estiver pronto
    private var googleMap: GoogleMap? = null


    // Lista de waypoints (pontos intermediários da rota)
    private var waypoints: List<LatLng> = emptyList()

    // Método chamado quando a View do Fragment é criada
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentRotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Método chamado após a View do Fragment ser criada
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa o MapView
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        // Inicializa o FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance()

        // Configurar o Spinner de turnos (substitua pelos seus turnos reais)
        val turnos = listOf("Manhã", "Tarde", "Noite")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, turnos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTurno.adapter = adapter

        // Define o listener para o botão "Gerar Rota"
        binding.buttonGerarRota.setOnClickListener { gerarRota() }
    }

    // Callback chamado quando o mapa está pronto para ser usado
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
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null){
                    db.collection("usuarios")
                        .document(userId)
                        .collection("cadastros")
                        .whereEqualTo("turno", turnoSelecionado)
                        .get()
                        .addOnSuccessListener { documents ->
                            // Mapear os documentos para waypoints usando corrotinas
                            GlobalScope.launch(Dispatchers.Main) {
                                val waypoints = withContext(Dispatchers.IO) {
                                    documents.mapNotNull { document ->
                                        val rua = document.getString("rua")
                                        val numero = document.getString("numero")
                                        val cidade = document.getString("cidade")
                                        val estado = document.getString("estado")

                                        if (rua != null && numero != null && cidade != null && estado != null) {
                                            val enderecoCompleto = "$rua, $numero - $cidade, $estado"
                                            geocodeEndereco(enderecoCompleto)
                                        } else {
                                            null
                                        }
                                    }
                                }

                                googleMap?.let { map ->
                                    GlobalScope.launch(Dispatchers.Main) {
                                        val resultadoRota = withContext(Dispatchers.IO) {
                                            obterRota(origin, enderecoDestino, waypoints)
                                        }

                                        // Convert waypoints to the correct LatLng type before using
                                        val convertedWaypoints = waypoints.map { LatLng(it.latitude, it.longitude) }
                                        exibirRotaNoMapa(resultadoRota, map) // Pass only resultadoRota and map

                                        // Display markers for the waypoints
                                        for (waypoint in convertedWaypoints) {
                                            map.addMarker(MarkerOptions().position(waypoint))
                                        }

                                        // Adjust the camera to show the entire route (including waypoints)
                                        val boundsBuilder = LatLngBounds.Builder()
                                        boundsBuilder.include(LatLng(origin.latitude, origin.longitude)) // Include origin
                                        boundsBuilder.include(LatLng(resultadoRota!!.routes[0].legs[0].endLocation.lat, resultadoRota.routes[0].legs[0].endLocation.lng)) // Include destination
                                        for (waypoint in convertedWaypoints) {
                                            boundsBuilder.include(waypoint) // Include each waypoint
                                        }
                                        val bounds = boundsBuilder.build()
                                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Lidar com o erro de acesso ao Firestore
                        }
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




    private fun exibirRotaNoMapa(resultado: DirectionsResult?, googleMap: GoogleMap) {
        googleMap.clear()

        resultado?.routes?.firstOrNull()?.let { route ->
            val decodedPath = PolyUtil.decode(route.overviewPolyline.encodedPath)

            // Convert com.google.maps.model.LatLng to com.google.android.gms.maps.model.LatLng
            val convertedPath = decodedPath.map { LatLng(it.latitude, it.longitude) }

            val polylineOptions = PolylineOptions().addAll(convertedPath)
            googleMap.addPolyline(polylineOptions)

            // Adiciona marcadores para origem e destino
            googleMap.addMarker(MarkerOptions().position(convertedPath.first()).title("Origem"))
            googleMap.addMarker(MarkerOptions().position(convertedPath.last()).title("Destino"))


            // Ajusta a câmera para mostrar toda a rota
            val boundsBuilder = LatLngBounds.Builder()
            for (point in convertedPath) {
                boundsBuilder.include(point)
            }
            val bounds = boundsBuilder.build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

            /*
            // Cria o link compartilhável
            val link = gerarLinkRota(convertedPath)
            binding.textViewLinkRota.text = "Link da Rota: $link"*/
        }
    }


        private fun gerarLinkRota(pontos: List<LatLng>): String {
            val waypointsStr = pontos.joinToString("|") { "${it.latitude},${it.longitude}" }
            return "https://www.google.com/maps/dir/?api=1&origin=${pontos.first().latitude},${pontos.first().longitude}&destination=${pontos.last().latitude},${pontos.last().longitude}&waypoints=$waypointsStr"
        }

        // Função para geocodificar o endereço (agora implementada)
        private suspend fun geocodeEndereco(endereco: String): LatLng? =
            withContext(Dispatchers.IO) {
                try {
                    val context = GeoApiContext.Builder()
                        .apiKey("AIzaSyBl563BA1QkQ78JZo-0gpm4d-Wik5Qa5B8")
                        .build()

                    val resultados: Array<GeocodingResult> =
                        GeocodingApi.geocode(context, endereco).await()
                    if (resultados.isNotEmpty()) {
                        val location = resultados[0].geometry.location
                        return@withContext LatLng(location.lat, location.lng)
                    } else {
                        return@withContext null // Endereço não encontrado
                    }
                } catch (e: Exception) {
                    // Lidar com erros na geocodificação
                    return@withContext null
                }
            }

        // ... (métodos de ciclo de vida do MapView)
    }
package com.example.rotacerta.fragments
import android.Manifest
import android.content.Intent
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
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.GeocodingApi
import com.google.maps.model.GeocodingResult
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil

class RotaFragment : Fragment(), OnMapReadyCallback {

    // Binding para acessar os elementos da interface
    private var _binding: FragmentRotaBinding? = null
    private val binding get() = _binding!!

    private var locationMarker: Marker? = null

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

        binding.buttonGerarRota.setOnClickListener {
            val enderecoDestino = binding.editTextEnderecoDestino.text.toString()

            var hasEmptyFields = false

            // Check if enderecoDestino is empty
            if (enderecoDestino.isBlank()) {
                binding.editTextEnderecoDestino.error = "Campo obrigatório"
                hasEmptyFields = true
            } else {
                binding.editTextEnderecoDestino.error = null
            }

            if (!hasEmptyFields) {
                gerarRota() // Call gerarRota only if there are no empty fields
            }
        }
    }

    // Callback chamado quando o mapa está pronto para ser usado
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        googleMap.isMyLocationEnabled = true

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
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val latLng = LatLng(it.latitude,
                it.longitude)
                // Add/update location marker
                if (locationMarker == null) {
                    locationMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title("Minha Localização")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                } else {
                    locationMarker?.position = latLng
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }

        // Enable map controls to fix the static issue
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isScrollGesturesEnabled = true
        googleMap.uiSettings.isZoomGesturesEnabled = true
    }


    private fun gerarRota() {
        val enderecoDestino = binding.editTextEnderecoDestino.text.toString()
        val turnoSelecionado = binding.spinnerTurno.selectedItem.toString()


        var hasEmptyFields = false

        // Check if enderecoDestino is empty
        if (enderecoDestino.isBlank()) {
            binding.editTextEnderecoDestino.error = "Campo obrigatório"
            hasEmptyFields = true
        } else {
            binding.editTextEnderecoDestino.error = null
        }

        if (hasEmptyFields) {
            return // Don't proceed if there are empty fields
        }

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

                                        resultadoRota?.let {  // Null check for resultadoRota
                                            val convertedWaypoints = waypoints.map { LatLng(it.latitude, it.longitude) }
                                            exibirRotaNoMapa(resultadoRota, map)

                                            // Display markers for the waypoints
                                            for (waypoint in convertedWaypoints) {
                                                map.addMarker(MarkerOptions().position(waypoint))
                                            }

                                            it.routes?.firstOrNull()?.let { route ->  // Null check for routes and firstOrNull()
                                                // Adjust the camera to show the entire route (including waypoints)
                                                val boundsBuilder = LatLngBounds.Builder()
                                                boundsBuilder.include(LatLng(origin.latitude, origin.longitude)) // Include origin

                                                route.legs?.lastOrNull()?.let { lastLeg ->  // Null check for legs and lastOrNull()
                                                    lastLeg.endLocation?.let { endLocation -> // Null check for endLocation
                                                        boundsBuilder.include(LatLng(endLocation.lat, endLocation.lng)) // Include destination
                                                    }
                                                }

                                                for (waypoint in convertedWaypoints) {
                                                    boundsBuilder.include(waypoint) // Include each waypoint
                                                }

                                                val bounds = boundsBuilder.build()
                                                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                                            }
                                        }
                                            ?: run {
                                                // Handle the case where no route is found
                                                Toast.makeText(requireContext(), "Ex: Rua Quinze de Novembro,123 -Centro, PortoAlegre, RS", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                }

                                // ... (rest of your code)
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

            val result =  DirectionsApi.newRequest(context)
                .mode(TravelMode.DRIVING)
                .origin(com.google.maps.model.LatLng(origem.latitude, origem.longitude))
                .destination(destino)
                .waypoints(*waypoints.map { com.google.maps.model.LatLng(it.latitude, it.longitude) }.toTypedArray())
                .optimizeWaypoints(true)
                .await()
            // Check if any routes were found
            if (result.routes.isEmpty()) {
                // No route found, handle the error gracefully
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Endereço incompleto ou inválido. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
                return@withContext null
            } else {
                return@withContext result
            }

        } catch (e: Exception) {
            // Handle other exceptions (e.g., network issues)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Verifique se o endereço está completo e tente novamente", Toast.LENGTH_LONG).show()
            }
            return@withContext null
        }
    }




    private fun exibirRotaNoMapa(resultado: DirectionsResult?, googleMap: GoogleMap) {
        googleMap.clear()

        resultado?.routes?.firstOrNull()?.let { route ->
            val decodedPath = PolyUtil.decode(route.overviewPolyline.encodedPath)
            val convertedPath = decodedPath.map { LatLng(it.latitude, it.longitude) }

            googleMap.addPolyline(PolylineOptions().addAll(convertedPath))
            googleMap.addMarker(MarkerOptions().position(convertedPath.first()).title("Origem"))
            googleMap.addMarker(MarkerOptions().position(convertedPath.last()).title("Destino"))

            val boundsBuilder = LatLngBounds.Builder()
            for (point in convertedPath) { boundsBuilder.include(point) }
            val bounds = boundsBuilder.build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

            // Get the destination address
            val destination = route.legs.last().endAddress

            // Make the "Navegar" button visible when the route is displayed
            binding.buttonNavegar.visibility = View.VISIBLE

            val waypoints = route.legs.flatMap { leg ->
                listOf(LatLng(leg.startLocation.lat, leg.startLocation.lng)) // Add the start location of each leg as a waypoint
            }.drop(1) // Remove the first waypoint (which is the origin)


            for (waypoint in waypoints) {
                googleMap.addMarker(MarkerOptions().position(waypoint).title("Parada"))
            }

            // Set onClickListener for the Navegar button
            binding.buttonNavegar.setOnClickListener {
                abrirNavegacao(route.legs.last().endAddress, waypoints) // Pass the waypoints
            }
        } ?: run {
            // Lidar com o caso em que não há rota (por exemplo, mostrar uma mensagem de erro)
            Toast.makeText(requireContext(), "Não foi possível calcular a rota", Toast.LENGTH_SHORT).show()
        }
    }
    private fun abrirNavegacao(destino: String, waypoints: List<LatLng>) {
        try {
            // Crie um URI com o destino e os waypoints formatados corretamente
            val waypointsStr = waypoints.joinToString("|") { "${it.latitude},${it.longitude}" }
            val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destino&waypoints=$waypointsStr")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            startActivity(mapIntent)
        } catch (e: Exception)
        {
            Toast.makeText(requireContext(), "Erro ao abrir o Google Maps", Toast.LENGTH_SHORT).show()
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
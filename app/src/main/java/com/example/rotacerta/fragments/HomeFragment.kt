package com.example.rotacerta.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
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
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { FirebaseFirestore.getInstance() }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null // Declare googleMap as a nullable property

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    private lateinit var locationCallback: LocationCallback

    private val iconeTempoMap = mapOf(
        // Clear
        Pair("céu limpo", "dia") to R.drawable.ceu_limpo_dia,
        Pair("céu limpo", "noite") to R.drawable.ceu_limpo_noite,

// Clouds
        Pair("poucas nuvens", "dia") to R.drawable.poucas_nuvens_dia,
        Pair("poucas nuvens", "noite") to R.drawable.poucas_nuvens_noite,

        /*
        Pair("nuvens dispersas", "dia") to R.drawable.nuvens_dispersas_dia,
        Pair("nuvens dispersas", "noite") to R.drawable.nuvens_dispersas_noite,
        Pair("nuvens quebradas", "dia") to R.drawable.nuvens_quebradas_dia,
        Pair("nuvens quebradas", "noite") to R.drawable.nuvens_quebradas_noite,
        Pair("nublado", "dia") to R.drawable.nublado_dia,
        Pair("nublado", "noite") to R.drawable.nublado_noite,

// Rain
        Pair("chuva leve", "dia") to R.drawable.chuva_leve_dia,
        Pair("chuva leve", "noite") to R.drawable.chuva_leve_noite,
        Pair("chuva moderada", "dia") to R.drawable.chuva_moderada_dia,
        Pair("chuva moderada", "noite") to R.drawable.chuva_moderada_noite,
        Pair("chuva forte", "dia") to R.drawable.chuva_forte_dia,
        Pair("chuva forte", "noite") to R.drawable.chuva_forte_noite,
        Pair("chuva de granizo", "dia") to R.drawable.chuva_de_granizo_dia,
        Pair("chuva de granizo", "noite") to R.drawable.chuva_de_granizo_noite,
        Pair("chuva leve e neve", "dia") to R.drawable.chuva_leve_e_neve_dia,
        Pair("chuva leve e neve", "noite") to R.drawable.chuva_leve_e_neve_noite,
        Pair("chuva e neve", "dia") to R.drawable.chuva_e_neve_dia,
        Pair("chuva e neve", "noite") to R.drawable.chuva_e_neve_noite,
        Pair("chuva congelante leve", "dia") to R.drawable.chuva_congelante_leve_dia,
        Pair("chuva congelante leve", "noite") to R.drawable.chuva_congelante_leve_noite,
        Pair("chuva congelante", "dia") to R.drawable.chuva_congelante_dia,
        Pair("chuva congelante", "noite") to R.drawable.chuva_congelante_noite,
        Pair("chuva congelante forte", "dia") to R.drawable.chuva_congelante_forte_dia,
        Pair("chuva congelante forte", "noite") to R.drawable.chuva_congelante_forte_noite,
        Pair("aguaceiros leves", "dia") to R.drawable.aguaceiros_leves_dia,
        Pair("aguaceiros leves", "noite") to R.drawable.aguaceiros_leves_noite,
        Pair("aguaceiros", "dia") to R.drawable.aguaceiros_dia,
        Pair("aguaceiros", "noite") to R.drawable.aguaceiros_noite,
        Pair("aguaceiros fortes", "dia") to R.drawable.aguaceiros_fortes_dia,
        Pair("aguaceiros fortes", "noite") to R.drawable.aguaceiros_fortes_noite,

// Snow
        Pair("neve leve", "dia") to R.drawable.neve_leve_dia,
        Pair("neve leve", "noite") to R.drawable.neve_leve_noite,
        Pair("neve", "dia") to R.drawable.neve_dia,
        Pair("neve", "noite") to R.drawable.neve_noite,
        Pair("neve forte", "dia") to R.drawable.neve_forte_dia,
        Pair("neve forte", "noite") to R.drawable.neve_forte_noite,
        Pair("granizo leve", "dia") to R.drawable.granizo_leve_dia,
        Pair("granizo leve", "noite") to R.drawable.granizo_leve_noite,
        Pair("granizo", "dia") to R.drawable.granizo_dia,
        Pair("granizo", "noite") to R.drawable.granizo_noite,
        Pair("granizo forte", "dia") to R.drawable.granizo_forte_dia,
        Pair("granizo forte", "noite") to R.drawable.granizo_forte_noite,
        Pair("neve leve e chuva", "dia") to R.drawable.neve_leve_e_chuva_dia,
        Pair("neve leve e chuva", "noite") to R.drawable.neve_leve_e_chuva_noite,
        Pair("neve e chuva", "dia") to R.drawable.neve_e_chuva_dia,
        Pair("neve e chuva", "noite") to R.drawable.neve_e_chuva_noite,
        Pair("neve forte e chuva", "dia") to R.drawable.neve_forte_e_chuva_dia,
        Pair("neve forte e chuva", "noite") to R.drawable.neve_forte_e_chuva_noite,

// Thunderstorm
        Pair("tempestade com chuva leve", "dia") to R.drawable.tempestade_com_chuva_leve_dia,
        Pair("tempestade com chuva leve", "noite") to R.drawable.tempestade_com_chuva_leve_noite,
        Pair("tempestade com chuva", "dia") to R.drawable.tempestade_com_chuva_dia,
        Pair("tempestade com chuva", "noite") to R.drawable.tempestade_com_chuva_noite,
        Pair("tempestade com chuva forte", "dia") to R.drawable.tempestade_com_chuva_forte_dia,
        Pair("tempestade com chuva forte", "noite") to R.drawable.tempestade_com_chuva_forte_noite,
        Pair("tempestade leve", "dia") to R.drawable.tempestade_leve_dia,
        Pair("tempestade leve", "noite") to R.drawable.tempestade_leve_noite,
        Pair("tempestade", "dia") to R.drawable.tempestade_dia,
        Pair("tempestade", "noite") to R.drawable.tempestade_noite,
        Pair("tempestade forte", "dia") to R.drawable.tempestade_forte_dia,
        Pair("tempestade forte", "noite") to R.drawable.tempestade_forte_noite,
        Pair("tempestade irregular", "dia") to R.drawable.tempestade_irregular_dia,
        Pair("tempestade irregular", "noite") to R.drawable.tempestade_irregular_noite,
        Pair("tempestade com chuva fraca", "dia") to R.drawable.tempestade_com_chuva_fraca_dia,
        Pair("tempestade com chuva fraca", "noite") to R.drawable.tempestade_com_chuva_fraca_noite,
        Pair("tempestade com garoa", "dia") to R.drawable.tempestade_com_garoa_dia,
        Pair("tempestade com garoa", "noite") to R.drawable.tempestade_com_garoa_noite,
        Pair("tempestade com garoa forte", "dia") to R.drawable.tempestade_com_garoa_forte_dia,
        Pair("tempestade com garoa forte", "noite") to R.drawable.tempestade_com_garoa_forte_noite,

// Drizzle
        Pair("garoa de intensidade leve", "dia") to R.drawable.garoa_intensidade_leve_dia,
        Pair("garoa de intensidade leve", "noite") to R.drawable.garoa_intensidade_leve_noite,
        Pair("garoa", "dia") to R.drawable.garoa_dia,
        Pair("garoa", "noite") to R.drawable.garoa_noite,
        Pair("garoa de forte intensidade", "dia") to R.drawable.garoa_forte_intensidade_dia,
        Pair("garoa de forte intensidade", "noite") to R.drawable.garoa_forte_intensidade_noite,
        Pair("garoa leve e chuva", "dia") to R.drawable.garoa_leve_e_chuva_dia,
        Pair("garoa leve e chuva", "noite") to R.drawable.garoa_leve_e_chuva_noite,
        Pair("garoa e chuva", "dia") to R.drawable.garoa_e_chuva_dia,
        Pair("garoa e chuva", "noite") to R.drawable.garoa_e_chuva_noite,
        Pair("garoa forte e chuva", "dia") to R.drawable.garoa_forte_e_chuva_dia,
        Pair("garoa forte e chuva", "noite") to R.drawable.garoa_forte_e_chuva_noite,
        Pair("aguaceiros e garoa", "dia") to R.drawable.aguaceiros_e_garoa_dia,
        Pair("aguaceiros e garoa", "noite") to R.drawable.aguaceiros_e_garoa_noite,
        Pair("aguaceiros fortes e garoa", "dia") to R.drawable.aguaceiros_fortes_e_garoa_dia,
        Pair("aguaceiros fortes e garoa", "noite") to R.drawable.aguaceiros_fortes_e_garoa_noite,
        Pair("garoa de aguaceiros", "dia") to R.drawable.garoa_de_aguaceiros_dia,
        Pair("garoa de aguaceiros", "noite") to R.drawable.garoa_de_aguaceiros_noite,

// Atmosphere
        Pair("neblina", "dia") to R.drawable.neblina_dia,
        Pair("neblina", "noite") to R.drawable.neblina_noite,
        Pair("fumaça", "dia") to R.drawable.fumaca_dia,
        Pair("fumaça", "noite") to R.drawable.fumaca_noite,
        Pair("névoa seca", "dia") to R.drawable.nevoa_seca_dia,
        Pair("névoa seca", "noite") to R.drawable.nevoa_seca_noite,
        Pair("poeira", "dia") to R.drawable.poeira_dia,
        Pair("poeira", "noite") to R.drawable.poeira_noite,
        Pair("nevoeiro", "dia") to R.drawable.nevoeiro_dia,
        Pair("nevoeiro", "noite") to R.drawable.nevoeiro_noite,
        Pair("areia", "dia") to R.drawable.areia_dia,
        Pair("areia", "noite") to R.drawable.areia_noite,
        Pair("cinza", "dia") to R.drawable.cinza_dia,
        Pair("cinza", "noite") to R.drawable.cinza_noite,
        Pair("tornado", "dia") to R.drawable.tornado_dia,
        Pair("tornado", "noite") to R.drawable.tornado_noite,
        Pair("redemoinho de poeira", "dia") to R.drawable.redemoinho_poeira_dia,
        Pair("redemoinho de poeira", "noite") to R.drawable.redemoinho_poeira_noite,
        Pair("redemoinho de areia", "dia") to R.drawable.redemoinho_areia_dia,
        Pair("redemoinho de areia", "noite") to R.drawable.redemoinho_areia_noite,
        Pair("calmaria", "dia") to R.drawable.calmaria_dia,
        Pair("calmaria", "noite") to R.drawable.calmaria_noite,
        Pair("vento", "dia") to R.drawable.vento_dia,
        Pair("vento", "noite") to R.drawable.vento_noite,
        Pair("furacão", "dia") to R.drawable.furacao_dia,
        Pair("furacão", "noite") to R.drawable.furacao_noite,
        Pair("frio", "dia") to R.drawable.frio_dia,
        Pair("frio", "noite") to R.drawable.frio_noite,
        Pair("quente", "dia") to R.drawable.quente_dia,
        Pair("quente", "noite") to R.drawable.quente_noite,
        Pair("tempo seco", "dia") to R.drawable.tempo_seco_dia,
        Pair("tempo seco", "noite") to R.drawable.tempo_seco_noite,
        Pair("tempo úmido", "dia") to R.drawable.tempo_umido_dia,
        Pair("tempo úmido", "noite") to R.drawable.tempo_umido_noite

        */

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

        val nomeUsuario = obterNomeUsuario()
        binding.textBoasVindas.text = "Bem-vindo, $nomeUsuario!"

        obterLocalizacaoEBuscarTempo()
    }

    private fun obterNomeUsuario(): String {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        return if (userId != null) {
            var nomeUsuario = "" // Valor padrão caso não encontre no Firestore ou ocorra um erro

            val userDocRef = db.collection("usuarios").document(userId)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userData = document.data
                        nomeUsuario = userData?.get("nomeMotorista") as? String ?: "Usuário"
                        // Atualiza a saudação com o nome obtido (se disponível)
                       binding.textBoasVindas.text = "Bem-vindo, $nomeUsuario!"
                    }
                }
                .addOnFailureListener { exception ->
                    // Lidar com erros na busca de dados (opcional)
                }

            nomeUsuario // Retorna o nome, mesmo que a busca no Firestore ainda esteja em andamento
        } else {
            "Usuário" // Retorna "Usuário" se não estiver logado
        }
    }

    private fun obterLocalizacaoEBuscarTempo() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED

        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            // You might want to adjust interval and fastestInterval for your needs
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations)
                {
                    val latitude = location.latitude
                    val longitude = location.longitude


                    // Get city name using Geocoder
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)


                    val firstAddressLine = addresses?.get(0)?.getAddressLine(0)

                    // Regex modificado para capturar a parte desejada
                    val cityNameRegex = Regex(",\\s*\\d+\\s*-\\s*([^,]+),\\s*([^\\s-]+)")
                    val matchResult = cityNameRegex.find(firstAddressLine ?: "")

                    // Extraia os dois grupos capturados e combine-os
                    val cityName = addresses?.get(0)?.locality ?: (matchResult?.groupValues?.get(1) ?: "") + ", " + (matchResult?.groupValues?.get(2) ?: "")

                    Log.d("LocationData", "City Name: $cityName")
                    binding.textLocalizacao.text = cityName


                    buscarDadosTempo(latitude, longitude)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        fusedLocationProviderClient.lastLocation

        .addOnSuccessListener { location:
            Location? ->
            location?.let {
                val latitude = it.latitude
                val longitude = it.longitude
                buscarDadosTempo(latitude, longitude)
                buscarPrevisao3Horas(latitude, longitude)
            } ?: run {
                // Lidar com o caso em que a localização é nula
                binding.textSituacaoTempo.text = "Verifique se o GPS esta ligado!"
            }
        }
            .addOnFailureListener { exception ->
                // Lidar com erros ao obter a localização
                binding.textSituacaoTempo.text = "Erro ao obter a localização: ${exception.message}"
            }
    }

    private fun buscarPrevisao3Horas(latitude: Double, longitude: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&appid=6601b16013c44333071e70f296e346ea&units=metric&lang=pt_br")
                    .build()

                try {
                    val response = client.newCall(request).execute()
                    val jsonData = response.body?.string()
                    withContext(Dispatchers.Main) {
                        if (jsonData != null) {
                            val jsonObject = JSONObject(jsonData)

                            // Obter as previsões para as próximas 3 horas
                            val previsao3Horas = jsonObject.getJSONArray("list").let { list ->
                                listOf(list.getJSONObject(0), list.getJSONObject(1))
                            }

                            // Encontrar as temperaturas mínima e máxima
                            val tempMin = previsao3Horas.minOf { it.getJSONObject("main").getDouble("temp_min") }
                            val tempMax = previsao3Horas.maxOf { it.getJSONObject("main").getDouble("temp_max") }

                            // Atualizar apenas os campos de min e max na UI
                            binding.textMinima.text = "Min: ${tempMin.toInt()}°"
                            binding.textMaxima.text = "Max: ${tempMax.toInt()}°"
                        }
                    }
                } catch (e: Exception) {
                    // Lidar com erros na requisição (opcional)
                    Log.e("WeatherData", "Erro ao buscar previsão de 3 horas: ${e.message}")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback) //pode acessar locationCallback aqui
    }



    private fun buscarDadosTempo(latitude: Double, longitude: Double) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }
                    withContext(Dispatchers.IO) {
                        val client = OkHttpClient()
                        val request = Request.Builder()
                            .url("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&appid=6601b16013c44333071e70f296e346ea&units=metric&lang=pt_br")
                            .build()

                        try {
                            val response = client.newCall(request).execute()
                            val jsonData = response.body?.string()
                            withContext(Dispatchers.Main) {
                                if (jsonData != null) {

                                    Log.d("WeatherData", "JSON Response: $jsonData")

                                    val jsonObject = JSONObject(jsonData)
                                    // Extrair dados relevantes do JSON
                                    val descricaoTempo =
                                        jsonObject.getJSONArray("weather").getJSONObject(0).getString("description")
                                    val temperatura = jsonObject.getJSONObject("main").getDouble("temp")
                                    //val tempMin = jsonObject.getJSONObject("main").getDouble("temp_min")
                                   // val tempMax = jsonObject.getJSONObject("main").getDouble("temp_max")
                                   // val nomeCidade = jsonObject.getString("name")
                                    val nascerSol = jsonObject.getJSONObject("sys").getLong("sunrise") * 1000
                                    val porSol = jsonObject.getJSONObject("sys").getLong("sunset") * 1000



                                    // Calcular período do dia
                                    val horarioAtual = System.currentTimeMillis()
                                    val periodo =
                                        if (horarioAtual in nascerSol..porSol) "dia" else "noite"

                                    // Obter o ID do recurso de imagem e atualizar o ImageView
                                    val iconeResourceId = getIconResourceId(descricaoTempo, periodo)
                                    binding.iconeTempo.setImageResource(iconeResourceId)

                                    // Atualizar outros elementos da UI
                                    //binding.textMinima.text = "Min: ${tempMin.toInt()}°"
                                   // binding.textMaxima.text = "Max: ${tempMax.toInt()}°"
                                   // binding.textLocalizacao.text = nomeCidade
                                    binding.textSituacaoTempo.text = descricaoTempo
                                    binding.textTemperatura.text = "${temperatura.toInt()}°C"

                                    // Alterar o background de acordo com o período
                                    val meioDia = (nascerSol + porSol) / 2
                                    val backgroundResourceId = when {
                                        horarioAtual in nascerSol until meioDia -> R.drawable.bg_home_dia
                                        horarioAtual in meioDia until porSol -> R.drawable.bg_home_tarde
                                        else -> R.drawable.bg_home_noite
                                    }
                                    binding.root.setBackgroundResource(backgroundResourceId)

                                } else {
                                    binding.textSituacaoTempo.text =
                                        "Erro ao buscar dados do tempo (resposta nula)"
                                }
                                binding.progressBar.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                binding.textSituacaoTempo.text =
                                    "Erro ao buscar dados do tempo: ${e.message}"
                                binding.progressBar.visibility = View.GONE
                            }
                        }
                    }
                }

                // Agendar a próxima execução após 1 minuto (60000 milissegundos)
                handler.postDelayed(this, 60000)
            }
        }

        // Iniciar a atualização periódica
        handler.post(runnable)
    }


    private fun getIconResourceId(descricaoTempo: String, periodo: String): Int {
        return iconeTempoMap[Pair(descricaoTempo, periodo)] ?: R.drawable.ic_padrao
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

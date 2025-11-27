package com.example.agrogesf.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.PestType
import com.example.agrogesf.data.models.RaspberryDetection
import com.example.agrogesf.data.models.TransferStatus
import com.example.agrogesf.data.repository.AgroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

class WifiDataTransferManager(
    private val context: Context,
    private val repository: AgroRepository
) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _transferStatus = MutableStateFlow<TransferStatus>(TransferStatus.Idle)
    val transferStatus: StateFlow<TransferStatus> = _transferStatus

    private val _isConnectedToDataWifi = MutableStateFlow(false)
    val isConnectedToDataWifi: StateFlow<Boolean> = _isConnectedToDataWifi
    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    // ⭐ NOVO: Estado do script (se está rodando ou não)
    private val _isScriptRunning = MutableStateFlow(false)
    val isScriptRunning: StateFlow<Boolean> = _isScriptRunning

    private val targetSSID = "AGRO_GESF_DATA"
    private val dataServerUrl = "http://192.168.4.1:8080"

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            checkWifiConnection()
        }

        override fun onLost(network: Network) {
            _isConnectedToDataWifi.value = false
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            checkWifiConnection()
        }
    }

    init {
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun checkWifiConnection() {
        try {
            val wifiInfo: WifiInfo? = wifiManager.connectionInfo
            val ssid = wifiInfo?.ssid?.replace("\"", "") ?: ""

            _isConnectedToDataWifi.value = ssid == targetSSID

            if (_isConnectedToDataWifi.value) {
                // ✅ NOVO: Força usar esta rede
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bindToWifiNetwork()
                }

                CoroutineScope(Dispatchers.IO).launch {
                    checkScriptStatus()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun bindToWifiNetwork() {
        connectivityManager.allNetworks.forEach { network ->
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
                val wifiInfo = wifiManager.connectionInfo
                val ssid = wifiInfo?.ssid?.replace("\"", "") ?: ""

                if (ssid == targetSSID) {
                    connectivityManager.bindProcessToNetwork(network)
                    Log.d("WifiManager", "✅ Android forçado a usar a rede WiFi Direct")
                    return
                }
            }
        }
    }

    suspend fun startScript(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = sendControlSignal(true)
            _isScriptRunning.value = true
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun stopScript(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = sendControlSignal(false)
            _isScriptRunning.value = false
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleScript(): Result<String> {
        return if (_isScriptRunning.value) {
            stopScript()
        } else {
            startScript()
        }
    }

        suspend fun checkScriptStatus(): Boolean = withContext(Dispatchers.IO) {
            try {
                val response = fetchDataFromServer("/api/status")
                val json = JSONObject(response)
                val isRunning = json.getBoolean("is_running")
                _isScriptRunning.value = isRunning
                isRunning
            } catch (e: Exception) {
                Log.w("WifiManager", "Não foi possível verificar status: ${e.message}")
                false
            }
        }


    private fun sendControlSignal(signal: Boolean): String {
        val url = "$dataServerUrl/api/control?signal=$signal"

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ByteArray(0)))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Server returned code: ${response.code}")
            }
            return response.body?.string() ?: "OK"
        }
    }

    suspend fun startDataTransfer() {
        if (_transferStatus.value is TransferStatus.Transferring) {
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _transferStatus.value = TransferStatus.Connecting

                val pestsData = fetchDataFromServer("/api/pests")
                val pestsArray = JSONArray(pestsData)

                val totalItems = pestsArray.length()
                val pests = mutableListOf<Pest>()

                _transferStatus.value = TransferStatus.Transferring(0, "Baixando informações...")

                for (i in 0 until totalItems) {
                    val pestJson = pestsArray.getJSONObject(i)

                    val images = mutableListOf<String>()
                    val imagesArray = pestJson.getJSONArray("images")

                    for (j in 0 until imagesArray.length()) {
                        val imageUrl = imagesArray.getString(j)
                        val imagePath = downloadImage(imageUrl, "${pestJson.getString("id")}_$j.jpg")
                        images.add(imagePath)

                        val progress = ((i * imagesArray.length() + j + 1) * 100) / (totalItems * imagesArray.length())
                        _transferStatus.value = TransferStatus.Transferring(
                            progress,
                            "Baixando imagem ${j + 1}/${imagesArray.length()} de ${pestJson.getString("name")}"
                        )
                    }

                    val pest = Pest(
                        id = pestJson.getString("id"),
                        name = pestJson.getString("name"),
                        description = pestJson.getString("description"),
                        images = images,
                        type = if (pestJson.getString("type") == "PRAGA")
                            PestType.PRAGA
                        else
                            PestType.DOENCA
                    )

                    pests.add(pest)
                }

                _transferStatus.value = TransferStatus.Transferring(95, "Salvando detecções...")
                _transferStatus.value = TransferStatus.Success(pests.size)

            } catch (e: Exception) {
                e.printStackTrace()
                _transferStatus.value = TransferStatus.Error(e.message ?: "Erro na transferência")
            }
        }
    }

    private fun fetchDataFromServer(endpoint: String): String {
        val url = "$dataServerUrl$endpoint"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Server returned code: ${response.code}")
            }
            return response.body?.string() ?: throw IOException("Empty response body")
        }
    }
    suspend fun fetchRaspberryDetections(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Log.d("WifiManager", "🔍 Iniciando busca de detecções...")
            _transferStatus.value = TransferStatus.Transferring(0, "Buscando detecções...")

            // 1. Buscar da API
            Log.d("WifiManager", "📡 Fazendo requisição para /api/pests")
            val response = fetchDataFromServer("/api/pests")
            Log.d("WifiManager", "📦 Resposta recebida: ${response.take(200)}...") // Log parcial

            // ✅ CORRIGIDO: O retorno é um array direto, não um objeto
            val jsonArray = JSONArray(response)
            val total = jsonArray.length()

            Log.d("WifiManager", "📊 Total de detecções: $total")

            if (total == 0) {
                Log.d("WifiManager", "⚠️ Nenhuma detecção encontrada")
                _transferStatus.value = TransferStatus.Success(0)
                return@withContext Result.success(0)
            }

            val detections = mutableListOf<RaspberryDetection>()

            for (i in 0 until total) {
                val json = jsonArray.getJSONObject(i)
                Log.d("WifiManager", "📝 Processando detecção ${i + 1}/$total: ${json.getString("name")}")

                // Pegar a primeira imagem do array de imagens
                val imagesArray = json.getJSONArray("images")
                val imageFilename = if (imagesArray.length() > 0) {
                    imagesArray.getString(0).removePrefix("/images/")
                } else {
                    ""
                }

                val imagePath = if (imageFilename.isNotEmpty()) {
                    try {
                        Log.d("WifiManager", "🖼️ Baixando imagem: $imageFilename")
                        downloadImage("/images/$imageFilename", imageFilename)
                    } catch (e: Exception) {
                        Log.e("WifiManager", "❌ Erro ao baixar imagem: ${e.message}")
                        ""
                    }
                } else {
                    ""
                }

                val detection = RaspberryDetection(
                    pestId = json.getString("id"),
                    pestName = json.optString("name_en", json.getString("name")),
                    pestType = json.optString("type", "PRAGA"),
                    confidence = json.optDouble("confidence", 0.0).toFloat(),
                    imagePath = imagePath,
                    detectedAt = parseTimestamp(json.optString("timestamp", ""))
                )

                detections.add(detection)

                val progress = ((i + 1) * 100) / total
                _transferStatus.value = TransferStatus.Transferring(
                    progress,
                    "Baixando detecção ${i + 1}/$total"
                )
            }

            // 4. Salvar no banco
            if (detections.isNotEmpty()) {
                Log.d("WifiManager", "💾 Salvando ${detections.size} detecções no banco...")
                repository.saveRaspberryDetections(detections)
                Log.d("WifiManager", "✅ Detecções salvas com sucesso!")
            }

            _transferStatus.value = TransferStatus.Success(detections.size)
            Result.success(detections.size)

        } catch (e: Exception) {
            Log.e("WifiManager", "❌ ERRO: ${e.message}", e)
            e.printStackTrace()
            _transferStatus.value = TransferStatus.Error(e.message ?: "Erro ao buscar detecções")
            Result.failure(e)
        }
    }
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            if (timestamp.isEmpty()) {
                System.currentTimeMillis()
            } else {
                // Formato: "2025-11-27T01:42:04.589788"
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                sdf.parse(timestamp.substringBefore("."))?.time ?: System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    private fun downloadImage(imageUrl: String, filename: String): String {
        val url = "$dataServerUrl$imageUrl"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to download image: ${response.code}")
            }

            val imageBytes = response.body?.bytes()
                ?: throw IOException("Empty image response")

            return repository.saveImageLocally(imageBytes, filename)
        }
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun resetStatus() {
        _transferStatus.value = TransferStatus.Idle
    }

}
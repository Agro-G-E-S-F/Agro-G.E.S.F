package com.example.agrogesf.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.example.agrogesf.data.models.Pest
import com.example.agrogesf.data.models.TransferStatus
import com.example.agrogesf.data.repository.AgroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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
                CoroutineScope(Dispatchers.IO).launch {
                    startDataTransfer()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun startDataTransfer() {
        if (_transferStatus.value is TransferStatus.Transferring) {
            return
        }

        withContext(Dispatchers.IO) {
            try {
                _transferStatus.value = TransferStatus.Connecting

                // 1. Buscar lista de pragas disponíveis
                val pestsData = fetchDataFromServer("/api/pests")
                val pestsArray = JSONArray(pestsData)

                val totalItems = pestsArray.length()
                val pests = mutableListOf<Pest>()

                _transferStatus.value = TransferStatus.Transferring(0, "Baixando informações...")

                for (i in 0 until totalItems) {
                    val pestJson = pestsArray.getJSONObject(i)

                    // 2. Baixar imagens para cada praga
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

                    // 3. Criar objeto Pest
                    val pest = Pest(
                        id = pestJson.getString("id"),
                        name = pestJson.getString("name"),
                        description = pestJson.getString("description"),
                        images = images,
                        type = if (pestJson.getString("type") == "PRAGA")
                            com.example.agrogesf.data.models.PestType.PRAGA
                        else
                            com.example.agrogesf.data.models.PestType.DOENCA
                    )

                    pests.add(pest)
                }

                // 4. ⭐ MUDANÇA: Salvar APENAS como detecções, NÃO no glossário
                _transferStatus.value = TransferStatus.Transferring(95, "Salvando detecções...")

                // ❌ REMOVIDO: repository.savePestsFromWifi(pests)
                // ✅ NOVO: Salvar apenas como detecções temporárias


                _transferStatus.value = TransferStatus.Success(pests.size)

            } catch (e: Exception) {
                e.printStackTrace()
                _transferStatus.value = TransferStatus.Error(e.message ?: "Erro na transferência")
            }
        }
    }

    private fun fetchDataFromServer(endpoint: String): String {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$dataServerUrl$endpoint")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                return response.toString()
            } else {
                throw Exception("Server returned code: $responseCode")
            }
        } finally {
            connection?.disconnect()
        }
    }

    private fun downloadImage(imageUrl: String, filename: String): String {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$dataServerUrl$imageUrl")
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val imageBytes = connection.inputStream.readBytes()
            return repository.saveImageLocally(imageBytes, filename)

        } finally {
            connection?.disconnect()
        }
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun resetStatus() {
        _transferStatus.value = TransferStatus.Idle
    }
}
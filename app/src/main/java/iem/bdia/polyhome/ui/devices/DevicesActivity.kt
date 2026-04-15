package iem.bdia.polyhome.ui.devices

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import iem.bdia.polyhome.R
import iem.bdia.polyhome.data.local.TokenStorage
import iem.bdia.polyhome.data.remote.api.RetrofitInstance
import iem.bdia.polyhome.data.remote.repository.DeviceRepoImpl
import iem.bdia.polyhome.domain.model.Command
import iem.bdia.polyhome.utils.Result
import kotlinx.coroutines.launch

class DevicesActivity : AppCompatActivity() {

    private lateinit var viewModel: DevicesViewModel
    private lateinit var houseId: String
    private lateinit var tokenStorage: TokenStorage
    private lateinit var deviceAdapter: DeviceAdapter

    private lateinit var lvDevices: ListView
    private lateinit var tvTitle: TextView
    private lateinit var btnBack: View
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmpty: View
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_devices)

        houseId = intent.getStringExtra("houseId").toString()
        tokenStorage = TokenStorage(this)

        lvDevices = findViewById(R.id.devicesListView)
        tvTitle = findViewById(R.id.tvTitle)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        webView = findViewById(R.id.houseWebView)

        val repo = DeviceRepoImpl(RetrofitInstance.apiService)
        viewModel = DevicesViewModel(repo)

        tvTitle.text = "Maison #$houseId"

        deviceAdapter = DeviceAdapter(this) { deviceId, command ->
            lifecycleScope.launch {
                onDeviceCommandSent(deviceId, command)
            }
        }
        lvDevices.adapter = deviceAdapter
        setupWebView()

        btnBack.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            loadDevices()
        }
        setupGlobalActions()
        observeViewModel()
        findViewById<ChipGroup>(R.id.chipGroupFilters).setOnCheckedStateChangeListener { group, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipLights -> DevicesViewModel.FILTER.LIGHT
                R.id.chipShutters -> DevicesViewModel.FILTER.SHUTTER
                R.id.chipGarage -> DevicesViewModel.FILTER.GARAGE
                else -> DevicesViewModel.FILTER.ALL
            }
            viewModel.setFilter(filter)
        }

    }

    private fun setupGlobalActions() {
        findViewById<MaterialSwitch>(R.id.switchAllLights).setOnCheckedChangeListener { _, isChecked ->
            val command = if (isChecked) "TURN ON" else "TURN OFF"
            sendGlobalCommand("LIGHT", command)
        }

        findViewById<MaterialSwitch>(R.id.switchAllBlinds).setOnCheckedChangeListener { _, isChecked ->
            val command = if (isChecked) "OPEN" else "CLOSE"
            sendGlobalCommand("SHUTTER", command)
        }

        findViewById<View>(R.id.btnAllBlindsStop).setOnClickListener {
            sendGlobalCommand("SHUTTER", "STOP")
        }
    }

    private fun sendGlobalCommand(category: String, command: String) {
        val devices = viewModel.devices.value
        val targets = devices.filter {
            val isLight = it.type.contains("light", ignoreCase = true)
            val isShutter = it.type.contains("shutter", ignoreCase = true)

            (category == "LIGHT" && isLight) || (category == "SHUTTER" && isShutter)
        }

        if (targets.isEmpty()) {
            Toast.makeText(this, "Aucun équipement de ce type trouvé", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            targets.forEach { device ->
                if (device.availableCommands.contains(command)) {
                    onDeviceCommandSent(device.id, command)
                }
            }
        }
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                findViewById<View>(R.id.cardWebView).visibility = View.VISIBLE
            }
        }
        webView.loadUrl("https://polyhome.lesmoulinsdudev.com?houseId=$houseId")
    }

    private suspend fun loadDevices() {
        val token = tokenStorage.getToken()
        if (token != null) {
            viewModel.loadDevices(token, houseId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.loading.collect { isLoading ->
                        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.error.collect { errorMessage ->
                        if (errorMessage != null) {
                            Toast.makeText(this@DevicesActivity, errorMessage, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }

                launch {
                    viewModel.filterFlow.collect { devices ->
                        deviceAdapter.updateDevices(devices)
                        val isEmpty = devices.isEmpty() && !viewModel.loading.value
                        lvDevices.visibility = if (isEmpty) View.GONE else View.VISIBLE
                        layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private suspend fun onDeviceCommandSent(deviceId: String, command: String) {
        val token = tokenStorage.getToken()
        if (token != null) {
            val cmd = Command(command)
            when (val result = viewModel.sendCommand(token, houseId, deviceId, cmd)) {
                is Result.Success -> {
                    viewModel.updateDeviceStateLocal(deviceId, command)
                }

                is Result.Error -> {
                    Toast.makeText(
                        this@DevicesActivity,
                        "Erreur: ${result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}

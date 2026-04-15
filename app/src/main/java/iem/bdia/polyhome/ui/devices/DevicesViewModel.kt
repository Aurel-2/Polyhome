package iem.bdia.polyhome.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import iem.bdia.polyhome.domain.model.Command
import iem.bdia.polyhome.domain.model.Device
import iem.bdia.polyhome.domain.repository.DeviceRepository
import iem.bdia.polyhome.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DevicesViewModel(private val deviceRepository: DeviceRepository) : ViewModel() {

    enum class FILTER { ALL, LIGHT, SHUTTER, GARAGE }

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()


    private val _filterDevices = MutableStateFlow(FILTER.ALL)
    val filterDevice: StateFlow<FILTER> = _filterDevices.asStateFlow()

    val filterFlow: StateFlow<List<Device>> = combine(_devices, _filterDevices) { devices, filter ->
        when (filter) {
            FILTER.ALL -> devices
            FILTER.LIGHT -> devices.filter { it.type.contains("Light", ignoreCase = true) }
            FILTER.SHUTTER -> devices.filter { it.type.contains("Shutter", ignoreCase = true) }
            FILTER.GARAGE -> devices.filter { it.type.contains("Garage", ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun setFilter(filter: FILTER) {
        _filterDevices.value = filter
    }


    fun loadDevices(token: String?, houseId: String) {
        viewModelScope.launch {
            if (token == null) {
                _error.value = "Session expirée, veuillez vous reconnecter"
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            when (val result = deviceRepository.getDevices(token, houseId)) {
                is Result.Success -> {
                    _devices.value = result.data
                }

                is Result.Error -> {
                    _error.value = result.message
                    _devices.value = emptyList()
                }
            }

            _isLoading.value = false
        }
    }

    suspend fun sendCommand(
        token: String,
        houseId: String,
        deviceId: String,
        command: Command
    ): Result<Unit> {
        return deviceRepository.sendCommand(token, houseId, deviceId, command)
    }

    fun updateDeviceStateLocal(deviceId: String, commandText: String) {
        val currentList = _devices.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == deviceId }
        if (index != -1) {
            val device = currentList[index]
            val updatedDevice =
                when (commandText) {
                    "TURN ON", "OPEN" -> device.copy(power = 1, opening = 1)
                    "TURN OFF", "CLOSE" -> device.copy(power = 0, opening = 0)
                    else -> device
                }
            currentList[index] = updatedDevice
            _devices.value = currentList
        }
    }
}
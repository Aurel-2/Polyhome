package iem.bdia.polyhome.domain.repository

import iem.bdia.polyhome.domain.model.Command
import iem.bdia.polyhome.domain.model.Device
import iem.bdia.polyhome.utils.Result

interface DeviceRepository {
    suspend fun getDevices(token: String, houseId: String): Result<List<Device>>
    suspend fun sendCommand(token: String, houseId: String, deviceId: String, command: Command): Result<Unit>
}
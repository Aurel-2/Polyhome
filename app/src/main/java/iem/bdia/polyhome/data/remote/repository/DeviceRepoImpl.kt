package iem.bdia.polyhome.data.remote.repository

import iem.bdia.polyhome.data.remote.api.ApiService
import iem.bdia.polyhome.domain.model.Command
import iem.bdia.polyhome.domain.model.Device
import iem.bdia.polyhome.domain.repository.DeviceRepository
import iem.bdia.polyhome.utils.Result
import iem.bdia.polyhome.utils.safeApiCall

class DeviceRepoImpl(private val api: ApiService) : DeviceRepository {

    override suspend fun getDevices(token: String, houseId: String): Result<List<Device>> =
        safeApiCall(
            messages = HttpErrorMessage(
                unauthorized = "Session expirée, reconnectez-vous",
                badRequest = "Les données fournies sont incorrectes",
                forbidden = "Accès interdit",
                notFound = "Aucun équipement trouvé",
                serverError = "Une erreur s'est produite au niveau du serveur"
            )
        )
        {
            val response = api.getDevices(houseId, "Bearer $token")
            if (response.isSuccessful) {
                response.body()?.devices ?: emptyList()
            } else {
                throw retrofit2.HttpException(response)
            }
        }

    override suspend fun sendCommand(
        token: String,
        houseId: String,
        deviceId: String,
        command: Command
    ): Result<Unit> =
        safeApiCall(
            messages = HttpErrorMessage(
                unauthorized = "Session expirée, reconnectez-vous",
                notFound = "Équipement introuvable",
                serverError = "Erreur lors de l'envoi de la commande"
            )
        )
        {
            val response = api.sendCommand(houseId, deviceId, "Bearer $token", command)
            if (!response.isSuccessful) {
                throw retrofit2.HttpException(response)
            }
        }
}
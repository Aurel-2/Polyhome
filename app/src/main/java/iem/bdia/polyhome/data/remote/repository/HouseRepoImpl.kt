package iem.bdia.polyhome.data.remote.repository

import iem.bdia.polyhome.data.remote.api.ApiService
import iem.bdia.polyhome.domain.model.House
import iem.bdia.polyhome.domain.model.HouseUser
import iem.bdia.polyhome.domain.repository.HouseRepository
import iem.bdia.polyhome.utils.Result
import iem.bdia.polyhome.utils.safeApiCall

class HouseRepoImpl(private val api: ApiService) : HouseRepository {

    override suspend fun getHouses(token: String): Result<List<House>> =
        safeApiCall(
            messages = HttpErrorMessage(
                unauthorized = "Session expirée, reconnectez-vous",
                notFound = "Aucune maison trouvée"
            )
        )
        {
            api.getHouses("Bearer $token").body() ?: emptyList()
        }

    override suspend fun givePermission(
        houseId: String,
        token: String,
        houseUser: HouseUser
    ): Result<Unit> =
        safeApiCall(
            messages = HttpErrorMessage(
                unauthorized = "Session expirée, reconnectez-vous",
                notFound = "Maison introuvable",
                conflict = "Cet utilisateur a déjà accès à cette maison"
            )
        )
        {
            api.givePermission(houseId, "Bearer $token", houseUser)
        }

    override suspend fun removePermission(
        houseId: String,
        token: String,
        houseUser: HouseUser
    ): Result<Unit> =
        safeApiCall(
            messages = HttpErrorMessage(
                unauthorized = "Session expirée, reconnectez-vous",
                notFound = "Utilisateur ou maison introuvable"
            )
        )
        {
            api.removePermission(houseId, "Bearer $token", houseUser)
        }

    override suspend fun getHouseUsers(
        houseId: String,
        token: String
    ): Result<List<HouseUser>> =
        safeApiCall(
            messages = HttpErrorMessage(
                unauthorized = "Session expirée, reconnectez-vous",
                notFound = "Aucun utilisateur trouvé pour cette maison"
            )
        ) {
            api.getHouseUsers(houseId, "Bearer $token").body() ?: emptyList()
        }
}
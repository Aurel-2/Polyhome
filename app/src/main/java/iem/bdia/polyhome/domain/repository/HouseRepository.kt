package iem.bdia.polyhome.domain.repository

import iem.bdia.polyhome.domain.model.House
import iem.bdia.polyhome.domain.model.HouseUser
import iem.bdia.polyhome.utils.Result

interface HouseRepository {
    suspend fun getHouses(token: String): Result<List<House>>
    suspend fun givePermission(houseId: String, token: String, houseUser: HouseUser): Result<Unit>
    suspend fun removePermission(houseId: String, token: String, houseUser: HouseUser): Result<Unit>
    suspend fun getHouseUsers(houseId: String, token: String): Result<List<HouseUser>>
}
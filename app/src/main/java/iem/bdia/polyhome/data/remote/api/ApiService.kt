package iem.bdia.polyhome.data.remote.api

import iem.bdia.polyhome.domain.model.Command
import iem.bdia.polyhome.domain.model.DeviceList
import iem.bdia.polyhome.domain.model.House
import iem.bdia.polyhome.domain.model.HouseUser
import iem.bdia.polyhome.domain.model.Login
import iem.bdia.polyhome.domain.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // ---- Users
    @POST("users/register")
    suspend fun register(@Body request: User): Response<Unit>

    @POST("users/auth")
    suspend fun login(@Body request: User): Response<Login>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>


    // ---- House
    @GET("houses")
    suspend fun getHouses(@Header("Authorization") token: String): Response<List<House>>

    @POST("houses/{houseId}/users")
    suspend fun givePermission(
        @Path("houseId") houseId: String,
        @Header("Authorization") token: String,
        @Body request: HouseUser
    ): Response<Unit>

    // Simple delete ne fonctionne pas car pas de donnée dans le body
    @HTTP(method = "DELETE", path = "houses/{houseId}/users", hasBody = true)
    suspend fun removePermission(
        @Path("houseId") houseId: String,
        @Header("Authorization") token: String,
        @Body request: HouseUser
    ): Response<Unit>

    @GET("houses/{houseId}/users")
    suspend fun getHouseUsers(
        @Path("houseId") houseId: String,
        @Header("Authorization") token: String
    ): Response<List<HouseUser>>


    // ---- Device
    @GET("houses/{houseId}/devices")
    suspend fun getDevices(
        @Path("houseId") houseId: String,
        @Header("Authorization") token: String
    ): Response<DeviceList>

    @POST("houses/{houseId}/devices/{deviceId}/command")
    suspend fun sendCommand(
        @Path("houseId") houseId: String,
        @Path("deviceId") deviceId: String,
        @Header("Authorization") token: String,
        @Body request: Command
    ): Response<Unit>

}

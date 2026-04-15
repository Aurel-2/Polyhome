package iem.bdia.polyhome.data.remote.repository

import iem.bdia.polyhome.data.remote.api.ApiService
import iem.bdia.polyhome.domain.model.Login
import iem.bdia.polyhome.domain.model.User
import iem.bdia.polyhome.domain.repository.UserRepository
import iem.bdia.polyhome.utils.Result
import iem.bdia.polyhome.utils.safeApiCall
import retrofit2.Response

class UserRepoImpl(private val api: ApiService) : UserRepository {

    override suspend fun register(user: User): Result<Unit> =
        safeApiCall(messages = HttpErrorMessage(conflict = "Un compte avec cet email existe déjà")) {
            handleResponse(api.register(user))
        }

    override suspend fun login(user: User): Result<Login> =
        safeApiCall(
            messages = HttpErrorMessage(
                unauthorized = "Email ou mot de passe incorrect",
                notFound = "Aucun compte trouvé avec cet email"
            )
        ) {
            handleResponse(api.login(user)) ?: throw Exception("Réponse vide du serveur")
        }

    override suspend fun getUsers(): Result<List<User>> =
        safeApiCall(messages = HttpErrorMessage(unauthorized = "Session expirée, reconnectez-vous")) {
            handleResponse(api.getUsers()) ?: emptyList()
        }

    private fun <T> handleResponse(response: Response<T>): T? {
        if (!response.isSuccessful) {
            throw retrofit2.HttpException(response)
        }
        return response.body()
    }
}
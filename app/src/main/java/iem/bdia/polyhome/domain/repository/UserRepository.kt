package iem.bdia.polyhome.domain.repository

import iem.bdia.polyhome.domain.model.Login
import iem.bdia.polyhome.domain.model.User
import iem.bdia.polyhome.utils.Result

interface UserRepository {
    suspend fun register(user: User): Result<Unit>
    suspend fun login(user: User): Result<Login>
    suspend fun getUsers(): Result<List<User>>
}
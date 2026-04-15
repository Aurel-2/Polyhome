package iem.bdia.polyhome.utils

import iem.bdia.polyhome.data.remote.repository.HttpErrorMessage
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


fun parseHttpError(e: Exception, messages: HttpErrorMessage = HttpErrorMessage()): Pair<String, Int?> {
    return when (e) {
        is HttpException -> {
            val message = when (e.code()) {
                400 -> messages.badRequest
                401 -> messages.unauthorized
                403 -> messages.forbidden
                404 -> messages.notFound
                409 -> messages.conflict
                500 -> messages.serverError
                else -> "HTTP error ${e.code()}"
            }
            Pair(message, e.code())
        }

        is UnknownHostException -> Pair(messages.noInternet, null)
        is SocketTimeoutException -> Pair(messages.timeout, null)
        is IOException -> Pair(messages.noInternet, null)
        else -> Pair(messages.unknown, null)
    }
}

suspend fun <T> safeApiCall(messages: HttpErrorMessage = HttpErrorMessage(), call: suspend () -> T): Result<T> {
    return try {
        Result.Success(call())
    } catch (e: Exception) {
        val (message, code) = parseHttpError(e, messages)
        Result.Error(message, code)
    }
}
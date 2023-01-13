package ru.netology.nework.error

import android.database.SQLException
import okhttp3.ResponseBody
import java.io.IOException

sealed class AppError(var status:Int =-1,var code: String) : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbError
            is IOException -> NetworkError
            else -> UnknownError
        }
    }
}

class ApiError(status: Int, code: String) : AppError(code=code)
class ApiError2(status:Int, code: String, val responseBody: ResponseBody?) : AppError(status,code)
object NetworkError : AppError(code="error_network")
object DbError : AppError(code="error_db")
object UnknownError : AppError(code="error_unknown")



package com.selfgrowthfund.sgf.utils

/**
 * A sealed class representing success, error, or loading states.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Extension property to check if the result is a success.
 */
val <T> Result<T>.isSuccess: Boolean
    get() = this is Result.Success

/**
 * Extension property to check if the result is an error.
 */
val <T> Result<T>.isError: Boolean
    get() = this is Result.Error

/**
 * Extension property to safely extract the data from a success result.
 */
val <T> Result<T>.dataOrNull: T?
    get() = (this as? Result.Success)?.data

/**
 * Extension property to safely extract the exception from an error result.
 */
val Result<*>.exceptionOrNull: Throwable?
    get() = (this as? Result.Error)?.exception

/**
 * Maps the success value to another type, preserving error/loading.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    Result.Loading -> Result.Loading
}

/**
 * FlatMaps the result to another Result, useful for chaining.
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
    is Result.Success -> transform(data)
    is Result.Error -> this
    Result.Loading -> Result.Loading
}
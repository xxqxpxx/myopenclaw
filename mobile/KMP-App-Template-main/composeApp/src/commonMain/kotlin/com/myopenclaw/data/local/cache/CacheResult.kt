package com.myopenclaw.data.local.cache

/**
 * Represents the result of a cache-aware data fetch operation.
 *
 * This sealed class supports the "stale-while-revalidate" pattern:
 * 1. Return cached data immediately if available
 * 2. Fetch fresh data in the background
 * 3. Update the UI when fresh data arrives
 *
 * @param T The type of data being cached
 */
sealed class CacheResult<out T> {
    /**
     * Data is fresh (either from cache that hasn't expired, or just fetched from network).
     */
    data class Fresh<T>(
        val data: T
    ) : CacheResult<T>()

    /**
     * Data is from cache but may be stale. A background refresh is in progress.
     */
    data class Stale<T>(
        val data: T,
        val isRefreshing: Boolean = true,
        val cacheAgeMs: Long = 0
    ) : CacheResult<T>()

    /**
     * Currently loading data. May contain previously cached data.
     */
    data class Loading<T>(
        val cachedData: T? = null
    ) : CacheResult<T>()

    /**
     * Error occurred while fetching data. May contain cached fallback data.
     */
    data class Error<T>(
        val exception: Throwable,
        val cachedData: T? = null,
        val message: String = exception.message ?: "Unknown error"
    ) : CacheResult<T>()

    /**
     * Returns the data if available, regardless of freshness.
     */
    fun dataOrNull(): T? = when (this) {
        is Fresh -> data
        is Stale -> data
        is Loading -> cachedData
        is Error -> cachedData
    }

    /**
     * Returns true if data is available (fresh, stale, or cached).
     */
    fun hasData(): Boolean = dataOrNull() != null

    /**
     * Returns true if currently refreshing in background.
     */
    fun isCurrentlyRefreshing(): Boolean = when (this) {
        is Stale -> isRefreshing
        is Loading -> true
        else -> false
    }

    /**
     * Returns true if there was an error.
     */
    fun isError(): Boolean = this is Error

    /**
     * Map the data to another type while preserving the cache state.
     */
    fun <R> map(transform: (T) -> R): CacheResult<R> = when (this) {
        is Fresh -> Fresh(transform(data))
        is Stale -> Stale(transform(data), isRefreshing, cacheAgeMs)
        is Loading -> Loading(cachedData?.let(transform))
        is Error -> Error(exception, cachedData?.let(transform), message)
    }

    companion object {
        /**
         * Create a Loading state with optional cached data.
         */
        fun <T> loading(cachedData: T? = null): CacheResult<T> = Loading(cachedData)

        /**
         * Create a Fresh state with the given data.
         */
        fun <T> fresh(data: T): CacheResult<T> = Fresh(data)

        /**
         * Create a Stale state with the given data.
         */
        fun <T> stale(
            data: T,
            isRefreshing: Boolean = true,
            cacheAgeMs: Long = 0
        ): CacheResult<T> = Stale(data, isRefreshing, cacheAgeMs)

        /**
         * Create an Error state with the given exception and optional cached fallback.
         */
        fun <T> error(
            exception: Throwable,
            cachedData: T? = null
        ): CacheResult<T> = Error(exception, cachedData)
    }
}

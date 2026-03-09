package com.myopenclaw.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.header
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.myopenclaw.util.AppLogger

object HttpClientFactory {
    @Suppress("OPT_IN_USAGE")
    fun create(
        getAuthToken: suspend () -> String? = { null }
    ): HttpClient {
        return HttpClient {
            // JSON Serialization
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    explicitNulls = false
                    encodeDefaults = false
                })
            }

            // Logging disabled for production
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        AppLogger.d("API", message)
                    }
                }
                level = LogLevel.NONE
            }

            // Timeout with proper values
            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.DEFAULT_TIMEOUT
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = ApiConfig.DEFAULT_TIMEOUT
            }

            // Default headers
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }

            // Auth
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = getAuthToken()
                        if (token != null) {
                            BearerTokens(token, "")
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        val newToken = getAuthToken()
                        if (newToken != null) {
                            BearerTokens(newToken, "")
                        } else {
                            null
                        }
                    }
                }
            }

            // Retry
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                retryOnException(maxRetries = 2, retryOnTimeout = true)
                exponentialDelay()
            }

            // Handle HTTP errors
            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value
                    if (statusCode >= 400) {
                        AppLogger.e("API", "HTTP Error: $statusCode ${response.status.description} from ${response.call.request.url}")
                    }
                }
                handleResponseExceptionWithRequest { exception, request ->
                    AppLogger.e("API", "Exception for ${request.url}: ${exception.message}")
                }
            }
        }
    }
}

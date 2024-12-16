package naveed.sample.currencyconversion.core.domain.util

import naveed.sample.crypto.core.domain.util.Error

enum class NetworkError: Error {
    REQUEST_TIMEOUT,
    TOO_MANY_REQUESTS,
    NO_INTERNET,
    SERVER_ERROR,
    SERIALIZATION,
    UNKNOWN,
}
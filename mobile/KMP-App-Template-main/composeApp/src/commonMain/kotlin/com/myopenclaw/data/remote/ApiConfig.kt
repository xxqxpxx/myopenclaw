package com.myopenclaw.data.remote

object ApiConfig {
    var BASE_URL = "https://myopenclaw-production-810e.up.railway.app"
        private set

    fun setBaseUrl(url: String) {
        BASE_URL = url
    }

    const val DEFAULT_TIMEOUT = 30_000L
    const val STREAM_TIMEOUT = 300_000L  // 5 min for SSE streaming

    object Stripe {
        var PRICE_WEEKLY = ""
            private set
        var PRICE_MONTHLY = ""
            private set
        var PRICE_QUARTERLY = ""
            private set

        fun configure(weekly: String, monthly: String, quarterly: String) {
            PRICE_WEEKLY = weekly
            PRICE_MONTHLY = monthly
            PRICE_QUARTERLY = quarterly
        }

        const val SUCCESS_URL = "myopenclaw://checkout/success"
        const val CANCEL_URL = "myopenclaw://checkout/cancel"
    }

    object Firebase {
        var WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
            private set

        fun setWebClientId(clientId: String) {
            WEB_CLIENT_ID = clientId
        }
    }
}

package com.example.api

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PicaSignature {
    const val API_KEY = "C69BAF41DA5ABD1FFEDC6D2FEA56B"
    // 63-character secret key for PicACG API authentication
    private const val SECRET_KEY = "~d}\$Q7\$eIni=V)9\\RK/P.RM4;9[7|@/CA}b~OW!3?EV`:<>M7pddUBL5n|0/*Cn"

    fun calculate(pathAndQuery: String, time: Long, nonce: String, method: String): String {
        val cleanPath = pathAndQuery.removePrefix("/")
        val raw = (cleanPath + time.toString() + nonce + method + API_KEY).lowercase()
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), "HmacSHA256")
            mac.init(keySpec)
            val hash = mac.doFinal(raw.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
}

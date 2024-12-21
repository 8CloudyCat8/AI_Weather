package com.example.myapplication

import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HelperClass {

    suspend fun sendRequest(userRequest: String): String {
        return suspendCancellableCoroutine { continuation ->
            val gson = Gson()

            val prompt = mapOf(
                "modelUri" to "gpt://b1gmsmbhqnhsm5445hli/yandexgpt-lite",
                "completionOptions" to mapOf(
                    "stream" to false,
                    "temperature" to 0.6,
                    "maxTokens" to "2000"
                ),
                "messages" to listOf(
                    mapOf(
                        "role" to "user",
                        "text" to userRequest
                    )
                )
            )

            val url = "https://llm.api.cloud.yandex.net/foundationModels/v1/completion"
            val apiKey = "AQVN1RTl2FdsS2NTXxC9i2OA-u-sedo-ftpge8LJ"

            val client = OkHttpClient()

            val requestBody = gson.toJson(prompt)
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Api-Key $apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val resultData = gson.fromJson(response.body?.string(), Map::class.java)
                        val textResponse = (resultData["result"] as Map<*, *>)["alternatives"]
                            .let { (it as List<*>)[0] as Map<*, *> }
                            .let { it["message"] as Map<*, *> }
                            .let { it["text"] as String }

                        continuation.resume(textResponse)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            })

            continuation.invokeOnCancellation {
                client.dispatcher.cancelAll()
            }
        }
    }
}

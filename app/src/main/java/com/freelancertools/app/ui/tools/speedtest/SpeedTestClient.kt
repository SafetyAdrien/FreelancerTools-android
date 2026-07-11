package com.freelancertools.app.ui.tools.speedtest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class SpeedTestResult(val pingMs: Long, val downloadMbps: Double, val uploadMbps: Double)

/**
 * Uses Cloudflare's public, key-free speed-test endpoints (the same ones used by
 * speed.cloudflare.com) to measure round-trip latency and download/upload throughput.
 */
object SpeedTestClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun run(onProgress: (String) -> Unit): SpeedTestResult = withContext(Dispatchers.IO) {
        onProgress("Mesure du ping...")
        val ping = measurePing()

        onProgress("Test de téléchargement...")
        val download = measureDownload(10_000_000)

        onProgress("Test d'envoi...")
        val upload = measureUpload(4_000_000)

        SpeedTestResult(pingMs = ping, downloadMbps = download, uploadMbps = upload)
    }

    private fun measurePing(): Long {
        val samples = (1..4).map {
            val start = System.nanoTime()
            val request = Request.Builder().url("https://speed.cloudflare.com/__down?bytes=0").build()
            client.newCall(request).execute().use { it.body?.bytes() }
            (System.nanoTime() - start) / 1_000_000
        }
        return samples.sorted()[samples.size / 2]
    }

    private fun measureDownload(bytes: Int): Double {
        val request = Request.Builder().url("https://speed.cloudflare.com/__down?bytes=$bytes").build()
        val start = System.nanoTime()
        var totalBytes = 0L
        client.newCall(request).execute().use { response ->
            val source = response.body?.source() ?: return 0.0
            val buffer = Buffer()
            while (true) {
                val read = source.read(buffer, 65536)
                if (read == -1L) break
                totalBytes += read
                buffer.clear()
            }
        }
        val seconds = (System.nanoTime() - start) / 1_000_000_000.0
        if (seconds <= 0) return 0.0
        return (totalBytes * 8 / 1_000_000.0) / seconds
    }

    private fun measureUpload(bytes: Int): Double {
        val payload = Random.nextBytes(bytes)
        val body = payload.toRequestBody("application/octet-stream".toMediaType())
        val request = Request.Builder().url("https://speed.cloudflare.com/__up").post(body).build()
        val start = System.nanoTime()
        val success = runCatching { client.newCall(request).execute().use { it.isSuccessful } }.getOrDefault(false)
        val seconds = (System.nanoTime() - start) / 1_000_000_000.0
        if (!success || seconds <= 0) return 0.0
        return (bytes * 8 / 1_000_000.0) / seconds
    }
}

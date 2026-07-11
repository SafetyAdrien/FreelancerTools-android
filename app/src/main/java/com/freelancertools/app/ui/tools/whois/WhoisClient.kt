package com.freelancertools.app.ui.tools.whois

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * Queries the WHOIS protocol (RFC 3912, TCP port 43) directly — no third-party HTTP API or key
 * needed, and the raw text response matches the tool's "brut" output requirement exactly.
 */
object WhoisClient {

    private const val PORT = 43
    private const val TIMEOUT_MS = 8000

    // Fallback map for common TLDs in case the IANA referral step fails.
    private val KNOWN_SERVERS = mapOf(
        "com" to "whois.verisign-grs.com",
        "net" to "whois.verisign-grs.com",
        "org" to "whois.pir.org",
        "io" to "whois.nic.io",
        "dev" to "whois.nic.google",
        "app" to "whois.nic.google",
        "co" to "whois.nic.co",
        "info" to "whois.afilias.net",
    )

    suspend fun lookup(domain: String): String = withContext(Dispatchers.IO) {
        val clean = domain.trim().lowercase().removePrefix("http://").removePrefix("https://").substringBefore("/")
        val tld = clean.substringAfterLast(".", "")

        val server = runCatching { findAuthoritativeServer(clean) }.getOrNull()
            ?: KNOWN_SERVERS[tld]
            ?: "whois.iana.org"

        query(server, clean)
    }

    private fun findAuthoritativeServer(domain: String): String? {
        val response = query("whois.iana.org", domain)
        val referLine = response.lineSequence().firstOrNull { it.startsWith("refer:", ignoreCase = true) }
        return referLine?.substringAfter(":")?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun query(server: String, domain: String): String {
        return try {
            Socket().use { socket ->
                socket.connect(java.net.InetSocketAddress(server, PORT), TIMEOUT_MS)
                socket.soTimeout = TIMEOUT_MS
                val writer = OutputStreamWriter(socket.getOutputStream())
                writer.write("$domain\r\n")
                writer.flush()
                BufferedReader(InputStreamReader(socket.getInputStream())).readText()
            }
        } catch (e: SocketTimeoutException) {
            "Le serveur WHOIS ($server) n'a pas répondu à temps."
        }
    }
}

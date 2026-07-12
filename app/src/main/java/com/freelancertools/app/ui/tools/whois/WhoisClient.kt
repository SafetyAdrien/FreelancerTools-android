package com.freelancertools.app.ui.tools.whois

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Queries the WHOIS protocol (RFC 3912, TCP port 43) directly — no third-party HTTP API or key
 * needed, and the raw text response matches the tool's "brut" output requirement exactly.
 *
 * WHOIS has no TLS variant, so cleartext socket traffic must be explicitly allowed via
 * res/xml/network_security_config.xml — without it, Android blocks the raw Socket connection
 * outright on API 28+ (targetSdk 28+ defaults to cleartextTrafficPermitted=false for every
 * networking API, not just HTTP libraries), which is the most likely cause of an instant crash
 * on every lookup rather than just "some" domains.
 */
object WhoisClient {

    private const val PORT = 43
    private const val TIMEOUT_MS = 10_000
    private const val OVERALL_TIMEOUT_MS = 20_000L
    private const val MAX_RESPONSE_CHARS = 20_000

    class WhoisException(message: String, cause: Throwable? = null) : Exception(message, cause)

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
        if (clean.isBlank() || !clean.contains(".")) {
            throw WhoisException("Domaine invalide.")
        }
        val tld = clean.substringAfterLast(".", "")

        try {
            withTimeout(OVERALL_TIMEOUT_MS) {
                val server = runCatching { findAuthoritativeServer(clean) }.getOrNull()
                    ?: KNOWN_SERVERS[tld]
                    ?: "whois.iana.org"

                val raw = query(server, clean)
                if (raw.length > MAX_RESPONSE_CHARS) {
                    raw.take(MAX_RESPONSE_CHARS) + "\n\n… Résultat tronqué (réponse trop longue)."
                } else {
                    raw
                }
            }
        } catch (e: TimeoutCancellationException) {
            // A slow-trickling server can keep resetting the per-read soTimeout without ever
            // hitting it; this hard ceiling guarantees the lookup always ends in a clean error
            // instead of hanging indefinitely.
            throw WhoisException("Délai dépassé, réessayez.", e)
        }
    }

    private fun findAuthoritativeServer(domain: String): String? {
        val response = query("whois.iana.org", domain)
        val referLine = response.lineSequence().firstOrNull { it.startsWith("refer:", ignoreCase = true) }
        return referLine?.substringAfter(":")?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun query(server: String, domain: String): String {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(server, PORT), TIMEOUT_MS)
                socket.soTimeout = TIMEOUT_MS
                OutputStreamWriter(socket.getOutputStream()).apply {
                    write("$domain\r\n")
                    flush()
                }
                return readBounded(socket, MAX_RESPONSE_CHARS)
            }
        } catch (e: UnknownHostException) {
            throw WhoisException("Serveur WHOIS introuvable ($server).", e)
        } catch (e: SocketTimeoutException) {
            throw WhoisException("Le serveur WHOIS ($server) n'a pas répondu à temps.", e)
        } catch (e: IOException) {
            throw WhoisException("Erreur réseau lors de la connexion à $server.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Catches anything not covered above (e.g. IllegalArgumentException from a malformed
            // "refer:" hostname parsed out of a WHOIS response) so it surfaces as a clean, catchable
            // WhoisException instead of an unclassified RuntimeException.
            throw WhoisException("Erreur inattendue lors de la requête WHOIS.", e)
        }
    }

    /** Reads at most [maxChars] characters so a chatty/misbehaving server can't exhaust memory. */
    private fun readBounded(socket: Socket, maxChars: Int): String {
        val reader = InputStreamReader(socket.getInputStream())
        val buffer = CharArray(4096)
        val builder = StringBuilder()
        while (builder.length < maxChars) {
            val read = reader.read(buffer)
            if (read == -1) break
            builder.append(buffer, 0, read)
        }
        return builder.toString()
    }
}

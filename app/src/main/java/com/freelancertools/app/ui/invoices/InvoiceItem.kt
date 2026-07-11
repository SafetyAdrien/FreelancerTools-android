package com.freelancertools.app.ui.invoices

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class InvoiceLineItem(
    val description: String,
    val price: Double,
)

private val json = Json { ignoreUnknownKeys = true }

fun List<InvoiceLineItem>.toJson(): String = json.encodeToString(this)

fun String.toInvoiceLineItems(): List<InvoiceLineItem> =
    runCatching { json.decodeFromString<List<InvoiceLineItem>>(this) }.getOrDefault(emptyList())

package com.freelancertools.app.ui.invoices

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Renders an invoice to a single-page A4 PDF using the native PdfDocument API (no third-party dependency). */
object InvoicePdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 @ 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 48f

    fun generate(
        context: Context,
        invoiceNumber: Int,
        issuerName: String,
        clientName: String?,
        items: List<InvoiceLineItem>,
        totalHT: Double,
        createdAt: Long,
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val currency = NumberFormat.getCurrencyInstance(Locale.FRANCE)
        val dateText = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date(createdAt))

        val issuerPaint = Paint().apply { color = Color.BLACK; textSize = 16f; isFakeBoldText = true }
        val accentPaint = Paint().apply { color = Color.parseColor("#FF3B5C"); textSize = 26f; isFakeBoldText = true }
        val labelPaint = Paint().apply { color = Color.parseColor("#8A8A8A"); textSize = 11f }
        val bodyPaint = Paint().apply { color = Color.BLACK; textSize = 12f }
        val boldPaint = Paint().apply { color = Color.BLACK; textSize = 13f; isFakeBoldText = true }
        val linePaint = Paint().apply { color = Color.parseColor("#E5E5E5"); strokeWidth = 1f }

        var y = MARGIN + 16f
        canvas.drawText(issuerName.ifBlank { "Votre société" }, MARGIN, y, issuerPaint)
        canvas.drawText("Facture #$invoiceNumber", MARGIN, y + 18f, labelPaint)
        canvas.drawText(dateText, MARGIN, y + 34f, labelPaint)

        val accentText = "FACTURE"
        val accentWidth = accentPaint.measureText(accentText)
        canvas.drawText(accentText, PAGE_WIDTH - MARGIN - accentWidth, y, accentPaint)

        y += 70f
        canvas.drawText("À l'attention de :", MARGIN, y, labelPaint)
        y += 16f
        canvas.drawText(clientName?.ifBlank { "Client" } ?: "Client", MARGIN, y, boldPaint)

        y += 40f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 24f
        canvas.drawText("Description", MARGIN, y, labelPaint)
        canvas.drawText("Prix", PAGE_WIDTH - MARGIN - 60f, y, labelPaint)
        y += 12f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 22f

        items.forEach { item ->
            canvas.drawText(item.description.ifBlank { "Prestation" }, MARGIN, y, bodyPaint)
            val priceText = currency.format(item.price)
            val priceWidth = bodyPaint.measureText(priceText)
            canvas.drawText(priceText, PAGE_WIDTH - MARGIN - priceWidth, y, bodyPaint)
            y += 24f
        }

        y = PAGE_HEIGHT - MARGIN - 60f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 24f
        canvas.drawText("Total HT", MARGIN, y, labelPaint)
        val totalText = currency.format(totalHT)
        val totalPaint = Paint().apply { color = Color.parseColor("#FF3B5C"); textSize = 20f; isFakeBoldText = true }
        val totalWidth = totalPaint.measureText(totalText)
        canvas.drawText(totalText, PAGE_WIDTH - MARGIN - totalWidth, y + 6f, totalPaint)

        document.finishPage(page)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "facture-$invoiceNumber.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun uriFor(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

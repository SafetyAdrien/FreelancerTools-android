package com.freelancertools.app.ui.tools.fontlibrary

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.freelancertools.app.R

enum class FontKind(val label: String) {
    ALL("Toutes"),
    MONO("Monospace"),
    SERIF("Serif"),
    SANS("Sans-Serif"),
}

data class FontCatalogEntry(val name: String, val category: FontKind, val fontFamily: FontFamily)

/**
 * Certificate hashes for the Google Play Services fonts provider, declared in
 * res/values/font_certs.xml (same content used by Google's own Compose samples) rather than
 * pulled from play-services-basement's R class, which avoids depending on that library at all.
 */
@OptIn(ExperimentalTextApi::class)
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

/** Built once per process and reused — a fresh FontFamily instance per recomposition would just
 *  re-trigger Compose's async font resolution for no reason. */
@OptIn(ExperimentalTextApi::class)
private fun googleFontFamily(name: String): FontFamily = FontFamily(
    androidx.compose.ui.text.googlefonts.Font(googleFont = GoogleFont(name), fontProvider = googleFontProvider),
)

/** ~30 varied Google Fonts, cached on-device by the platform's Fonts Provider after first use. */
val FONT_CATALOG: List<FontCatalogEntry> = listOf(
    "Roboto" to FontKind.SANS,
    "Open Sans" to FontKind.SANS,
    "Lato" to FontKind.SANS,
    "Montserrat" to FontKind.SANS,
    "Poppins" to FontKind.SANS,
    "Inter" to FontKind.SANS,
    "Nunito" to FontKind.SANS,
    "Raleway" to FontKind.SANS,
    "Rubik" to FontKind.SANS,
    "Work Sans" to FontKind.SANS,

    "Playfair Display" to FontKind.SERIF,
    "Merriweather" to FontKind.SERIF,
    "Lora" to FontKind.SERIF,
    "PT Serif" to FontKind.SERIF,
    "Source Serif Pro" to FontKind.SERIF,
    "Crimson Text" to FontKind.SERIF,
    "Libre Baskerville" to FontKind.SERIF,
    "EB Garamond" to FontKind.SERIF,
    "Cormorant" to FontKind.SERIF,
    "Bitter" to FontKind.SERIF,

    "Roboto Mono" to FontKind.MONO,
    "Source Code Pro" to FontKind.MONO,
    "JetBrains Mono" to FontKind.MONO,
    "Space Mono" to FontKind.MONO,
    "IBM Plex Mono" to FontKind.MONO,
    "Fira Code" to FontKind.MONO,
    "Inconsolata" to FontKind.MONO,
    "Courier Prime" to FontKind.MONO,
    "Ubuntu Mono" to FontKind.MONO,
    "DM Mono" to FontKind.MONO,
).map { (name, kind) -> FontCatalogEntry(name, kind, googleFontFamily(name)) }

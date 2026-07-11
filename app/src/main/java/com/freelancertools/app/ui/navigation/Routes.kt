package com.freelancertools.app.ui.navigation

/** Central registry of every navigation destination in the app. */
object Routes {
    // General
    const val DASHBOARD = "dashboard"
    const val FINANCES = "finances"
    const val CLIENTS = "clients"
    const val CLIENT_DETAIL = "clients/{clientId}"
    const val INVOICES = "invoices"
    const val INVOICE_EDITOR = "invoices/editor?invoiceId={invoiceId}"
    const val SETTINGS = "settings"
    const val MANAGE_TOOLS = "settings/manage_tools"

    fun clientDetail(clientId: String) = "clients/$clientId"
    fun invoiceEditor(invoiceId: String? = null) = "invoices/editor?invoiceId=${invoiceId ?: ""}"

    // Business
    const val ROI_CALCULATOR = "tools/roi_calculator"

    // Images
    const val IMAGE_OPTIMIZER = "tools/image_optimizer"
    const val BACKGROUND_REMOVAL = "tools/background_removal"
    const val EXIF_CLEANER = "tools/exif_cleaner"
    const val FAVICON_GENERATOR = "tools/favicon_generator"
    const val WATERMARK = "tools/watermark"

    // Dev
    const val META_TAGS = "tools/meta_tags"
    const val MARKDOWN_PREVIEW = "tools/markdown_preview"
    const val WHOIS_LOOKUP = "tools/whois_lookup"
    const val EMBED_VISUALIZER = "tools/embed_visualizer"
    const val SPEED_TEST = "tools/speed_test"
    const val DIFF_CHECKER = "tools/diff_checker"
    const val CONVERTER = "tools/converter"

    // Design
    const val PALETTES = "tools/palettes"
    const val FONT_LIBRARY = "tools/font_library"
    const val FONT_PAIRER = "tools/font_pairer"
    const val SCALE_CALCULATOR = "tools/scale_calculator"
    const val BLOB_MAKER = "tools/blob_maker"

    // Utilities
    const val QR_CODES = "tools/qr_codes"
    const val LOREM_IPSUM = "tools/lorem_ipsum"
    const val SMART_TIMER = "tools/smart_timer"
    const val VIDEO_CUTTER = "tools/video_cutter"
    const val PROMPT_MANAGER = "tools/prompt_manager"
}

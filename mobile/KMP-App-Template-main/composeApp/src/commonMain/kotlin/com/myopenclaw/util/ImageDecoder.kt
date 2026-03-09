package com.myopenclaw.util

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decode a base64-encoded image string to an ImageBitmap.
 * Platform-specific implementations handle the actual decoding.
 */
expect fun decodeBase64ToImageBitmap(base64: String): ImageBitmap?

package com.myopenclaw.util

/**
 * Result of picking/capturing an image
 */
data class ImagePickerResult(
    val imageBase64: String,
    val mimeType: String,
    val fileName: String? = null
)

/**
 * Platform-specific image picker for camera and gallery access
 * Used for chart analysis feature
 */
expect class ImagePicker {
    /**
     * Check if camera is available on this device
     */
    fun isCameraAvailable(): Boolean

    /**
     * Launch camera to capture an image
     * @param onResult Callback with the captured image as base64 or null if cancelled/failed
     */
    fun launchCamera(onResult: (ImagePickerResult?) -> Unit)

    /**
     * Launch gallery/photo picker to select an image
     * @param onResult Callback with the selected image as base64 or null if cancelled/failed
     */
    fun launchGallery(onResult: (ImagePickerResult?) -> Unit)
}

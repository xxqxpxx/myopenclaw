package com.myopenclaw.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Android implementation of ImagePicker
 * Note: This requires registering result launchers in the Activity.
 * Call registerActivityResultLaunchers() from onCreate() before setContent().
 */
actual class ImagePicker(private val context: Context) {

    private var cameraLauncher: ActivityResultLauncher<Uri>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private var permissionLauncher: ActivityResultLauncher<String>? = null

    private var currentPhotoUri: Uri? = null
    private var pendingCameraResult: ((ImagePickerResult?) -> Unit)? = null
    private var pendingGalleryResult: ((ImagePickerResult?) -> Unit)? = null
    private var pendingPermissionCallback: (() -> Unit)? = null

    companion object {
        private var activityRef: WeakReference<ComponentActivity>? = null
        private var sharedInstance: ImagePicker? = null

        /**
         * Initialize the ImagePicker with the activity.
         * Must be called from Activity.onCreate() before setContent().
         */
        fun initialize(activity: ComponentActivity, context: Context): ImagePicker {
            activityRef = WeakReference(activity)
            return ImagePicker(context).also { picker ->
                sharedInstance = picker
                picker.registerActivityResultLaunchers(activity)
            }
        }

        fun getInstance(): ImagePicker? = sharedInstance
    }

    private fun registerActivityResultLaunchers(activity: ComponentActivity) {
        cameraLauncher = activity.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success && currentPhotoUri != null) {
                val result = processImageUri(currentPhotoUri!!)
                pendingCameraResult?.invoke(result)
            } else {
                pendingCameraResult?.invoke(null)
            }
            pendingCameraResult = null
        }

        galleryLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                val result = processImageUri(uri)
                pendingGalleryResult?.invoke(result)
            } else {
                pendingGalleryResult?.invoke(null)
            }
            pendingGalleryResult = null
        }

        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                pendingPermissionCallback?.invoke()
            }
            pendingPermissionCallback = null
        }
    }

    actual fun isCameraAvailable(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    actual fun launchCamera(onResult: (ImagePickerResult?) -> Unit) {
        val launcher = cameraLauncher ?: run {
            onResult(null)
            return
        }

        // Check camera permission
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            pendingPermissionCallback = { launchCamera(onResult) }
            permissionLauncher?.launch(permission)
            return
        }

        val photoUri = createImageFile()
        if (photoUri != null) {
            currentPhotoUri = photoUri
            pendingCameraResult = onResult
            launcher.launch(photoUri)
        } else {
            onResult(null)
        }
    }

    actual fun launchGallery(onResult: (ImagePickerResult?) -> Unit) {
        val launcher = galleryLauncher ?: run {
            onResult(null)
            return
        }

        pendingGalleryResult = onResult
        launcher.launch("image/*")
    }

    private fun createImageFile(): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(
                "CHART_${timeStamp}_",
                ".jpg",
                storageDir
            )

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun processImageUri(uri: Uri): ImagePickerResult? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Resize if too large (max 1920px on longest side)
            val resizedBitmap = resizeBitmapIfNeeded(bitmap, 1920)

            // Convert to base64
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val imageBytes = outputStream.toByteArray()
            val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            // Clean up
            if (resizedBitmap != bitmap) {
                bitmap.recycle()
            }
            resizedBitmap.recycle()

            ImagePickerResult(
                imageBase64 = base64String,
                mimeType = "image/jpeg",
                fileName = "chart_${System.currentTimeMillis()}.jpg"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun resizeBitmapIfNeeded(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}

package com.myopenclaw.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import platform.Foundation.NSData
import platform.Foundation.create
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * iOS implementation of ImagePicker using PHPickerViewController and UIImagePickerController
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class ImagePicker {

    private var pendingResult: ((ImagePickerResult?) -> Unit)? = null

    actual fun isCameraAvailable(): Boolean {
        return UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        )
    }

    actual fun launchCamera(onResult: (ImagePickerResult?) -> Unit) {
        if (!isCameraAvailable()) {
            onResult(null)
            return
        }

        pendingResult = onResult

        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        picker.delegate = createCameraDelegate()

        presentViewController(picker)
    }

    actual fun launchGallery(onResult: (ImagePickerResult?) -> Unit) {
        pendingResult = onResult

        // Use PHPickerViewController for modern photo picking
        val configuration = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = 1
        }

        val picker = PHPickerViewController(configuration)
        picker.delegate = createPhotoPickerDelegate()

        presentViewController(picker)
    }

    private fun presentViewController(viewController: UIViewController) {
        val scenes = UIApplication.sharedApplication.connectedScenes
        val windowScene = scenes.firstOrNull { scene ->
            scene is UIWindowScene
        } as? UIWindowScene

        val keyWindow = windowScene?.windows?.firstOrNull { window ->
            (window as? UIWindow)?.isKeyWindow() == true
        } as? UIWindow

        val rootViewController = keyWindow?.rootViewController

        // Find the topmost presented view controller
        var topController = rootViewController
        while (topController?.presentedViewController != null) {
            topController = topController.presentedViewController
        }

        topController?.presentViewController(viewController, animated = true, completion = null)
    }

    private fun createCameraDelegate(): CameraPickerDelegate {
        return CameraPickerDelegate { image ->
            val result = image?.let { processImage(it) }
            pendingResult?.invoke(result)
            pendingResult = null
        }
    }

    private fun createPhotoPickerDelegate(): PhotoPickerDelegate {
        return PhotoPickerDelegate { image ->
            val result = image?.let { processImage(it) }
            pendingResult?.invoke(result)
            pendingResult = null
        }
    }

    private fun processImage(image: UIImage): ImagePickerResult? {
        // Resize if too large
        val resizedImage = resizeImageIfNeeded(image, 1920.0)

        // Convert to JPEG data
        val jpegData = UIImageJPEGRepresentation(resizedImage, 0.85) ?: return null

        // Convert NSData to base64 string using Kotlin's Base64
        @OptIn(ExperimentalEncodingApi::class)
        val base64String = run {
            val bytes = ByteArray(jpegData.length.toInt())
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), jpegData.bytes, jpegData.length)
            }
            Base64.encode(bytes)
        }

        return ImagePickerResult(
            imageBase64 = base64String,
            mimeType = "image/jpeg",
            fileName = "chart_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}.jpg"
        )
    }

    private fun resizeImageIfNeeded(image: UIImage, maxSize: Double): UIImage {
        val width = image.size.useContents { this.width }
        val height = image.size.useContents { this.height }

        if (width <= maxSize && height <= maxSize) {
            return image
        }

        val ratio = minOf(maxSize / width, maxSize / height)
        val newWidth = width * ratio
        val newHeight = height * ratio

        platform.UIKit.UIGraphicsBeginImageContextWithOptions(
            platform.CoreGraphics.CGSizeMake(newWidth, newHeight),
            false,
            1.0
        )

        image.drawInRect(platform.CoreGraphics.CGRectMake(0.0, 0.0, newWidth, newHeight))
        val resizedImage = platform.UIKit.UIGraphicsGetImageFromCurrentImageContext()
        platform.UIKit.UIGraphicsEndImageContext()

        return resizedImage ?: image
    }
}

/**
 * Delegate for UIImagePickerController (camera)
 */
@OptIn(ExperimentalForeignApi::class)
private class CameraPickerDelegate(
    private val onImagePicked: (UIImage?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true) {
            onImagePicked(image)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) {
            onImagePicked(null)
        }
    }
}

/**
 * Delegate for PHPickerViewController (photo library)
 */
@OptIn(ExperimentalForeignApi::class)
private class PhotoPickerDelegate(
    private val onImagePicked: (UIImage?) -> Unit
) : NSObject(), PHPickerViewControllerDelegateProtocol {

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        picker.dismissViewControllerAnimated(true) {
            val result = didFinishPicking.firstOrNull() as? PHPickerResult
            if (result != null && result.itemProvider.hasItemConformingToTypeIdentifier("public.image")) {
                result.itemProvider.loadDataRepresentationForTypeIdentifier(
                    typeIdentifier = "public.image"
                ) { data, error ->
                    platform.Foundation.NSOperationQueue.mainQueue.addOperationWithBlock {
                        val image = data?.let { UIImage(data = it) }
                        onImagePicked(image)
                    }
                }
            } else {
                onImagePicked(null)
            }
        }
    }
}

package de.crysxd.cameraXTracker

import android.app.AlertDialog
import android.os.Bundle
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.crysxd.cameraXTracker.ar.ArOverlayView
import kotlinx.android.synthetic.main.fragment_camera.*
import timber.log.Timber
import java.io.File

@Suppress("unused")
open class CameraFragment : Fragment() {

    private val mutableArOverlayView = MutableLiveData<ArOverlayView>()
    val arOverlayView: LiveData<ArOverlayView> = mutableArOverlayView
    var cameraRunning = false
        private set
    var imageAnalyzer: ThreadedImageAnalyzer? = null
        set(value) {
            field = value
            if (cameraRunning) {
                startCamera()
            }
        }

    var flashMode: FlashMode? = null
    var imageCapture: ImageCapture? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_camera, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mutableArOverlayView.postValue(arOverlays)

        CameraPermissionHelper().requestCameraPermission(childFragmentManager) {
            if (it) {
                startCamera()
            } else {
                activity?.finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (cameraRunning) {
            CameraX.unbindAll()
            cameraRunning = false
            Timber.i("Stopping camera")
        }
    }

    private fun startCamera() {
        preview.post {
            try {
                val usesCases = mutableListOf<UseCase>()

                // Make sure that there are no other use cases bound to CameraX
                CameraX.unbindAll()

                // Create configuration object for the viewfinder use case
                val previewConfig = onCreatePreviewConfigBuilder().build()
                usesCases.add(AutoFitPreviewBuilder.build(previewConfig, preview))

                val imageCaptureConfig = onCreateImageCaptureConfigBuilder().build()
                val fileName = System.currentTimeMillis().toString()
                val fileFormat = ".jpg"
                val imageFile = createTempFile(fileName, fileFormat)

                // Setup image analysis pipeline that computes average pixel luminance in real time
                if (imageAnalyzer != null) {
                    val analyzerConfig = onCreateAnalyzerConfigBuilder().build()
                    usesCases.add(ImageAnalysis(analyzerConfig).apply {
                        analyzer = imageAnalyzer
                    })
                }

                usesCases.add(usesCases.size - 1,
                    ImageCapture(imageCaptureConfig).apply {
                        takePicture(imageFile, object : ImageCapture.OnImageSavedListener {
                            override fun onImageSaved(file: File) {
                                // You may display the image for example using its path file.absolutePath
                            }

                            override fun onError(
                                useCaseError: ImageCapture.UseCaseError,
                                message: String,
                                cause: Throwable?
                            ) {
                                // Display error message
                            }
                        })
                    })

                // Bind use cases to lifecycle
                CameraX.bindToLifecycle(this, *usesCases.toTypedArray())
                cameraRunning = true
                Timber.i("Started camera with useCases=$usesCases")
            } catch (e: Exception) {
                Timber.e(e)
                AlertDialog.Builder(context)
                    .setMessage(getString(R.string.camera_error))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        activity?.finish()
                    }
                    .create()
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onCreateAnalyzerConfigBuilder() = ImageAnalysisConfig.Builder().apply {
        // Use a worker thread for image analysis to prevent preview glitches
        setCallbackHandler(imageAnalyzer!!.getHandler())
        // In our analysis, we care more about the latest image than analyzing *every* image
        setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        setTargetResolution(Size(preview.width / 2, preview.height / 2))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun onCreatePreviewConfigBuilder() = PreviewConfig.Builder().apply {
        setTargetAspectRatio(Rational(16, 9))
        setTargetResolution(Size(preview.width, preview.height))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun onCreateImageCaptureConfigBuilder() = ImageCaptureConfig.Builder().apply {
        setTargetAspectRatio(Rational(16, 9))
        setTargetRotation(Surface.ROTATION_0)
        setTargetResolution(Size(preview.width, preview.height))
        if (flashMode != null) {
            setFlashMode(flashMode)
        }
//            setCaptureMode(captureMode)
    }
//        val captureConfig = ImageCaptureConfig.Builder()
//            .setTargetAspectRatio()
//            .setTargetRotation(rotation)
//            .setTargetResolution(resolution)
//            .setFlashMode(flashMode)
//            .setCaptureMode(captureMode)
//            .build()
//
//        val capture = ImageCapture(captureConfig)
//        cameraCaptureImageButton.setOnClickListener {
//            // Create temporary file
//            val fileName = System.currentTimeMillis().toString()
//            val fileFormat = ".jpg"
//            val imageFile = createTempFile(fileName, fileFormat)
//
//            // Store captured image in the temporary file
//            capture.takePicture(imageFile, object : ImageCapture.OnImageSavedListener {
//                override fun onImageSaved(file: File) {
//                    // You may display the image for example using its path file.absolutePath
//                }
//
//                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
//                    // Display error message
//                }
//            })
//        }
//
//        return capture
//    }

}
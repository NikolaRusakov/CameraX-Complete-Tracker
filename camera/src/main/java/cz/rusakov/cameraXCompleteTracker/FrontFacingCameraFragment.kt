package cz.rusakov.cameraXCompleteTracker

import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.PreviewConfig

class FrontFacingCameraFragment : CameraFragment() {

    override fun onCreateAnalyzerConfigBuilder(): ImageAnalysisConfig.Builder = super.onCreateAnalyzerConfigBuilder().apply {
        setLensFacing(CameraX.LensFacing.FRONT)
    }

    override fun onCreatePreviewConfigBuilder(): PreviewConfig.Builder = super.onCreatePreviewConfigBuilder().apply {
        setLensFacing(CameraX.LensFacing.FRONT)
    }
}
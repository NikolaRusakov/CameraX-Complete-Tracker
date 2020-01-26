package cz.rusakov.cameraXCompleteTracker

import android.os.Handler
import androidx.camera.core.ImageAnalysis

interface ThreadedImageAnalyzer : ImageAnalysis.Analyzer {

    fun getHandler(): Handler

}
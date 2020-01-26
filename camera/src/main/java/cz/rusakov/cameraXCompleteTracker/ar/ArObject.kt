package cz.rusakov.cameraXCompleteTracker.ar

import android.graphics.RectF
import android.util.Size

data class ArObject(val trackingId: Int, val boundingBox: RectF, val sourceSize: Size, val sourceRotationDegrees: Int)
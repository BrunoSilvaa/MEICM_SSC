package com.example.ssc_project

import android.app.Activity
import android.content.Context
import android.hardware.Camera
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import java.io.IOException

class CameraPreview(context: Context, private var mCamera: Camera?) : SurfaceView(context), SurfaceHolder.Callback {

    private var mHolder: SurfaceHolder = holder

    init {
        mHolder.addCallback(this)
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            mCamera?.setPreviewDisplay(holder)
            setCameraDisplayOrientation(context as Activity, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera)
            mCamera?.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
            mCamera?.release()
            mCamera = null
        } catch (e: RuntimeException) {
            e.printStackTrace()
            mCamera?.release()
            mCamera = null
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mCamera?.stopPreview()
        mCamera?.release()
        mCamera = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        if (mHolder.surface == null) {
            return
        }

        try {
            mCamera?.stopPreview()
        } catch (e: Exception) {
        }

        try {
            mCamera?.setPreviewDisplay(mHolder)
            mCamera?.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setCameraDisplayOrientation(activity: Activity, cameraId: Int, camera: Camera?) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = activity.windowManager.defaultDisplay.rotation
        val degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360  // compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        camera?.setDisplayOrientation(result)
    }
}

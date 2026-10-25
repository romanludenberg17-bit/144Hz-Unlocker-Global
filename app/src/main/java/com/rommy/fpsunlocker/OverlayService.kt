package com.rommy.fpsunlocker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Choreographer
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat

class OverlayService : Service(), Choreographer.FrameCallback {
    private lateinit var windowManager: WindowManager
    private lateinit var overlay: TextView
    private var frames = 0
    private var lastTime = 0L

    companion object {
        const val CHANNEL_ID = "fps_overlay"
        var isRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createChannel()
        startForeground(44, notification())

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlay = TextView(this).apply {
            text = "-- FPS"
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(190, 20, 20, 24))
            setPadding(16, 8, 16, 8)
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 18
            y = 90
        }
        windowManager.addView(overlay, params)
        lastTime = System.nanoTime()
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        frames++
        val elapsed = frameTimeNanos - lastTime
        if (elapsed >= 1_000_000_000L) {
            val fps = (frames * 1_000_000_000L / elapsed).toInt().coerceIn(0, 240)
            overlay.text = "$fps FPS"
            frames = 0
            lastTime = frameTimeNanos
        }
        Choreographer.getInstance().postFrameCallback(this)
    }

    override fun onDestroy() {
        Choreographer.getInstance().removeFrameCallback(this)
        if (::overlay.isInitialized) windowManager.removeView(overlay)
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "FPS Overlay", NotificationManager.IMPORTANCE_LOW))
        }
    }

    private fun notification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FPS Overlay")
            .setContentText("144Hz Unlocker overlay active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .setSilent(true)
            .build()
}

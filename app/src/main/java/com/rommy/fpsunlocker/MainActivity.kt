package com.rommy.fpsunlocker

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {
    private lateinit var fpsSwitch: MaterialSwitch
    private lateinit var overlaySwitch: MaterialSwitch
    private lateinit var status: TextView
    private lateinit var peakRate: TextView
    private lateinit var glassCard: View

    private val prefs by lazy { getSharedPreferences("ui", MODE_PRIVATE) }
    private val colors = intArrayOf(
        Color.rgb(124, 92, 255),
        Color.rgb(0, 188, 212),
        Color.rgb(76, 175, 80),
        Color.rgb(255, 152, 0),
        Color.rgb(244, 67, 54),
        Color.rgb(233, 30, 99)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fpsSwitch = findViewById(R.id.fps_switch)
        overlaySwitch = findViewById(R.id.overlay_switch)
        status = findViewById(R.id.status_text)
        peakRate = findViewById(R.id.peak_rate_text)
        glassCard = findViewById(R.id.glass_card)

        applyAccent(prefs.getInt("accent", colors[0]))
        fpsSwitch.isChecked = isUnlocked()
        overlaySwitch.isChecked = OverlayService.isRunning
        renderState(fpsSwitch.isChecked)

        fpsSwitch.setOnCheckedChangeListener { _, checked ->
            setFpsUnlock(checked)
        }

        overlaySwitch.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                if (!Settings.canDrawOverlays(this)) {
                    overlaySwitch.isChecked = false
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                    return@setOnCheckedChangeListener
                }
                ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java))
            } else {
                stopService(Intent(this, OverlayService::class.java))
            }
        }

        findViewById<View>(R.id.telegram_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/rommy_apps")))
        }

        findViewById<View>(R.id.github_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/romanludenberg17-bit/144Hz-Unlocker-Global")))
        }

        val accentButtons = listOf(
            R.id.accent_1, R.id.accent_2, R.id.accent_3,
            R.id.accent_4, R.id.accent_5, R.id.accent_6
        )
        accentButtons.forEachIndexed { index, id ->
            findViewById<MaterialButton>(id).setOnClickListener {
                prefs.edit().putInt("accent", colors[index]).apply()
                applyAccent(colors[index])
            }
        }

        findViewById<View>(R.id.root).setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                glassCard.animate().scaleX(.985f).scaleY(.985f).setDuration(90).start()
            } else if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
                glassCard.animate().scaleX(1f).scaleY(1f).setDuration(360).setInterpolator(DecelerateInterpolator()).start()
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()
        if (::fpsSwitch.isInitialized) {
            fpsSwitch.isChecked = isUnlocked()
            overlaySwitch.isChecked = OverlayService.isRunning
            renderState(fpsSwitch.isChecked)
        }
    }

    private fun setFpsUnlock(enabled: Boolean) {
        if (!Settings.System.canWrite(this)) {
            fpsSwitch.isChecked = false
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
            return
        }

        val value = if (enabled) "1:1:1:72:144" else "0:-1:0:0:0"
        val written = Settings.System.putString(contentResolver, "gamecube_frame_interpolation_for_sr", value)
        val actual = Settings.System.getString(contentResolver, "gamecube_frame_interpolation_for_sr")

        if (!written || actual != value) {
            fpsSwitch.isChecked = false
            Toast.makeText(this, R.string.write_error, Toast.LENGTH_LONG).show()
            return
        }
        renderState(enabled)
    }

    private fun isUnlocked(): Boolean =
        Settings.System.getString(contentResolver, "gamecube_frame_interpolation_for_sr") == "1:1:1:72:144"

    private fun renderState(enabled: Boolean) {
        status.setText(if (enabled) R.string.status_enabled else R.string.status_disabled)
        peakRate.setText(R.string.peak_rate)
        status.alpha = 0f
        status.animate().alpha(1f).setDuration(280).setInterpolator(DecelerateInterpolator()).start()
    }

    private fun applyAccent(color: Int) {
        fpsSwitch.thumbTintList = android.content.res.ColorStateList.valueOf(color)
        fpsSwitch.trackTintList = android.content.res.ColorStateList.valueOf(Color.argb(90, Color.red(color), Color.green(color), Color.blue(color)))
        overlaySwitch.thumbTintList = android.content.res.ColorStateList.valueOf(color)
        overlaySwitch.trackTintList = android.content.res.ColorStateList.valueOf(Color.argb(90, Color.red(color), Color.green(color), Color.blue(color)))
        status.setTextColor(color)
        findViewById<MaterialButton>(R.id.telegram_button).backgroundTintList = android.content.res.ColorStateList.valueOf(color)
    }
}

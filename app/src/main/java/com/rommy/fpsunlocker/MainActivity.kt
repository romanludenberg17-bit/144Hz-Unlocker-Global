package com.rommy.fpsunlocker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {
    private lateinit var fpsSwitch: MaterialSwitch
    private lateinit var status: TextView
    private lateinit var glassCard: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fpsSwitch = findViewById(R.id.fps_switch)
        status = findViewById(R.id.status_text)
        glassCard = findViewById(R.id.glass_card)

        fpsSwitch.isChecked = isUnlocked()
        renderState(fpsSwitch.isChecked)

        fpsSwitch.setOnCheckedChangeListener { _, checked ->
            if (!Settings.System.canWrite(this)) {
                fpsSwitch.isChecked = false
                startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
                return@setOnCheckedChangeListener
            }
            try {
                Settings.System.putString(
                    contentResolver,
                    "gamecube_frame_interpolation_for_sr",
                    if (checked) "1:1::72:144" else "0:-1:0:0:0"
                )
                renderState(checked)
            } catch (e: Exception) {
                fpsSwitch.isChecked = !checked
                Toast.makeText(this, R.string.write_error, Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.telegram_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/rommy_apps")))
        }

        findViewById<View>(R.id.github_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/romanludenberg17-bit/144Hz-Unlocker-Global")))
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
            renderState(fpsSwitch.isChecked)
        }
    }

    private fun isUnlocked(): Boolean =
        Settings.System.getString(contentResolver, "gamecube_frame_interpolation_for_sr") == "1:1::72:144"

    private fun renderState(enabled: Boolean) {
        status.setText(if (enabled) R.string.status_enabled else R.string.status_disabled)
        status.alpha = 0f
        status.animate().alpha(1f).setDuration(280).setInterpolator(DecelerateInterpolator()).start()
    }
}

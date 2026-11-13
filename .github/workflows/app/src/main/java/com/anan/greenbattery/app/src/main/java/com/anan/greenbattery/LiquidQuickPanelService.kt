package com.anan.greenbattery

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.*
import android.widget.*

class LiquidQuickPanelService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var edgeTrigger: View
    private var panelOverlay: FrameLayout? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotif()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        edgeTrigger = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setOnTouchListener(object : View.OnTouchListener {
                var startY = 0f
                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> startY = event.rawY
                        MotionEvent.ACTION_UP -> {
                            if (event.rawY - startY > 60) openLiquidPanel()
                        }
                    }
                    return true
                }
            })
        }

        val triggerParams = WindowManager.LayoutParams(
            180, 90,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
        }
        windowManager.addView(edgeTrigger, triggerParams)
    }

    private fun openLiquidPanel() {
        if (panelOverlay != null) return

        val backdrop = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#8805080D"))
            setOnClickListener { closeLiquidPanel() }
        }

        val glassCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            isClickable = true
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#CC0E1724"),
                    Color.parseColor("#662BC4F3"),
                    Color.parseColor("#55F05A7E")
                )
            ).apply {
                cornerRadius = 56f
                setStroke(3, Color.parseColor("#B34CD3F7"))
            }
        }

        glassCard.addView(TextView(this).apply {
            text = "LIQUID GLASS CONTROL"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.parseColor("#4CD3F7"))
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 32)
        })

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 36)
        }
        btnRow.addView(createGlassTile("📶 Wi-Fi") {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            closeLiquidPanel()
        })
        btnRow.addView(createGlassTile("🎧 Bluetooth") {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            closeLiquidPanel()
        })
        btnRow.addView(createGlassTile("⚙️ Cài đặt") {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            closeLiquidPanel()
        })
        glassCard.addView(btnRow)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        glassCard.addView(TextView(this).apply {
            text = "🔊 Âm lượng Media"
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(0, 0, 0, 12)
        })
        val volSeek = SeekBar(this).apply {
            max = maxVol
            progress = curVol
            setPadding(0, 0, 0, 28)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, p, 0)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        glassCard.addView(volSeek)

        glassCard.addView(TextView(this).apply {
            text = "Chạm vùng trống để đóng"
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 12f
        })

        val cardParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP
            setMargins(36, 120, 36, 0)
        }
        backdrop.addView(glassCard, cardParams)

        val windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(backdrop, windowParams)
        panelOverlay = backdrop
    }

    private fun createGlassTile(label: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(24, 32, 24, 32)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(10, 0, 10, 0)
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#442BC4F3"))
                cornerRadius = 32f
                setStroke(2, Color.parseColor("#884CD3F7"))
            }
            setOnClickListener { onClick() }
        }
    }

    private fun closeLiquidPanel() {
        panelOverlay?.let {
            windowManager.removeView(it)
            panelOverlay = null
        }
    }

    private fun startForegroundNotif() {
        val id = "quick_panel_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(id, "Liquid Quick Panel", NotificationManager.IMPORTANCE_MIN))
        }
        startForeground(3, Notification.Builder(this, id).setContentTitle("Liquid Quick Panel sẵn sàng").setSmallIcon(android.R.drawable.ic_menu_manage).build())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::edgeTrigger.isInitialized) windowManager.removeView(edgeTrigger)
        closeLiquidPanel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

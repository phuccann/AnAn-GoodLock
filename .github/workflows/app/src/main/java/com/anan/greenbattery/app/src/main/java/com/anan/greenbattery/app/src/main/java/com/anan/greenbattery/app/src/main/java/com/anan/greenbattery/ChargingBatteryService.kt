package com.anan.greenbattery

import android.app.*
import android.content.*
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView

class ChargingBatteryService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var batteryPill: LinearLayout
    private lateinit var batteryText: TextView
    private var isOverlayAdded = false

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_CONNECTED -> showGreenBattery()
                Intent.ACTION_POWER_DISCONNECTED -> hideGreenBattery()
                Intent.ACTION_BATTERY_CHANGED -> {
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                     status == BatteryManager.BATTERY_STATUS_FULL
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
                    batteryText.text = "⚡ $pct%"
                    if (isCharging) showGreenBattery() else hideGreenBattery()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        batteryPill = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(18, 4, 18, 4)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#22C55E"))
                cornerRadius = 24f
                setStroke(2, Color.parseColor("#86EFAC"))
            }
        }
        batteryText = TextView(this).apply {
            text = "⚡ 100%"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTextColor(Color.parseColor("#052E16"))
            typeface = Typeface.DEFAULT_BOLD
        }
        batteryPill.addView(batteryText)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(powerReceiver, filter)
    }

    private fun showGreenBattery() {
        if (isOverlayAdded) return
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 28
            y = 18
        }
        windowManager.addView(batteryPill, params)
        isOverlayAdded = true
    }

    private fun hideGreenBattery() {
        if (!isOverlayAdded) return
        windowManager.removeView(batteryPill)
        isOverlayAdded = false
    }

    private fun startForegroundNotification() {
        val channelId = "green_battery_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Hiệu ứng Pin Xanh", NotificationManager.IMPORTANCE_MIN)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("AnAn GoodLock đang chạy ngầm")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .build()
        startForeground(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(powerReceiver)
        hideGreenBattery()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

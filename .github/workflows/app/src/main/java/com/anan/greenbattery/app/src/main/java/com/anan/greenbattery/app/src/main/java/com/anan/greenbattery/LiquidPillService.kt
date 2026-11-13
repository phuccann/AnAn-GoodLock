package com.anan.greenbattery

import android.app.*
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextClock

class LiquidPillService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var pillView: LinearLayout

    override fun onCreate() {
        super.onCreate()
        val id = "liquid_pill_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(id, "Liquid Glass Pill", NotificationManager.IMPORTANCE_MIN))
        }
        startForeground(4, Notification.Builder(this, id).setContentTitle("Liquid Glass Pill đang chạy").setSmallIcon(android.R.drawable.ic_menu_recent_history).build())

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        pillView = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(36, 10, 36, 10)
            background = GradientDrawable(
                GradientDrawable.Orientation.LR,
                intArrayOf(Color.parseColor("#550E1724"), Color.parseColor("#442BC4F3"), Color.parseColor("#44F05A7E"))
            ).apply {
                cornerRadius = 50f
                setStroke(2, Color.parseColor("#AA4CD3F7"))
            }
        }

        val clock = TextClock(this).apply {
            format24Hour = "HH:mm"
            format12Hour = "hh:mm"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTextColor(Color.parseColor("#E6F7FF"))
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            letterSpacing = 0.08f
        }
        pillView.addView(clock)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 105
        }
        windowManager.addView(pillView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::pillView.isInitialized) windowManager.removeView(pillView)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

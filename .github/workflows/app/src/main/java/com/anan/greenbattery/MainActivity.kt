package com.anan.greenbattery

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("AnAnGoodLock", MODE_PRIVATE)

        val scroll = ScrollView(this).apply { setBackgroundColor(Color.parseColor("#05080D")) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(52, 100, 52, 64)
        }

        val title = TextView(this).apply {
            text = "AN AN GOOD LOCK"
            textSize = 26f
            setTextColor(Color.parseColor("#4CD3F7"))
            typeface = Typeface.DEFAULT_BOLD
        }
        val sub = TextView(this).apply {
            text = "Suite giao diện Liquid Glass & HyperOS cho One UI"
            textSize = 13f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, 8, 0, 56)
        }
        root.addView(title)
        root.addView(sub)

        root.addView(createModuleCard(
            prefs, "quick_panel_on",
            "💠 Liquid Glass Quick Panel",
            "Vuốt góc trên bên phải mở bảng điều khiển kính lỏng & thanh âm lượng",
            "#4CD3F7",
            LiquidQuickPanelService::class.java
        ))

        root.addView(createModuleCard(
            prefs, "liquid_pill_on",
            "🔮 Liquid Glass Live Pill",
            "Viên thuốc kính mờ hiển thị đồng hồ số cao dẹt dưới camera đục lỗ",
            "#F25C84",
            LiquidPillService::class.java
        ))

        root.addView(createModuleCard(
            prefs, "green_battery_on",
            "⚡ Pin Xanh HyperOS khi sạc",
            "Tự động phủ viên pin xanh lá kèm % pin khi cắm cáp sạc",
            "#22C55E",
            ChargingBatteryService::class.java
        ))

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun createModuleCard(
        prefs: android.content.SharedPreferences,
        key: String,
        titleStr: String,
        descStr: String,
        accentHex: String,
        serviceCls: Class<*>
    ): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(40, 40, 40, 40)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 32 }
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0E1724"))
                cornerRadius = 42f
                setStroke(2, Color.parseColor(accentHex))
            }
        }

        val labelBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        labelBox.addView(TextView(this).apply {
            text = titleStr
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })
        labelBox.addView(TextView(this).apply {
            text = descStr
            textSize = 12f
            setTextColor(Color.parseColor("#94A3B8"))
            setPadding(0, 6, 16, 0)
        })

        val toggle = Switch(this).apply {
            isChecked = prefs.getBoolean(key, false)
            setOnCheckedChangeListener { btn, isChecked ->
                if (isChecked && !Settings.canDrawOverlays(this@MainActivity)) {
                    btn.isChecked = false
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else {
                    prefs.edit().putBoolean(key, isChecked).apply()
                    val intent = Intent(this@MainActivity, serviceCls)
                    if (isChecked) startForegroundService(intent) else stopService(intent)
                }
            }
        }

        card.addView(labelBox)
        card.addView(toggle)
        return card
    }
}

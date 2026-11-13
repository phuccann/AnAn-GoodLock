package com.anan.greenbattery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("AnAnGoodLock", Context.MODE_PRIVATE)
            if (prefs.getBoolean("quick_panel_on", false)) {
                context.startForegroundService(Intent(context, LiquidQuickPanelService::class.java))
            }
            if (prefs.getBoolean("liquid_pill_on", false)) {
                context.startForegroundService(Intent(context, LiquidPillService::class.java))
            }
            if (prefs.getBoolean("green_battery_on", false)) {
                context.startForegroundService(Intent(context, ChargingBatteryService::class.java))
            }
        }
    }
}

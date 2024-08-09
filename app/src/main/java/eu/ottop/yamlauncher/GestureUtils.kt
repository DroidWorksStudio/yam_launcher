package eu.ottop.yamlauncher

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ServiceInfo
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity.ACCESSIBILITY_SERVICE

class GestureUtils(private val context: Context) {

    private val sharedPreferenceManager = SharedPreferenceManager(context)

    fun getSwipeInfo(launcherApps: LauncherApps, direction: String): Pair<LauncherActivityInfo?, Int?> {
        val app = sharedPreferenceManager.getGestureInfo(direction)

        if (app != null) {
            if (app.size >= 3)

                return Pair(
                    launcherApps.getActivityList(
                        app[1], launcherApps.profiles[app[2]
                            .toInt()]
                    ).firstOrNull(), app[2].toInt()
                )
        }
        return Pair(null, null)
    }

    fun isAccessibilityServiceEnabled(service: Class<out AccessibilityService>): Boolean {
        val am = context.getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        for (enabledService in enabledServices) {
            val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
            if (enabledServiceInfo.packageName.equals(context.packageName) && enabledServiceInfo.name.equals(
                    service.name
                )
            ) return true
        }

        return false
    }

    fun promptEnableAccessibility() {
        AlertDialog.Builder(context).apply {
            setTitle("Confirmation")
            setMessage("To lock with double tap, enable YAM Launcher in accessibility settings.")
            setPositiveButton("Yes") { _, _ ->
                // Perform action on confirmation
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            setNegativeButton("Cancel") { _, _ ->

            }

        }.create().show()
    }
}
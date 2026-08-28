package com.helper.heretoosmand

import android.content.Context
import android.content.Intent
import android.net.Uri

object OsmAndIntentBuilder {

    const val PACKAGE_OSMAND_FREE = "net.osmand"
    const val PACKAGE_OSMAND_PLUS = "net.osmand.plus"
    const val PACKAGE_OSMAND_DEV = "net.osmand.dev"
    const val OPTION_AUTO = "auto_select"
    const val OPTION_CHOOSER = "show_chooser"

    val KNOWN_PACKAGES = listOf(
        PACKAGE_OSMAND_FREE,
        PACKAGE_OSMAND_PLUS,
        PACKAGE_OSMAND_DEV
    )

    fun getOsmAndPackageLabel(pkg: String): String {
        return when (pkg) {
            PACKAGE_OSMAND_FREE -> "OsmAnd (Free)"
            PACKAGE_OSMAND_PLUS -> "OsmAnd+ (Paid)"
            PACKAGE_OSMAND_DEV -> "OsmAnd Dev / Nightly"
            OPTION_CHOOSER -> "App Chooser (Prompt user)"
            else -> "Auto-detect"
        }
    }

    fun findInstalledOsmAndPackage(context: Context): String? {
        val pm = context.packageManager
        for (pkg in KNOWN_PACKAGES) {
            try {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
                return pkg
            } catch (_: Exception) {}
        }
        return null
    }

    fun buildOsmAndIntent(
        context: Context,
        target: NavigationTarget,
        preferredPackageSetting: String = OPTION_AUTO,
        overrideMode: NavigationMode? = null
    ): Intent {
        val mode = overrideMode ?: target.mode

        val uri = when {
            target.hasCoordinates() -> {
                val lat = target.destLat
                val lon = target.destLon
                Uri.parse("google.navigation:q=$lat,$lon&mode=${mode.osmandMode}")
            }
            !target.query.isNullOrBlank() -> {
                Uri.parse("google.navigation:q=${Uri.encode(target.query)}&mode=${mode.osmandMode}")
            }
            else -> {
                Uri.parse("geo:0,0")
            }
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val targetPkg = when (preferredPackageSetting) {
            OPTION_CHOOSER -> null
            OPTION_AUTO, "" -> findInstalledOsmAndPackage(context)
            else -> preferredPackageSetting
        }

        if (!targetPkg.isNullOrBlank()) {
            intent.setPackage(targetPkg)
        }

        return intent
    }
}

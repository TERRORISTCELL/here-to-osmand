package com.helper.heretoosmand

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast

class RedirectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferencesManager(this)

        if (!prefs.isRedirectEnabled) {
            Toast.makeText(this, "HereToOsmAnd redirection is currently disabled", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val incomingUriString = intent.dataString ?: intent.getStringExtra(Intent.EXTRA_TEXT)

        if (incomingUriString.isNullOrBlank()) {
            Toast.makeText(this, "No valid URI found to redirect", Toast.LENGTH_SHORT).show()
            openMainActivityWithError("No valid intent data received")
            finish()
            return
        }

        val navigationTarget = HereUriParser.parse(incomingUriString)

        val overrideMode = when (prefs.defaultNavigationMode) {
            "DRIVING" -> NavigationMode.DRIVING
            "WALKING" -> NavigationMode.WALKING
            "BICYCLE" -> NavigationMode.BICYCLE
            else -> null // Preserve detected mode
        }

        val osmandIntent = OsmAndIntentBuilder.buildOsmAndIntent(
            context = this,
            target = navigationTarget,
            preferredPackageSetting = prefs.preferredOsmAndPackage,
            overrideMode = overrideMode
        )

        try {
            startActivity(osmandIntent)

            if (prefs.showToastNotification) {
                val dest = navigationTarget.getFormattedDestination()
                Toast.makeText(this, "Redirecting to OsmAnd: $dest", Toast.LENGTH_SHORT).show()
            }

            prefs.addLogEntry(
                RedirectLogEntry(
                    timestamp = System.currentTimeMillis(),
                    sourceUri = incomingUriString,
                    destFormatted = navigationTarget.getFormattedDestination(),
                    mode = (overrideMode ?: navigationTarget.mode).label,
                    success = true
                )
            )

        } catch (e: ActivityNotFoundException) {
            val errMsg = "OsmAnd application not found on device"
            Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()

            prefs.addLogEntry(
                RedirectLogEntry(
                    timestamp = System.currentTimeMillis(),
                    sourceUri = incomingUriString,
                    destFormatted = navigationTarget.getFormattedDestination(),
                    mode = navigationTarget.mode.label,
                    success = false,
                    errorMsg = errMsg
                )
            )

            openMainActivityWithError(errMsg)

        } catch (e: Exception) {
            val errMsg = e.localizedMessage ?: "Error launching OsmAnd intent"
            Toast.makeText(this, "Redirect Error: $errMsg", Toast.LENGTH_LONG).show()

            prefs.addLogEntry(
                RedirectLogEntry(
                    timestamp = System.currentTimeMillis(),
                    sourceUri = incomingUriString,
                    destFormatted = navigationTarget.getFormattedDestination(),
                    mode = navigationTarget.mode.label,
                    success = false,
                    errorMsg = errMsg
                )
            )

            openMainActivityWithError(errMsg)
        }

        finish()
    }

    private fun openMainActivityWithError(error: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_ERROR_MSG", error)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }
}

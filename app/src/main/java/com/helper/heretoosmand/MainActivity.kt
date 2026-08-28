package com.helper.heretoosmand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.helper.heretoosmand.ui.HomeScreen
import com.helper.heretoosmand.ui.theme.HereToOsmAndTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferencesManager(this)
        val initialErrorMsg = intent.getStringExtra("EXTRA_ERROR_MSG")

        setContent {
            HereToOsmAndTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        prefs = prefs,
                        initialErrorMsg = initialErrorMsg
                    )
                }
            }
        }
    }
}

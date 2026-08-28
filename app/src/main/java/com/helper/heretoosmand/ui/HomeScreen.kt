package com.helper.heretoosmand.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helper.heretoosmand.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    prefs: PreferencesManager,
    initialErrorMsg: String? = null
) {
    val context = LocalContext.current

    var isEnabled by remember { mutableStateOf(prefs.isRedirectEnabled) }
    var selectedPackage by remember { mutableStateOf(prefs.preferredOsmAndPackage) }
    var selectedMode by remember { mutableStateOf(prefs.defaultNavigationMode) }
    var showToast by remember { mutableStateOf(prefs.showToastNotification) }
    var logs by remember { mutableStateOf(prefs.getRecentLogs()) }

    var installedOsmAndPkg by remember {
        mutableStateOf(OsmAndIntentBuilder.findInstalledOsmAndPackage(context))
    }

    var testUriInput by remember {
        mutableStateOf("https://wego.here.com/directions/drive/Berlin:52.5200,13.4050/Brandenburg:52.5163,13.3777")
    }
    var testResult by remember { mutableStateOf<NavigationTarget?>(null) }
    var testIntentUriString by remember { mutableStateOf<String?>(null) }

    val sampleUrls = listOf(
        "https://wego.here.com/directions/drive/Berlin:52.5200,13.4050/Brandenburg:52.5163,13.3777",
        "https://wego.here.com/location/Brandenburg-Gate:52.5163,13.3777",
        "here-route://52.5200,13.4050/52.5163,13.3777",
        "geo:52.5163,13.3777?q=52.5163,13.3777(Brandenburg+Gate)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "HereToOsmAnd",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // Initial Error Banner (if opened from failed redirect)
            if (!initialErrorMsg.isNullOrBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = initialErrorMsg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 1. Status & Activation Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEnabled) "Redirection Active" else "Redirection Disabled",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isEnabled) "Intercepting HERE WeGo directions & opening OsmAnd" else "Turn on to automatically redirect map intents",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = {
                                isEnabled = it
                                prefs.isRedirectEnabled = it
                            }
                        )
                    }
                }
            }

            // 2. OsmAnd Detection Status Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (installedOsmAndPkg != null) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (installedOsmAndPkg != null) Color(0xFF2E7D32) else Color(0xFFED6C02)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "OsmAnd App Status",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (installedOsmAndPkg != null)
                                        "Detected: ${OsmAndIntentBuilder.getOsmAndPackageLabel(installedOsmAndPkg!!)} ($installedOsmAndPkg)"
                                    else
                                        "OsmAnd not detected on this device",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        if (installedOsmAndPkg == null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        openStoreIntent(context, "net.osmand")
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Install OsmAnd")
                                }
                                OutlinedButton(
                                    onClick = {
                                        openStoreIntent(context, "net.osmand.plus")
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("OsmAnd+")
                                }
                            }
                        }
                    }
                }
            }

            // 3. System Defaults Setup Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Android Default App Setup",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To allow HereToOsmAnd to catch web links (wego.here.com), set this app as a supported link handler in Android App Settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open app settings", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configure Open by Default Settings")
                        }
                    }
                }
            }

            // 4. App Preferences Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Preferences",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Target OsmAnd Variant Selector
                        Text(
                            text = "Preferred Navigation App Target",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val packageOptions = listOf(
                            OsmAndIntentBuilder.OPTION_AUTO to "Auto-detect",
                            OsmAndIntentBuilder.PACKAGE_OSMAND_FREE to "OsmAnd (Free)",
                            OsmAndIntentBuilder.PACKAGE_OSMAND_PLUS to "OsmAnd+ (Paid)",
                            OsmAndIntentBuilder.PACKAGE_OSMAND_DEV to "OsmAnd Dev",
                            OsmAndIntentBuilder.OPTION_CHOOSER to "Show App Chooser"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            packageOptions.forEach { (key, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedPackage == key,
                                        onClick = {
                                            selectedPackage = key
                                            prefs.preferredOsmAndPackage = key
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Default Travel Mode Override
                        Text(
                            text = "Default Travel Mode",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val modeOptions = listOf(
                            "AUTO" to "Preserve original from link",
                            "DRIVING" to "Force Driving Mode",
                            "WALKING" to "Force Walking Mode",
                            "BICYCLE" to "Force Cycling Mode"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            modeOptions.forEach { (key, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedMode == key,
                                        onClick = {
                                            selectedMode = key
                                            prefs.defaultNavigationMode = key
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Notification Toast Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Show Redirect Toast",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "Display confirmation popup when redirecting",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Switch(
                                checked = showToast,
                                onCheckedChange = {
                                    showToast = it
                                    prefs.showToastNotification = it
                                }
                            )
                        }
                    }
                }
            }

            // 5. Interactive Test Sandbox Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Test Redirect Sandbox",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Test how HERE WeGo URLs and URIs are parsed and transformed into OsmAnd Intents.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = testUriInput,
                            onValueChange = { testUriInput = it },
                            label = { Text("Input HERE WeGo Link / URI") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Quick Presets:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            sampleUrls.forEach { sample ->
                                TextButton(
                                    onClick = { testUriInput = sample },
                                    contentPadding = PaddingValues(vertical = 2.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = sample,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val target = HereUriParser.parse(testUriInput)
                                    testResult = target
                                    val intent = OsmAndIntentBuilder.buildOsmAndIntent(
                                        context = context,
                                        target = target,
                                        preferredPackageSetting = selectedPackage
                                    )
                                    testIntentUriString = intent.dataString
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Parse & Preview")
                            }

                            Button(
                                onClick = {
                                    val intent = Intent(context, RedirectActivity::class.java).apply {
                                        data = Uri.parse(testUriInput)
                                    }
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Launch Redirect")
                            }
                        }

                        // Test output display
                        if (testResult != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Parsed Target Details:",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = "Destination: ${testResult?.getFormattedDestination()}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Mode: ${testResult?.mode?.label}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (testResult?.originLat != null) {
                                        Text(
                                            text = "Origin: ${testResult?.originLat}, ${testResult?.originLon}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Generated OsmAnd Data URI:",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = testIntentUriString ?: "None",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Redirect History Log Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Recent Redirects Log",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (logs.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        prefs.clearLogs()
                                        logs = emptyList()
                                    }
                                ) {
                                    Text("Clear")
                                }
                            }
                        }

                        if (logs.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No redirects logged yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                logs.take(10).forEach { log ->
                                    LogItemView(log)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemView(log: RedirectLogEntry) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss - MMM dd", Locale.getDefault()) }
    val dateStr = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (log.success) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            )
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.destFormatted,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Mode: ${log.mode} | Source: ${log.sourceUri}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (!log.errorMsg.isNullOrBlank()) {
                Text(
                    text = "Error: ${log.errorMsg}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun openStoreIntent(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

package com.helper.heretoosmand

enum class NavigationMode(val key: String, val osmandMode: String, val label: String) {
    DRIVING("drive", "d", "Driving"),
    WALKING("walk", "w", "Walking"),
    BICYCLE("bicycle", "b", "Cycling"),
    TRANSIT("public_transport", "d", "Transit")
}

data class NavigationTarget(
    val destLat: Double? = null,
    val destLon: Double? = null,
    val originLat: Double? = null,
    val originLon: Double? = null,
    val query: String? = null,
    val label: String? = null,
    val mode: NavigationMode = NavigationMode.DRIVING,
    val rawUri: String = ""
) {
    fun hasCoordinates(): Boolean = destLat != null && destLon != null

    fun getFormattedDestination(): String {
        return when {
            hasCoordinates() -> String.format("%.5f, %.5f", destLat, destLon)
            !query.isNullOrBlank() -> query
            else -> "Unknown Destination"
        }
    }
}

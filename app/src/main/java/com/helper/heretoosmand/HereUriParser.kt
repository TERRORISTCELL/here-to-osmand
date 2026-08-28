package com.helper.heretoosmand

import java.net.URI
import java.net.URLDecoder
import java.util.regex.Pattern

object HereUriParser {

    private val COORD_PATTERN = Pattern.compile("(-?\\d+\\.\\d+)\\s*,\\s*(-?\\d+\\.\\d+)")
    private val WEGO_SEGMENT_COORD_PATTERN = Pattern.compile("(?:^|:)?(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")

    fun parse(rawUriString: String?): NavigationTarget {
        if (rawUriString.isNullOrBlank()) {
            return NavigationTarget(rawUri = rawUriString ?: "")
        }

        val uriString = rawUriString.trim()

        // 1. Handle standard geo: URIs
        if (uriString.startsWith("geo:", ignoreCase = true)) {
            return parseGeoUri(uriString)
        }

        // 2. Handle google.navigation: URIs
        if (uriString.startsWith("google.navigation:", ignoreCase = true)) {
            return parseGoogleNavigationUri(uriString)
        }

        // 3. Handle HERE custom URIs (here-location://, here-route://, here.location:, here.direction:)
        if (uriString.startsWith("here-", ignoreCase = true) || uriString.startsWith("here.", ignoreCase = true)) {
            return parseHereCustomUri(uriString)
        }

        // 4. Handle WEGO web URLs (https://wego.here.com/... or http://wego.here.com/...)
        val isHttp = uriString.startsWith("http://", ignoreCase = true) || uriString.startsWith("https://", ignoreCase = true)
        if (isHttp && (uriString.contains("wego.here.com", ignoreCase = true) || uriString.contains("here.com", ignoreCase = true))) {
            return parseWegoWebUrl(uriString)
        }

        // Fallback regex match for any coordinate pair in the string
        val matcher = COORD_PATTERN.matcher(uriString)
        if (matcher.find()) {
            val lat = matcher.group(1)?.toDoubleOrNull()
            val lon = matcher.group(2)?.toDoubleOrNull()
            if (lat != null && lon != null) {
                return NavigationTarget(destLat = lat, destLon = lon, rawUri = uriString)
            }
        }

        return NavigationTarget(query = uriString, rawUri = uriString)
    }

    private fun parseGeoUri(uriString: String): NavigationTarget {
        val schemeSpecific = uriString.substringAfter("geo:")
        val parts = schemeSpecific.split("?", limit = 2)
        val mainPart = parts[0]
        val queryPart = parts.getOrNull(1)

        var destLat: Double? = null
        var destLon: Double? = null
        var queryStr: String? = null
        var labelStr: String? = null

        val mainMatcher = COORD_PATTERN.matcher(mainPart)
        if (mainMatcher.find()) {
            val lat = mainMatcher.group(1)?.toDoubleOrNull()
            val lon = mainMatcher.group(2)?.toDoubleOrNull()
            if (lat != null && lon != null && (lat != 0.0 || lon != 0.0)) {
                destLat = lat
                destLon = lon
            }
        }

        if (queryPart != null) {
            val qVal = getQueryParam(queryPart, "q")
            if (qVal != null) {
                val labelIndex = qVal.indexOf('(')
                val cleanQ = if (labelIndex != -1) {
                    val endLabel = qVal.indexOf(')', labelIndex)
                    if (endLabel != -1) {
                        labelStr = qVal.substring(labelIndex + 1, endLabel).trim()
                    }
                    qVal.substring(0, labelIndex).trim()
                } else {
                    qVal.trim()
                }

                val qMatcher = COORD_PATTERN.matcher(cleanQ)
                if (qMatcher.find()) {
                    val lat = qMatcher.group(1)?.toDoubleOrNull()
                    val lon = qMatcher.group(2)?.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        destLat = lat
                        destLon = lon
                    }
                } else if (cleanQ.isNotBlank()) {
                    queryStr = cleanQ
                }
            }
        }

        return NavigationTarget(
            destLat = destLat,
            destLon = destLon,
            query = queryStr,
            label = labelStr,
            rawUri = uriString
        )
    }

    private fun parseGoogleNavigationUri(uriString: String): NavigationTarget {
        val queryPart = uriString.substringAfter("google.navigation:")
        val qVal = getQueryParam(queryPart, "q")
        val modeVal = getQueryParam(queryPart, "mode")

        var destLat: Double? = null
        var destLon: Double? = null
        var queryStr: String? = null

        if (qVal != null) {
            val matcher = COORD_PATTERN.matcher(qVal)
            if (matcher.find()) {
                destLat = matcher.group(1)?.toDoubleOrNull()
                destLon = matcher.group(2)?.toDoubleOrNull()
            } else {
                queryStr = qVal
            }
        }

        val mode = when (modeVal?.lowercase()) {
            "w" -> NavigationMode.WALKING
            "b" -> NavigationMode.BICYCLE
            else -> NavigationMode.DRIVING
        }

        return NavigationTarget(
            destLat = destLat,
            destLon = destLon,
            query = queryStr,
            mode = mode,
            rawUri = uriString
        )
    }

    private fun parseHereCustomUri(uriString: String): NavigationTarget {
        val matches = mutableListOf<Pair<Double, Double>>()
        val matcher = COORD_PATTERN.matcher(uriString)
        while (matcher.find()) {
            val lat = matcher.group(1)?.toDoubleOrNull()
            val lon = matcher.group(2)?.toDoubleOrNull()
            if (lat != null && lon != null) {
                matches.add(Pair(lat, lon))
            }
        }

        return when {
            matches.size >= 2 -> {
                NavigationTarget(
                    originLat = matches[0].first,
                    originLon = matches[0].second,
                    destLat = matches[1].first,
                    destLon = matches[1].second,
                    rawUri = uriString
                )
            }
            matches.size == 1 -> {
                NavigationTarget(
                    destLat = matches[0].first,
                    destLon = matches[0].second,
                    rawUri = uriString
                )
            }
            else -> NavigationTarget(query = uriString, rawUri = uriString)
        }
    }

    private fun parseWegoWebUrl(rawUriString: String): NavigationTarget {
        val parsedUri = try { URI(rawUriString) } catch (e: Exception) { null }
        val path = parsedUri?.path ?: rawUriString.substringAfter("here.com").substringBefore("?")

        val mode = when {
            path.contains("/directions/walk", ignoreCase = true) -> NavigationMode.WALKING
            path.contains("/directions/bicycle", ignoreCase = true) -> NavigationMode.BICYCLE
            path.contains("/directions/public_transport", ignoreCase = true) -> NavigationMode.TRANSIT
            else -> NavigationMode.DRIVING
        }

        val coordsInPath = mutableListOf<Pair<Double, Double>>()
        val segments = path.split("/")

        for (segment in segments) {
            val decoded = try { URLDecoder.decode(segment, "UTF-8") } catch (e: Exception) { segment }
            val matcher = WEGO_SEGMENT_COORD_PATTERN.matcher(decoded)
            if (matcher.find()) {
                val lat = matcher.group(1)?.toDoubleOrNull()
                val lon = matcher.group(2)?.toDoubleOrNull()
                if (lat != null && lon != null) {
                    coordsInPath.add(Pair(lat, lon))
                }
            }
        }

        var queryLat: Double? = null
        var queryLon: Double? = null
        var searchQuery: String? = null

        val queryPart = parsedUri?.query ?: if (rawUriString.contains("?")) rawUriString.substringAfter("?") else null

        if (queryPart != null) {
            val mapParam = getQueryParam(queryPart, "map")
            if (mapParam != null) {
                val matcher = COORD_PATTERN.matcher(mapParam)
                if (matcher.find()) {
                    queryLat = matcher.group(1)?.toDoubleOrNull()
                    queryLon = matcher.group(2)?.toDoubleOrNull()
                }
            }

            val qParam = getQueryParam(queryPart, "q") ?: getQueryParam(queryPart, "dest")
            if (qParam != null) {
                val matcher = COORD_PATTERN.matcher(qParam)
                if (matcher.find()) {
                    queryLat = matcher.group(1)?.toDoubleOrNull()
                    queryLon = matcher.group(2)?.toDoubleOrNull()
                } else {
                    searchQuery = qParam
                }
            }
        }

        return when {
            coordsInPath.size >= 2 -> {
                val origin = coordsInPath.first()
                val dest = coordsInPath.last()
                NavigationTarget(
                    originLat = origin.first,
                    originLon = origin.second,
                    destLat = dest.first,
                    destLon = dest.second,
                    mode = mode,
                    rawUri = rawUriString
                )
            }
            coordsInPath.size == 1 -> {
                val dest = coordsInPath.first()
                NavigationTarget(
                    destLat = dest.first,
                    destLon = dest.second,
                    mode = mode,
                    rawUri = rawUriString
                )
            }
            queryLat != null && queryLon != null -> {
                NavigationTarget(
                    destLat = queryLat,
                    destLon = queryLon,
                    mode = mode,
                    rawUri = rawUriString
                )
            }
            !searchQuery.isNullOrBlank() -> {
                NavigationTarget(
                    query = searchQuery,
                    mode = mode,
                    rawUri = rawUriString
                )
            }
            else -> {
                NavigationTarget(rawUri = rawUriString)
            }
        }
    }

    private fun getQueryParam(queryString: String, paramName: String): String? {
        val params = queryString.split("&")
        for (p in params) {
            val pair = p.split("=", limit = 2)
            if (pair[0].equals(paramName, ignoreCase = true) && pair.size > 1) {
                return try { URLDecoder.decode(pair[1], "UTF-8") } catch (e: Exception) { pair[1] }
            }
        }
        return null
    }
}

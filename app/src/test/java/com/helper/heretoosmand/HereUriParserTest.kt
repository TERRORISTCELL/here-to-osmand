package com.helper.heretoosmand

import org.junit.Assert.*
import org.junit.Test

class HereUriParserTest {

    @Test
    fun testWegoDirectionsDriveUrlWithOriginAndDestination() {
        val url = "https://wego.here.com/directions/drive/Berlin:52.5200,13.4050/Brandenburg-Gate:52.5163,13.3777"
        val target = HereUriParser.parse(url)

        assertNotNull(target.destLat)
        assertNotNull(target.destLon)
        assertEquals(52.5163, target.destLat!!, 0.0001)
        assertEquals(13.3777, target.destLon!!, 0.0001)

        assertNotNull(target.originLat)
        assertNotNull(target.originLon)
        assertEquals(52.5200, target.originLat!!, 0.0001)
        assertEquals(13.4050, target.originLon!!, 0.0001)

        assertEquals(NavigationMode.DRIVING, target.mode)
    }

    @Test
    fun testWegoDirectionsWalkUrlSingleLocation() {
        val url = "https://wego.here.com/directions/walk/Target-Location:52.5163,13.3777"
        val target = HereUriParser.parse(url)

        assertNotNull(target.destLat)
        assertEquals(52.5163, target.destLat!!, 0.0001)
        assertEquals(13.3777, target.destLon!!, 0.0001)
        assertNull(target.originLat)
        assertEquals(NavigationMode.WALKING, target.mode)
    }

    @Test
    fun testWegoLocationUrl() {
        val url = "https://wego.here.com/location/Brandenburg-Gate:52.5163,13.3777"
        val target = HereUriParser.parse(url)

        assertNotNull(target.destLat)
        assertEquals(52.5163, target.destLat!!, 0.0001)
        assertEquals(13.3777, target.destLon!!, 0.0001)
    }

    @Test
    fun testWegoMapQueryUrl() {
        val url = "https://wego.here.com/?map=52.5163,13.3777,15,normal"
        val target = HereUriParser.parse(url)

        assertNotNull(target.destLat)
        assertEquals(52.5163, target.destLat!!, 0.0001)
        assertEquals(13.3777, target.destLon!!, 0.0001)
    }

    @Test
    fun testHereCustomSchemeRoute() {
        val uri = "here-route://52.5200,13.4050/52.5163,13.3777"
        val target = HereUriParser.parse(uri)

        assertEquals(52.5200, target.originLat!!, 0.0001)
        assertEquals(13.4050, target.originLon!!, 0.0001)
        assertEquals(52.5163, target.destLat!!, 0.0001)
        assertEquals(13.3777, target.destLon!!, 0.0001)
    }

    @Test
    fun testHereCustomSchemeLocation() {
        val uri = "here-location://52.5163,13.3777"
        val target = HereUriParser.parse(uri)

        assertEquals(52.5163, target.destLat!!, 0.0001)
        assertEquals(13.3777, target.destLon!!, 0.0001)
    }

    @Test
    fun testGeoUriWithCoordinatesAndLabel() {
        val uri = "geo:52.5163,13.3777?q=52.5163,13.3777(Brandenburg+Gate)"
        val target = HereUriParser.parse(uri)

        assertEquals(52.5163, target.destLat!!, 0.0001)
        assertEquals(13.3777, target.destLon!!, 0.0001)
        assertEquals("Brandenburg Gate", target.label)
    }

    @Test
    fun testGoogleNavigationUriWithBicycleMode() {
        val uri = "google.navigation:q=52.5163,13.3777&mode=b"
        val target = HereUriParser.parse(uri)

        assertEquals(52.5163, target.destLat!!, 0.0001)
        assertEquals(13.3777, target.destLon!!, 0.0001)
        assertEquals(NavigationMode.BICYCLE, target.mode)
    }
}

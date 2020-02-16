package ru.rst.rescuesmstracker

import android.location.Location
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.rescuesmstracker.settings.RSTPreferences

@RunWith(RobolectricTestRunner::class)
class FormatTests {

    @Test
    fun formatDegrees1() {
        val location = Location("test_provider")
        location.latitude = 25.222222
        location.longitude = 25.222222
        Assert.assertEquals("N25.22222° E025.22222°", RSTPreferences.CoordsFormat.FORMAT_DEGREES.format(location))
    }

    @Test
    fun formatDegrees2() {
        val location = Location("test_provider")
        location.latitude = 125.222222
        location.longitude = 125.222222
        Assert.assertEquals("N125.22222° E125.22222°", RSTPreferences.CoordsFormat.FORMAT_DEGREES.format(location))
    }

    @Test
    fun formatDegrees3() {
        val location = Location("test_provider")
        location.latitude = 125.2
        location.longitude = 125.2
        Assert.assertEquals("N125.20000° E125.20000°", RSTPreferences.CoordsFormat.FORMAT_DEGREES.format(location))
    }

    @Test
    fun formatMinutes1() {
        val location = Location("test_provider")
        location.latitude = 25.222222
        location.longitude = 25.222222
        Assert.assertEquals("N25°13.33332' E025°13.33332'", RSTPreferences.CoordsFormat.FORMAT_MINUTES.format(location))
    }

    @Test
    fun formatMinutes2() {
        val location = Location("test_provider")
        location.latitude = 125.222222
        location.longitude = 125.222222
        Assert.assertEquals("N125°13.33332' E125°13.33332'", RSTPreferences.CoordsFormat.FORMAT_MINUTES.format(location))
    }

    @Test
    fun formatMinutes3() {
        val location = Location("test_provider")
        location.latitude = 125.222
        location.longitude = 125.222
        Assert.assertEquals("N125°13.32000' E125°13.32000'", RSTPreferences.CoordsFormat.FORMAT_MINUTES.format(location))
    }

    @Test
    fun formatSeconds1() {
        val location = Location("test_provider")
        location.latitude = 25.123456
        location.longitude = 25.123456
        Assert.assertEquals("N25°7'24.44160\" E025°7'24.44160\"", RSTPreferences.CoordsFormat.FORMAT_SECONDS.format(location))
    }

    @Test
    fun formatSeconds2() {
        val location = Location("test_provider")
        location.latitude = 125.123456
        location.longitude = 125.123456
        Assert.assertEquals("N125°7'24.44160\" E125°7'24.44160\"", RSTPreferences.CoordsFormat.FORMAT_SECONDS.format(location))
    }

    @Test
    fun negativeLatitude() {
        val location = Location("test_provider")
        location.latitude = -25.222222
        location.longitude = 25.222222
        Assert.assertEquals("S25.22222° E025.22222°", RSTPreferences.CoordsFormat.FORMAT_DEGREES.format(location))
        Assert.assertEquals("S25°13'19.99920\" E025°13'19.99920\"", RSTPreferences.CoordsFormat.FORMAT_SECONDS.format(location))
        Assert.assertEquals("S25°13.33332' E025°13.33332'", RSTPreferences.CoordsFormat.FORMAT_MINUTES.format(location))
        Assert.assertEquals("maps.google.com/?q=-25.22222,025.22222", RSTPreferences.CoordsFormat.FORMAT_GOOGLE_MAPS.format(location))
        Assert.assertEquals("<NavitelLoc>S25.22222 E025.22222<N>(i'm here)", RSTPreferences.CoordsFormat.FORMAT_NAVITEL.format(location))
    }

    @Test
    fun negativeLongitude() {
        val location = Location("test_provider")
        location.latitude = 51.509865
        location.longitude = -0.118092
        Assert.assertEquals("N51.50986° W0.11809°", RSTPreferences.CoordsFormat.FORMAT_DEGREES.format(location))
        Assert.assertEquals("N51°30'35.51400\" W0°7'5.13120\"", RSTPreferences.CoordsFormat.FORMAT_SECONDS.format(location))
        Assert.assertEquals("N51°30.59190' W0°7.08552'", RSTPreferences.CoordsFormat.FORMAT_MINUTES.format(location))
        Assert.assertEquals("maps.google.com/?q=51.50986,-0.11809", RSTPreferences.CoordsFormat.FORMAT_GOOGLE_MAPS.format(location))
        Assert.assertEquals("<NavitelLoc>N51.50986 W0.11809<N>(i'm here)", RSTPreferences.CoordsFormat.FORMAT_NAVITEL.format(location))
    }

    @Test
    fun integerCoords() {
        val location = Location("test_provider")
        location.latitude = 12.0
        location.longitude = 34.0
        Assert.assertEquals("N12.00000° E034.00000°", RSTPreferences.CoordsFormat.FORMAT_DEGREES.format(location))
        Assert.assertEquals("N12°0'0.00000\" E034°0'0.00000\"", RSTPreferences.CoordsFormat.FORMAT_SECONDS.format(location))
        Assert.assertEquals("N12°0.00000' E034°0.00000'", RSTPreferences.CoordsFormat.FORMAT_MINUTES.format(location))
        Assert.assertEquals("maps.google.com/?q=12.00000,034.00000", RSTPreferences.CoordsFormat.FORMAT_GOOGLE_MAPS.format(location))
        Assert.assertEquals("<NavitelLoc>N12.00000 E034.00000<N>(i'm here)", RSTPreferences.CoordsFormat.FORMAT_NAVITEL.format(location))
    }
}
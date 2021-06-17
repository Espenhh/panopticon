package pro.panopticon.client.sensor.impl

import org.junit.Test
import java.time.ZonedDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValueChainSensorTest {
    @Test
    fun `disabled periods`() {
        val night = ValueChainSensor.DisabledHours(23, 5)

        val time2259 = ZonedDateTime.parse("2021-06-17T22:59:59+02:00[Europe/Oslo]")
        val time2300 = ZonedDateTime.parse("2021-06-17T23:00:00+02:00[Europe/Oslo]")
        val time2301 = ZonedDateTime.parse("2021-06-17T23:00:01+02:00[Europe/Oslo]")
        val time0459 = ZonedDateTime.parse("2021-06-17T04:59:59+02:00[Europe/Oslo]")
        val time0500 = ZonedDateTime.parse("2021-06-17T05:00:00+02:00[Europe/Oslo]")
        val time0501 = ZonedDateTime.parse("2021-06-17T05:00:01+02:00[Europe/Oslo]")

        assertFalse(night.isInsideDeadPeriod(time2259), "22:59 is inside $night")
        assertTrue(night.isInsideDeadPeriod(time2300), "23:00 is outside $night")
        assertTrue(night.isInsideDeadPeriod(time2301), "23:01 is outside $night")
        assertTrue(night.isInsideDeadPeriod(time0459), "04:59 is outside $night")
        assertFalse(night.isInsideDeadPeriod(time0500), "05:00 is inside $night")
        assertFalse(night.isInsideDeadPeriod(time0501), "05:01 is inside $night")

        val day = ValueChainSensor.DisabledHours(5, 23)

        assertFalse(day.isInsideDeadPeriod(time0459), "04:59 is inside $day")
        assertTrue(day.isInsideDeadPeriod(time0500), "05:00 is outside $day")
        assertTrue(day.isInsideDeadPeriod(time0501), "05:01 is outside $day")
        assertTrue(day.isInsideDeadPeriod(time2259), "22:59 is outside $day")
        assertFalse(day.isInsideDeadPeriod(time2300), "23:00 is inside $day")
        assertFalse(day.isInsideDeadPeriod(time2301), "23:01 is inside $day")
    }
}

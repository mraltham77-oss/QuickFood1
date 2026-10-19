package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.remote.SerovalCodec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QuickFood", appName)
    }

    @Test
    fun `test Seroval serialization`() {
        val payload = mapOf(
            "orderType" to "delivery",
            "name" to "أحمد",
            "phone" to "01012345678",
            "lines" to listOf(
                mapOf("dishId" to "smash", "qty" to 1)
            )
        )
        val serialized = SerovalCodec.serialize(mapOf("data" to payload))
        assertTrue(serialized.contains("\"f\":127"))
        assertTrue(serialized.contains("smash"))
    }

    @Test
    fun `test Seroval deserialization`() {
        val sampleJson = """
            {"t":10,"i":0,"p":{"k":["ok","code"],"v":[{"t":2,"s":2},{"t":1,"s":"QF-TEST12"}]}}
        """.trimIndent()
        val parsed = SerovalCodec.parse(sampleJson) as? Map<*, *>
        assertNotNull(parsed)
        assertEquals(true, parsed?.get("ok"))
        assertEquals("QF-TEST12", parsed?.get("code"))
    }
}

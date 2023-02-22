package redis_kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppTest {
    @Test fun appHasAGreeting() {
        val classUnderTest = App()
        assertEquals(classUnderTest.doRedisStuff(), true)
    }
}

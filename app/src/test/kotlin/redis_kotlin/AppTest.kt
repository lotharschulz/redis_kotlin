package redis_kotlin

import kotlin.test.Test
import kotlin.test.assertEquals

class AppTest {
    @Test fun appHasAPrintHelper() {
        val classUnderTest = App()
        assertEquals(classUnderTest.printHelper("a"), true)
    }
}

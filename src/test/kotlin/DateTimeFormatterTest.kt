import dev.limebeck.openconf.common.DEFAULT_FORMATTER
import dev.limebeck.openconf.common.format
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeFormatterTest {
    @Test
    fun `Format time`() {
        val time = Instant.parse("2024-10-21T12:12:00Z")
        val formatted = time.format(DEFAULT_FORMATTER)
        assertEquals("2024-10-21 15:12:00", formatted)
    }
}
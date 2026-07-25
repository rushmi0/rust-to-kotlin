import org.example.libkmp.add
import kotlin.test.Test
import kotlin.test.assertEquals

class JvmRustlinTest {
    @Test
    fun addsAcrossFfi() {
        assertEquals(4uL, add(2uL, 2uL))
    }
}

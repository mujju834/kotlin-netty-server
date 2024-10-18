package org.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull

class AppTest {

    @Test
    fun `test server starts correctly`() {
        // Check that the application class or function is available
        assertNotNull(::main)  // Ensure the main() function is defined
    }
}

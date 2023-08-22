package ispd.gui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

class TextSupplierTest {

    @Test
    void givenNullBundle_whenConstructed_thenThrowsNpe () {
        assertThrows(NullPointerException.class, () -> TextSupplier.setInstance(null));
    }
}
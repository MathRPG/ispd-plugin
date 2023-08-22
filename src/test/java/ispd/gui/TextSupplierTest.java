package ispd.gui;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;

class TextSupplierTest {

    @Test
    void givenNullBundle_whenConstructed_thenThrowsNpe () {
        assertThrows(NullPointerException.class, () -> TextSupplier.setInstance(null));
    }

    @Test
    void givenBundleWithKey_whenGetText_returnsTextInBundle () {
        final var key    = "key";
        final var value  = "value";
        final var bundle = this.bundleFromMap(Map.of(key, value));

        TextSupplier.setInstance(bundle);

        assertThat(TextSupplier.getText(key), is(value));
    }

    private ResourceBundle bundleFromMap (final Map<String, String> map) {
        return new MapResourceBundle(map);
    }

    private static class MapResourceBundle extends ResourceBundle {

        private final Map<String, String> map;

        public MapResourceBundle (final Map<String, String> map) {
            this.map = map;
        }

        @Override
        protected Object handleGetObject (
            final @NotNull String s
        ) {
            return this.map.get(s);
        }

        @Override
        public @NotNull Enumeration<String> getKeys () {
            return Collections.enumeration(this.map.keySet());
        }
    }
}
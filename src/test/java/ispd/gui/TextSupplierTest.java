package ispd.gui;

import static java.util.Collections.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.*;

import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;

class TextSupplierTest {

    private static final String KEY = "key";

    private static final String VALUE = "value";

    @BeforeAll
    static void givenNoBundle_whenGetText_thenThrowsMissingResourceException () {
        assertThrowsExactly(
            TextSupplier.MissingInstanceException.class,
            () -> TextSupplier.getText(KEY)
        );
    }

    private static Supplier<String> anyStringSupplier () {
        return any();
    }

    @Test
    void givenNullBundle_whenSetInstance_thenThrowsNullPointerException () {
        assertThrows(NullPointerException.class, () -> TextSupplier.setInstance(null));
    }

    @Test
    void givenNullLogger_whenSetInstance_thenThrowsNullPointerException () {
        assertThrows(
            NullPointerException.class,
            () -> TextSupplier.setInstance(MapBundle.EMPTY, null)
        );
    }

    @Test
    void givenBundleWithKey_whenGetText_returnsValueInBundle () {
        final var bundle = new MapBundle(Map.of(KEY, VALUE));

        TextSupplier.setInstance(bundle);

        assertThat(TextSupplier.getText(KEY), is(VALUE));
    }

    @Test
    void givenBundleWithoutKey_whenGetText_thenLogsWarningAndReturnsKey () {
        final var logger = mock(Logger.class);

        TextSupplier.setInstance(MapBundle.EMPTY, logger);

        assertThat(TextSupplier.getText(KEY), is(KEY));
        verify(logger, times(1)).warning(anyStringSupplier());
    }

    private static final class MapBundle extends ResourceBundle {

        private static final ResourceBundle EMPTY = new MapBundle(emptyMap());

        private final Map<String, String> map;

        private MapBundle (final Map<String, String> map) {
            super();
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
            return enumeration(this.map.keySet());
        }
    }
}
package ispd.gui.text;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.*;

class TextSupplierTest {

    private static final String KEY = "key";

    private static final String VALUE = "value";

    @BeforeAll
    static void givenNoBundle_whenGetText_thenThrowsNoConfiguredBundleException () {
        assertThatThrownBy(() -> TextSupplier.getText(KEY))
            .isInstanceOf(NoConfiguredBundleException.class);
    }

    private static void assertThrowsNpe (final Executable executable) {
        assertThrows(NullPointerException.class, executable);
    }

    private static Supplier<String> messageContainsKey () {
        return argThat(sup -> sup.get().contains(KEY));
    }

    @Test
    void givenNullBundle_whenSetInstance_thenThrowsNpe () {
        assertThrowsNpe(() -> TextSupplier.configure(null));
    }

    @Test
    void givenNullLogger_whenSetInstance_thenThrowsNpe () {
        assertThrowsNpe(() -> TextSupplier.configure(MapBundle.EMPTY, null));
    }

    @Test
    void givenNullKey_whenGetText_thenThrowsNpe () {
        TextSupplier.configure(MapBundle.EMPTY);
        assertThrowsNpe(() -> TextSupplier.getText(null));
    }

    @Test
    void givenBundleWithoutKey_whenGetText_thenLogsWarningAndReturnsKey () {
        final var logger = mock(Logger.class);

        TextSupplier.configure(MapBundle.EMPTY, logger);

        assertThat(TextSupplier.getText(KEY), is(KEY));
        verify(logger, times(1)).warning(messageContainsKey());
    }

    @Test
    void givenBundleWithKey_whenGetText_returnsValueInBundle () {
        final var bundle = new MapBundle(Map.of(KEY, VALUE));

        TextSupplier.configure(bundle);

        assertThat(TextSupplier.getText(KEY), is(VALUE));
    }

    private static final class MapBundle extends ResourceBundle {

        private static final ResourceBundle EMPTY = new MapBundle(emptyMap());

        private final Map<String, String> map;

        private MapBundle (final Map<String, String> map) {
            super();
            this.map = map;
        }

        @Override
        protected Object handleGetObject (final @NotNull String s) {
            return this.map.get(s);
        }

        @Override
        public @NotNull Enumeration<String> getKeys () {
            return enumeration(this.map.keySet());
        }
    }
}
package ispd.gui.drawing;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import ispd.gui.drawing.ruler.*;
import org.junit.jupiter.api.*;

final class TestRulerUnit {

    @Test
    void testNextUnit () {
        assertThat(RulerUnit.CENTIMETERS.nextUnit(), is(equalTo(RulerUnit.INCHES)));
        assertThat(RulerUnit.INCHES.nextUnit(), is(equalTo(RulerUnit.CENTIMETERS)));
    }
}

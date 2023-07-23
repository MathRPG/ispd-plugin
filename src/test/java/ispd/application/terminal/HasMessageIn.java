package ispd.application.terminal;

import org.hamcrest.*;

public class HasMessageIn <T extends Throwable> extends TypeSafeMatcher<T> {

    private final String str;

    private HasMessageIn (final String str) {
        this.str = str;
    }

    public static <T extends Throwable> Matcher<T> hasMessageIn (final String str) {
        return new HasMessageIn<>(str);
    }

    @Override
    public void describeTo (final Description description) {
        description.appendText("has message in given string");
    }

    @Override
    protected boolean matchesSafely (final T item) {
        return this.str.contains(item.getMessage());
    }
}

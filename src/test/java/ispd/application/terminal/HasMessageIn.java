package ispd.application.terminal;

import org.hamcrest.*;

public class HasMessageIn <E extends Exception> extends TypeSafeMatcher<E> {

    private final String str;

    private HasMessageIn (final String str) {
        this.str = str;
    }

    public static <T extends Exception> Matcher<T> hasMessageIn (final String str) {
        return new HasMessageIn<>(str);
    }

    @Override
    public void describeTo (final Description description) {
        description.appendText("has message in given string");
    }

    @Override
    protected boolean matchesSafely (final E item) {
        return this.str.contains(item.getMessage());
    }
}

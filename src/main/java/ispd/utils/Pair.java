package ispd.utils;

/**
 * A {@link Pair} is used to simultaneously store two values of possibly different types.
 * <p>
 * The values can be acessed via {@link #first()} and {@link #second()}, respectively.
 *
 * @param <T>
 *     The type for the first value stored.
 * @param <U>
 *     The type for the second value stored.
 * @param first
 *     The pair's first value, of type {@code T}.
 * @param second
 *     The pair's second value, of type {@code U}.
 */
public record Pair <T, U>(T first, U second) {

}

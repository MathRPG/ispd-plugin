package ispd.utils;

import java.util.function.IntSupplier;

/**
 * Generates a (practically) infinite number of sequential integers.
 * <p>
 * Simply call {@link #getAsInt()} to get the next integer in line.<br>
 * The first generated integer after construction is {@code 0}.
 * <p>
 * Example:
 *
 * <pre>{@code
 * final var supplier = new SequentialIntSupplier();
 * final int x = supplier.getAsInt(); // 0
 * final int y = supplier.getAsInt(); // 1
 * // And so on and so forth...
 * }</pre>
 */
public class SequentialIntSupplier implements IntSupplier {

    private int nextValue = 0;

    @Override
    public int getAsInt () {
        final var returnValue = this.nextValue;
        this.nextValue++;
        return returnValue;
    }
}
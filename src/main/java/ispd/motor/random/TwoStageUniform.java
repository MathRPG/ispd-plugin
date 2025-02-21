package ispd.motor.random;

import jdk.jfr.*;

/**
 * Utility class to contain information about computing or communication sizes.
 * They are expressed as an inclusive range, with an {@link #intervalSplit
 * interval split} and {@link #firstIntervalProbability probability for the
 * first interval} (parameters for a two-stage uniform distribution).
 *
 * @see Distribution#twoStageUniform(double, double, double, double)
 */
public record TwoStageUniform(
        double minimum, double intervalSplit, double maximum, @Percentage double firstIntervalProbability
) {

    @Percentage
    private static final double EVEN_PROBABILITY = 0.5d;

    /**
     * Construct an instance with all values set to 0.
     */
    public TwoStageUniform () {
        this(0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Construct an instance with given {@code minimum} and {@code maximum}
     * values, and with {@link #intervalSplit split} and
     * {@link #firstIntervalProbability probability} set to represent a
     * <b>uniform distribution</b>.
     *
     * @param minimum
     *         distribution minimum
     * @param maximum
     *         distribution maximum
     */
    public TwoStageUniform (final double minimum, final double maximum) {
        this(minimum, (minimum + maximum) / 2.0, maximum, EVEN_PROBABILITY);
    }

    /**
     * Construct an instance with given {@code minimum}, {@code intervalSplit
     * } and {@code maximum} values, and with {@code firstIntervalProbability
     * } set to the default to permit and even probability between the two
     * intervals.
     *
     * @param minimum
     *         distribution minimum
     * @param intervalSplit
     *         distribution split point
     * @param maximum
     *         distribution maximum
     */
    public TwoStageUniform (final double minimum, final double intervalSplit, final double maximum) {
        this(minimum, intervalSplit, maximum, EVEN_PROBABILITY);
    }

    /**
     * Generate a two-stage uniform with parameters from the fields of the
     * instance, using the given {@link Distribution}.
     *
     * @param random
     *         {@link Distribution} to generate a random value.
     *
     * @return value in the interval from [{@link #minimum()},
     *         {@link #maximum()}, with probability following a two-stage uniform
     *         distribution.
     */
    public double generateValue (final Distribution random) {
        return random.twoStageUniform(this.minimum, this.intervalSplit, this.maximum, this.firstIntervalProbability);
    }

    /**
     * Makes a string with the values of the fields of this instance, in a
     * human-readable manner.
     *
     * @return {@link String} representing how this instance is initialized,
     *         in a human-readable format.
     */
    @Override
    public String toString () {
        return "TwoStageUniform{min=%f, med=%f, max=%f, prob=%f}".formatted(
                this.minimum, this.intervalSplit, this.maximum, this.firstIntervalProbability
        );
    }
}
package ispd.application.terminal;

/**
 * Provides a way to access the current system time.
 *
 * @see SystemTimeProvider#getSystemTime()
 */
public interface SystemTimeProvider {

    /**
     * Get the current system time.
     *
     * @return The current system time, expressed in <i>milliseconds</i>.
     */
    default long getSystemTime () {
        return System.currentTimeMillis();
    }
}

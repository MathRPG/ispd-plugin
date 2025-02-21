package ispd.gui.utils;

/**
 * Represents an interval of time during which a machine was executing. Note: this class has a
 * natural ordering that is inconsistent with equals.
 */
public class ParesOrdenadosUso implements Comparable<ParesOrdenadosUso> {

    private final double start;

    private final double end;

    public ParesOrdenadosUso (final double start, final double end) {
        this.start = start;
        this.end   = end;
    }

    @Override
    public String toString () {
        return "%s %s".formatted(this.start, this.end);
    }

    @Override
    public int compareTo (final ParesOrdenadosUso o) {
        return Double.compare(this.start, o.start);
    }

    public double getFim () {
        return this.end;
    }

    public double getInicio () {
        return this.start;
    }
}
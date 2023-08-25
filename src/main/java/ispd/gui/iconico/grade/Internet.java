package ispd.gui.iconico.grade;

import static ispd.gui.TextSupplier.*;

import java.awt.*;

public class Internet extends VertexGridItem {

    /**
     * It represents the bandwidth.
     */
    private double bandwidth;

    /**
     * It represents the latency.
     */
    private double latency;

    /**
     * It represents the load factor.
     */
    private double loadFactor;

    /**
     * Constructor of  which specifies the x-coordinate and y-coordinate (in
     * cartesian coordinates), the local and global identifiers.
     *
     * @param x
     *     the x-coordinate in cartesian coordinates
     * @param y
     *     the y-coordinate in cartesian coordinates
     * @param localId
     *     the local identifier
     * @param globalId
     *     the global identifier
     */
    public Internet (final int x, final int y, final int localId, final int globalId) {
        super(localId, globalId, "net", x, y);
    }

    /**
     * Return the internet attributes.
     *
     * @return the internet attributes
     */
    @Override
    public String makeDescription () {
        return (
            "%s %d<br>%s %d<br>%s: %s<br>%s %d<br>%s %d<br>%s: %s<br>%s: %s<br>%s: %s"
        ).formatted(
            getText("Local ID:"),
            this.id.getLocalId(),
            getText("Global ID:"),
            this.id.getGlobalId(),
            getText("Label"),
            this.id.getName(),
            getText("X-coordinate:"),
            this.getX(),
            getText("Y-coordinate:"),
            this.getY(),
            getText("Bandwidth"),
            this.bandwidth,
            getText("Latency"),
            this.latency,
            getText("Load Factor"),
            this.loadFactor
        );
    }

    @Override
    public Internet makeCopy (
        final int mousePosX,
        final int mousePosY,
        final int globalId,
        final int localId
    ) {
        final var internet = new Internet(mousePosX, mousePosY, globalId, localId);
        internet.bandwidth = this.bandwidth;
        internet.loadFactor = this.loadFactor;
        internet.latency = this.latency;
        internet.checkConfiguration();
        return internet;
    }

    /**
     * Returns the internet image.
     *
     * @return the internet image
     */
    @Override
    public Image getImage () {
        return GridDrawing.INTERNET_ICON;
    }

    /**
     * It checks if the current internet configuration is well configured; if so, then
     * {@link #configured} is set to {@code true}; otherwise, is set to {@code false}.
     */
    private void checkConfiguration () {
        this.configured = this.bandwidth > 0 && this.latency > 0;
    }

    /**
     * Returns the bandwidth.
     *
     * @return the bandwidth
     */
    public double getBandwidth () {
        return this.bandwidth;
    }

    /**
     * It sets the bandwidth.
     *
     * @param bandwidth
     *     the bandwidth to be set
     */
    public void setBandwidth (final double bandwidth) {
        this.bandwidth = bandwidth;
        this.checkConfiguration();
    }

    /**
     * Returns the load factor.
     *
     * @return the load factor
     */
    public double getLoadFactor () {
        return this.loadFactor;
    }

    /**
     * It sets the load factor.
     *
     * @param loadFactor
     *     the load factor to be set
     */
    public void setLoadFactor (final double loadFactor) {
        this.loadFactor = loadFactor;
    }

    /**
     * Returns the latency.
     *
     * @return the latency
     */
    public double getLatency () {
        return this.latency;
    }

    /**
     * It sets the latency.
     *
     * @param latency
     *     the latency to be set to
     */
    public void setLatency (final double latency) {
        this.latency = latency;
        this.checkConfiguration();
    }
}

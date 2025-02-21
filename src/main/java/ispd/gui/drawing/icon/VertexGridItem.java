package ispd.gui.drawing.icon;

import ispd.gui.drawing.*;
import java.awt.*;
import java.util.*;

public abstract class VertexGridItem extends Vertex implements GridItem {

    /**
     * It contains the grid item identifier.
     */
    protected final GridItemIdentifier id;

    /**
     * It stores all of inbound connections.
     */
    private final Set<GridItem> inboundConnections;

    /**
     * It stores all of outbound connections.
     */
    private final Set<GridItem> outboundConnections;

    /**
     * It stores the state of whether this vertex is configured or not. Therefore, this stores
     * {@code true} if vertex is configured; otherwise, it stores {@code false}.
     * <p>
     * The state of this variable is {@code false} by default, however, it can be changed by
     * attribution or at construction.
     */
    protected boolean configured;

    /**
     * Constructor of  which specifies the local, global and name identifiers,
     * as well as, the X and Y coordinates.
     *
     * @param localId
     *     the local id
     * @param globalId
     *     the global id
     * @param name
     *     the name
     * @param x
     *     the vertex grid item x-coordinate in cartesian coordinates
     * @param y
     *     the vertex grid item y-coordinate in cartesian coordinates
     */
    protected VertexGridItem (
        final int localId,
        final int globalId,
        final String name,
        final Integer x,
        final Integer y
    ) {
        this(localId, globalId, name, x, y, false);
    }

    /**
     * Constructor of  which specifies the local, global and name identifiers,
     * as well as, the X and Y coordinates and whether is selected.
     *
     * @param localId
     *     the local id
     * @param globalId
     *     the global id
     * @param name
     *     the name
     * @param x
     *     the vertex grid item x-coordinate in cartesian coordinates
     * @param y
     *     the vertex grid item y-coordinate in cartesian coordinates
     * @param selected
     *     whether is selected
     */
    protected VertexGridItem (
        final int localId, final int globalId, final String name, final Integer x, final Integer y,
        final boolean selected
    ) {
        super(x, y, selected);
        this.id                  = new GridItemIdentifier(localId, globalId, name + globalId);
        this.inboundConnections  = new HashSet<>();
        this.outboundConnections = new HashSet<>();
    }

    @Override
    public void draw (final Graphics g) {
        final var configuredStatusImage =
            this.configured ? DrawingArea.GREEN_ICON : DrawingArea.RED_ICON;

        g.drawImage(this.getImage(), this.getX() - 15, this.getY() - 15, null);
        g.drawImage(configuredStatusImage, this.getX() + 15, this.getY() + 15, null);

        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(this.id.getGlobalId()), this.getX(), this.getY() + 30);

        /* If the icon is active, then a margin is drawn */
        if (this.isSelected()) {
            final var offset     = this.getOffset();
            final var squareSize = 34;

            g.setColor(Color.RED);
            g.drawRect(this.getX() - offset,
                       this.getY() - offset,
                       squareSize, squareSize
            );
        }
    }

    /**
     * Returns {@code true} if this grid item is contained at the given x-coordinate and
     * y-coordinate (in cartesian coordinates) plus a <em>offset</em>. Otherwise, {@code false} is
     * returned.
     *
     * @param x
     *     the X-coordinate
     * @param y
     *     the Y-coordinate
     *
     * @return {@code true} if this grid item is contained at the given coordinates; otherwise
     * {@code false} is returned.
     */
    @Override
    public boolean contains (final int x, final int y) {
        final var offset = this.getOffset();
        return (x > this.getX() - offset && x < this.getX() + offset) &&
               (y > this.getY() - offset && y < this.getY() + offset);
    }

    @Override
    public GridItemIdentifier getId () {
        return this.id;
    }

    @Override
    public Set<GridItem> getInboundConnections () {
        return this.inboundConnections;
    }

    @Override
    public Set<GridItem> getOutboundConnections () {
        return this.outboundConnections;
    }

    @Override
    public boolean isConfigured () {
        return this.configured;
    }

    /**
     * Returns this grid item offset.
     *
     * @return this grid item offset
     *
     * @apiNote The offset represents a <em>margin of error</em> to state whether this grid item is
     * contained at a given x-coordinate and y-coordinate in {@link #contains(int, int)} method.
     */
    private int getOffset () {
        return 17;
    }
}
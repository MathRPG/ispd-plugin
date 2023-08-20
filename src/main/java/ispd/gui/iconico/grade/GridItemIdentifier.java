package ispd.gui.iconico.grade;

public class GridItemIdentifier {

    /**
     * It represents the {@link GridItem} local identifier.
     */
    private final Integer localId;

    /**
     * It represents the {@link GridItem} global identifier.
     */
    private final Integer globalId;

    /**
     * It represents the {@link GridItem} name identifier.
     */
    private String name;

    /**
     * Constructor of  which specifies the local, global and name
     * identifiers.
     *
     * @param localId
     *     the local identifier
     * @param globalId
     *     the global identifier
     * @param name
     *     the name identifier
     */
    public GridItemIdentifier (final int localId, final int globalId, final String name) {
        this.localId  = localId;
        this.globalId = globalId;
        this.name     = name;
    }

    /**
     * Returns the local id.
     *
     * @return the local id
     */
    public Integer getLocalId () {
        return this.localId;
    }

    /**
     * Returns the global identifier
     *
     * @return the global identifier
     */
    public Integer getGlobalId () {
        return this.globalId;
    }

    /**
     * Returns the name identifier
     *
     * @return the name identifier
     */
    public String getName () {
        return this.name;
    }

    /**
     * It sets the name
     *
     * @param name
     *     the name to be set
     */
    public void setName (final String name) {
        this.name = name;
    }
}

package ispd.arquivo.xml;

public enum SimulationMode {
    DEFAULT((byte) 0, "default"),
    OPTIMISTIC((byte) 1, "optimistic"),
    GRAPHICAL((byte) 2, "graphical");

    public final byte   asInt;
    public final String xmlName;

    SimulationMode (final byte i, final String xmlName) {
        this.asInt   = i;
        this.xmlName = xmlName;
    }

    public boolean hasName (final String modeName) {
        return this.xmlName.equals(modeName);
    }
}

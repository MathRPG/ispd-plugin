package ispd.arquivo.xml;

import java.util.*;
import org.jetbrains.annotations.*;

public enum SimulationMode {
    DEFAULT("default"),
    OPTIMISTIC("optimistic"),
    GRAPHICAL("graphical");

    public final @NonNls String xmlName;

    SimulationMode (final String xmlName) {
        this.xmlName = xmlName;
    }

    public static SimulationMode fromString (final String s) {
        return Arrays.stream(values())
            .filter(t -> t.xmlName.equals(s))
            .findFirst()
            .orElseThrow(() -> new IllegalSimulationModeException(s));
    }
}

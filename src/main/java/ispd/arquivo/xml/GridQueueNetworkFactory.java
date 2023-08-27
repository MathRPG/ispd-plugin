package ispd.arquivo.xml;

import ispd.arquivo.xml.models.builders.*;
import ispd.arquivo.xml.utils.*;
import ispd.motor.queues.*;
import org.w3c.dom.*;

public enum GridQueueNetworkFactory {
    ;

    /**
     * Convert an iconic model into a queue network, usable in the simulation motor.
     *
     * @param document
     *     Object from xml with modeled computational grid
     *
     * @return Simulable queue network, in accordance to given model
     */
    public static GridQueueNetwork fromDocument (final Document document) {
        return new GridQueueNetworkParser().parseDocument(new WrappedDocument(document)).build();
    }
}

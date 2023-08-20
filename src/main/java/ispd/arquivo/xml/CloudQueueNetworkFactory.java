package ispd.arquivo.xml;

import ispd.arquivo.xml.models.builders.*;
import ispd.arquivo.xml.utils.*;
import ispd.motor.filas.*;
import org.w3c.dom.*;

public enum CloudQueueNetworkFactory {
    ;

    /**
     * Convert an iconic model into a cloud queue network, usable in the cloud simulation motor.
     *
     * @param document
     *     Object from xml with modeled computational grid
     *
     * @return Simulable cloud queue network, in accordance to given model
     */
    public static RedeDeFilasCloud fromDocument (final Document document) {
        return (RedeDeFilasCloud) new CloudQueueNetworkParser()
            .parseDocument(new WrappedDocument(document))
            .build();
    }
}

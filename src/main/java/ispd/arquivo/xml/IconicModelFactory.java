package ispd.arquivo.xml;

import ispd.arquivo.xml.models.builders.*;
import ispd.arquivo.xml.utils.*;
import ispd.gui.drawing.*;
import ispd.gui.drawing.icon.*;
import java.util.*;
import java.util.stream.*;
import org.w3c.dom.*;

public enum IconicModelFactory {
    ;

    /**
     * Add iconic model vertices and edges to the collections passed as arguments. <b>The
     * collections are modified.</b>
     *
     * @param doc
     *     {@link Document} containing the iconic model
     *
     * @see IconicModelBuilder
     */
    public static void iconsFromDocument (
        final Document doc,
        final Collection<? super Vertex> vertices,
        final Collection<? super Edge> edges
    ) {
        final var model = new IconicModelBuilder(new WrappedDocument(doc)).build();
        vertices.addAll(model.vertices());
        edges.addAll(model.edges());
    }

    /**
     * @return set with all user ids from the iconic model
     */
    public static HashSet<String> userSetFromDocument (final Document document) {
        return new WrappedDocument(document).owners()
            .map(WrappedElement::id)
            .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * @return list with all user ids from the iconic model
     */
    public static List<String> userListFromDocument (final Document document) {
        return new WrappedDocument(document).owners()
            .map(WrappedElement::id)
            .toList();
    }

    /**
     * @return set with all virtual machines from the (cloud) iconic model
     */
    public static HashSet<VirtualMachine> virtualMachinesFromDocument (final Document document) {
        return new WrappedDocument(document).virtualMachines()
            .map(ServiceCenterFactory::aVirtualMachineWithVmm)
            .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * @return map with all user power limits, indexed by id
     */
    public static HashMap<String, Double> profilesFromDocument (final Document document) {
        return new WrappedDocument(document).owners()
            .collect(Collectors.toMap(
                WrappedElement::id,
                WrappedElement::powerLimit,
                (prev, next) -> next,
                HashMap::new
            ));
    }
}

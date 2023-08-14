package ispd.arquivo.xml.models.builders;

import ispd.arquivo.xml.utils.*;
import ispd.motor.filas.servidores.*;
import ispd.motor.filas.servidores.implementacao.*;
import java.util.*;

public abstract class QueueNetworkParser <M, S> {

    /**
     * Map or {@link CentroServico}s parsed from the document, indexed by id.
     */
    protected final Map<Integer, CentroServico> serviceCenters = new HashMap<>();

    /**
     * Map of {@link CS_Internet}s parsed from the document.
     */
    protected final List<CS_Internet> internets = new ArrayList<>();

    /**
     * Map of {@link CS_Link}s parsed from the document.
     */
    protected final List<CS_Comunicacao> links = new ArrayList<>();

    protected final Map<String, Double> powerLimits = new HashMap<>();

    /**
     * Whether this instance has already parsed a document successfully. Each instance should be
     * responsible for parsing <b>only one</b> document.
     */
    protected boolean hasParsedADocument = false;

    private static void connectLinkAndVertices (
        final CS_Link link, final Vertice origination, final Vertice destination
    ) {
        link.setConexoesSaida((CentroServico) destination);
        origination.addConexoesSaida(link);
        destination.addConexoesEntrada(link);
    }

    /**
     * Process a {@link WrappedElement} that is representing a cluster of {@link CentroServico}s.
     * The {@link CS_Mestre}, {@link CS_Maquina}s and {@link CS_Link}s in the cluster are
     * differentiated and all processed individually.
     *
     * @param e
     *     {@link WrappedElement} representing a cluster.
     */
    protected abstract void processClusterElement (WrappedElement e);

    protected void processInternetElement (final WrappedElement e) {
        final var net = ServiceCenterFactory.anInternet(e);

        this.internets.add(net);
        this.serviceCenters.put(e.globalIconId(), net);
    }

    protected void processLinkElement (final WrappedElement e) {
        final var link = ServiceCenterFactory.aLink(e);

        connectLinkAndVertices(
            link,
            this.getVertex(e.origination()),
            this.getVertex(e.destination())
        );

        this.links.add(link);
    }

    private Vertice getVertex (final int e) {
        return (Vertice) this.serviceCenters.get(e);
    }
}

package ispd.gui.iconico.grade;

import static ispd.gui.BundleManager.*;

import ispd.arquivo.xml.*;
import ispd.gui.*;
import ispd.gui.iconico.Icon;
import ispd.gui.iconico.*;
import ispd.motor.workload.*;
import ispd.motor.workload.impl.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.util.List;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import javax.swing.*;
import org.w3c.dom.*;

public class DesenhoGrade extends DrawingArea {

    public static final int MACHINE = 1;

    public static final int NETWORK = 2;

    public static final int CLUSTER = 3;

    public static final int INTERNET = 4;

    public static final Image MACHINE_ICON = getImage("imagens/botao_no.gif");

    public static final Image CLUSTER_ICON = getImage("imagens/botao_cluster.gif");

    public static final Image INTERNET_ICON = getImage("imagens/botao_internet.gif");

    public static final Image GREEN_ICON = getImage("imagens/verde.png");

    public static final Image RED_ICON = getImage("imagens/vermelho.png");

    public static final int INITIAL_SIZE = 1500;

    private static final Color ALMOST_WHITE = new Color(220, 220, 220);

    private static final int SOME_OFFSET = 50;

    private static final double FULL_CAPACITY = 100.0;

    private final Cursor crossHairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

    private final Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    /**
     * (GRID, IAAS ou PAAS)
     */
    private int modelType = PickModelTypeDialog.GRID;

    private HashSet<String> users;

    private HashMap<String, Double> profiles;

    private WorkloadGenerator loadConfiguration = null;

    private int edgeCount = 0;

    private int vertexCount = 0;

    private int iconCount = 0;

    private MainWindow mainWindow = null;

    private boolean shouldPrintDirectConnections = false;

    private boolean shouldPrintIndirectConnections = false;

    private boolean shouldPrintSchedulableNodes = true;

    private Vertex copiedIcon = null;

    private int vertexType = -1;

    private HashSet<VirtualMachine> virtualMachines = null;

    public DesenhoGrade () {
        this(INITIAL_SIZE, INITIAL_SIZE);
    }

    private DesenhoGrade (final int w, final int h) {
        super(true, true, true, false);
        this.setSize(w, h);

        this.users = new HashSet<>(0);
        this.users.add("user1");

        this.profiles = new HashMap<>(0);
        this.profiles.put("user1", FULL_CAPACITY);
    }

    private static Image getImage (final String name) {
        return new ImageIcon(getResource(name)).getImage();
    }

    private static URL getResource (final String name) {
        return MainWindow.class.getResource(name);
    }

    @Override
    public void mouseEntered (final MouseEvent me) {
        this.repaint();
    }

    @Override
    public void mouseExited (final MouseEvent me) {
        this.repaint();
    }

    @Override
    public void botaoPainelActionPerformed (final ActionEvent evt) {
        if (this.copiedIcon == null) {
            return;
        }

        final var copy = ((GridItem) this.copiedIcon)
            .makeCopy(
                this.getPosicaoMouseX(),
                this.getPosicaoMouseY(),
                this.iconCount,
                this.vertexCount
            );
        this.vertices.add((Vertex) copy);
        copy.getId();
        this.iconCount++;
        this.vertexCount++;
        this.selectedIcons.add((Icon) copy);
        this.mainWindow.modificar();
        this.setLabelAtributos(copy);
        this.repaint();
    }

    @Override
    public void botaoVerticeActionPerformed (final ActionEvent evt) {
        //Não copia conexão de rede
        if (this.selectedIcons.isEmpty()) {
            final var text = "WARNING";
            JOptionPane.showMessageDialog(
                null,
                getText("No icon selected."),
                getText(text),
                JOptionPane.WARNING_MESSAGE
            );
        } else if (this.selectedIcons.size() == 1) {
            final var item = this.selectedIcons.iterator().next();
            if (item instanceof Vertex) {
                this.copiedIcon = (Vertex) item;
                this.generalPopup.getComponent(0).setEnabled(true);
            } else {
                this.copiedIcon = null;
            }
        }
        if (this.copiedIcon == null) {
            this.generalPopup.getComponent(0).setEnabled(false);
        }
    }

    @Override
    public void botaoArestaActionPerformed (final ActionEvent evt) {
        if (this.selectedIcons.size() == 1) {
            final var link = (Link) this.selectedIcons.iterator().next();
            this.selectedIcons.remove(link);
            link.setSelected(false);
            final var temp = link.makeCopy(0, 0, this.iconCount, this.edgeCount);
            this.edgeCount++;
            this.iconCount++;
            temp.setPosition(link.getDestination(), link.getSource());
            ((GridItem) temp.getSource()).getOutboundConnections().add(temp);
            ((GridItem) temp.getDestination()).getOutboundConnections().add(temp);
            this.selectedIcons.add(temp);
            this.edges.add(temp);
            this.mainWindow.appendNotificacao(getText("Network connection added."));
            this.mainWindow.modificar();
            this.setLabelAtributos(temp);
        }
    }

    @Override
    public void botaoIconeActionPerformed (final ActionEvent evt) {
        if (this.selectedIcons.isEmpty()) {
            JOptionPane.showMessageDialog(
                null,
                getText("No icon selected."),
                getText("WARNING"),
                JOptionPane.WARNING_MESSAGE
            );
        } else {
            final var opcao = JOptionPane.showConfirmDialog(
                null,
                getText("Remove this icon?"),
                getText("Remove"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (opcao == JOptionPane.YES_OPTION) {
                for (final var iconeRemover : this.selectedIcons) {
                    if (iconeRemover instanceof Edge) {
                        final var or = (GridItem) ((Edge) iconeRemover).getSource();
                        or.getOutboundConnections().remove((GridItem) iconeRemover);
                        final var de = (GridItem) ((Edge) iconeRemover).getDestination();
                        de.getInboundConnections().remove((GridItem) iconeRemover);
                        ((GridItem) iconeRemover).getId();
                        this.edges.remove((Edge) iconeRemover);
                        this.mainWindow.modificar();
                    } else {
                        //Remover dados das conexoes q entram
                        var listanos = ((GridItem) iconeRemover).getInboundConnections();
                        for (final var I : listanos) {
                            this.edges.remove((Edge) I);
                            I.getId();
                        }
                        //Remover dados das conexoes q saem
                        listanos = ((GridItem) iconeRemover).getOutboundConnections();
                        for (final var I : listanos) {
                            this.edges.remove((Edge) I);
                            I.getId();
                        }
                        ((GridItem) iconeRemover).getId();
                        this.vertices.remove((Vertex) iconeRemover);
                        this.mainWindow.modificar();
                    }
                }
                this.repaint();
            }
        }
    }

    @Override
    public void adicionarAresta (final Vertex Origem, final Vertex Destino) {
        final var link = new Link(Origem, Destino, this.edgeCount, this.iconCount);
        ((GridItem) Origem).getOutboundConnections().add(link);
        ((GridItem) Destino).getInboundConnections().add(link);
        this.edgeCount++;
        this.iconCount++;
        this.edges.add(link);
        for (final var icon : this.selectedIcons) {
            icon.setSelected(false);
        }
        this.selectedIcons.clear();
        this.selectedIcons.add(link);
        this.mainWindow.appendNotificacao(getText("Network connection added."));
        this.mainWindow.modificar();
        this.setLabelAtributos(link);
    }

    @Override
    public void showActionIcon (final MouseEvent me, final Icon icon) {
        this.mainWindow.modificar();
        if (icon instanceof Machine || icon instanceof Cluster) {
            this.mainWindow
                .getjPanelConfiguracao()
                .setIcone((GridItem) icon, this.users, this.modelType);
            JOptionPane.showMessageDialog(
                this.mainWindow,
                this.mainWindow.getjPanelConfiguracao(),
                this.mainWindow.getjPanelConfiguracao().getTitle(),
                JOptionPane.PLAIN_MESSAGE
            );
        } else {
            this.mainWindow.getjPanelConfiguracao().setIcone((GridItem) icon);
            JOptionPane.showMessageDialog(
                this.mainWindow,
                this.mainWindow.getjPanelConfiguracao(),
                this.mainWindow.getjPanelConfiguracao().getTitle(),
                JOptionPane.PLAIN_MESSAGE
            );
        }
        this.setLabelAtributos((GridItem) icon);
    }

    @Override
    public void showSelectionIcon (final MouseEvent me, final Icon icon) {
        this.setLabelAtributos((GridItem) icon);
    }

    @Override
    public void adicionarVertice (final int x, final int y) {
        GridItem vertice = null;
        switch (this.vertexType) {
            case MACHINE -> {
                vertice = new Machine(x, y, this.vertexCount, this.iconCount, 0.0);
                vertice.getId();
                this.mainWindow.appendNotificacao(getText("Machine icon added."));
            }
            case CLUSTER -> {
                vertice = new Cluster(x, y, this.vertexCount, this.iconCount, 0.0);
                vertice.getId();
                this.mainWindow.appendNotificacao(getText("Cluster icon added."));
            }
            case INTERNET -> {
                vertice = new Internet(x, y, this.vertexCount, this.iconCount);
                vertice.getId();
                this.mainWindow.appendNotificacao(getText("Internet icon added."));
            }
        }
        if (vertice != null) {
            this.vertices.add((Vertex) vertice);
            this.vertexCount++;
            this.iconCount++;
            this.selectedIcons.add((Icon) vertice);
            this.mainWindow.modificar();
            this.setLabelAtributos(vertice);
        }
    }

    @Override
    public Dimension getPreferredSize () {
        return this.getSize();
    }

    @Override
    public Dimension getMaximumSize () {
        return this.getPreferredSize();
    }

    @Override
    public Dimension getMinimumSize () {
        return this.getPreferredSize();
    }

    public int getModelType () {
        return this.modelType;
    }

    public void setModelType (final int modelType) {
        this.modelType = modelType;
    }

    public void setMainWindow (final MainWindow janelaPrincipal) {
        this.mainWindow = janelaPrincipal;
        this.initTexts();
    }

    //utilizado para inserir novo valor nas Strings dos componentes
    private void initTexts () {
        this.setPopupButtonText(
            getText("Remove"),
            getText("Copy"),
            getText("Turn Over"),
            getText("Paste")
        );
        this.setErrorText(
            getText("You must click an icon."),
            getText("WARNING")
        );
    }

    public HashSet<VirtualMachine> getVirtualMachines () {
        return this.virtualMachines;
    }

    public void setVirtualMachines (final HashSet<VirtualMachine> virtualMachines) {
        this.virtualMachines = virtualMachines;
    }

    public void setShouldPrintDirectConnections (final boolean should) {
        this.shouldPrintDirectConnections = should;
    }

    public void setShouldPrintIndirectConnections (final boolean should) {
        this.shouldPrintIndirectConnections = should;
    }

    public void setShouldPrintSchedulableNodes (final boolean should) {
        this.shouldPrintSchedulableNodes = should;
    }

    public HashMap<String, Double> getProfiles () {
        return this.profiles;
    }

    public void setProfiles (final HashMap<String, Double> profiles) {
        this.profiles = profiles;
    }

    public HashSet<String> getUsuarios () {
        return this.users;
    }

    public void setUsers (final HashSet<String> users) {
        this.users = users;
    }

    public WorkloadGenerator getLoadConfiguration () {
        return this.loadConfiguration;
    }

    public void setLoadConfiguration (final WorkloadGenerator loadConfiguration) {
        this.loadConfiguration = loadConfiguration;
    }

    private void setLabelAtributos (final GridItem icon) {
        final var text = new StringBuilder("<html>");
        text.append(icon.makeDescription());
        if (this.shouldPrintDirectConnections && icon instanceof Vertex) {
            text.append("<br>").append(getText("Output Connection:"));
            for (final var i : icon.getOutboundConnections()) {
                final var saida = (GridItem) ((Edge) i).getDestination();
                text.append("<br>").append(saida.getId().getName());
            }
            text.append("<br>").append(getText("Input Connection:"));
            for (final var i : icon.getInboundConnections()) {
                final var entrada = (GridItem) ((Edge) i).getSource();
                text.append("<br>").append(entrada.getId().getName());
            }
        }
        if (this.shouldPrintDirectConnections && icon instanceof Edge) {
            for (final var i : icon.getInboundConnections()) {
                text.append("<br>").append(getText("Source Node:")).append(" ")
                    .append(i.getInboundConnections());
            }
            for (final var i : icon.getInboundConnections()) {
                text.append("<br>").append(getText("Destination Node:")).append(" ")
                    .append(i.getOutboundConnections());
            }
        }
        if (this.shouldPrintIndirectConnections && icon instanceof final Machine I) {
            final var listaEntrada = I.connectedInboundNodes();
            final var listaSaida   = I.connectedOutboundNodes();
            text.append("<br>").append(getText("Output Nodes Indirectly Connected:"));
            for (final var i : listaSaida) {
                text.append("<br>").append(i.getId().getGlobalId());
            }
            text.append("<br>").append(getText("Input Nodes Indirectly Connected:"));
            for (final var i : listaEntrada) {
                text.append("<br>").append(i.getId().getGlobalId());
            }
        }
        if (this.shouldPrintSchedulableNodes && icon instanceof final Machine I) {
            text.append("<br>").append(getText("Schedulable Nodes:"));
            for (final var i : I.connectedSchedulableNodes()) {
                text.append("<br>").append(i.getId().getGlobalId());
            }
            if (I.isMaster()) {
                final var escravos = ((Machine) icon).getSlaves();
                text.append("<br>").append(getText("Slave Nodes:"));
                for (final var i : escravos) {
                    text.append("<br>").append(i.getId().getName());
                }
            }
        }
        text.append("</html>");
        this.mainWindow.setSelectedIcon(icon, text.toString());
    }

    public String makeDescriptiveModel () {
        final var saida = new StringBuilder();
        for (final Icon icon : this.vertices) {
            if (icon instanceof final Machine I) {
                saida.append(String.format(
                    "MAQ %s %f %f ",
                    I.getId().getName(),
                    I.getComputationalPower(),
                    I.getLoadFactor()
                ));
                if (((Machine) icon).isMaster()) {
                    saida.append(String.format("MESTRE %s LMAQ".formatted(I.getSchedulingAlgorithm())));
                    final var lista = ((Machine) icon).getSlaves();
                    for (final var slv : lista) {
                        if (this.vertices.contains((Vertex) slv)) {
                            saida.append(" ").append(slv.getId().getName());
                        }
                    }
                } else {
                    saida.append("ESCRAVO");
                }
                saida.append("\n");
            }
        }
        for (final Icon icon : this.vertices) {
            if (icon instanceof final Cluster I) {
                saida.append(String.format(
                    "CLUSTER %s %d %f %f %f %s\n",
                    I.getId().getName(),
                    I.getSlaveCount(),
                    I.getComputationalPower(),
                    I.getBandwidth(),
                    I.getLatency(),
                    I.getSchedulingAlgorithm()
                ));
            }
        }
        for (final Icon icon : this.vertices) {
            if (icon instanceof final Internet I) {
                saida.append(String.format("INET %s %f %f %f\n",
                                           I.getId().getName(), I.getBandwidth(), I.getLatency(),
                                           I.getLoadFactor()
                ));
            }
        }
        for (final var icon : this.edges) {
            final var I = (Link) icon;
            saida.append(String.format(
                "REDE %s %f %f %f CONECTA",
                I.getId().getName(),
                I.getBandwidth(),
                I.getLatency(),
                I.getLoadFactor()
            ));
            saida.append(" ").append(((GridItem) icon.getSource()).getId().getName());
            saida.append(" ").append(((GridItem) icon.getDestination()).getId().getName());
            saida.append("\n");
        }
        saida.append("CARGA");
        if (this.loadConfiguration != null) {
            switch (this.loadConfiguration.getType()) {
                case RANDOM -> saida
                    .append(" RANDOM\n")
                    .append(this.loadConfiguration.formatForIconicModel())
                    .append("\n");
                case PER_NODE -> saida
                    .append(" MAQUINA\n")
                    .append(this.loadConfiguration.formatForIconicModel())
                    .append("\n");
                case TRACE -> saida
                    .append(" TRACE\n")
                    .append(this.loadConfiguration.formatForIconicModel())
                    .append("\n");
            }
        }
        return saida.toString();
    }

    /**
     * Transforma os icones da area de desenho em um Document xml dom
     */
    public Document getGrade () {
        final var xml = new IconicModelDocumentBuilder(this.modelType);
        xml.addUsers(this.users, this.profiles);

        for (final var vertice : this.vertices) {
            if (vertice instanceof final Machine m) {
                final var slaves = m.getSlaves().stream()
                    .filter(this.vertices::contains)
                    .map(s -> s.getId().getGlobalId())
                    .toList();

                if (this.modelType == PickModelTypeDialog.GRID) {
                    xml.addMachine(m, slaves);
                } else if (this.modelType == PickModelTypeDialog.IAAS) {
                    xml.addMachineIaas(m, slaves);
                }
            } else if (vertice instanceof final Cluster c) {

                if (this.modelType == PickModelTypeDialog.GRID) {
                    xml.addCluster(c);
                } else if (this.modelType == PickModelTypeDialog.IAAS) {
                    xml.addClusterIaas(c);
                }
            } else if (vertice instanceof final Internet i) {
                xml.addInternet(i);
            }
        }

        for (final var link : this.edges) {
            xml.addLink((Link) link);
        }

        if (this.virtualMachines != null) {
            for (final var vm : this.virtualMachines) {
                xml.addVirtualMachine(vm);
            }
        }

        if (this.loadConfiguration != null) {
            if (this.loadConfiguration instanceof final GlobalWorkloadGenerator load) {
                xml.addGlobalWorkload(load);
            } else if (this.loadConfiguration instanceof final CollectionWorkloadGenerator collection) {
                xml.addPerNodeLoadCollection(collection);
            } else if (this.loadConfiguration instanceof final TraceFileWorkloadGenerator load) {
                xml.addTraceLoad(load);
            }
        }

        return xml.finishDocument();
    }

    public void setGrid (final Document document) {
        //Realiza leitura dos usuários/proprietários do modelo
        this.users           = IconicModelFactory.userSetFromDocument(document);
        this.virtualMachines = IconicModelFactory.virtualMachinesFromDocument(document);
        this.modelType       = this.getModelType(document);
        this.profiles        = IconicModelFactory.profilesFromDocument(document);

        //Realiza leitura dos icones
        IconicModelFactory.iconsFromDocument(document, this.vertices, this.edges);
        //Realiza leitura da configuração de carga do modelo
        this.loadConfiguration = WorkloadGeneratorFactory.fromDocument(document);

        this.updateVertexAndEdgeCount();
        this.repaint();
    }

    private int getModelType (final Document doc) {
        final var sys = (Element) doc.getElementsByTagName("system").item(0);
        return switch (sys.getAttribute("version")) {
            case "2.1" -> PickModelTypeDialog.GRID;
            case "2.2" -> PickModelTypeDialog.IAAS;
            case "2.3" -> PickModelTypeDialog.PAAS;
            default -> this.modelType;
        };
    }

    private void updateVertexAndEdgeCount () {

        for (final var icon : this.edges) {
            final var i = (GridItem) icon;
            if (this.edgeCount < i.getId().getLocalId()) {
                this.edgeCount = i.getId().getLocalId();
            }
            if (this.iconCount < i.getId().getGlobalId()) {
                this.iconCount = i.getId().getGlobalId();
            }
        }

        for (final var icon : this.vertices) {
            final var i = (GridItem) icon;
            if (this.vertexCount < i.getId().getLocalId()) {
                this.vertexCount = i.getId().getLocalId();
            }
            if (this.iconCount < i.getId().getGlobalId()) {
                this.iconCount = i.getId().getGlobalId();
            }
        }

        this.iconCount++;
        this.vertexCount++;
        this.edgeCount++;
    }

    public BufferedImage createImage () {
        final var greatestX = this.findGreatestX();
        final var greatestY = this.findGreatestY();

        final var image = new BufferedImage(
            greatestX + SOME_OFFSET,
            greatestY + SOME_OFFSET,
            BufferedImage.TYPE_INT_RGB
        );

        final var g = (Graphics2D) image.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(
            0,
            0,
            greatestX + SOME_OFFSET,
            greatestY + SOME_OFFSET
        );

        g.setColor(ALMOST_WHITE);
        final var increment = this.getUnit().getIncrement();
        if (this.isGridOn()) {
            for (var w = 0; w <= greatestX + SOME_OFFSET; w += increment) {
                g.drawLine(w, 0, w, greatestY + SOME_OFFSET);
            }
            for (var h = 0; h <= greatestY + SOME_OFFSET; h += increment) {
                g.drawLine(0, h, greatestX + SOME_OFFSET, h);
            }
        }

        this.allIcons().forEach(icon -> icon.draw(g));

        return image;
    }

    private int findGreatestX () {
        return this.findGreatestCoord(Icon::getX);
    }

    private int findGreatestY () {
        return this.findGreatestCoord(Icon::getY);
    }

    private Stream<Icon> allIcons () {
        return Stream.concat(this.edges.stream(), this.vertices.stream());
    }

    private int findGreatestCoord (final Function<? super Icon, Integer> getCoord) {
        return this.vertices.stream()
            .mapToInt(getCoord::apply)
            .max()
            .orElse(0);
    }

    /**
     * Metodo publico para efetuar a copia dos valores de uma conexão de rede especifica informada
     * pelo usuário para as demais conexões de rede.
     */
    public void matchNetwork () {
        if (this.selectedIcons.size() != 1) {
            JOptionPane.showMessageDialog(
                null,
                getText("Please select a network icon"),
                getText("WARNING"),
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        final var link           = (Link) this.selectedIcons.iterator().next();
        final var bandwidth      = link.getBandwidth();
        final var occupationToll = link.getLoadFactor();
        final var latency        = link.getLatency();

        for (final var e : this.edges) {
            final var otherLink = (Link) e;
            otherLink.setBandwidth(bandwidth);
            otherLink.setLoadFactor(occupationToll);
            otherLink.setLatency(latency);
        }
    }

    /**
     * Organizes the icons of the DrawingArea in a rectangular grid-like way.
     */
    public void iconArrange () {

        final var distanceBetweenIcons = 100;
        final var initialX             = distanceBetweenIcons;
        final var initialY             = distanceBetweenIcons;

        var currentX      = initialX;
        var currentY      = initialY;
        var currentColumn = 0;

        final var totalColumns = (int) Math.sqrt(this.vertices.size()) + 1;

        for (final var icon : this.vertices) {
            icon.setPosition(currentX, currentY);

            currentX += distanceBetweenIcons;
            currentColumn++;

            if (currentColumn == totalColumns) {
                currentColumn = 0;
                currentX      = initialX;
                currentY += distanceBetweenIcons;
            }
        }
    }

    public void setTranslator () {
        this.initTexts();
    }

    public List<String> getNosEscalonadores () {
        final List<String> machines = new ArrayList<>(0);
        for (final var icon : this.vertices) {
            if (icon instanceof final Machine m && m.isMaster()) {
                machines.add(m.getId().getName());
            }
            if (icon instanceof final Cluster c && c.isMaster()) {
                machines.add(c.getId().getName());
            }
        }
        return machines;
    }

    public void setIconeSelecionado (final Integer object) {
        if (object == null) {
            this.setIsDrawingEdge(false);
            this.setAddVertice(false);
            this.setCursor(this.normalCursor);
            return;
        }

        if (object == NETWORK) {
            this.setIsDrawingEdge(true);
            this.setAddVertice(false);
            this.setCursor(this.crossHairCursor);
        } else {
            this.vertexType = object;
            this.setIsDrawingEdge(false);
            this.setAddVertice(true);
            this.setCursor(this.crossHairCursor);
        }
    }
}
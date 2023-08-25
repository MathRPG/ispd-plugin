package ispd.gui.iconico;

import static ispd.gui.TextSupplier.*;

import ispd.arquivo.xml.*;
import ispd.gui.*;
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

public class DrawingArea extends JPanel implements MouseListener, MouseMotionListener {

    public static final Image MACHINE_ICON = getImage("imagens/botao_no.gif");

    public static final Image CLUSTER_ICON = getImage("imagens/botao_cluster.gif");

    public static final Image INTERNET_ICON = getImage("imagens/botao_internet.gif");

    public static final Image GREEN_ICON = getImage("imagens/verde.png");

    public static final Image RED_ICON = getImage("imagens/vermelho.png");

    private static final int INITIAL_SIZE = 1500;

    private static final Color ALMOST_WHITE = new Color(220, 220, 220);

    private static final int SOME_OFFSET = 50;

    private static final double FULL_CAPACITY = 100.0;

    private static final Color RECTANGLE_FILL_COLOR = new Color(0.0f, 0.0f, 1.0f, 0.2f);

    private static final RulerUnit DEFAULT_UNIT = RulerUnit.CENTIMETERS;

    private final Collection<Icon> selectedIcons = new HashSet<>();

    private final Set<Vertex> vertices = new HashSet<>();

    private final Collection<Edge> edges = new HashSet<>();

    private final Cursor crossHairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

    private final Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);

    private JPopupMenu generalPopup;

    private ModelType modelType = ModelType.GRID;

    private HashSet<String> users = new HashSet<>();

    private HashMap<String, Double> profiles = new HashMap<>();

    private WorkloadGenerator loadConfiguration = null;

    private int edgeCount = 0;

    private int vertexCount = 0;

    private int iconCount = 0;

    private MainWindow mainWindow = null;

    private boolean shouldPrintDirectConnections = false;

    private boolean shouldPrintIndirectConnections = false;

    private boolean shouldPrintSchedulableNodes = true;

    private Vertex copiedIcon = null;

    private IconType vertexType = IconType.NONE;

    private HashSet<VirtualMachine> virtualMachines = null;

    private JPopupMenu vertexPopup;

    private JPopupMenu edgePopup;

    private JMenuItem jMenuIcon1A;

    private JMenuItem jMenuIcon1V;

    private JMenuItem jMenuVertex;

    private JMenuItem jMenuEdge;

    private JMenuItem jMenuPanel;

    private boolean isGridOn = true;

    private RulerUnit unit = null;

    private Ruler columnRuler;

    private Ruler rowRuler;

    private JPanel cornerUnitButton;

    private boolean shouldDrawRect = false;

    private int rectangleX = 0;

    private int rectangleY = 0;

    private int rectangleWidth = 0;

    private int rectangleHeight = 0;

    private boolean addVertex = false;

    private boolean isDrawingEdge = false;

    private Vertex edgeOrigin = null;

    private int mousePosX = 0;

    private int mousePosY = 0;

    private String errorMessage = null;

    private String errorTitle = null;

    public DrawingArea () {
        this(INITIAL_SIZE, INITIAL_SIZE);
    }

    private DrawingArea (final int w, final int h) {
        super();
        this.initRuler();
        this.initGeneralPopup();
        this.initIconPopup();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setSize(w, h);

        this.users.add("user1");
        this.profiles.put("user1", FULL_CAPACITY);
    }

    private static Image getImage (final String name) {
        return new ImageIcon(getResource(name)).getImage();
    }

    private static URL getResource (final String name) {
        return MainWindow.class.getResource(name);
    }

    private static JPanel panelWith (final Component component) {
        final var corner = new JPanel();
        corner.add(component);
        return corner;
    }

    private static boolean isIconWithinRect (
        final Icon icon,
        final int x,
        final int y,
        final int w,
        final int h
    ) {
        return isInRange(icon.getX(), x, w) && isInRange(icon.getY(), y, h);
    }

    private static boolean isInRange (final int pos, final int start, final int size) {
        return start <= pos && pos <= start + size;
    }

    @Override
    public void mouseClicked (final MouseEvent mouseEvent) {
        if (this.isDrawingEdge) {
            final var destination = this.getSelectedIcon(mouseEvent.getX(), mouseEvent.getY());
            if (this.edgeOrigin == null) {
                if (destination instanceof final Vertex vertex) {
                    this.edgeOrigin = vertex;
                } else {
                    this.showWarning();
                }
            } else {
                if (destination instanceof final Vertex vertex
                    && !this.edgeOrigin.equals(destination)) {
                    this.adicionarAresta(this.edgeOrigin, vertex);
                    this.edgeOrigin = null;
                } else {
                    this.showWarning();
                }
            }
        } else if (!this.selectedIcons.isEmpty()) {
            final var icon = this.getSelectedIcon(mouseEvent.getX(), mouseEvent.getY());
            if (icon != null) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
                    this.showPopupIcon(mouseEvent, icon);
                } else if (mouseEvent.getClickCount() == 2) {
                    this.showActionIcon(mouseEvent, icon);
                } else if (mouseEvent.getClickCount() == 1) {
                    this.showSelectionIcon(icon);
                }
            }
        } else if (this.addVertex) {
            this.adicionarVertice(mouseEvent.getX(), mouseEvent.getY());
        } else if (mouseEvent.getButton() == MouseEvent.BUTTON3) {
            this.generalPopup.show(
                mouseEvent.getComponent(),
                mouseEvent.getX(),
                mouseEvent.getY()
            );
        }
    }

    @Override
    public void mousePressed (final MouseEvent mouseEvent) {
        //Verifica se algum icone foi selecionado
        final var icon = this.getSelectedIcon(mouseEvent.getX(), mouseEvent.getY());
        if (icon != null) {
            if (icon instanceof Vertex) {
                ((Vertex) icon).setBase(0, 0);
            }
            if (!this.selectedIcons.contains(icon)) {
                if (mouseEvent.getButton() != MouseEvent.BUTTON2
                    && !this.selectedIcons.isEmpty()) {
                    for (final var icone : this.selectedIcons) {
                        icone.setSelected(false);
                    }
                    this.selectedIcons.clear();
                }
                icon.setSelected(true);
                this.selectedIcons.add(icon);
            }
            if (this.selectedIcons.size() > 1) {
                for (final var icone : this.selectedIcons) {
                    if (icone instanceof Vertex) {
                        ((Vertex) icone).setBase(
                            icone.getX() - mouseEvent.getX(),
                            icone.getY() - mouseEvent.getY()
                        );
                    }
                }
            }
        }
        //Indica ponto inicial do retangulo
        if (this.selectedIcons.isEmpty()) {
            this.shouldDrawRect  = true;
            this.rectangleX = mouseEvent.getX();
            this.rectangleY = mouseEvent.getY();
            this.rectangleWidth  = 0;
            this.rectangleHeight = 0;
        }
        this.repaint();
    }

    @Override
    public void mouseReleased (final MouseEvent mouseEvent) {
        //Ajusta posição do retangulo
        if (this.rectangleWidth < 0) {
            this.rectangleX += this.rectangleWidth;
            this.rectangleWidth *= -1;
        }
        if (this.rectangleHeight < 0) {
            this.rectangleY += this.rectangleHeight;
            this.rectangleHeight *= -1;
        }
        //Adiciona icone na lista de selecionados
        if (this.selectedIcons.isEmpty()) {
            for (final var icone : this.vertices) {
                if (this.isInSelectionRectangle(icone)) {
                    icone.setSelected(true);
                    this.selectedIcons.add(icone);
                }
            }
            for (final var icone : this.edges) {
                if (this.isInSelectionRectangle(icone)) {
                    icone.setSelected(true);
                    this.selectedIcons.add(icone);
                }
            }
        }
        this.shouldDrawRect = false;
        this.repaint();
    }

    @Override
    public void mouseEntered (final MouseEvent mouseEvent) {
        this.repaint();
    }

    @Override
    public void mouseExited (final MouseEvent mouseEvent) {
        this.repaint();
    }

    @Override
    public void mouseDragged (final MouseEvent mouseEvent) {
        this.updateIcons(mouseEvent.getX(), mouseEvent.getY());
        this.repaint();
    }

    @Override
    public void mouseMoved (final MouseEvent mouseEvent) {
        this.mousePosX = mouseEvent.getX();
        this.mousePosY = mouseEvent.getY();
        if (this.isDrawingEdge) {
            this.repaint();
        }
    }

    @Override
    protected void paintComponent (final Graphics g) {
        super.paintComponent(g);
        this.drawBackground(g);
        this.drawGrid(g);
        this.drawPoints(g);
        //Desenha a linha da conexão de rede antes dela se estabelcer.
        if (this.edgeOrigin != null) {
            g.setColor(new Color(0, 0, 0));
            g.drawLine(
                this.edgeOrigin.getX(),
                this.edgeOrigin.getY(),
                this.mousePosX,
                this.mousePosY
            );
        }
        this.drawRect(g);
        // Desenhamos todos os icones
        for (final Icon icone : this.edges) {
            icone.draw(g);
        }
        for (final Icon icone : this.vertices) {
            icone.draw(g);
        }
    }

    public void processKeyEvent (final KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            this.botaoIconeActionPerformed(null);
        }

        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
            this.botaoVerticeActionPerformed(null);
        }

        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
            this.botaoPainelActionPerformed(null);
        }
    }

    public void botaoPainelActionPerformed (final ActionEvent evt) {
        if (this.copiedIcon == null) {
            return;
        }

        final var copy = ((GridItem) this.copiedIcon)
            .makeCopy(
                this.mousePosX,
                this.mousePosY,
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

    private void botaoArestaActionPerformed (final ActionEvent evt) {
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

    private void showWarning () {
        JOptionPane.showMessageDialog(
            null, this.errorMessage, this.errorTitle, JOptionPane.WARNING_MESSAGE);
    }

    private void showActionIcon (final MouseEvent me, final Icon icon) {
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

    private void showSelectionIcon (final Icon icon) {
        this.setLabelAtributos((GridItem) icon);
    }

    public ModelType getModelType () {
        return this.modelType;
    }

    public void setModelType (final ModelType modelType) {
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

                if (this.modelType == ModelType.GRID) {
                    xml.addMachine(m, slaves);
                } else if (this.modelType == ModelType.IAAS) {
                    xml.addMachineIaas(m, slaves);
                }
            } else if (vertice instanceof final Cluster c) {

                if (this.modelType == ModelType.GRID) {
                    xml.addCluster(c);
                } else if (this.modelType == ModelType.IAAS) {
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

    private ModelType getModelType (final Document doc) {
        final var sys = (Element) doc.getElementsByTagName("system").item(0);
        return switch (sys.getAttribute("version")) {
            case "2.1" -> ModelType.GRID;
            case "2.2" -> ModelType.IAAS;
            case "2.3" -> ModelType.PAAS;
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
        final var increment = this.unit.getIncrement();
        if (this.isGridOn) {
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

        var currentX = distanceBetweenIcons;
        var currentY = distanceBetweenIcons;
        var currentColumn = 0;

        final var totalColumns = (int) Math.sqrt(this.vertices.size()) + 1;

        for (final var icon : this.vertices) {
            icon.setPosition(currentX, currentY);

            currentX += distanceBetweenIcons;
            currentColumn++;

            if (currentColumn == totalColumns) {
                currentColumn = 0;
                currentX = distanceBetweenIcons;
                currentY += distanceBetweenIcons;
            }
        }
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

    public void setIconeSelecionado (final IconType object) {
        if (object == IconType.NONE) {
            this.setIsDrawingEdge(false);
            this.addVertex = false;
            this.setCursor(this.normalCursor);
            return;
        }

        if (object == IconType.NETWORK) {
            this.setIsDrawingEdge(true);
            this.addVertex = false;
            this.setCursor(this.crossHairCursor);
        } else {
            this.vertexType = object;
            this.setIsDrawingEdge(false);
            this.addVertex = true;
            this.setCursor(this.crossHairCursor);
        }
    }

    /**
     * Realiza a adição de uma aresta à area de desenho. Este método é chamado quando se realiza a
     * conexão entre dois vertices com o addAresta ativo
     *
     * @param Origem
     *     Vertice de origem da aresta
     * @param Destino
     *     Vertice de destino da aresta
     */
    private void adicionarAresta (final Vertex Origem, final Vertex Destino) {
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

    /**
     * Realiza a adição de um vertice à area de desenho. Este método é chamado quando o mouse é
     * precionado com addVertice ativo
     *
     * @param x
     *     posição no eixo X
     * @param y
     *     posição no eixo Y
     */
    private void adicionarVertice (final int x, final int y) {
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

    /**
     * It initializes the horizontal and the vertical rulers, as well as the unit button used to
     * change the both ruler's unit.
     */
    private void initRuler () {
        this.updateUnitTo(DEFAULT_UNIT);

        this.columnRuler = new Ruler(RulerOrientation.HORIZONTAL, this.unit);
        this.columnRuler.setPreferredWidth(this.getWidth());
        this.rowRuler = new Ruler(RulerOrientation.VERTICAL, this.unit);
        this.rowRuler.setPreferredHeight(this.getHeight());

        final var unitButton = new JButton(this.unit.getSymbol());
        unitButton.addActionListener(this::onUnitButtonClicked);
        this.cornerUnitButton = panelWith(unitButton);
    }

    private void initGeneralPopup () {
        this.jMenuPanel = new JMenuItem("Paste");
        this.jMenuPanel.setEnabled(false);
        this.jMenuPanel.addActionListener(this::botaoPainelActionPerformed);
        this.generalPopup = new JPopupMenu();
        this.generalPopup.add(this.jMenuPanel);
    }

    private void initIconPopup () {
        this.vertexPopup = new JPopupMenu();
        this.edgePopup   = new JPopupMenu();

        final var jMenuVertice0 = new JMenuItem();
        jMenuVertice0.addActionListener(evt -> {});
        this.vertexPopup.add(jMenuVertice0);
        jMenuVertice0.setVisible(false);

        this.jMenuVertex = new JMenuItem("Copy");
        this.jMenuVertex.addActionListener(this::botaoVerticeActionPerformed);
        this.vertexPopup.add(this.jMenuVertex);

        this.jMenuEdge = new JMenuItem("Turn Over");
        this.jMenuEdge.addActionListener(this::botaoArestaActionPerformed);
        this.edgePopup.add(this.jMenuEdge);

        this.vertexPopup.add(new JSeparator());
        this.edgePopup.add(new JSeparator());

        this.jMenuIcon1V = new JMenuItem("Remove");
        this.jMenuIcon1V.addActionListener(this::botaoIconeActionPerformed);
        this.vertexPopup.add(this.jMenuIcon1V);

        this.jMenuIcon1A = new JMenuItem("Remove");
        this.jMenuIcon1A.addActionListener(this::botaoIconeActionPerformed);
        this.edgePopup.add(this.jMenuIcon1A);
    }

    /**
     * It updates the grid unit.
     *
     * @param newUnit
     *     the unit to be updated to
     */
    private void updateUnitTo (final RulerUnit newUnit) {
        this.unit = newUnit;
    }

    private void onUnitButtonClicked (final ActionEvent evt) {
        this.updateUnitTo(this.unit.nextUnit());

        this.rowRuler.updateUnitTo(this.unit);
        this.columnRuler.updateUnitTo(this.unit);

        ((AbstractButton) evt.getSource())
            .setText(this.unit.getSymbol());

        if (this.isGridOn) {
            this.repaint();
        }
    }

    public Ruler getColumnView () {
        return this.columnRuler;
    }

    public Ruler getRowView () {
        return this.rowRuler;
    }

    public JPanel getCorner () {
        return this.cornerUnitButton;
    }

    private Icon getSelectedIcon (final int x, final int y) {
        for (final var v : this.vertices) {
            if (v.contains(x, y)) {
                return v;
            }
        }
        for (final var e : this.edges) {
            if (e.contains(x, y)) {
                return e;
            }
        }
        if (!this.selectedIcons.isEmpty()) {
            for (final var icon : this.selectedIcons) {
                icon.setSelected(false);
            }
            this.selectedIcons.clear();
        }
        return null;
    }

    private boolean isInSelectionRectangle (final Icon icon) {
        return isInRange(icon.getX(), this.rectangleX, this.rectangleWidth)
               && isInRange(icon.getY(), this.rectangleY, this.rectangleHeight);
    }

    private void showPopupIcon (final MouseEvent me, final Icon icon) {
        if (icon instanceof Vertex) {
            if (this.jMenuVertex.isEnabled()) {
                this.jMenuVertex.setVisible(true);
            }
            this.vertexPopup.show(me.getComponent(), me.getX(), me.getY());
        } else if (icon instanceof Edge) {
            if (this.jMenuEdge.isEnabled()) {
                this.jMenuEdge.setVisible(true);
            }
            this.edgePopup.show(me.getComponent(), me.getX(), me.getY());
        }
    }

    private void updateIcons (final int x, final int y) {
        if (!this.selectedIcons.isEmpty()) {
            this.dragSelectedIcons(x, y);
            return;
        }

        if (!this.shouldDrawRect) {
            return;
        }

        this.updateRectangleAndSelectIcons(x, y);
    }

    private void dragSelectedIcons (final int x, final int y) {
        this.selectedIcons.stream()
            .filter(Vertex.class::isInstance)
            .map(Vertex.class::cast)
            .forEach(v -> v.setPosition(x + v.getBaseX(), y + v.getBaseY()));
    }

    private void updateRectangleAndSelectIcons (final int x, final int y) {
        this.rectangleWidth  = x - this.rectangleX;
        this.rectangleHeight = y - this.rectangleY;

        final int retX;
        final int retLag;
        if (this.rectangleWidth < 0) {
            retX   = this.rectangleX + this.rectangleWidth;
            retLag = this.rectangleWidth * -1;
        } else {
            retX   = this.rectangleX;
            retLag = this.rectangleWidth;
        }

        final int retY;
        final int retAlt;
        if (this.rectangleHeight < 0) {
            retY   = this.rectangleY + this.rectangleHeight;
            retAlt = this.rectangleHeight * -1;
        } else {
            retY   = this.rectangleY;
            retAlt = this.rectangleHeight;
        }

        Stream.concat(this.vertices.stream(), this.edges.stream())
            .filter(icon -> isIconWithinRect(icon, retX, retY, retLag, retAlt))
            .forEach(icon -> icon.setSelected(true));
    }

    private void setIsDrawingEdge (final boolean isDrawingEdge) {
        this.isDrawingEdge = isDrawingEdge;
        this.edgeOrigin    = null;
    }

    private void drawBackground (final Graphics g) {
        ((Graphics2D) g).setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
    }

    private void drawGrid (final Graphics g) {
        if (!this.isGridOn) {
            return;
        }

        g.setColor(Color.LIGHT_GRAY);
        final var increment = this.unit.getIncrement();
        for (var w = 0; w <= this.getWidth(); w += increment) {
            g.drawLine(w, 0, w, this.getHeight());
        }
        for (var h = 0; h <= this.getHeight(); h += increment) {
            g.drawLine(0, h, this.getWidth(), h);
        }
    }

    private void drawPoints (final Graphics g) {
    }

    private void drawRect (final Graphics g) {
        if (!this.shouldDrawRect) {
            return;
        }

        final int x;
        final int w;

        if (this.rectangleWidth >= 0) {
            x = this.rectangleX;
            w = this.rectangleWidth;
        } else {
            x = this.rectangleX + this.rectangleWidth;
            w = this.rectangleWidth * -1;
        }

        final int y;
        final int h;

        if (this.rectangleHeight >= 0) {
            y = this.rectangleY;
            h = this.rectangleHeight;
        } else {
            y = this.rectangleY + this.rectangleHeight;
            h = this.rectangleHeight * -1;
        }

        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);
        g.setColor(RECTANGLE_FILL_COLOR);
        g.fillRect(x, y, w, h);
    }

    public void setGridOn (final boolean gridOn) {
        this.isGridOn = gridOn;
        this.repaint();
    }

    private void setErrorText (final String message, final String title) {
        this.errorMessage = message;
        this.errorTitle   = title;
    }

    private void setPopupButtonText (
        final String icon,
        final String vertex,
        final String edge,
        final String panel
    ) {
        if (icon != null) {
            this.jMenuIcon1A.setText(icon);
            this.jMenuIcon1A.setVisible(true);
            this.jMenuIcon1V.setText(icon);
            this.jMenuIcon1V.setVisible(true);
        } else {
            this.jMenuIcon1A.setEnabled(false);
            this.jMenuIcon1A.setVisible(false);
            this.jMenuIcon1V.setEnabled(false);
            this.jMenuIcon1V.setVisible(false);
        }

        if (vertex != null) {
            this.jMenuVertex.setText(vertex);
            this.jMenuVertex.setVisible(true);
        } else {
            this.jMenuVertex.setEnabled(false);
            this.jMenuVertex.setVisible(false);
        }

        if (edge != null) {
            this.jMenuEdge.setText(edge);
            this.jMenuEdge.setVisible(true);
        } else {
            this.jMenuEdge.setEnabled(false);
            this.jMenuEdge.setVisible(false);
        }

        if (panel != null) {
            this.jMenuPanel.setText(panel);
            this.jMenuPanel.setVisible(true);
        } else {
            this.jMenuPanel.setEnabled(false);
            this.jMenuPanel.setVisible(false);
        }
    }
}
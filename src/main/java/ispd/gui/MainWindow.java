package ispd.gui;

import static ispd.gui.TextSupplier.*;

import ispd.arquivo.exportador.*;
import ispd.arquivo.xml.*;
import ispd.gui.auxiliar.*;
import ispd.gui.configuracao.*;
import ispd.gui.iconico.grade.*;
import ispd.gui.policy.*;
import ispd.gui.utils.*;
import ispd.utils.*;
import ispd.utils.constants.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public final class MainWindow extends JFrame implements KeyListener {

    private static final String[] ISPD_FILE_EXTENSIONS = { FileExtensions.ICONIC_MODEL };

    private static final String[] ALL_FILE_EXTENSIONS = {
        FileExtensions.ICONIC_MODEL,
        FileExtensions.WORKLOAD_MODEL
    };

    private static final String ISPD_LOGO_FILE_PATH = "imagens/Logo_iSPD_25.png";

    private static final int NOTIFICATION_AREA_COLS = 20;

    private static final int NOTIFICATION_AREA_ROWS = 5;

    private final ConfiguracaoISPD configure = new ConfiguracaoISPD();

    private final JFileChooser jFileChooser = new JFileChooser();

    private final GenericPolicyManagementWindow jFrameManager =
        new GridSchedulingPolicyManagementWindow();

    private final GenericPolicyManagementWindow jFrameAllocManager =
        new VmAllocationPolicyManagementWindow();

    private final GenericPolicyManagementWindow jFrameCloudManager =
        new CloudSchedulingPolicyManagementWindow();

    private final SimplePanel jPanelSimple = new SimplePanel();

    private final JScrollPane jScrollPaneDrawingArea = new JScrollPane();

    private final JScrollPane jScrollPaneSideBar = new JScrollPane();

    private final JScrollPane jScrollPaneNotificationBar = new JScrollPane();

    private final JTextArea jTextAreaNotification = new JTextArea();

    private final JToggleButton jToggleButtonCluster = new JToggleButton();

    private final JToggleButton jToggleButtonInternet = new JToggleButton();

    private final JToggleButton jToggleButtonMachine = new JToggleButton();

    private final JToggleButton jToggleButtonNetwork = new JToggleButton();

    private final JToggleButton[] iconButtons = {
        this.jToggleButtonMachine,
        this.jToggleButtonNetwork,
        this.jToggleButtonCluster,
        this.jToggleButtonInternet,
    };

    private final JToolBar jToolBar = new JToolBar();

    private final JMenuItem jCheckBoxMenuConnectedItem = new JCheckBoxMenuItem();

    private final JMenuItem jCheckBoxMenuSchedulableItem = new JCheckBoxMenuItem();

    private final JMenuItem jCheckBoxMenuGridItem = new JCheckBoxMenuItem();

    private final JMenuItem jCheckBoxIndirectMenuItem = new JCheckBoxMenuItem();

    private final JMenuItem jCheckBoxRulerMenuItem = new JCheckBoxMenuItem();

    private final JMenu jMenuHelp = new JMenu();

    private final JMenu jMenuFile = new JMenu();

    private final JMenu jMenuEdit = new JMenu();

    private final JMenu jMenuShow = new JMenu();

    private final JMenu jMenuExport = new JMenu();

    private final JMenu jMenuTools = new JMenu();

    private final JMenuItem jMenuItemOpen = new JMenuItem();

    private final JMenuItem jMenuItemHelp = new JMenuItem();

    private final JMenuItem jMenuItemCopy = new JMenuItem();

    private final JMenuItem jMenuItemDelete = new JMenuItem();

    private final JMenuItem jMenuItemCompare = new JMenuItem();

    private final JMenuItem jMenuItemClose = new JMenuItem();

    private final JMenuItem jMenuItemGenerate = new JMenuItem();

    private final JMenuItem jMenuItemManage = new JMenuItem();

    private final JMenuItem jMenuItemNew = new JMenuItem();

    private final JMenuItem jMenuItemSort = new JMenuItem();

    private final JMenuItem jMenuItemPaste = new JMenuItem();

    private final JMenuItem jMenuItemExit = new JMenuItem();

    private final JMenuItem jMenuItemSave = new JMenuItem();

    private final JMenuItem jMenuItemSaveAs = new JMenuItem();

    private final JMenuItem jMenuItemAbout = new JMenuItem();

    private final JMenuItem jMenuItemToGridSim = new JMenuItem();

    private final JMenuItem jMenuItemToJPG = new JMenuItem();

    private final JMenuItem jMenuItemToSimGrid = new JMenuItem();

    private final JMenuItem jMenuItemToTxt = new JMenuItem();

    private final SimplePanel jPanelProperties = new SimplePanel();

    private final JScrollPane jScrollPaneProperties = new JScrollPane();

    private final JMenuItem jMenuItemOpenResult = new JMenuItem();

    private final JMenuItem jMenuItemPreferences = new JMenuItem();

    private final JMenuItem jMenuItemManageCloud = new JMenuItem();

    private final JMenuItem jMenuItemManageAllocation = new JMenuItem();

    private final AbstractButton jButtonInjectFaults = ButtonBuilder
        .aButton("Faults Injection", MainWindow::jButtonInjectFaultsActionPerformed)
        .withIcon(getImage("/ispd/gui/imagens/vermelho.png"))
        .withToolTip("Select the faults")
        .withCenterBottomTextPosition()
        .disabled()
        .nonFocusable()
        .build();

    private final MultipleExtensionFileFilter fileFilter =
        new MultipleExtensionFileFilter(
            getText("Iconic Model of Simulation"),
            ALL_FILE_EXTENSIONS,
            true
        );

    private JPanelConfigIcon jPanelSettings;

    /**
     * define se o modelo é GRID, IAAS ou PAAS;
     */
    private int modelType = 0;

    private boolean currentFileHasUnsavedChanges = false;

    private File openFile = null;

    private DesenhoGrade drawingArea = null;

    private final AbstractButton jButtonTasks = ButtonBuilder
        .aButton(getImage("/ispd/gui/imagens/botao_tarefas.gif"), this::jButtonTaskActionPerformed)
        .withToolTip(getText("Selects insertion model of tasks"))
        .withCenterBottomTextPosition()
        .disabled()
        .nonFocusable()
        .build();

    private final AbstractButton jButtonSimulate = ButtonBuilder
        .aButton(getText("Simulate"), this::jButtonSimulateActionPerformed)
        .withIcon(getImage("/ispd/gui/imagens" + "/system-run.png"))
        .withToolTip(getText("Starts the " + "simulation"))
        .withCenterBottomTextPosition()
        .disabled()
        .nonFocusable()
        .build();

    private final AbstractButton jButtonUsers = ButtonBuilder
        .aButton(getImage("/ispd/gui/imagens/system-users.png"), this::jButtonUsersActionPerformed)
        .withToolTip(getText("Add and remove users to the model"))
        .withCenterBottomTextPosition()
        .disabled()
        .nonFocusable()
        .build();

    private HashSet<VirtualMachine> virtualMachines = null;

    private final AbstractButton jButtonConfigVM = ButtonBuilder
        .aButton(getImage("/ispd/gui/imagens/vm_icon.png"), this::jButtonConfigVMActionPerformed)
        .withToolTip("Add and remove the virtual machines")
        .withCenterBottomTextPosition()
        .disabled()
        .nonFocusable()
        .build();

    private final JComponent[] interactables = {
        this.jToggleButtonCluster,
        this.jToggleButtonInternet,
        this.jToggleButtonMachine,
        this.jToggleButtonNetwork,
        this.jButtonSimulate,
        this.jButtonInjectFaults,
        this.jButtonTasks,
        this.jButtonUsers,
        this.jButtonConfigVM,
        this.jMenuItemSave,
        this.jMenuItemSaveAs,
        this.jMenuItemClose,
        this.jMenuItemToJPG,
        this.jMenuItemToTxt,
        this.jMenuItemToSimGrid,
        this.jMenuItemToGridSim,
        this.jMenuItemCompare,
        this.jMenuItemSort,
        this.jMenuItemCopy,
        this.jMenuItemPaste,
        this.jMenuItemDelete,
        this.jCheckBoxIndirectMenuItem,
        this.jCheckBoxMenuConnectedItem,
        this.jCheckBoxMenuSchedulableItem,
    };

    public MainWindow () {
        this.initComponents();
        this.addKeyListeners();
        this.buildLayoutAndPack();
        this.setLocationRelativeTo(null);
    }

    private static URL getResourceOrThrow (final String resourcePath) {
        final var url = Optional.ofNullable(MainWindow.class.getResource(resourcePath));

        return url.orElseThrow(
            () -> new MissingResourceException(
                String.format("Missing resource from %s", resourcePath),
                MainWindow.class.getName(), resourcePath
            )
        );
    }

    private static ImageIcon getImage (final String imagePath) {
        return new ImageIcon(getResourceOrThrow(imagePath));
    }

    private static boolean isValidDirectory (final File dir) {
        return dir.isDirectory() && dir.exists();
    }

    private static boolean hasValidIspdFileExtension (final File file) {
        return file.getName().endsWith(FileExtensions.ICONIC_MODEL);
    }

    private static void jButtonInjectFaultsActionPerformed (final ActionEvent evt) {
        new PickSimulationFaultsDialog().setVisible(true);
    }

    @Override
    public void keyTyped (final KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed (final KeyEvent keyEvent) {
        if (this.drawingArea == null) {
            return;
        }

        if (keyEvent.getKeyCode() == KeyEvent.VK_DELETE) {
            this.drawingArea.botaoIconeActionPerformed(null);
        }

        if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_C) {
            this.drawingArea.botaoVerticeActionPerformed(null);
        }

        if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_V) {
            this.drawingArea.botaoPainelActionPerformed(null);
        }
    }

    @Override
    public void keyReleased (final KeyEvent keyEvent) {
    }

    private void initComponents () {
        this.initWindowProperties();
        this.initNotificationArea();
        this.initToolBarAndButtons();
        this.initMenus();
        this.initPanels();
        this.initFileChooser();
    }

    private void initMenus () {
        this.initMenuBar();
        this.initMenuFile();
        this.initMenuEdit();
        this.initMenuShow();
        this.initMenuTools();
        this.initMenuHelp();
    }

    private void initPanels () {
        this.jPanelSettings = new JPanelConfigIcon();
        this.jPanelSettings.setEscalonadores(this.jFrameManager.getManager());
        this.jPanelSettings.setEscalonadoresCloud(this.jFrameCloudManager.getManager());
        this.jPanelSettings.setAlocadores(this.jFrameAllocManager.getManager());

        this.jPanelSimple.setText(getText("No icon selected."));

        this.jScrollPaneSideBar.setBorder(BorderFactory.createTitledBorder("Settings"));

        this.jScrollPaneNotificationBar.setBorder(BorderFactory.createTitledBorder(getText(
            "Notifications")));
        this.jScrollPaneNotificationBar.setViewportView(this.jTextAreaNotification);

        this.jScrollPaneProperties.setBorder(BorderFactory.createTitledBorder(getText("Properties")));
        this.jScrollPaneProperties.setViewportView(this.jPanelProperties);
    }

    private void initMenuHelp () {
        this.jMenuHelp.setText(getText("Help"));
        this.jMenuItemHelp.setIcon(getImage("/ispd/gui/imagens" + "/help-faq.png"));
        this.jMenuItemHelp.setText(getText("Help"));
        this.jMenuItemHelp.setToolTipText(getText("Help"));
        this.jMenuItemHelp.addActionListener(this::jMenuItemHelpActionPerformed);
        this.jMenuHelp.add(this.jMenuItemHelp);
        this.jMenuHelp.add(new JPopupMenu.Separator());
        this.jMenuItemAbout.setIcon(getImage("/ispd/gui/imagens" + "/help-about.png"));
        final var aboutProgramText =
            String.format(
                "%s %s",
                getText("About"),
                getText("nomePrograma")
            );
        this.jMenuItemAbout.setText(aboutProgramText);
        this.jMenuItemAbout.setToolTipText(aboutProgramText);
        this.jMenuItemAbout.addActionListener(this::jMenuItemAboutActionPerformed);
        this.jMenuHelp.add(this.jMenuItemAbout);
    }

    private void initMenuTools () {
        this.jMenuTools.setText(getText("Tools"));
        this.jMenuTools.addActionListener(this::jMenuToolsActionPerformed);
        this.jMenuItemManage.setText(getText("Manage Schedulers"));
        this.jMenuItemManage.addActionListener(this::jMenuItemManageActionPerformed);
        this.jMenuTools.add(this.jMenuItemManage);
        this.jMenuItemGenerate.setText(getText("Generate Scheduler"));
        this.jMenuItemGenerate.addActionListener(this::jMenuItemGenerateActionPerformed);
        this.jMenuTools.add(this.jMenuItemGenerate);
        this.jMenuItemManageCloud.setText("Manage Cloud Schedulers");
        this.jMenuItemManageCloud.addActionListener(this::jMenuItemManageCloudActionPerformed);
        this.jMenuTools.add(this.jMenuItemManageCloud);
        this.jMenuItemManageAllocation.setText("Manage Allocation Policies");
        this.jMenuItemManageAllocation.addActionListener(this::jMenuItemManageAllocationActionPerformed);
        this.jMenuTools.add(this.jMenuItemManageAllocation);
    }

    private void initMenuShow () {
        this.jMenuShow.setText(getText("View"));
        this.jCheckBoxMenuConnectedItem.setText(getText("Show "
                                                        + "Connected "
                                                        + "Nodes"));
        this.jCheckBoxMenuConnectedItem.setToolTipText(getText(
            "Displays in the settings area, the list of nodes connected "
            + "for the selected icon"));
        this.jCheckBoxMenuConnectedItem.setEnabled(false);
        this.jCheckBoxMenuConnectedItem.addActionListener(this::jCheckBoxMenuItemConnectedActionPerformed);
        this.jMenuShow.add(this.jCheckBoxMenuConnectedItem);
        this.jCheckBoxIndirectMenuItem.setText(getText("Show "
                                                       + "Indirectly "
                                                       + "Connected "
                                                       + "Nodes"));
        this.jCheckBoxIndirectMenuItem.setToolTipText(getText(
            "Displays in the settings area, the list of nodes connected " +
            "through the internet icon, to the icon selected"));
        this.jCheckBoxIndirectMenuItem.setEnabled(false);
        this.jCheckBoxIndirectMenuItem.addActionListener(this::jCheckBoxMenuItemIndirectActionPerformed);
        this.jMenuShow.add(this.jCheckBoxIndirectMenuItem);
        this.jCheckBoxMenuSchedulableItem.setSelected(true);
        this.jCheckBoxMenuSchedulableItem.setText(getText("Show "
                                                          + "Schedulable"
                                                          + " Nodes"));
        this.jCheckBoxMenuSchedulableItem.setToolTipText(getText(
            "Displays in the settings area, the list of nodes schedulable" +
            " for the selected icon"));
        this.jCheckBoxMenuSchedulableItem.setEnabled(false);
        this.jCheckBoxMenuSchedulableItem.addActionListener(this::jCheckBoxMenuItemSchedulableActionPerformed);
        this.jMenuShow.add(this.jCheckBoxMenuSchedulableItem);
        this.jCheckBoxMenuGridItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_G,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jCheckBoxMenuGridItem.setSelected(true);
        this.jCheckBoxMenuGridItem.setText(getText("Drawing grid"));
        this.jCheckBoxMenuGridItem.setToolTipText(getText("Displays "
                                                          + "grid in the"
                                                          + " drawing "
                                                          + "area"));
        this.jCheckBoxMenuGridItem.addActionListener(this::jCheckBoxMenuItemGradeActionPerformed);
        this.jMenuShow.add(this.jCheckBoxMenuGridItem);
        this.jCheckBoxRulerMenuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_R,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jCheckBoxRulerMenuItem.setSelected(true);
        this.jCheckBoxRulerMenuItem.setText(getText("Drawing rule"));
        this.jCheckBoxRulerMenuItem.setToolTipText(
            getText("Displays " + "rule in " + "the " + "drawing " + "area"));
        this.jCheckBoxRulerMenuItem.addActionListener(this::jCheckBoxMenuItemRulerActionPerformed);
        this.jMenuShow.add(this.jCheckBoxRulerMenuItem);
    }

    private void initMenuEdit () {
        this.jMenuEdit.setText(getText("Edit"));
        this.jMenuItemCopy.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_C,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jMenuItemCopy.setIcon(getImage("/ispd/gui/imagens" + "/edit-copy.png"));
        this.jMenuItemCopy.setText(getText("Copy"));
        this.jMenuItemCopy.setEnabled(false);
        this.jMenuItemCopy.addActionListener(this::jMenuItemCopyActionPerformed);
        this.jMenuEdit.add(this.jMenuItemCopy);
        this.jMenuItemPaste.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_V,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jMenuItemPaste.setIcon(getImage("/ispd/gui/imagens" + "/edit-paste.png"));
        this.jMenuItemPaste.setText(getText("Paste"));
        this.jMenuItemPaste.setEnabled(false);
        this.jMenuItemPaste.addActionListener(this::jMenuItemPasteActionPerformed);
        this.jMenuEdit.add(this.jMenuItemPaste);
        this.jMenuItemDelete.setIcon(getImage("/ispd/gui/imagens" + "/edit-delete.png"));
        this.jMenuItemDelete.setText(getText("Delete"));
        this.jMenuItemDelete.setEnabled(false);
        this.jMenuItemDelete.addActionListener(this::jMenuItemDeleteActionPerformed);
        this.jMenuEdit.add(this.jMenuItemDelete);
        this.jMenuItemCompare.setText(getText("Match network settings"));
        this.jMenuItemCompare.setToolTipText(
            getText(
                "Matches the settings of icons of networks according to a selected icon"));
        this.jMenuItemCompare.setActionCommand(getText("Match network settings"));
        this.jMenuItemCompare.setEnabled(false);
        this.jMenuItemCompare.addActionListener(this::jMenuItemCompareActionPerformed);
        this.jMenuEdit.add(this.jMenuItemCompare);
        this.jMenuItemSort.setText("Arrange icons");
        this.jMenuItemSort.setEnabled(false);
        this.jMenuItemSort.addActionListener(this::jMenuItemSortActionPerformed);
        this.jMenuEdit.add(this.jMenuItemSort);
        this.jMenuEdit.add(new JPopupMenu.Separator());
        this.jMenuItemPreferences.setText("Preferences");
        this.jMenuItemPreferences.addActionListener(this::jMenuItem1ActionPerformed);
        this.jMenuEdit.add(this.jMenuItemPreferences);
    }

    private void initMenuFile () {
        this.jMenuFile.setText(getText("File"));
        this.jMenuFile.addActionListener(this::jMenuFileActionPerformed);
        this.jMenuItemNew.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_N,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jMenuItemNew.setIcon(getImage("/ispd/gui/imagens" + "/insert-object_1.png"));
        this.jMenuItemNew.setText(getText("New"));
        this.jMenuItemNew.setToolTipText(getText("Starts a new model"));
        this.jMenuItemNew.addActionListener(this::jMenuItemNovoActionPerformed);
        this.jMenuFile.add(this.jMenuItemNew);
        this.jMenuItemOpen.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_O,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jMenuItemOpen.setIcon(getImage("/ispd/gui/imagens" + "/document-open.png"));
        this.jMenuItemOpen.setText(getText("Open"));
        this.jMenuItemOpen.setToolTipText(getText("Opens an existing " + "model"));
        this.jMenuItemOpen.addActionListener(this::jMenuItemOpenActionPerformed);
        this.jMenuFile.add(this.jMenuItemOpen);
        this.jMenuItemSave.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_S,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jMenuItemSave.setIcon(getImage("/ispd/gui/imagens" + "/document-save_1.png"));
        this.jMenuItemSave.setText(getText("Save"));
        this.jMenuItemSave.setToolTipText(getText("Save the open " + "model"));
        this.jMenuItemSave.setEnabled(false);
        this.jMenuItemSave.addActionListener(this::jMenuItemSaveActionPerformed);
        this.jMenuFile.add(this.jMenuItemSave);
        this.jMenuItemSaveAs.setText(getText("Save as..."));
        this.jMenuItemSaveAs.setEnabled(false);
        this.jMenuItemSaveAs.addActionListener(this::jMenuItemSaveAsActionPerformed);
        this.jMenuFile.add(this.jMenuItemSaveAs);
        this.jMenuItemOpenResult.setIcon(getImage("/ispd/gui"
                                                  + "/imagens"
                                                  + "/document-open"
                                                  + ".png"));
        this.jMenuItemOpenResult.setText("Open Results");
        this.jMenuItemOpenResult.addActionListener(this::jMenuItemOpenResultActionPerformed);
        this.jMenuFile.add(this.jMenuItemOpenResult);
        this.jMenuExport.setIcon(getImage("/ispd/gui/imagens/document-export.png"));
        this.jMenuExport.setText(getText("Export"));
        this.jMenuItemToJPG.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_J,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jMenuItemToJPG.setText(getText("to JPG"));
        this.jMenuItemToJPG.setToolTipText(getText(
            "Creates a jpg file with the model image"));
        this.jMenuItemToJPG.setEnabled(false);
        this.jMenuItemToJPG.addActionListener(this::jMenuItemToJPGActionPerformed);
        this.jMenuExport.add(this.jMenuItemToJPG);
        this.jMenuItemToTxt.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_T,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jMenuItemToTxt.setText(getText("to TXT"));
        this.jMenuItemToTxt.setToolTipText(getText(
            "Creates a file in plain text with the model data according to the grammar of the iconic model"));
        this.jMenuItemToTxt.setEnabled(false);
        this.jMenuItemToTxt.addActionListener(this::jMenuItemToTxtActionPerformed);
        this.jMenuExport.add(this.jMenuItemToTxt);
        this.jMenuItemToSimGrid.setText("to SimGrid");
        this.jMenuItemToSimGrid.setEnabled(false);
        this.jMenuItemToSimGrid.addActionListener(this::jMenuItemToSimGridActionPerformed);
        this.jMenuExport.add(this.jMenuItemToSimGrid);
        this.jMenuItemToGridSim.setText("to GridSim");
        this.jMenuItemToGridSim.setEnabled(false);
        this.jMenuItemToGridSim.addActionListener(this::jMenuItemToGridSimActionPerformed);
        this.jMenuExport.add(this.jMenuItemToGridSim);
        this.jMenuFile.add(this.jMenuExport);
        this.jMenuFile.add(new JPopupMenu.Separator());
        this.jMenuItemClose.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_F4,
            InputEvent.CTRL_DOWN_MASK
        ));
        this.jMenuItemClose.setIcon(getImage("/ispd/gui/imagens" + "/document-close.png"));
        this.jMenuItemClose.setText(getText("Close"));
        this.jMenuItemClose.setToolTipText(getText("Closes the "
                                                   + "currently open "
                                                   + "model"));
        this.jMenuItemClose.setEnabled(false);
        this.jMenuItemClose.addActionListener(this::jMenuItemCloseActionPerformed);
        this.jMenuFile.add(this.jMenuItemClose);
        this.jMenuItemExit.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_F4,
            InputEvent.ALT_DOWN_MASK
        ));
        this.jMenuItemExit.setIcon(getImage("/ispd/gui/imagens" + "/window-close.png"));
        this.jMenuItemExit.setText(getText("Exit"));
        this.jMenuItemExit.setToolTipText(getText("Closes the program"));
        this.jMenuItemExit.addActionListener(this::jMenuItemExitActionPerformed);
        this.jMenuFile.add(this.jMenuItemExit);
    }

    private void initToolBarAndButtons () {
        this.jToolBar.setFloatable(false);
        this.initButton(
            this.jToggleButtonMachine, "/ispd/gui/imagens/botao_no.gif",
            "Selects machine icon for add to the model",
            this::jToggleButtonMachineActionPerformed
        );
        this.initButton(
            this.jToggleButtonNetwork, "/ispd/gui/imagens/botao_rede.gif",
            "Selects network icon for add to the model",
            this::jToggleButtonNetworkActionPerformed
        );
        this.initButton(
            this.jToggleButtonCluster, "/ispd/gui/imagens/botao_cluster" + ".gif",
            "Selects cluster icon for add to the model",
            this::jToggleButtonClusterActionPerformed
        );
        this.initButton(
            this.jToggleButtonInternet, "/ispd/gui/imagens/botao_internet" + ".gif",
            "Selects internet icon for add to the model",
            this::jToggleButtonInternetActionPerformed
        );

        this.jToolBar.add(new JToolBar.Separator());
        this.jToolBar.add(this.jButtonTasks);
        this.jToolBar.add(this.jButtonConfigVM);
        this.jToolBar.add(this.jButtonUsers);
        this.jToolBar.add(this.jButtonSimulate);
        this.jToolBar.add(this.jButtonInjectFaults);
    }

    private void initWindowProperties () {
        this.setTitle(getText("nomePrograma"));
        this.setIconImage(Toolkit
                              .getDefaultToolkit()
                              .getImage(getResourceOrThrow(ISPD_LOGO_FILE_PATH)));
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new IspdWindowAdapter());
    }

    private void initNotificationArea () {
        this.jTextAreaNotification.setEditable(false);
        this.jTextAreaNotification.setColumns(NOTIFICATION_AREA_COLS);
        this.jTextAreaNotification.setRows(NOTIFICATION_AREA_ROWS);
        this.jTextAreaNotification.setBorder(null);
    }

    private void buildLayoutAndPack () {
        final var contentPane = this.getContentPane();
        final var layout      = new GroupLayout(contentPane);
        contentPane.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                              .addContainerGap()
                              .addGroup(layout
                                            .createParallelGroup(
                                                GroupLayout.Alignment.TRAILING,
                                                false
                                            )
                                            .addComponent(this.jScrollPaneProperties)
                                            .addComponent(
                                                this.jScrollPaneSideBar,
                                                GroupLayout.DEFAULT_SIZE,
                                                236,
                                                Short.MAX_VALUE
                                            ))
                              .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(this.jScrollPaneNotificationBar)
                                            .addComponent(
                                                this.jToolBar,
                                                GroupLayout.Alignment.TRAILING,
                                                GroupLayout.DEFAULT_SIZE,
                                                595,
                                                Short.MAX_VALUE
                                            )
                                            .addComponent(this.jScrollPaneDrawingArea))
                              .addContainerGap())
        );

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    GroupLayout.Alignment.TRAILING,
                    layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                      .addGroup(layout.createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(
                                                        this.jScrollPaneSideBar,
                                                        GroupLayout.DEFAULT_SIZE, 248,
                                                        Short.MAX_VALUE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        this.jScrollPaneProperties,
                                                        GroupLayout.PREFERRED_SIZE, 205,
                                                        GroupLayout.PREFERRED_SIZE
                                                    ))
                                      .addGroup(layout.createSequentialGroup()
                                                    .addComponent(this.jToolBar,
                                                                  GroupLayout.PREFERRED_SIZE, 42,
                                                                  GroupLayout.PREFERRED_SIZE
                                                    )
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(this.jScrollPaneDrawingArea)
                                                    .addPreferredGap(
                                                        LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(
                                                        this.jScrollPaneNotificationBar,
                                                        GroupLayout.PREFERRED_SIZE, 102,
                                                        GroupLayout.PREFERRED_SIZE
                                                    )))
                        .addContainerGap()
                )
        );

        this.pack();
    }

    private void initMenuBar () {
        final var menus = new JMenu[] {
            this.jMenuFile,
            this.jMenuEdit,
            this.jMenuShow,
            this.jMenuTools,
            this.jMenuHelp,
        };

        final var menuBar = new JMenuBar();

        for (final var menu : menus) {
            menuBar.add(menu);
        }

        this.setJMenuBar(menuBar);
    }

    private void addKeyListeners () {
        final var components = new Component[] {
            this,
            this.jScrollPaneDrawingArea,
            this.jScrollPaneSideBar,
            this.jScrollPaneNotificationBar,
            this.jScrollPaneProperties,
            this.jTextAreaNotification,
            this.jToolBar,
            this.jToggleButtonCluster,
            this.jToggleButtonInternet,
            this.jToggleButtonMachine,
            this.jToggleButtonNetwork,
            this.jButtonTasks,
            this.jButtonUsers,
            this.jButtonSimulate,
            this.jPanelSimple,
            this.jPanelSettings,
            this.jPanelProperties,
        };

        for (final var component : components) {
            component.addKeyListener(this);
        }
    }

    private void initFileChooser () {
        this.jFileChooser.setAcceptAllFileFilterUsed(false);
        this.jFileChooser.setFileFilter(this.fileFilter);
        this.jFileChooser.setFileView(new IspdFileView());
        this.jFileChooser.setSelectedFile(this.configure.getLastFile());
    }

    private void jMenuItemManageActionPerformed (final ActionEvent evt) {
        this.showSubWindow(this.jFrameManager);
    }

    private void jMenuItemAboutActionPerformed (final ActionEvent evt) {
        this.showSubWindow(new AboutDialog(this, true));
    }

    private void iconButtonOnClick (
        final AbstractButton clickedButton, final int drawingIndex, final String notificationText
    ) {
        this.deselectOtherButtons(clickedButton);
        this.updateDrawingAreaButton(clickedButton, drawingIndex, notificationText);
    }

    private void initButton (
        final AbstractButton button,
        final String iconPath,
        final String toolTip,
        final ActionListener onClick
    ) {
        button.setIcon(getImage(iconPath));
        button.setToolTipText(getText(toolTip));
        button.setEnabled(false);
        button.setFocusable(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.addActionListener(onClick);
        this.jToolBar.add(button);
    }

    private void deselectOtherButtons (final AbstractButton button) {
        final boolean originalStatus = button.isSelected();
        this.deselectAllIconButtons();
        button.setSelected(originalStatus);
    }

    private void deselectAllIconButtons () {
        for (final var button : this.iconButtons) {
            button.setSelected(false);
        }
    }

    private void jToggleButtonMachineActionPerformed (final ActionEvent evt) {
        this.iconButtonOnClick(
            this.jToggleButtonMachine,
            DesenhoGrade.MACHINE,
            "Machine button selected."
        );
    }

    private void jToggleButtonNetworkActionPerformed (final ActionEvent evt) {
        this.iconButtonOnClick(
            this.jToggleButtonNetwork,
            DesenhoGrade.NETWORK,
            "Network button selected."
        );
    }

    private void jToggleButtonClusterActionPerformed (final ActionEvent evt) {
        this.iconButtonOnClick(
            this.jToggleButtonCluster,
            DesenhoGrade.CLUSTER,
            "Cluster button selected."
        );
    }

    private void jToggleButtonInternetActionPerformed (final ActionEvent evt) {
        this.iconButtonOnClick(
            this.jToggleButtonInternet,
            DesenhoGrade.INTERNET,
            "Internet button selected."
        );
    }

    private void jButtonTaskActionPerformed (final ActionEvent evt) {
        if (this.drawingArea == null) {
            return;
        }

        final var loadConfigWindow = new LoadConfigurationDialog(
            this,
            true,
            this.drawingArea.getUsuarios().toArray(),
            this.drawingArea.getNosEscalonadores().toArray(),
            this.drawingArea.getLoadConfiguration()
        );

        this.showSubWindow(loadConfigWindow);
        this.updateDrawingLoad(loadConfigWindow);
        this.modificar();
    }

    private void showSubWindow (final Window w) {
        w.setLocationRelativeTo(this);
        w.setVisible(true);
    }

    private void updateDrawingLoad (final LoadConfigurationDialog loadConfigWindow) {
        this.drawingArea.setLoadConfiguration(loadConfigWindow.getCargasConfiguracao());
        this.drawingArea.setUsers(loadConfigWindow.getUsuarios());
    }

    private void updateDrawingAreaButton (
        final AbstractButton clickedButton, final int drawingIndex, final String notificationText
    ) {
        if (!clickedButton.isSelected()) {
            this.drawingArea.setIconeSelecionado(null);
            return;
        }

        this.drawingArea.setIconeSelecionado(drawingIndex);
        this.appendNotificacao(getText(notificationText));
    }

    private String getOpenFileNameOrDefault () {
        return (this.openFile == null) ? "New_Model.ims" : this.openFile.getName();
    }

    public void modificar () {
        final var newTitle = String.format(
            "%s [%s] - %s", this.getOpenFileNameOrDefault(), getText("modified"),
            getText("nomePrograma")
        );

        this.setTitle(newTitle);
        this.currentFileHasUnsavedChanges = true;
    }

    private void jMenuItem1ActionPerformed (final ActionEvent evt) {
        this.showSubWindow(new SettingsDialog(this, true, this.configure));
    }

    private void jButtonSimulateActionPerformed (final ActionEvent evt) {
        final var simulationWindow = new SimulationDialog(
            this,
            true,
            this.drawingArea.getGrade(),
            this.modelType
        );

        simulationWindow.iniciarSimulacao();
        this.showSubWindow(simulationWindow);
        this.appendNotificacao(getText("Simulate button added."));
    }

    public void appendNotificacao (final String notificationText) {
        this.jTextAreaNotification.append(notificationText + "\n");
    }

    private void jMenuItemNovoActionPerformed (final ActionEvent evt) {
        if (this.currentFileHasUnsavedChanges) {
            this.saveChanges();
        }

        //janela de escolha de qual tipo de serviço irá ser modelado
        final var classPickWindow = new PickModelTypeDialog(this, true);
        this.showSubWindow(classPickWindow);

        this.drawingArea = new DesenhoGrade();
        this.updateGuiWithOpenFile("New model opened", null);
        this.modificar();
        this.onModelTypeChange(classPickWindow);
    }

    private void onModelTypeChange (final PickModelTypeDialog classPickWindow) {
        this.modelType = classPickWindow.getEscolha();
        this.drawingArea.setModelType(this.modelType);
        this.updateVmConfigButtonVisibility();
    }

    private void updateVmConfigButtonVisibility () {
        this.jButtonConfigVM.setVisible(this.modelType == PickModelTypeDialog.IAAS);
    }

    private void jMenuItemOpenActionPerformed (final ActionEvent evt) {
        if (this.shouldContinueEditingCurrentlyOpenedFile()) {
            return;
        }

        this.configureFileFilterAndChooser(
            "Iconic Model of Simulation",
            ISPD_FILE_EXTENSIONS,
            true
        );

        if (this.jFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final var file = this.jFileChooser.getSelectedFile();

        if (!hasValidIspdFileExtension(file)) {
            JOptionPane.showMessageDialog(
                null,
                getText("Invalid file"),
                getText("WARNING"),
                JOptionPane.PLAIN_MESSAGE
            );
            return;
        }

        try {
            this.readFileContents(file);
            this.updateGuiWithOpenFile("model opened", file);
        } catch (final ParserConfigurationException | IOException | SAXException ex) {
            this.processFileOpeningException(ex);
        }
    }

    private void processFileOpeningException (final Throwable ex) {
        Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        final var message =
            String.format(
                "%s\n%s",
                getText("Error opening file."),
                ex.getMessage()
            );
        JOptionPane.showMessageDialog(
            null,
            message,
            getText("WARNING"),
            JOptionPane.PLAIN_MESSAGE
        );
    }

    private void readFileContents (final File file)
        throws ParserConfigurationException, IOException, SAXException {
        final var doc = ManipuladorXML.readModelFromFile(file);
        this.startNewDrawing(doc);
        this.modelType       = this.drawingArea.getModelType();
        this.virtualMachines = this.drawingArea.getVirtualMachines();
        this.updateVmConfigButtonVisibility();
    }

    private void updateGuiWithOpenFile (final String message, final File file) {
        this.drawingArea.addKeyListener(this);
        this.drawingArea.setMainWindow(this);
        this.jScrollPaneSideBar.setViewportView(null);
        this.jPanelProperties.setText("");
        this.jScrollPaneDrawingArea.setViewportView(this.drawingArea);
        this.appendNotificacao(getText(message));
        this.openEditing(file);
    }

    private void startNewDrawing (final Document doc) {
        this.drawingArea = new DesenhoGrade();
        this.drawingArea.setGrid(doc);
    }

    private boolean shouldContinueEditingCurrentlyOpenedFile () {
        if (!this.currentFileHasUnsavedChanges) {
            return false;
        }

        final int userChoice = this.saveChanges();
        return userChoice == JOptionPane.CANCEL_OPTION || userChoice == JOptionPane.CLOSED_OPTION;
    }

    private void jMenuItemSaveActionPerformed (final ActionEvent evt) {
        if (this.openFile == null) {
            this.jMenuItemSaveAsActionPerformed(null);
            return;
        }

        if (this.drawingArea == null) {
            return;
        }

        this.saveDrawingAreaToFile(this.openFile);
        this.refreshEdits();
    }

    private void configureFileFilterAndChooser (
        final String description, final String[] extensions, final boolean shouldAcceptAllFiles
    ) {
        this.fileFilter.setDescricao(getText(description));
        this.fileFilter.setExtensao(extensions);
        this.jFileChooser.setAcceptAllFileFilterUsed(shouldAcceptAllFiles);
    }

    private void jMenuItemToJPGActionPerformed (final ActionEvent evt) {
        this.configureFileFilterAndChooser("JPG Image (.jpg)", new String[] { ".jpg" }, false);

        if (this.jFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final var file = this.getFileWithExtension(".jpg");
        final var img  = this.drawingArea.createImage();

        try {
            ImageIO.write(img, "jpg", file);
        } catch (final IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE,
                                                             null, ex
            );
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Warning",
                                          JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void jMenuItemCloseActionPerformed (final ActionEvent evt) {
        final int choice = this.currentFileHasUnsavedChanges ?
                           this.saveChanges() : JOptionPane.YES_OPTION;

        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        this.closeModel();
    }

    private void closeModel () {
        this.jScrollPaneDrawingArea.setViewportView(null);
        this.jScrollPaneSideBar.setViewportView(null);
        this.jPanelProperties.setText("");
        this.appendNotificacao(getText("model closed"));
        this.closeEditing();
    }

    private void jMenuItemExitActionPerformed (final ActionEvent evt) {
        this.formWindowClosing();
    }

    private void jMenuItemPasteActionPerformed (final ActionEvent evt) {
        if (this.drawingArea == null) {
            return;
        }

        this.drawingArea.botaoPainelActionPerformed(evt);
    }

    private void jMenuItemDeleteActionPerformed (final ActionEvent evt) {
        if (this.drawingArea == null) {
            return;
        }

        this.drawingArea.botaoIconeActionPerformed(evt);
    }

    private void jMenuItemCopyActionPerformed (final ActionEvent evt) {
        if (this.drawingArea == null) {
            return;
        }

        this.drawingArea.botaoVerticeActionPerformed(evt);
    }

    private void jMenuItemCompareActionPerformed (final ActionEvent evt) {
        if (this.drawingArea == null) {
            return;
        }

        this.drawingArea.matchNetwork();
    }

    private void showOrHideElements (
        final AbstractButton box,
        final String textIfSelected,
        final String textIfUnselected,
        final Consumer<? super Boolean> drawingAreaSetter,
        final ActionEvent event
    ) {
        final boolean isSelected = box.isSelected();
        final String  text       = isSelected ? textIfSelected : textIfUnselected;
        box.setSelected(isSelected);
        if (this.drawingArea != null) {
            drawingAreaSetter.accept(isSelected);
        }
        if (event != null) {
            this.appendNotificacao(getText(text));
        }
    }

    private void jCheckBoxMenuItemConnectedActionPerformed (final ActionEvent evt) {
        this.showOrHideElements(
            this.jCheckBoxMenuConnectedItem,
            "Connected Nodes unhidden.",
            "Connected Nodes hidden.",
            this.drawingArea::setShouldPrintDirectConnections,
            evt
        );
    }

    private void jCheckBoxMenuItemIndirectActionPerformed (final ActionEvent evt) {
        this.showOrHideElements(
            this.jCheckBoxIndirectMenuItem,
            "Indirectly Connected Nodes are now being shown",
            "Indirectly Connected Nodes are now not being shown",
            this.drawingArea::setShouldPrintIndirectConnections,
            evt
        );
    }

    private void jCheckBoxMenuItemSchedulableActionPerformed (final ActionEvent evt) {
        this.showOrHideElements(
            this.jCheckBoxMenuSchedulableItem,
            "Schedulable Nodes unhidden.",
            "Schedulable Nodes hidden.",
            this.drawingArea::setShouldPrintSchedulableNodes,
            evt
        );
    }

    private void jCheckBoxMenuItemGradeActionPerformed (final ActionEvent evt) {
        this.showOrHideElements(
            this.jCheckBoxMenuGridItem,
            "Drawing grid enabled.",
            "Drawing grid disabled.",
            status -> this.drawingArea.setGridOn(status), // Do this to
            // avoid NPE when no file is open
            evt
        );
    }

    private void jCheckBoxMenuItemRulerActionPerformed (final ActionEvent evt) {
        this.showOrHideElements(
            this.jCheckBoxRulerMenuItem,
            "Drawing rule enabled.",
            "Drawing rule disabled.",
            shouldShowRuler -> {
                if (shouldShowRuler) {
                    this.showRuler();
                } else {
                    this.closeDrawingArea();
                }
            },
            evt
        );
    }

    private void showRuler () {
        this.jScrollPaneDrawingArea.setColumnHeaderView(this.drawingArea.getColumnView());
        this.jScrollPaneDrawingArea.setRowHeaderView(this.drawingArea.getRowView());

        this.jScrollPaneDrawingArea.setCorner(
            ScrollPaneConstants.UPPER_LEFT_CORNER,
            this.drawingArea.getCorner()
        );
        this.jScrollPaneDrawingArea.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, new Corner());
        this.jScrollPaneDrawingArea.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, new Corner());
    }

    private void jMenuItemGenerateActionPerformed (final ActionEvent evt) {
        if (this.modelType == PickModelTypeDialog.GRID) {
            this.generatePolicy(this.jFrameManager);
            return;
        }

        if (this.modelType == PickModelTypeDialog.IAAS) {
            this.generatePolicy(this.jFrameCloudManager);
            this.generatePolicy(this.jFrameAllocManager);
        }
    }

    private void generatePolicy (final GenericPolicyManagementWindow window) {
        final var manager = window.getManager();
        final var path    = manager.directory().getAbsolutePath();

        final var policyGenerator =
            new PolicyGeneratorWindow(this, true, path, manager);

        this.showSubWindow(policyGenerator);

        Optional.ofNullable(policyGenerator.getParse())
            .ifPresent(i -> window.updatePolicyList());
    }

    private void jMenuItemHelpActionPerformed (final ActionEvent evt) {
        this.showSubWindow(new HelpWindow());
    }

    private void jMenuItemToTxtActionPerformed (final ActionEvent evt) {
        this.configureFileFilterAndChooser("Plane Text", new String[] { ".txt" }, false);

        if (this.jFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (
            final var fw = new FileWriter(
                this.getFileWithExtension(".txt"), java.nio.charset.StandardCharsets.UTF_8
            );
            final var pw = new PrintWriter(fw, true)
        ) {
            pw.print(this.drawingArea.makeDescriptiveModel());
        } catch (final IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(
                null,
                ex.getMessage(),
                "Warning",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void jMenuItemSaveAsActionPerformed (final ActionEvent evt) {
        if (this.drawingArea == null) {
            return;
        }

        this.configureFileFilterAndChooser(
            "Iconic Model of Simulation",
            new String[] { FileExtensions.ICONIC_MODEL },
            false
        );
        if (this.jFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final var file = this.getFileWithExtension(FileExtensions.ICONIC_MODEL);
        this.saveDrawingAreaToFile(file);
        this.openEditing(file);
    }

    private void saveDrawingAreaToFile (final File file) {
        final var doc = this.drawingArea.getGrade();
        ManipuladorXML.writeModelToFile(doc, file);
        this.appendNotificacao(getText("model saved"));
    }

    private void jButtonUsersActionPerformed (final ActionEvent evt) {
        if (this.drawingArea == null) {
            return;
        }

        final var users = new UserConfigurationDialog(
            this,
            true,
            this.drawingArea.getUsuarios(),
            this.drawingArea.getProfiles()
        );

        this.showSubWindow(users);
        this.updateDrawingAreaUsers(users);
        this.modificar();
    }

    private void updateDrawingAreaUsers (final UserConfigurationDialog users) {
        this.drawingArea.setUsers(users.getUsuarios());
        this.drawingArea.setProfiles(users.getLimite());
    }

    private void formWindowClosing () {
        this.configure.setLastFile(this.openFile);
        this.configure.saveCurrentConfig();

        if (!this.currentFileHasUnsavedChanges) {
            System.exit(0);
        }

        final int choice = this.saveChanges();
        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        System.exit(0);
    }

    private void jMenuFileActionPerformed (final ActionEvent evt) {
    }

    private void jMenuItemSortActionPerformed (final ActionEvent evt) {
        if (this.drawingArea == null) {
            return;
        }

        if (this.jMenuItemSort.getDisplayedMnemonicIndex() == 2) {
            this.jMenuItemSort.setDisplayedMnemonicIndex(1);
        } else {
            this.jMenuItemSort.setDisplayedMnemonicIndex(2);
            this.drawingArea.iconArrange();
        }

        this.drawingArea.repaint();
    }

    private void jMenuItemToSimGridActionPerformed (final ActionEvent evt) {
        this.exportToFileType("XML File", ".xml");
    }

    private void exportToFileType (final String description, final String extension) {
        this.configureFileFilterAndChooser(description, new String[] { extension }, false);

        if (this.jFileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final var file = this.getFileWithExtension(extension);

        try {
            new Exportador(this.drawingArea.getGrade()).toGridSim(file);
            JOptionPane.showMessageDialog(
                this,
                getText("model saved"),
                "Done",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (final IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                getText("WARNING"),
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void jMenuItemOpenResultActionPerformed (final ActionEvent evt) {
        this.jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        this.openResultInternal();
        this.jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    private void openResultInternal () {
        if (this.jFileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        final var dir = this.jFileChooser.getSelectedFile();

        if (!isValidDirectory(dir)) {
            return;
        }

        try {
            final var path = String.format("file://%s/result.html", dir.getAbsolutePath());
            HtmlPane.openDefaultBrowser(new URL(path));
        } catch (final IOException e) {
            final var message =
                String.format(
                    "%s\n%s",
                    getText("Error opening file."),
                    e.getMessage()
                );
            JOptionPane.showMessageDialog(
                null,
                message,
                getText("WARNING"),
                JOptionPane.PLAIN_MESSAGE
            );
        }
    }

    private void jMenuItemToGridSimActionPerformed (final ActionEvent evt) {
        this.exportToFileType("Java Source Files (. java)", FileExtensions.JAVA_SOURCE);
    }

    private File getFileWithExtension (final String ext) {
        final var file = this.jFileChooser.getSelectedFile();

        if (file.getName().endsWith(ext)) {
            return file;
        }

        return new File(file + ext);
    }

    private void jButtonConfigVMActionPerformed (final ActionEvent evt) {
        if (this.drawingArea.getNosEscalonadores().isEmpty()) {
            JOptionPane.showMessageDialog(
                null,
                "One or more VMMs need to be configurated",
                "WARNING!",
                JOptionPane.PLAIN_MESSAGE
            );
            return;
        }

        final var vmConfigWindow = new VmConfiguration(
            this,
            true,
            this.drawingArea.getUsuarios().toArray(),
            this.drawingArea.getNosEscalonadores().toArray(),
            this.virtualMachines
        );

        this.showSubWindow(vmConfigWindow);
        this.updateVirtualMachines(vmConfigWindow);
        this.modificar();
    }

    private void updateVirtualMachines (final VmConfiguration vmConfigWindow) {
        this.virtualMachines = vmConfigWindow.getMaqVirtuais();
        this.updateDrawingVms(vmConfigWindow);
    }

    private void updateDrawingVms (final VmConfiguration vmConfigWindow) {
        this.drawingArea.setUsers(vmConfigWindow.atualizaUsuarios());
        this.drawingArea.setVirtualMachines(vmConfigWindow.getMaqVirtuais());
    }

    private void jMenuItemManageCloudActionPerformed (final ActionEvent evt) {
        this.showSubWindow(this.jFrameCloudManager);
    }

    private void jMenuItemManageAllocationActionPerformed (final ActionEvent evt) {
        this.showSubWindow(this.jFrameAllocManager);
    }

    private void jMenuToolsActionPerformed (final ActionEvent evt) {
    }

    public JPanelConfigIcon getjPanelConfiguracao () {
        return this.jPanelSettings;
    }

    private void refreshEdits () {
        final var newTitle = String.format(
            "%s - %s", this.getOpenFileNameOrDefault(), getText("nomePrograma")
        );

        this.setTitle(newTitle);
        this.currentFileHasUnsavedChanges = false;
    }

    private int saveChanges () {
        final int choice = this.getChoiceForSavingChanges();

        if (choice == JOptionPane.YES_OPTION) {
            this.jMenuItemSaveActionPerformed(null);
            this.refreshEdits();
        }

        return choice;
    }

    private int getChoiceForSavingChanges () {
        final var message = String.format(
            "%s %s",
            getText("Do you want to save changes to"),
            this.getOpenFileNameOrDefault()
        );

        return JOptionPane.showConfirmDialog(this, message);
    }

    private void openEditing (final File file) {
        this.openFile = file;
        this.updateDrawingWithViewMenuOptions();
        this.jCheckBoxMenuItemRulerActionPerformed(null);
        this.deselectAllIconButtons();
        this.enableInteractables();
        this.refreshEdits();
    }

    private void enableInteractables () {
        this.setInteractablesEnabled(true);
    }

    private void updateDrawingWithViewMenuOptions () {
        this.drawingArea.setShouldPrintDirectConnections(this.jCheckBoxMenuConnectedItem.isSelected());
        this.drawingArea.setShouldPrintIndirectConnections(this.jCheckBoxIndirectMenuItem.isSelected());
        this.drawingArea.setShouldPrintSchedulableNodes(this.jCheckBoxMenuSchedulableItem.isSelected());
        this.drawingArea.setGridOn(this.jCheckBoxMenuGridItem.isSelected());
    }

    private void closeEditing () {
        this.setTitle(getText("nomePrograma"));
        this.openFile = null;
        this.disableInteractables();
        this.currentFileHasUnsavedChanges = false;
        this.closeDrawingArea();
    }

    private void closeDrawingArea () {
        this.jScrollPaneDrawingArea.setColumnHeaderView(null);
        this.jScrollPaneDrawingArea.setRowHeaderView(null);
        this.jScrollPaneDrawingArea.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, null);
        this.jScrollPaneDrawingArea.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER, null);
        this.jScrollPaneDrawingArea.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER, null);
    }

    private void disableInteractables () {
        this.setInteractablesEnabled(false);
    }

    private void setInteractablesEnabled (final boolean enabled) {
        for (final var interactable : this.interactables) {
            interactable.setEnabled(enabled);
        }
    }

    public void setSelectedIcon (final GridItem icon, final String text) {
        if (icon == null) {
            this.jScrollPaneSideBar.setViewportView(this.jPanelSimple);
            this.jPanelProperties.setText("");
            return;
        }

        if (icon instanceof Machine || icon instanceof Cluster) {
            this.jPanelSettings.setIcone(icon, this.drawingArea.getUsuarios(), this.modelType);
        } else {
            this.jPanelSettings.setIcone(icon);
        }

        this.jScrollPaneSideBar.setViewportView(this.jPanelSettings);
        this.jPanelProperties.setText(text);
    }

    private static class IspdFileView extends FileView {

        private static ImageIcon getIconForFileExtension (final File file) {
            final var ext = FileUtils.fileExtensionOf(file);

            if (ext.isEmpty() || !isIspdFileExtension(ext)) {
                return null;
            }

            final var imgURL = MainWindow.class.getResource(ISPD_LOGO_FILE_PATH);

            if (imgURL == null) {
                return null;
            }

            return new ImageIcon(imgURL);
        }

        private static boolean isIspdFileExtension (final String ext) {
            return "ims".equals(ext) || FileExtensions.ICONIC_MODEL.equals(ext);
        }

        @Override
        public Icon getIcon (final File f) {
            return getIconForFileExtension(f);
        }
    }

    private class IspdWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing (final WindowEvent e) {
            MainWindow.this.formWindowClosing();
        }
    }
}
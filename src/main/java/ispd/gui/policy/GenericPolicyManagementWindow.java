package ispd.gui.policy;

import static ispd.gui.TextSupplier.*;

import ispd.arquivo.interpretador.gerador.*;
import ispd.gui.auxiliar.*;
import ispd.gui.utils.*;
import ispd.policy.*;
import ispd.utils.*;
import ispd.utils.constants.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

public abstract class GenericPolicyManagementWindow extends JFrame {

    private static final String ICON_IMAGE_PATH = String.join(
        File.separator, "..", "imagens", "Logo_iSPD_25.png"
    );

    private final NonThrowingUndoManager undoManager = new NonThrowingUndoManager();

    private final PolicyManager manager;

    private final JFileChooser fileChooser = this.configuredFileChooser();

    private final JList<String> policyList = this.makePolicyList();

    private final JTextPane textPane = disabledTextPane();

    private final TextEditorStyle textEditor = new TextEditorStyle();

    private Optional<String> currentlyOpenFileName = Optional.empty();

    private boolean hasPendingChanges = false;

    protected GenericPolicyManagementWindow (final PolicyManager manager) {
        this.addWindowListener(new CancelableCloseWindowAdapter());
        this.setAlwaysOnTop(true);
        this.setFocusable(false);
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getResource(
            ICON_IMAGE_PATH
        )));

        this.configureMenuBar();
        this.configureTextEditor();

        this.makeLayout();
        this.pack();

        this.manager = manager;
        this.updateTitle();
        this.updatePolicyList();
    }

    private static JTextPane disabledTextPane () {
        final var tp = new JTextPane();
        tp.setEnabled(false);
        return tp;
    }

    private static String policyNameFromFile (final File file) {
        final var fullName = file.getName();
        return fullName.substring(0, fullName.length() - FileExtensions.JAVA_SOURCE.length());
    }

    private static JMenuBar makeMenuBarWith (final JMenu... menus) {
        final JMenuBar menuBar = new JMenuBar();

        for (final var menu : menus) {
            menuBar.add(menu);
        }

        return menuBar;
    }

    protected abstract String getButtonOpenTooltip ();

    protected abstract String getButtonNewTooltip ();

    protected abstract String getPolicyListTitle ();

    protected abstract String getWindowTitle ();

    private void configureTextEditor () {
        this.textEditor.configurarTextComponent(this.textPane);

        final var doc = this.textPane.getDocument();
        doc.addUndoableEditListener(this::onUndoableEvent);
        doc.addDocumentListener(new PendingChangesDocListener());
    }

    private JFileChooser configuredFileChooser () {
        final var chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new MultipleExtensionFileFilter(
            getText("Java Source Files (. java)"), FileExtensions.JAVA_SOURCE, true
        ));
        return chooser;
    }

    private void configureMenuBar () {
        this.setJMenuBar(makeMenuBarWith(
            this.makeMenu(
                "File",
                this.makeMenuItem("New",
                                  "/ispd/gui/imagens/insert-object_1.png",
                                  this::onNew, KeyEvent.VK_N,
                                  this.getButtonNewTooltip()
                ),
                this.makeMenuItem("Open",
                                  "/ispd/gui/imagens/document-open.png",
                                  evt1 -> this.runCancelableAction(this::openFile), KeyEvent.VK_O,
                                  this.getButtonOpenTooltip()
                ),
                this.makeMenuItem("Save",
                                  "/ispd/gui/imagens/document-save_1.png",
                                  this::onSave, KeyEvent.VK_S,
                                  "Save the open file"
                ),
                this.makeMenuItem("Import",
                                  "/ispd/gui/imagens/document-import.png",
                                  evt -> this.runCancelableAction(this::importFile), KeyEvent.VK_I
                )
            ),
            this.makeMenu(
                "Edit",
                this.makeMenuItem("Undo",
                                  "/ispd/gui/imagens/edit-undo.png",
                                  this.undoManager::undo, KeyEvent.VK_Z
                ),
                this.makeMenuItem("Redo",
                                  "/ispd/gui/imagens/edit-redo.png",
                                  this.undoManager::redo, KeyEvent.VK_Y
                ),
                new JPopupMenu.Separator(),
                this.makeMenuItem("Cut",
                                  "/ispd/gui/imagens/edit-cut.png",
                                  e -> this.textPane.cut(), KeyEvent.VK_X
                ),
                this.makeMenuItem("Copy",
                                  "/ispd/gui/imagens/edit-copy.png",
                                  e -> this.textPane.copy(), KeyEvent.VK_C
                ),
                this.makeMenuItem("Paste",
                                  "/ispd/gui/imagens/edit-paste.png",
                                  e -> this.textPane.paste(), KeyEvent.VK_P
                ),
                new JPopupMenu.Separator(),
                this.makeMenuItem(
                    "Delete",
                    "/ispd/gui/imagens/edit-delete.png",
                    this::onDelete
                )
            )
        ));
    }

    private JScrollPane makeEditorScrollPane () {
        final var scrollPane = new JScrollPane(this.textPane);
        scrollPane.setRowHeaderView(this.textEditor.getLinhas());
        scrollPane.setColumnHeaderView(this.textEditor.getCursor());
        return scrollPane;
    }

    private void onUndoableEvent (final UndoableEditEvent evt) {
        final var edit = evt.getEdit();
        if (!"style change".equals(edit.getPresentationName())) {
            this.undoManager.addEdit(edit);
        }
    }

    private JList<String> makePolicyList () {
        final var list = new JList<String>();
        final String cut = this.getPolicyListTitle();
        list.setBorder(BorderFactory.createTitledBorder(
            null,
            getText(cut),
            TitledBorder.CENTER,
            TitledBorder.DEFAULT_POSITION
        ));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new PolicyListMouseAdapter());
        return list;
    }

    private void makeLayout () {
        final var toolBar         = this.makeToolBar();
        final var policyListPanel = this.makePolicyListPane();
        final var textEditorPanel = this.makeEditorPanel();
        final var caretPosLabel   = new JLabel();

        final var layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(layout
                                      .createParallelGroup(GroupLayout.Alignment.LEADING)
                                      .addGroup(layout
                                                    .createSequentialGroup()
                                                    .addGroup(layout
                                                                  .createParallelGroup(
                                                                      GroupLayout.Alignment.LEADING)
                                                                  .addGroup(layout
                                                                                .createSequentialGroup()
                                                                                .addComponent(
                                                                                    policyListPanel,
                                                                                    GroupLayout.PREFERRED_SIZE,
                                                                                    GroupLayout.DEFAULT_SIZE,
                                                                                    GroupLayout.PREFERRED_SIZE
                                                                                )
                                                                                .addPreferredGap(
                                                                                    LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(
                                                                                    textEditorPanel,
                                                                                    GroupLayout.DEFAULT_SIZE,
                                                                                    GroupLayout.DEFAULT_SIZE,
                                                                                    Short.MAX_VALUE
                                                                                ))
                                                                  .addGroup(
                                                                      GroupLayout.Alignment.TRAILING,
                                                                      layout
                                                                          .createSequentialGroup()
                                                                          .addContainerGap(
                                                                              904,
                                                                              Short.MAX_VALUE
                                                                          )
                                                                          .addComponent(
                                                                              caretPosLabel)
                                                                  )
                                                                  .addComponent(
                                                                      toolBar,
                                                                      GroupLayout.DEFAULT_SIZE,
                                                                      904,
                                                                      Short.MAX_VALUE
                                                                  ))
                                                    .addContainerGap()));

        layout.setVerticalGroup(layout
                                    .createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(layout
                                                  .createSequentialGroup()
                                                  .addComponent(
                                                      toolBar,
                                                      GroupLayout.PREFERRED_SIZE,
                                                      GroupLayout.DEFAULT_SIZE,
                                                      GroupLayout.PREFERRED_SIZE
                                                  )
                                                  .addGap(2, 2, 2)
                                                  .addGroup(layout
                                                                .createParallelGroup(
                                                                    GroupLayout.Alignment.LEADING)
                                                                .addComponent(
                                                                    caretPosLabel,
                                                                    GroupLayout.Alignment.TRAILING
                                                                )
                                                                .addComponent(
                                                                    policyListPanel,
                                                                    GroupLayout.DEFAULT_SIZE,
                                                                    GroupLayout.DEFAULT_SIZE,
                                                                    Short.MAX_VALUE
                                                                )
                                                                .addComponent(
                                                                    textEditorPanel,
                                                                    GroupLayout.Alignment.TRAILING,
                                                                    GroupLayout.DEFAULT_SIZE,
                                                                    GroupLayout.DEFAULT_SIZE,
                                                                    Short.MAX_VALUE
                                                                ))
                                                  .addContainerGap()));
    }

    private JPanel makeEditorPanel () {
        final var editorPane  = this.makeEditorScrollPane();
        final var editorPanel = new JPanel();
        final var layout      = new GroupLayout(editorPanel);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                      .addGroup(layout
                                                    .createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(
                                                        editorPane,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        734,
                                                        Short.MAX_VALUE
                                                    )
                                                    .addContainerGap()));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(layout
                                                  .createSequentialGroup()
                                                  .addContainerGap()
                                                  .addComponent(editorPane)
                                                  .addContainerGap())
        );

        editorPanel.setLayout(layout);
        return editorPanel;
    }

    private JPanel makePolicyListPane () {
        final var policyListScrollPane = new JScrollPane(this.policyList);

        final var policyListPanel = new JPanel();
        final var layout          = new GroupLayout(policyListPanel);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                      .addGroup(layout
                                                    .createSequentialGroup()
                                                    .addContainerGap()
                                                    .addComponent(
                                                        policyListScrollPane,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        120,
                                                        Short.MAX_VALUE
                                                    )
                                                    .addContainerGap()));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(GroupLayout.Alignment.TRAILING, layout
                                        .createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(policyListScrollPane,
                                                      GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE
                                        )
                                        .addContainerGap())
        );

        policyListPanel.setLayout(layout);
        return policyListPanel;
    }

    private JMenu makeMenu (final String name, final Component... items) {
        final var menu = new JMenu();
        menu.setText(getText(name));

        for (final var item : items) {
            menu.add(item);
        }

        return menu;
    }

    private JMenuItem makeMenuItem (
        final String itemName,
        final String imagePath,
        final ActionListener action,
        final int acceleratorKey,
        final String toolTip
    ) {
        final var item = this.makeMenuItem(
            itemName, imagePath,
            action, acceleratorKey
        );
        item.setToolTipText(getText(toolTip));
        return item;
    }

    private JMenuItem makeMenuItem (
        final String itemName,
        final String imagePath,
        final ActionListener action,
        final int acceleratorKey
    ) {
        final var item = this.makeMenuItem(itemName, imagePath, action);
        item.setAccelerator(KeyStroke.getKeyStroke(
            acceleratorKey,
            InputEvent.CTRL_DOWN_MASK
        ));
        return item;
    }

    private JMenuItem makeMenuItem (
        final String itemName,
        final String imagePath,
        final ActionListener action
    ) {
        final var item = new JMenuItem();
        item.setIcon(new ImageIcon(this.getResource(imagePath)));
        item.setText(getText(itemName));
        item.addActionListener(action);
        return item;
    }

    private JToolBar makeToolBar () {
        final var tb = new JToolBar();
        tb.setRollover(true);

        tb.add(this.makeButton(
            "/ispd/gui/imagens/insert-object.png",
            this.getButtonNewTooltip(), this::onNew
        ));

        tb.add(this.makeButton(
            "/ispd/gui/imagens/document-save.png",
            "Save the open file", this::onSave
        ));

        tb.add(this.makeButton(
            "/ispd/gui/imagens/system-run.png",
            "Compile", this::onCompile
        ));

        return tb;
    }

    private JButton makeButton (
        final String iconPath,
        final String helpText,
        final ActionListener action
    ) {
        return ButtonBuilder.aButton(new ImageIcon(this.getResource(iconPath)), action)
            .withToolTip(getText(helpText))
            .withCenterBottomTextPosition()
            .nonFocusable()
            .build();
    }

    private URL getResource (final String name) {
        return this.getClass().getResource(name);
    }

    private void onNew (final ActionEvent e) {
        this.runCancelableAction(this::makeNewPolicy);
    }

    private void makeNewPolicy () {
        final var options = Map.<String, Runnable>of(
            "Code Editing", this::newPolicyFromCode,
            "Policy Generator", this::newPolicyFromGenerator
        );

        final var result = (String) JOptionPane.showInputDialog(
            this,
            "Create the policy with:",
            null,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            options.keySet().toArray(String[]::new),
            null
        );

        Optional.ofNullable(result)
            .map(options::get)
            .ifPresent(Runnable::run);
    }

    private void newPolicyFromCode () {
        final var name = JOptionPane.showInputDialog(
            this, "Enter the name of the policy"
        );

        Optional.ofNullable(name)
            .filter(NameValidator::isValidClassName)
            .ifPresent(this::openNewPolicyInEditor);
    }

    private void openNewPolicyInEditor (final String name) {
        this.fillEditorWithCode(name, this.manager.getPolicyTemplate(name));
        this.setAsPendingChanges();
    }

    private void newPolicyFromGenerator () {
        this.getGeneratedPolicy()
            .ifPresent(this::saveAndCompileGeneratedPolicy);
    }

    private void saveAndCompileGeneratedPolicy (final InterpretadorGerador policy) {
        final var fileName = policy.getNome();
        this.manager.escrever(fileName, policy.getCodigo());
        this.tryCompileTarget(fileName);
    }

    private Optional<InterpretadorGerador> getGeneratedPolicy () {
        final var dialog = new PolicyGeneratorWindow(
            this,
            true,
            this.manager.directory().getAbsolutePath(),
            this.manager
        );

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return Optional.ofNullable(dialog.getParse());
    }

    private void tryCompileTarget (final String fileName) {
        final var errors = this.manager.compilar(fileName);

        if (errors != null) {
            JOptionPane.showMessageDialog(
                this,
                errors,
                "Errors during compilation",
                JOptionPane.ERROR_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                this,
                """
                Policy %s
                Compiled Successfully""".formatted(this.currentlyOpenFileName.get())
            );
        }

        this.updatePolicyList();
    }

    private void openSelectedPolicy () {
        final var name = (String) this.policyList.getSelectedValue();
        final var code = this.manager.ler(name);
        this.fillEditorWithCode(name, code);
    }

    private void fillEditorWithCode (final String fileName, final String code) {
        this.currentlyOpenFileName = Optional.of(fileName);
        this.setAsNoPendingChanges();

        this.clearTextPaneContents();

        try {
            this.textPane.getDocument().insertString(0, code, null);
        } catch (final BadLocationException ignored) {
        }

        this.textPane.setEnabled(true);
    }

    private void clearTextPaneContents () {
        final var doc = (TextEditorStyle) this.textPane.getDocument();

        try {
            doc.remove(0, doc.getLength());
        } catch (final BadLocationException ignored) {
        }
    }

    private void setAsNoPendingChanges () {
        this.hasPendingChanges = false;
        this.updateTitle();
    }

    private void updateTitle () {
        this.currentlyOpenFileName.ifPresentOrElse(
            this::updateTitleWithFileName,
            this::updateTitleWithNoFile
        );
    }

    private void updateTitleWithNoFile () {
        this.setTitle(this.getTranslatedWindowTitle());
    }

    private void updateTitleWithFileName (final String fileName) {
        final String afterFileName;
        afterFileName = this.hasPendingChanges ? " [%s] ".formatted(getText("modified")) : " ";

        this.setTitle("%s.java%s- %s".formatted(
            fileName, afterFileName,
            this.getTranslatedWindowTitle()
        ));
    }

    private String getTranslatedWindowTitle () {
        return getText(this.getWindowTitle());
    }

    private void onSave (final ActionEvent evt) {
        if (this.currentlyOpenFileName.isPresent() && this.hasPendingChanges) {
            this.savePendingChanges();
        }
    }

    private void onDelete (final ActionEvent evt) {
        if (this.policyList.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "A policy should be selected"
            );
            return;
        }

        final var selected = this.policyList.getSelectedValue();

        final int choice = JOptionPane.showConfirmDialog(
            this,
            """
            Are you sure want delete this policy:
            %s""".formatted(selected),
            null,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        if (!this.manager.remover(selected)) {
            JOptionPane.showMessageDialog(this, "Failed to remove policy %s".formatted(selected));
            return;
        }

        if (selected.equals(this.currentlyOpenFileName.get())) {
            this.closeEditing();
        }

        this.updatePolicyList();
    }

    private void onCompile (final ActionEvent evt) {
        if (this.currentlyOpenFileName.isEmpty()) {
            return;
        }

        if (this.hasPendingChanges) {
            this.savePendingChanges();
        }

        this.tryCompileTarget(this.currentlyOpenFileName.get());
    }

    private void runCancelableAction (final Runnable action) {
        final var choice =
            this.hasPendingChanges ? this.askAboutPendingChanges() : JOptionPane.YES_OPTION;
        switch (choice) {
            case JOptionPane.CANCEL_OPTION, JOptionPane.CLOSED_OPTION:
                return;
            default:
                action.run();
        }
    }

    private void openFile () {
        this.configureFileChooser(false, this.manager.directory());
        this.getFileChoice().ifPresent(this::openChosenFile);
    }

    private void openChosenFile (final File file) {
        final var name = policyNameFromFile(file);
        final var code = this.manager.ler(name);
        this.fillEditorWithCode(name, code);
    }

    private void importFile () {
        this.configureFileChooser(true, null);
        this.getFileChoice().ifPresent(this::importFile);
    }

    private void importFile (final File f) {
        if (!this.manager.importJavaPolicy(f)) {
            JOptionPane.showMessageDialog(
                this,
                "Could not import policy",
                "Error!",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        this.updatePolicyList();
        this.openChosenFile(f);
    }

    private Optional<File> getFileChoice () {
        final int fileChoice = this.fileChooser.showOpenDialog(this);

        if (fileChoice != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        final var file = this.fileChooser.getSelectedFile();
        return Optional.ofNullable(file);
    }

    private void configureFileChooser (
        final boolean showTopBorder, final File startDirectory
    ) {
        ((BorderLayout) this.fileChooser.getLayout())
            .getLayoutComponent(BorderLayout.NORTH)
            .setVisible(showTopBorder);

        this.fileChooser.getComponent(0).setVisible(showTopBorder);
        this.fileChooser.setCurrentDirectory(startDirectory);
    }

    private void updatePolicyList () {
        this.policyList.setListData(this.manager.listar().toArray(String[]::new));
    }

    private int askAboutPendingChanges () {
        final int choice = JOptionPane.showConfirmDialog(
            this,
            "%s %s.java".formatted(
                getText("Do you want to save changes to"),
                this.currentlyOpenFileName.get()
            )
        );

        if (choice == JOptionPane.YES_OPTION) {
            this.savePendingChanges();
        }

        return choice;
    }

    private void savePendingChanges () {
        this.manager.escrever(
            this.currentlyOpenFileName.get(),
            this.textPane.getText()
        );
        this.setAsNoPendingChanges();
    }

    private void closeEditing () {
        this.currentlyOpenFileName = Optional.empty();
        this.hasPendingChanges     = false;
        this.updateTitle();

        this.clearTextPaneContents();

        this.textPane.setEnabled(false);
    }

    private void setAsPendingChanges () {
        this.hasPendingChanges = true;
        this.updateTitle();
    }

    public PolicyManager getManager () {
        return this.manager;
    }

    private static class NonThrowingUndoManager extends UndoManager {

        private void undo (final ActionEvent evt) {
            try {
                this.undo();
            } catch (final CannotUndoException ignored) {
            }
        }

        private void redo (final ActionEvent evt) {
            try {
                this.redo();
            } catch (final CannotRedoException ignored) {
            }
        }
    }

    private class PendingChangesDocListener implements DocumentListener {

        @Override
        public void insertUpdate (final DocumentEvent e) {
            this.setAsPendingChanges();
        }

        @Override
        public void removeUpdate (final DocumentEvent e) {
            this.setAsPendingChanges();
        }

        @Override
        public void changedUpdate (final DocumentEvent e) {
        }

        private void setAsPendingChanges () {
            if (GenericPolicyManagementWindow.this.hasPendingChanges) {
                return;
            }

            GenericPolicyManagementWindow.this.setAsPendingChanges();
        }
    }

    private class CancelableCloseWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing (final WindowEvent e) {
            GenericPolicyManagementWindow.this.runCancelableAction(this::closeWindow);
        }

        private void closeWindow () {
            GenericPolicyManagementWindow.this.setVisible(false);
        }
    }

    private class PolicyListMouseAdapter extends MouseAdapter {

        private static boolean isDoubleClick (final MouseEvent evt) {
            return evt.getClickCount() == 2;
        }

        @Override
        public void mouseClicked (final MouseEvent e) {
            if (!isDoubleClick(e)) {
                return;
            }

            GenericPolicyManagementWindow.this.runCancelableAction(
                GenericPolicyManagementWindow.this::openSelectedPolicy);
        }
    }
}

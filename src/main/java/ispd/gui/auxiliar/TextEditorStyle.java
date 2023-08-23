package ispd.gui.auxiliar;

import ispd.gui.utils.fonts.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * Text style to visualize Java code. After instantiation, the method
 * configurarTextComponent(JTextComponent) must be invoked, which will configure the component
 * appropriately.
 */
public class TextEditorStyle extends DefaultStyledDocument implements CaretListener {

    private static final char NEW_LINE = '\n';

    private static final char OPEN_BRACKET = '{';

    private static final char CLOSE_BRACKET = '}';

    private static final String TAB_AS_SPACES = "    ";

    private static final String AUTOCOMPLETE_ACTION_KEY = "completar";

    private static final String[] STRING_MATCHERS = {
        "\"(.*)\"",
        "('.')",
        "('..')"
    };

    private static final String NUMBER_MATCHER = "\\b\\d+\\b";

    private static final String[] AUTOCOMPLETE_STRINGS = {
        "boolean", "break", "case", "class", "double", "else",
        "false", "final", "float", "for", "if", "instanceof", "int",
        "new", "null", "private", "protected", "public", "return",
        "static", "String", "super", "switch", "System",
        "this", "true", "try", "void", "while",
        "escravos", "filaEscravo", "tarefas", "metricaUsuarios",
        "mestre", "caminhoEscravo", "adicionarTarefa(tarefa)",
        "getTempoAtualizar()", "resultadoAtualizar(mensagem)",
        "addTarefaConcluida(tarefa)", "sendTask(Tarefa tarefa)",
        "executeScheduling()",
        "sendMessage(tarefa, escravo, tipo)",
        "cloneTask(Tarefa get)",
        "Mensagens.CANCELAR", "Mensagens.PARAR", "Mensagens.DEVOLVER",
        "Mensagens.DEVOLVER_COM_PREEMPCAO", "Mensagens.ATUALIZAR"
    };

    private static final Font font = Monospaced.BOLD;

    private final Element rootElement = this.getDefaultRootElement();

    private final String[] keywords = {
        "\\bfor\\b",
        "\\bif\\b",
        "\\belse\\b",
        "\\bwhile\\b",
        "\\bint\\b",
        "\\bboolean\\b",
        "\\bnew\\b",
        "\\bdouble\\b",
        "\\bpublic\\b",
        "\\bprivate\\b",
        "\\bprotected\\b",
        "\\breturn\\b",
        "\\bthis\\b",
        "\\bstatic\\b",
        "\\bvoid\\b",
        "\\btry\\b",
        "\\bcatch\\b",
        "\\bbreak\\b",
        "\\bthrow\\b",
        "\\bpackage\\b",
        "\\bimport\\b",
        "\\bclass\\b",
        "\\bextends\\b",
        "\\btrue\\b",
        "\\bfalse\\b"
    };

    private final MutableAttributeSet style = new SimpleAttributeSet();

    private final Color defaultStyle = Color.black;

    private final Color commentStyle = Color.lightGray;

    private final Color keyStyle = Color.blue;

    private final Color numberStyle = new Color(0, 150, 0);

    private final Color stringStyle = new Color(250, 125, 0);

    private final Pattern singleCommentDelim = Pattern.compile("//");

    private final Pattern multiCommentDelimStart = Pattern.compile("/\\*");

    private final Pattern multiCommentDelimEnd = Pattern.compile("\\*/");

    private final JTextArea lineCountBar = this.makeLineCountBar();

    private final JLabel barAfterCursor = makeBarAfterCursor();

    private final JList<String> autocompleteList = this.makeAutoCompleteList();

    private final JPopupMenu autocompletePopup = this.makeAutocompletePopup();

    private Integer lineCount = 1;

    public TextEditorStyle () {
        this.putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
        this.setStyleFromConstants();
    }

    private static JLabel makeBarAfterCursor () {
        final JLabel bac = new JLabel("Linha: 0 | Coluna: 0 ");
        bac.setBackground(Color.lightGray);
        bac.setHorizontalAlignment(SwingConstants.RIGHT);
        return bac;
    }

    private static int countMatches (final String input) {
        final var pa = Pattern.compile("\n");
        final var ma = pa.matcher(input);

        int total = 0;
        while (ma.find()) {
            total++;
        }
        return total;
    }

    @Override
    public void remove (final int offset, final int length)
        throws BadLocationException {
        final int total = countMatches(this.getText(offset, length));

        if (total > 0) {
            this.updateLineCountBar(total);
            this.updateColumns();
        }

        this.removeAndProcessChanges(offset, length);
    }

    @Override
    public void insertString (final int offset, final String text, final AttributeSet attr)
        throws BadLocationException {
        if (text.length() != 1) {
            this.insertPastedText(offset, text, attr);
            this.processChangedLines();
            return;
        }

        switch (text) {
            case "\n" -> {
                final var tabs =
                    TAB_AS_SPACES.repeat(this.calculateScopeDepthUntil(offset));
                super.insertString(offset, "\n" + tabs, this.style);
                this.insertLines(1);
            }
            case "\t" -> super.insertString(
                offset,
                TAB_AS_SPACES,
                this.style
            );
            case "}" -> {
                this.removeAdditionalSpaces(offset, text);
                this.processChangedLines();
            }
            default -> {
                super.insertString(offset, text, this.style);
                this.processChangedLines();
            }
        }
    }

    @Override
    public void caretUpdate (final CaretEvent ce) {
        final int start = ce.getDot();
        final int end   = ce.getMark();
        final var text  = (JTextComponent) ce.getSource();

        if (start == end) {
            this.caretUpdateWithoutSelection(text, start);
            return;
        }

        if (start < end) {
            this.caretUpdateWithSelection(text.getText(), start, end);
            return;
        }

        this.caretUpdateWithSelection(text.getText(), end, start);
    }

    private void setStyleFromConstants () {
        StyleConstants.setFontFamily(this.style, Font.MONOSPACED);
        StyleConstants.setBold(this.style, true);
        StyleConstants.setFontSize(this.style, 12);
    }

    private JPopupMenu makeAutocompletePopup () {
        final JScrollPane popupBar = new JScrollPane();
        popupBar.setBorder(null);
        popupBar.setViewportView(this.autocompleteList);
        final JPopupMenu acp = new JPopupMenu();
        acp.add(popupBar);
        return acp;
    }

    private JList<String> makeAutoCompleteList () {
        final var acl = new JList<>(AUTOCOMPLETE_STRINGS);
        acl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        acl.addMouseListener(new AutoCompleteMouseAdapter());
        acl.addKeyListener(new AutoCompleteKeyAdapter());
        return acl;
    }

    private void insertAutocomplete () {
        try {
            final int dot =
                ((JTextComponent) this.autocompletePopup.getInvoker()).getCaret().getDot();
            final var autocomplete = this.autocompleteList.getSelectedValue();
            final int insertPos    = this.ignoreSpace(dot, autocomplete);
            this.insertString(insertPos, autocomplete, null);
        } catch (final BadLocationException ex) {
            Logger.getLogger(TextEditorStyle.class.getName()).log(Level.SEVERE,
                                                                  null, ex
            );
        }

        this.autocompletePopup.setVisible(false);
    }

    private int ignoreSpace (final int dot, final String autocomplete)
        throws BadLocationException {
        if (dot <= 0) {
            return dot;
        }

        if (!this.getText(dot - 2, 2).equals(" " + autocomplete.charAt(0))) {
            return dot;
        }

        this.remove(dot - 1, 1);
        return dot - 1;
    }

    private void insertPastedText (final int offset, final String str, final AttributeSet attr)
        throws BadLocationException {
        final var spaces = str.replaceAll("\t", TAB_AS_SPACES);
        final int total  = countMatches(spaces);
        this.insertLines(total);
        super.insertString(offset, spaces, attr);
    }

    private int calculateScopeDepthUntil (final int offset)
        throws BadLocationException {
        final var textBefore = this.getText(0, offset);
        int       depth      = 0;

        for (int i = 0; i < textBefore.length(); i++) {
            if (textBefore.charAt(i) == OPEN_BRACKET) {
                depth++;
            } else if (textBefore.charAt(i) == CLOSE_BRACKET) {
                depth--;
            }
        }

        return depth;
    }

    private void insertLines (final int total)
        throws BadLocationException {
        this.updateText(total);
        this.updateColumns();
    }

    private void removeAdditionalSpaces (final int offset, final String str)
        throws BadLocationException {
        final String text = this.getText(0, offset);
        super.insertString(offset, str, this.style);
        if (text.substring(offset - 4).equals(TAB_AS_SPACES)) {
            super.remove(offset - 4, 4);
        }
    }

    private void updateLineCountBar (final int total)
        throws BadLocationException {
        this.lineCount -= total;
        for (int i = 0; i < total; i++) {
            final int end = this.lineCountBar.getText().length();
            final int pos = this.lineCountBar.getText().lastIndexOf(NEW_LINE, end);
            this.lineCountBar.getDocument().remove(pos, end - pos);
        }
    }

    private void updateColumns () {
        if (this.lineCount.toString().length() != this.lineCountBar.getColumns()) {
            this.lineCountBar.setColumns(this.lineCount.toString().length());
        }
    }

    private void removeAndProcessChanges (final int offset, final int length)
        throws BadLocationException {
        super.remove(offset, length);
        this.processChangedLines();
    }

    private void updateText (final int total)
        throws BadLocationException {
        final Document doc = this.lineCountBar.getDocument();
        for (int i = 0; i < total; i++) {
            this.lineCount++;
            doc.insertString(doc.getLength(), "\n" + this.lineCount, null);
        }
    }

    private JTextArea makeLineCountBar () {
        final var lcb = new JTextArea();
        lcb.setDisabledTextColor(Color.BLACK);
        lcb.setEnabled(false);
        lcb.setMargin(new Insets(-2, 0, 0, 0));
        lcb.setColumns(1);
        lcb.setFont(font);
        lcb.setText("1");
        lcb.setBackground(Color.lightGray);
        return lcb;
    }

    public Font getFont () {
        return font;
    }

    public Component getCursor () {
        return this.barAfterCursor;
    }

    public Component getLinhas () {
        return this.lineCountBar;
    }

    private void caretUpdateWithoutSelection (final JTextComponent text, final int start) {
        try {
            final var caretCoords = (Rectangle) text.modelToView2D(start);
            this.barAfterCursor.setText(
                "Linha: %d | Coluna: %d ".formatted(
                    (caretCoords.y - 4) / 15 + 1,
                    (caretCoords.x - 6) / 7
                ));
        } catch (final BadLocationException ignored) {
        }
    }

    private void caretUpdateWithSelection (final String text, final int start, final int end) {
        final int length = end - start;
        if (length > 1 && length < 50) {
            try {
                final var selected = "\\b%s\\b".formatted(this.getText(start, length));
                this.processChangedLines();
                this.markOnTextAllEqualToSelected(text, selected);
            } catch (final Exception ex) {
                StyleConstants.setBackground(this.style, Color.WHITE);
            }
        }
        this.barAfterCursor.setText("selection from: %d to %d ".formatted(start, end));
    }

    private void processChangedLines ()
        throws BadLocationException {
        // Normal Text
        final String text = this.getText(0, this.getLength());
        StyleConstants.setForeground(this.style, this.defaultStyle);
        StyleConstants.setBold(this.style, false);
        this.setCharacterAttributes(0, this.getLength(), this.style, true);

        // Keywords
        StyleConstants.setBold(this.style, true);
        StyleConstants.setForeground(this.style, this.keyStyle);
        for (final String keyword : this.keywords) {
            final Pattern p = Pattern.compile(keyword);
            final Matcher m = p.matcher(text);

            while (m.find()) {
                this.setCharacterAttributes(m.start(), m.end() - m.start(), this.style, true);
            }
        }

        //numbers
        StyleConstants.setForeground(this.style, this.numberStyle);

        {
            final Pattern p = Pattern.compile(NUMBER_MATCHER);
            final Matcher m = p.matcher(text);
            while (m.find()) {
                this.setCharacterAttributes(m.start(), m.end() - m.start(),
                                            this.style,
                                            true
                );
            }
        }

        StyleConstants.setForeground(this.style, this.stringStyle);
        for (final String keyword : STRING_MATCHERS) {
            final Pattern p = Pattern.compile(keyword);
            final Matcher m = p.matcher(text);

            while (m.find()) {
                this.setCharacterAttributes(m.start(), m.end() - m.start(),
                                            this.style,
                                            true
                );
            }
        }

        // Comments
        StyleConstants.setForeground(this.style, this.commentStyle);
        final Matcher mlcStart = this.multiCommentDelimStart.matcher(text);
        final Matcher mlcEnd   = this.multiCommentDelimEnd.matcher(text);
        while (mlcStart.find()) {
            if (mlcEnd.find(mlcStart.end())) {
                this.setCharacterAttributes(
                    mlcStart.start(),
                    (mlcEnd.end() - mlcStart.start()),
                    this.style,
                    true
                );
            } else {
                this.setCharacterAttributes(mlcStart.start(), this.getLength(), this.style, true);
            }
        }

        final Matcher slc = this.singleCommentDelim.matcher(text);

        while (slc.find()) {
            final int line      = this.rootElement.getElementIndex(slc.start());
            final int endOffset = this.rootElement.getElement(line).getEndOffset() - 1;
            this.setCharacterAttributes(slc.start(), (endOffset - slc.start()), this.style, true);
        }
    }

    private void markOnTextAllEqualToSelected (final String text, final String selectedText) {
        StyleConstants.setForeground(this.style, Color.BLACK);
        StyleConstants.setBackground(this.style, Color.YELLOW);
        final Pattern p = Pattern.compile(selectedText);
        final Matcher m = p.matcher(text);
        while (m.find()) {
            this.setCharacterAttributes(m.start(), m.end() - m.start(), this.style, true);
        }
        StyleConstants.setBackground(this.style, Color.WHITE);
    }

    public void close () {
        this.lineCount = 1;
        this.lineCountBar.setText("1");
        this.barAfterCursor.setText("Linha: 0 | Coluna: 0 ");
    }

    public void configurarTextComponent (final JTextComponent component) {
        component.setDocument(this);
        component.addCaretListener(this);
        component.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK),
            AUTOCOMPLETE_ACTION_KEY
        );
        component.getActionMap().put(
            AUTOCOMPLETE_ACTION_KEY,
            new TextPaneAction(component)
        );
    }

    private void displayAutocomplete (
        final int dot,
        final Rectangle caretCoords,
        final Component component
    )
        throws BadLocationException {
        this.autocompletePopup.show(component, caretCoords.x, caretCoords.y);
        this.autocompleteList.setSelectedIndex(this.autocompleteListIndex(dot));
        this.autocompleteList.repaint();
        this.autocompleteList.requestFocus();
    }

    private int autocompleteListIndex (final int dotPosition)
        throws BadLocationException {
        if (dotPosition <= 0) {
            return 0;
        }

        final var text = this.getText(dotPosition - 1, 1);
        return (int) Arrays
            .stream(AUTOCOMPLETE_STRINGS)
            .takeWhile(s -> !s.startsWith(text))
            .count();
    }

    private class AutoCompleteMouseAdapter extends MouseAdapter {

        @Override
        public void mouseClicked (final MouseEvent e) {
            if (e.getClickCount() == 2) {
                TextEditorStyle.this.insertAutocomplete();
            }
        }
    }

    private class AutoCompleteKeyAdapter extends KeyAdapter {

        @Override
        public void keyReleased (final KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                TextEditorStyle.this.insertAutocomplete();
            }
        }
    }

    private class TextPaneAction extends AbstractAction {

        private final JTextComponent area;

        private TextPaneAction (final JTextComponent area) {
            this.area = area;
        }

        @Override
        public void actionPerformed (final ActionEvent actionEvent) {
            try {
                final int dot      = this.area.getCaret().getDot();
                final var caretPos = (Rectangle) this.area.modelToView2D(dot);
                TextEditorStyle.this.displayAutocomplete(dot, caretPos, this.area);
            } catch (final BadLocationException ex) {
                Logger.getLogger(TextEditorStyle.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
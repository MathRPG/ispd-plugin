package ispd.gui.utils.components;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public class HtmlPane extends JEditorPane implements HyperlinkListener {

    private static final String TAG_END = ">";

    private static final String CLOSE_TAG_START = "</";

    private static final int SET_CARET_POSITION_UPPER_BOUND = 20;

    public HtmlPane () {
        this.setContentType("text/html");
        this.setEditable(false);
        this.addHyperlinkListener(this);
    }

    /**
     * Opens a link with user's default browser, if supported
     *
     * @param url
     *     address for browser to open
     */
    public static void openDefaultBrowser (final URL url) {
        final var desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

        if (desktop == null || !desktop.isSupported(Desktop.Action.BROWSE)) {
            return;
        }

        try {
            desktop.browse(url.toURI());
        } catch (final IOException | URISyntaxException ignored) {
        }
    }

    private static String getDocText (final Document doc) {
        try {
            return doc.getText(0, doc.getLength());
        } catch (final BadLocationException ex) {
            Logger.getLogger(HtmlPane.class.getName())
                .log(Level.SEVERE, null, ex);
        }

        return "";
    }

    @Override
    public void hyperlinkUpdate (final HyperlinkEvent event) {
        if (event.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
            return;
        }

        final var desc = event.getURL().getRef();
        if (desc != null && !desc.isEmpty()) {
            this.jumpToAnchor(desc);
            return;
        }

        if (event instanceof final HTMLFrameHyperlinkEvent ev) {
            final var doc = (HTMLDocument) this.getDocument();
            doc.processHTMLFrameHyperlinkEvent(ev);
            return;
        }

        openDefaultBrowser(event.getURL());
    }

    private void jumpToAnchor (final String anchorDesc) {
        final int pos = this.getStringPositionInDoc(this.searchStringFromAnchorDesc(anchorDesc));

        if (pos <= SET_CARET_POSITION_UPPER_BOUND) {
            this.setCaretPosition(0);
            return;
        }

        try {
            final var caret = (Rectangle) this.modelToView2D(pos);
            caret.y += this.getParent().getHeight() - caret.height;
            this.scrollRectToVisible(caret);
        } catch (final BadLocationException ex) {
            Logger.getLogger(HtmlPane.class.getName())
                .log(Level.SEVERE, null, ex);
        }
    }

    private int getStringPositionInDoc (final String s) {
        return getDocText(this.getDocument()).lastIndexOf(s);
    }

    private String searchStringFromAnchorDesc (final String desc) {
        final var html  = this.getText();
        final int tag   = html.indexOf(("id=\"%s\"").formatted(desc));
        final int start = 1 + html.indexOf(TAG_END, tag);
        final int end   = html.indexOf(CLOSE_TAG_START, start);

        return html.substring(start, end)
            .replaceAll("\n", "")
            .replaceAll("&#225;", "á")
            .replaceAll("&#227;", "ã")
            .replaceAll("&#231;", "ç")
            .replaceAll("&#233;", "é")
            .replaceAll("&#245;", "õ")
            .trim();
    }
}
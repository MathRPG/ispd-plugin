package ispd.arquivo.xml;

import java.io.*;
import java.util.logging.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Responsible for low-level xml file parsing, writing and creation calls
 *
 * @see IconicModelDocumentBuilder
 * @see ConfiguracaoISPD
 */
public enum ManipuladorXML {
    ;

    private static final String ISPD_DTD = "iSPD.dtd";

    /**
     * Read a xml file using the dtd pointed to by the path
     *
     * @param file
     *     file to be read
     * @param dtdPath
     *     path to dtd file with the xml specification to be used
     *
     * @return {@link Document} with the structured file information
     *
     * @throws ParserConfigurationException
     *     if the dtd is ill-formed
     * @throws SAXException
     *     if the file is ill-formed
     */
    public static Document read (final File file, final String dtdPath)
        throws ParserConfigurationException, IOException, SAXException {

        final var factory = makeFactory();
        return makeDocBuilder(dtdPath, factory).parse(file);
    }

    private static DocumentBuilderFactory makeFactory () {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        return factory;
    }

    private static DocumentBuilder makeDocBuilder (
        final String dtdPath,
        final DocumentBuilderFactory factory
    )
        throws ParserConfigurationException {
        final var builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new SubstituteEntityResolver(dtdPath));
        return builder;
    }

    /**
     * Write xml {@link Document} to file in {@code outputFile}
     *
     * @param doc
     *     document to be written
     * @param outputFile
     *     path in which to write resulting file
     * @param docTypeSystem
     *     type system to be used
     * @param omitXmlDecl
     *     whether to omit the xml declarations or not
     *
     * @return {@code true} if the file was saved successfully, {@code false} otherwise
     */
    public static boolean write (
        final Document doc,
        final File outputFile,
        final String docTypeSystem,
        final boolean omitXmlDecl
    ) {
        try {
            final var source = new DOMSource(doc);
            final var result = new StreamResult(outputFile);

            makeTransformer(docTypeSystem, omitXmlDecl).transform(source, result);

            return true;
        } catch (final TransformerException ex) {
            Logger.getLogger(ManipuladorXML.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private static Transformer makeTransformer (
        final String docTypeSystem,
        final boolean omitXmlDecl
    )
        throws TransformerConfigurationException {
        final var transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, docTypeSystem);

        if (omitXmlDecl) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }

        return transformer;
    }

    /**
     * Create empty xml {@link Document} in which elements can be inserted
     *
     * @return new, empty xml {@link Document}
     */
    public static Document newDocument () {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (final ParserConfigurationException ex) {
            Logger.getLogger(ManipuladorXML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Write iconic model in {@link Document} to the given file.
     *
     * @param doc
     *     {@link Document} containing an iconic model.
     * @param file
     *     file in which to save the model.
     *
     * @return {@code true} if the file was saved successfully, {@code false} otherwise.
     */
    public static boolean writeModelToFile (final Document doc, final File file) {
        return write(doc, file, ISPD_DTD, false);
    }

    /**
     * Reads xml file and parses it into a {@link Document} containing an iconic model.
     *
     * @param file
     *     XML file with an iconic model.
     *
     * @return {@link Document} with the iconic model represented in the file.
     */
    public static Document readModelFromFile (final File file)
        throws ParserConfigurationException, IOException, SAXException {
        return read(file, ISPD_DTD);
    }

    private static final class SubstituteEntityResolver implements EntityResolver {

        private final InputSource substitute;

        private SubstituteEntityResolver (final String dtd) {
            this.substitute =
                new InputSource(ManipuladorXML.class.getResourceAsStream("dtd/" + dtd));
        }

        @Override
        public InputSource resolveEntity (final String s, final String s1) {
            return this.substitute;
        }
    }
}

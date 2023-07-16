package ispd.arquivo.interpretador.simgrid;

import ispd.utils.StringUtils;
import java.io.Serial;

/**
 * This exception is thrown when parse errors are encountered. You can explicitly create objects of
 * this exception type by calling the method generateParseException in the generated parser.
 * <p>
 * You can modify this class to customize your error reporting mechanisms so long as you retain the
 * public fields.
 */
public class ParseException extends Exception {

    /**
     * The version identifier for this Serializable class. Increment only if the <i>serialized</i>
     * form of the class changes.
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * This is a reference to the "tokenImage" array of the generated parser within which the parse
     * error occurred.  This array is defined in the generated ...Constants interface.
     */
    public String[] tokenImage = null;

    /**
     * This constructor is used by the method "generateParseException" in the generated parser.
     * Calling this constructor generates a new object of this type with the fields "currentToken",
     * "expectedTokenSequences", and "tokenImage" set.
     */
    public ParseException (
        final Token currentTokenVal,
        final int[][] expectedTokenSequencesVal,
        final String[] tokenImageVal
    ) {
        super(initialise(currentTokenVal, expectedTokenSequencesVal, tokenImageVal));
        this.tokenImage = tokenImageVal;
    }

    /**
     * The following constructors are for use by you for whatever purpose you can think of.
     * Constructing the exception in this manner makes the exception behave in the normal way -
     * i.e., as documented in the class "Throwable".  The fields "errorToken",
     * "expectedTokenSequences", and "tokenImage" do not contain relevant information.  The JavaCC
     * generated code does not use these constructors.
     */

    public ParseException () {
        super();
    }

    /**
     * It uses "currentToken" and "expectedTokenSequences" to generate a parse error message and
     * returns it.  If this object has been created due to a parse error, and you do not catch it
     * (it gets thrown from the parser) the correct error message gets displayed.
     */
    private static String initialise (
        final Token currentToken,
        final int[][] expectedTokenSequences,
        final String[] tokenImage
    ) {
        final String       eol      = System.getProperty("line.separator", "\n");
        final StringBuffer expected = new StringBuffer();
        int                maxSize  = 0;
        for (int i = 0; i < expectedTokenSequences.length; i++) {
            if (maxSize < expectedTokenSequences[i].length) {
                maxSize = expectedTokenSequences[i].length;
            }
            for (int j = 0; j < expectedTokenSequences[i].length; j++) {
                expected.append(tokenImage[expectedTokenSequences[i][j]]).append(' ');
            }
            if (expectedTokenSequences[i][expectedTokenSequences[i].length - 1] != 0) {
                expected.append("...");
            }
            expected.append(eol).append("    ");
        }
        String retval = "Encountered \"";
        Token  tok    = currentToken.next;
        for (int i = 0; i < maxSize; i++) {
            if (i != 0) {
                retval += " ";
            }
            if (tok.kind == 0) {
                retval += tokenImage[0];
                break;
            }
            retval += " " + tokenImage[tok.kind];
            retval += " \"";
            retval += add_escapes(tok.image);
            retval += " \"";
            tok = tok.next;
        }
        retval += "\" at line "
                  + currentToken.next.beginLine
                  + ", column "
                  + currentToken.next.beginColumn;
        retval += "." + eol;
        if (expectedTokenSequences.length == 1) {
            retval += "Was expecting:" + eol + "    ";
        } else {
            retval += "Was expecting one of:" + eol + "    ";
        }
        retval += expected.toString();
        return retval;
    }

    /**
     * Used to convert raw characters to their escaped version when these raw version cannot be used
     * as part of an ASCII string literal.
     */
    private static String add_escapes (final CharSequence seq) {
        return StringUtils.escapeString(seq);
    }
}

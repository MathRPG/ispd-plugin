package ispd.arquivo.interpretador.simgrid;

import ispd.utils.StringUtils;
import java.io.Serial;

/**
 * Token Manager Error.
 */
public class TokenMgrError extends Error {

    /**
     * The version identifier for this Serializable class. Increment only if the <i>serialized</i>
     * form of the class changes.
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * Constructor with message and reason.
     */
    public TokenMgrError (final String message) {
        super(message);
    }

    /**
     * Full Constructor.
     */
    public TokenMgrError (
        final boolean EOFSeen,
        final int errorLine,
        final int errorColumn,
        final String errorAfter,
        final char curChar
    ) {
        this(LexicalError(EOFSeen, errorLine, errorColumn, errorAfter, curChar));
    }

    /**
     * Replaces unprintable characters by their escaped (or unicode escaped) equivalents in the
     * given string
     */
    private static String addEscapes (final CharSequence str) {
        return StringUtils.escapeString(str);
    }

    /**
     * Returns a detailed message for the Error when it is thrown by the token manager to indicate a
     * lexical error. Parameters : EOFSeen     : indicates if EOF caused the lexical error
     * curLexState : lexical state in which this error occurred errorLine   : line number when the
     * error occurred errorColumn : column number when the error occurred errorAfter  : prefix that
     * was seen before this error occurred curchar     : the offending character Note: You can
     * customize the lexical error message by modifying this method.
     */
    private static String LexicalError (
        final boolean EOFSeen,
        final int errorLine,
        final int errorColumn,
        final String errorAfter,
        final char curChar
    ) {
        return (
            "Lexical error at line " +
            errorLine + ", column " +
            errorColumn + ".  Encountered: " +
            (
                EOFSeen
                ? "<EOF> "
                : ("\"" + addEscapes(String.valueOf(curChar)) + "\"") + " (" + (int) curChar + "), "
            ) +
            "after : \"" + addEscapes(errorAfter) + "\""
        );
    }
}

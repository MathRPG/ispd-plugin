package ispd.arquivo.interpretador.gerador;

import java.io.Serial;
import java.io.Serializable;

/**
 * Describes the input token stream.
 */

public class Token implements Serializable {

    /**
     * The version identifier for this Serializable class. Increment only if the <i>serialized</i>
     * form of the class changes.
     */
    @Serial private static final long serialVersionUID = 1L;

    /**
     * An integer that describes the kind of this token.  This numbering system is determined by
     * JavaCCParser, and a table of these numbers is stored in the file ...Constants.java.
     */
    public int kind = 0;

    /**
     * The line number of the first character of this Token.
     */
    public int beginLine = 0;

    /**
     * The column number of the first character of this Token.
     */
    public int beginColumn = 0;

    /**
     * The line number of the last character of this Token.
     */
    public int endLine = 0;

    /**
     * The column number of the last character of this Token.
     */
    public int endColumn = 0;

    /**
     * The string image of the token.
     */
    public String image = null;

    /**
     * A reference to the next regular (non-special) token from the input stream.  If this is the
     * last token from the input stream, or if the token manager has not read tokens beyond this
     * one, this field is set to null.  This is true only if this token is also a regular token.
     * Otherwise, see below for a description of the contents of this field.
     */
    public Token next = null;

    /**
     * This field is used to access special tokens that occur prior to this token, but after the
     * immediately preceding regular (non-special) token. If there are no such special tokens, this
     * field is set to null. When there are more than one such special token, this field refers to
     * the last of these special tokens, which in turn refers to the next previous special token
     * through its specialToken field, and so on until the first special token (whose specialToken
     * field is null). The next fields of special tokens refer to other special tokens that
     * immediately follow it (without an intervening regular token).  If there is no such token,
     * this field is null.
     */
    public Token specialToken = null;

    /**
     * No-argument constructor
     */
    public Token () {
    }

    /**
     * Constructs a new token for the specified Image.
     */
    public Token (final int kind) {
        this(kind, null);
    }

    /**
     * Constructs a new token for the specified Image and Kind.
     */
    public Token (final int kind, final String image) {
        this.kind  = kind;
        this.image = image;
    }

    /**
     * Returns the image.
     */
    @Override
    public String toString () {
        return this.image;
    }
}

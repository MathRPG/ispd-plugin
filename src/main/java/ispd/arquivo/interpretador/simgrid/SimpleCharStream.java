package ispd.arquivo.interpretador.simgrid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * An implementation of interface CharStream, where the stream is assumed to contain only ASCII
 * characters (without unicode processing).
 */

public class SimpleCharStream {

    private static int column = 0;

    private static int line = 1;

    /**
     * Position in buffer.
     */
    private static int bufpos = -1;

    private static int[] bufline = null;

    private static int[] bufcolumn = null;

    private static boolean prevCharIsCR = false;

    private static boolean prevCharIsLF = false;

    private static Reader inputStream = null;

    private static char[] buffer = null;

    private static int maxNextCharInd = 0;

    private static int inBuf = 0;

    private static int tabSize = 8;

    private static int bufsize = 0;

    private static int available = 0;

    private static int tokenBegin = 0;

    /**
     * Constructor.
     */
    public SimpleCharStream (
        final Reader dstream, final int startline,
        final int startcolumn, final int buffersize
    ) {
        if (SimpleCharStream.inputStream != null) {
            throw new Error(
                "\n   ERROR: Second call to the constructor of a static SimpleCharStream.\n" +
                "       You must either use ReInit() or set the JavaCC option STATIC to false\n" +
                "       during the generation of this class.");
        }
        SimpleCharStream.inputStream = dstream;
        SimpleCharStream.line        = startline;
        SimpleCharStream.column      = startcolumn - 1;

        SimpleCharStream.available = SimpleCharStream.bufsize = buffersize;
        SimpleCharStream.buffer    = new char[buffersize];
        SimpleCharStream.bufline   = new int[buffersize];
        SimpleCharStream.bufcolumn = new int[buffersize];
    }

    /**
     * Constructor.
     */
    public SimpleCharStream (
        final InputStream dstream, final String encoding, final int startline,
        final int startcolumn, final int buffersize
    )
        throws UnsupportedEncodingException {
        this(
            encoding == null
            ? new InputStreamReader(dstream, StandardCharsets.UTF_8)
            : new InputStreamReader(dstream, encoding),
            startline,
            startcolumn,
            buffersize
        );
    }

    /**
     * Constructor.
     */
    public SimpleCharStream (
        final InputStream dstream, final String encoding, final int startline,
        final int startcolumn
    )
        throws UnsupportedEncodingException {
        this(dstream, encoding, startline, startcolumn, 4096);
    }

    private static void ExpandBuff (final boolean wrapAround) {
        final char[] newbuffer    = new char[SimpleCharStream.bufsize + 2048];
        final int[]  newbufline   = new int[SimpleCharStream.bufsize + 2048];
        final int[]  newbufcolumn = new int[SimpleCharStream.bufsize + 2048];

        try {
            if (wrapAround) {
                System.arraycopy(
                    SimpleCharStream.buffer,
                    SimpleCharStream.tokenBegin,
                    newbuffer,
                    0,
                    SimpleCharStream.bufsize
                    - SimpleCharStream.tokenBegin
                );
                System.arraycopy(
                    SimpleCharStream.buffer,
                    0,
                    newbuffer,
                    SimpleCharStream.bufsize - SimpleCharStream.tokenBegin,
                    SimpleCharStream.bufpos
                );
                SimpleCharStream.buffer = newbuffer;

                System.arraycopy(
                    SimpleCharStream.bufline,
                    SimpleCharStream.tokenBegin, newbufline, 0, SimpleCharStream.bufsize
                                                                - SimpleCharStream.tokenBegin
                );
                System.arraycopy(
                    SimpleCharStream.bufline,
                    0,
                    newbufline,
                    SimpleCharStream.bufsize - SimpleCharStream.tokenBegin,
                    SimpleCharStream.bufpos
                );
                SimpleCharStream.bufline = newbufline;

                System.arraycopy(
                    SimpleCharStream.bufcolumn,
                    SimpleCharStream.tokenBegin, newbufcolumn, 0, SimpleCharStream.bufsize
                                                                  - SimpleCharStream.tokenBegin
                );
                System.arraycopy(
                    SimpleCharStream.bufcolumn, 0, newbufcolumn, SimpleCharStream.bufsize
                                                                 - SimpleCharStream.tokenBegin,
                    SimpleCharStream.bufpos
                );
                SimpleCharStream.bufcolumn = newbufcolumn;

                SimpleCharStream.maxNextCharInd = (
                    SimpleCharStream.bufpos +=
                        (SimpleCharStream.bufsize - SimpleCharStream.tokenBegin)
                );
            } else {
                System.arraycopy(
                    SimpleCharStream.buffer,
                    SimpleCharStream.tokenBegin,
                    newbuffer,
                    0,
                    SimpleCharStream.bufsize
                    - SimpleCharStream.tokenBegin
                );
                SimpleCharStream.buffer = newbuffer;

                System.arraycopy(
                    SimpleCharStream.bufline,
                    SimpleCharStream.tokenBegin, newbufline, 0, SimpleCharStream.bufsize
                                                                - SimpleCharStream.tokenBegin
                );
                SimpleCharStream.bufline = newbufline;

                System.arraycopy(
                    SimpleCharStream.bufcolumn,
                    SimpleCharStream.tokenBegin, newbufcolumn, 0, SimpleCharStream.bufsize
                                                                  - SimpleCharStream.tokenBegin
                );
                SimpleCharStream.bufcolumn = newbufcolumn;

                SimpleCharStream.maxNextCharInd =
                    (SimpleCharStream.bufpos -= SimpleCharStream.tokenBegin);
            }
        } catch (final Throwable t) {
            throw new Error(t.getMessage());
        }

        SimpleCharStream.bufsize += 2048;
        SimpleCharStream.available  = SimpleCharStream.bufsize;
        SimpleCharStream.tokenBegin = 0;
    }

    private static void FillBuff ()
        throws IOException {
        if (SimpleCharStream.maxNextCharInd == SimpleCharStream.available) {
            if (SimpleCharStream.available == SimpleCharStream.bufsize) {
                if (SimpleCharStream.tokenBegin > 2048) {
                    SimpleCharStream.bufpos    = SimpleCharStream.maxNextCharInd = 0;
                    SimpleCharStream.available = SimpleCharStream.tokenBegin;
                } else if (SimpleCharStream.tokenBegin < 0) {
                    SimpleCharStream.bufpos = SimpleCharStream.maxNextCharInd = 0;
                } else {
                    ExpandBuff(false);
                }
            } else if (SimpleCharStream.available > SimpleCharStream.tokenBegin) {
                SimpleCharStream.available = SimpleCharStream.bufsize;
            } else if ((SimpleCharStream.tokenBegin - SimpleCharStream.available) < 2048) {
                ExpandBuff(true);
            } else {
                SimpleCharStream.available = SimpleCharStream.tokenBegin;
            }
        }

        final int i;
        try {
            if ((
                    i = SimpleCharStream.inputStream.read(
                        SimpleCharStream.buffer,
                        SimpleCharStream.maxNextCharInd,
                        SimpleCharStream.available
                        - SimpleCharStream.maxNextCharInd
                    )
                ) == -1) {
                SimpleCharStream.inputStream.close();
                throw new IOException();
            } else {
                SimpleCharStream.maxNextCharInd += i;
            }
        } catch (final IOException e) {
            --SimpleCharStream.bufpos;
            backup(0);
            if (SimpleCharStream.tokenBegin == -1) {
                SimpleCharStream.tokenBegin = SimpleCharStream.bufpos;
            }
            throw e;
        }
    }

    /**
     * Start.
     */
    public static char BeginToken ()
        throws IOException {
        SimpleCharStream.tokenBegin = -1;
        final char c = readChar();
        SimpleCharStream.tokenBegin = SimpleCharStream.bufpos;

        return c;
    }

    private static void UpdateLineColumn (final char c) {
        SimpleCharStream.column++;

        if (SimpleCharStream.prevCharIsLF) {
            SimpleCharStream.prevCharIsLF = false;
            SimpleCharStream.line += (SimpleCharStream.column = 1);
        } else if (SimpleCharStream.prevCharIsCR) {
            SimpleCharStream.prevCharIsCR = false;
            if (c == '\n') {
                SimpleCharStream.prevCharIsLF = true;
            } else {
                SimpleCharStream.line += (SimpleCharStream.column = 1);
            }
        }

        switch (c) {
            case '\r':
                SimpleCharStream.prevCharIsCR = true;
                break;
            case '\n':
                SimpleCharStream.prevCharIsLF = true;
                break;
            case '\t':
                SimpleCharStream.column--;
                SimpleCharStream.column += (
                    SimpleCharStream.tabSize - (
                        SimpleCharStream.column
                        % SimpleCharStream.tabSize
                    )
                );
                break;
            default:
                break;
        }

        SimpleCharStream.bufline[SimpleCharStream.bufpos]   = SimpleCharStream.line;
        SimpleCharStream.bufcolumn[SimpleCharStream.bufpos] = SimpleCharStream.column;
    }

    /**
     * Read a character.
     */
    public static char readChar ()
        throws IOException {
        if (SimpleCharStream.inBuf > 0) {
            --SimpleCharStream.inBuf;

            ++SimpleCharStream.bufpos;
            if (SimpleCharStream.bufpos == SimpleCharStream.bufsize) {
                SimpleCharStream.bufpos = 0;
            }

            return SimpleCharStream.buffer[SimpleCharStream.bufpos];
        }

        ++SimpleCharStream.bufpos;
        if (SimpleCharStream.bufpos >= SimpleCharStream.maxNextCharInd) {
            FillBuff();
        }

        final char c = SimpleCharStream.buffer[SimpleCharStream.bufpos];

        UpdateLineColumn(c);
        return c;
    }

    /**
     * Get token end column number.
     */
    public static int getEndColumn () {
        return SimpleCharStream.bufcolumn[SimpleCharStream.bufpos];
    }

    /**
     * Get token end line number.
     */
    public static int getEndLine () {
        return SimpleCharStream.bufline[SimpleCharStream.bufpos];
    }

    /**
     * Get token beginning column number.
     */
    public static int getBeginColumn () {
        return SimpleCharStream.bufcolumn[SimpleCharStream.tokenBegin];
    }

    /**
     * Get token beginning line number.
     */
    public static int getBeginLine () {
        return SimpleCharStream.bufline[SimpleCharStream.tokenBegin];
    }

    /**
     * Backup a number of characters.
     */
    public static void backup (final int amount) {

        SimpleCharStream.inBuf += amount;
        if ((SimpleCharStream.bufpos -= amount) < 0) {
            SimpleCharStream.bufpos += SimpleCharStream.bufsize;
        }
    }

    /**
     * Get token literal value.
     */
    public static String GetImage () {
        if (SimpleCharStream.bufpos >= SimpleCharStream.tokenBegin) {
            return new String(
                SimpleCharStream.buffer, SimpleCharStream.tokenBegin,
                SimpleCharStream.bufpos - SimpleCharStream.tokenBegin + 1
            );
        } else {
            return new String(
                SimpleCharStream.buffer,
                SimpleCharStream.tokenBegin, SimpleCharStream.bufsize - SimpleCharStream.tokenBegin
            ) +
                   new String(SimpleCharStream.buffer, 0, SimpleCharStream.bufpos + 1);
        }
    }

    /**
     * Get the suffix.
     */
    public static char[] GetSuffix (final int len) {
        final char[] ret = new char[len];

        if ((SimpleCharStream.bufpos + 1) >= len) {
            System.arraycopy(
                SimpleCharStream.buffer,
                SimpleCharStream.bufpos - len + 1,
                ret,
                0,
                len
            );
        } else {
            System.arraycopy(
                SimpleCharStream.buffer,
                SimpleCharStream.bufsize - (len - SimpleCharStream.bufpos - 1), ret, 0,
                len - SimpleCharStream.bufpos - 1
            );
            System.arraycopy(
                SimpleCharStream.buffer,
                0,
                ret,
                len - SimpleCharStream.bufpos - 1,
                SimpleCharStream.bufpos + 1
            );
        }

        return ret;
    }

    /**
     * Reinitialise.
     */
    public void ReInit (
        final Reader dstream, final int startline,
        final int startcolumn, final int buffersize
    ) {
        SimpleCharStream.inputStream = dstream;
        SimpleCharStream.line        = startline;
        SimpleCharStream.column      = startcolumn - 1;

        if (SimpleCharStream.buffer == null || buffersize != SimpleCharStream.buffer.length) {
            SimpleCharStream.available = SimpleCharStream.bufsize = buffersize;
            SimpleCharStream.buffer    = new char[buffersize];
            SimpleCharStream.bufline   = new int[buffersize];
            SimpleCharStream.bufcolumn = new int[buffersize];
        }
        SimpleCharStream.prevCharIsLF = SimpleCharStream.prevCharIsCR = false;
        SimpleCharStream.tokenBegin   =
        SimpleCharStream.inBuf        = SimpleCharStream.maxNextCharInd = 0;
        SimpleCharStream.bufpos       = -1;
    }

    /**
     * Reinitialise.
     */
    public void ReInit (
        final InputStream dstream, final String encoding, final int startline,
        final int startcolumn, final int buffersize
    )
        throws UnsupportedEncodingException {
        this.ReInit(
            encoding == null
            ? new InputStreamReader(dstream, StandardCharsets.UTF_8)
            : new InputStreamReader(dstream, encoding),
            startline,
            startcolumn,
            buffersize
        );
    }

    /**
     * Reinitialise.
     */
    public void ReInit (
        final InputStream dstream, final String encoding, final int startline,
        final int startcolumn
    )
        throws UnsupportedEncodingException {
        this.ReInit(dstream, encoding, startline, startcolumn, 4096);
    }
}

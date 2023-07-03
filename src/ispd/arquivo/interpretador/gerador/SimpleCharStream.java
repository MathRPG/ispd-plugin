package ispd.arquivo.interpretador.gerador;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * An implementation of interface CharStream, where the stream is assumed to contain only ASCII
 * characters (without unicode processing).
 */

public final class SimpleCharStream {

    private static final int BUFFER_SIZE = 4096;

    private final Reader inputStream;

    /**
     * Position in buffer.
     */
    private int bufpos = -1;

    private int[] bufline = new int[SimpleCharStream.BUFFER_SIZE];

    private int[] bufcolumn = new int[SimpleCharStream.BUFFER_SIZE];

    private int column = 0;

    private int line = 1;

    private boolean prevCharIsCR = false;

    private boolean prevCharIsLF = false;

    private char[] buffer = new char[SimpleCharStream.BUFFER_SIZE];

    private int maxNextCharInd = 0;

    private int inBuf = 0;

    private int bufferSize = SimpleCharStream.BUFFER_SIZE;

    private int available = SimpleCharStream.BUFFER_SIZE;

    private int tokenBegin = 0;

    /**
     * Constructor.
     */
    public SimpleCharStream (final InputStream inputStream) {
        this.inputStream = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    private void ExpandBuff (final boolean wrapAround) {
        final var newbuffer    = new char[this.bufferSize + 2048];
        final var newbufline   = new int[this.bufferSize + 2048];
        final var newbufcolumn = new int[this.bufferSize + 2048];

        try {
            if (wrapAround) {
                System.arraycopy(
                    this.buffer,
                    this.tokenBegin, newbuffer, 0, this.bufferSize - this.tokenBegin
                );
                System.arraycopy(
                    this.buffer,
                    0,
                    newbuffer,
                    this.bufferSize - this.tokenBegin,
                    this.bufpos
                );
                this.buffer = newbuffer;

                System.arraycopy(
                    this.bufline,
                    this.tokenBegin, newbufline, 0, this.bufferSize - this.tokenBegin
                );
                System.arraycopy(
                    this.bufline,
                    0,
                    newbufline,
                    this.bufferSize - this.tokenBegin,
                    this.bufpos
                );
                this.bufline = newbufline;

                System.arraycopy(
                    this.bufcolumn,
                    this.tokenBegin, newbufcolumn, 0, this.bufferSize - this.tokenBegin
                );
                System.arraycopy(
                    this.bufcolumn, 0, newbufcolumn,
                    this.bufferSize - this.tokenBegin, this.bufpos
                );
                this.bufcolumn = newbufcolumn;

                this.maxNextCharInd = (this.bufpos += (this.bufferSize - this.tokenBegin));
            } else {
                System.arraycopy(
                    this.buffer,
                    this.tokenBegin, newbuffer, 0, this.bufferSize - this.tokenBegin
                );
                this.buffer = newbuffer;

                System.arraycopy(
                    this.bufline,
                    this.tokenBegin, newbufline, 0, this.bufferSize - this.tokenBegin
                );
                this.bufline = newbufline;

                System.arraycopy(
                    this.bufcolumn,
                    this.tokenBegin, newbufcolumn, 0, this.bufferSize - this.tokenBegin
                );
                this.bufcolumn = newbufcolumn;

                this.maxNextCharInd = (this.bufpos -= this.tokenBegin);
            }
        } catch (final Throwable t) {
            throw new Error(t.getMessage());
        }

        this.bufferSize += 2048;
        this.available  = this.bufferSize;
        this.tokenBegin = 0;
    }

    private void FillBuff () throws IOException {
        if (this.maxNextCharInd == this.available) {
            if (this.available == this.bufferSize) {
                if (this.tokenBegin > 2048) {
                    this.bufpos    = this.maxNextCharInd = 0;
                    this.available = this.tokenBegin;
                } else if (this.tokenBegin < 0) {
                    this.bufpos = this.maxNextCharInd = 0;
                } else {
                    this.ExpandBuff(false);
                }
            } else if (this.available > this.tokenBegin) {
                this.available = this.bufferSize;
            } else if ((this.tokenBegin - this.available) < 2048) {
                this.ExpandBuff(true);
            } else {
                this.available = this.tokenBegin;
            }
        }

        try {
            final int i;
            if ((
                    i = this.inputStream.read(
                        this.buffer,
                        this.maxNextCharInd, this.available - this.maxNextCharInd
                    )
                ) == -1) {
                this.inputStream.close();
                throw new IOException();
            } else {
                this.maxNextCharInd += i;
            }
        } catch (final IOException e) {
            --this.bufpos;
            this.backup(0);
            if (this.tokenBegin == -1) {
                this.tokenBegin = this.bufpos;
            }
            throw e;
        }
    }

    /**
     * Start.
     */
    public char BeginToken () throws IOException {
        this.tokenBegin = -1;
        final var c = this.readChar();
        this.tokenBegin = this.bufpos;

        return c;
    }

    private void UpdateLineColumn (final char c) {
        this.column++;

        if (this.prevCharIsLF) {
            this.prevCharIsLF = false;
            this.line += (this.column = 1);
        } else if (this.prevCharIsCR) {
            this.prevCharIsCR = false;
            if (c == '\n') {
                this.prevCharIsLF = true;
            } else {
                this.line += (this.column = 1);
            }
        }

        switch (c) {
            case '\r' -> this.prevCharIsCR = true;
            case '\n' -> this.prevCharIsLF = true;
            case '\t' -> {
                this.column--;
                final var tabSize = 8;
                this.column += (tabSize - (this.column % tabSize));
            }
        }

        this.bufline[this.bufpos]   = this.line;
        this.bufcolumn[this.bufpos] = this.column;
    }

    /**
     * Read a character.
     */
    public char readChar () throws IOException {
        if (this.inBuf > 0) {
            --this.inBuf;

            ++this.bufpos;
            if (this.bufpos == this.bufferSize) {
                this.bufpos = 0;
            }

            return this.buffer[this.bufpos];
        }

        ++this.bufpos;
        if (this.bufpos >= this.maxNextCharInd) {
            this.FillBuff();
        }

        final var c = this.buffer[this.bufpos];

        this.UpdateLineColumn(c);
        return c;
    }

    /**
     * Get token end column number.
     */
    public int getEndColumn () {
        return this.bufcolumn[this.bufpos];
    }

    /**
     * Get token end line number.
     */
    public int getEndLine () {
        return this.bufline[this.bufpos];
    }

    /**
     * Get token beginning column number.
     */
    public int getBeginColumn () {
        return this.bufcolumn[this.tokenBegin];
    }

    /**
     * Get token beginning line number.
     */
    public int getBeginLine () {
        return this.bufline[this.tokenBegin];
    }

    /**
     * Backup a number of characters.
     */
    public void backup (final int amount) {
        this.inBuf += amount;
        if ((this.bufpos -= amount) < 0) {
            this.bufpos += this.bufferSize;
        }
    }

    /**
     * Get token literal value.
     */
    public String GetImage () {
        if (this.bufpos >= this.tokenBegin) {
            return new String(this.buffer, this.tokenBegin, this.bufpos - this.tokenBegin + 1);
        } else {
            return new String(this.buffer, this.tokenBegin, this.bufferSize - this.tokenBegin) +
                   new String(this.buffer, 0, this.bufpos + 1);
        }
    }

    /**
     * Get the suffix.
     */
    public char[] GetSuffix (final int len) {
        final var ret = new char[len];

        if ((this.bufpos + 1) >= len) {
            System.arraycopy(this.buffer, this.bufpos - len + 1, ret, 0, len);
        } else {
            System.arraycopy(
                this.buffer, this.bufferSize - (len - this.bufpos - 1), ret, 0,
                len - this.bufpos - 1
            );
            System.arraycopy(this.buffer, 0, ret, len - this.bufpos - 1, this.bufpos + 1);
        }

        return ret;
    }
}

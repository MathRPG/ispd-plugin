package ispd.arquivo.interpretador.gerador;

import java.io.IOException;

/**
 * Token Manager.
 */
public final class InterpretadorTokenManager {

    /**
     * Token literal values.
     */
    private static final String[] jjstrLiteralImages = {
        "",
        "SCHEDULER",
        "STATIC",
        "DYNAMIC",
        "TASK",
        "ENTRY",
        "DISPACTH",
        "COMPLETED",
        "TIME",
        "INTERVAL",
        "RANDOM",
        "FIFO",
        "CRESCENT",
        "DECREASING",
        "RESOURCE",
        "RESTRICT",
        "TASKPER",
        "USER",
        "[TCP]",
        "[TC]",
        "[NTS]",
        "[NTC]",
        "[PCU]",
        "[TCR]",
        "[PP]",
        "[LC]",
        "[TCT]",
        "[TCMT]",
        "[NTE]",
        "[MFE]",
        "*",
        "/",
        "-",
        "+",
        "(",
        ")",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        ":",
        "[",
        "]",
    };

    /**
     * Lex State array.
     */
    private static final int[] jjnewLexState = {
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        -1,
        0,
        -1,
        0,
        -1,
        1,
        2,
        -1,
        -1,
        -1,
        -1,
    };

    private static final long[] jjbitVec0 = {
        0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
    };

    private static final int[] jjnextStates = {
        4, 5, 6, 8, 9,
    };

    private static final long[] jjtoToken = {
        0x38007fffffffffL,
    };

    private static final long[] jjtoSpecial = {
        0x4000000000000L,
    };

    private final int[] jjrounds = new int[11];

    private final int[] jjstateSet = new int[22];

    private final StringBuilder jjimage = new StringBuilder();

    private char curChar;

    private SimpleCharStream input_stream;

    private int curLexState = 0;

    private int jjnewStateCnt;

    private int jjround;

    private int jjmatchedPos;

    private int jjmatchedKind;

    private String erros = "";

    private StringBuilder image = this.jjimage;

    private int jjimageLen;

    /**
     * Constructor.
     */
    public InterpretadorTokenManager (final SimpleCharStream stream) {
        this.input_stream = stream;
    }

    private void addErro (final String msg) {
        this.erros = this.erros + "\n" + msg;
    }

    private int jjStopStringLiteralDfa_0 (final int pos, final long active0) {
        switch (pos) {
            case 0 -> {
                if ((active0 & 0xc0040000000L) != 0L) {
                    return 2;
                }
                if ((active0 & 0x3fffeL) != 0L) {
                    this.jjmatchedKind = 38;
                    return 11;
                }
                return -1;
            }
            case 1 -> {
                if ((active0 & 0x3fffeL) != 0L) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos  = 1;
                    return 11;
                }
                return -1;
            }
            case 2 -> {
                if ((active0 & 0x3fffeL) != 0L) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos  = 2;
                    return 11;
                }
                return -1;
            }
            case 3 -> {
                if ((active0 & 0xf6eeL) != 0L) {
                    if (this.jjmatchedPos != 3) {
                        this.jjmatchedKind = 38;
                        this.jjmatchedPos  = 3;
                    }
                    return 11;
                }
                if ((active0 & 0x30910L) != 0L) {
                    return 11;
                }
                return -1;
            }
            case 4 -> {
                if ((active0 & 0x20L) != 0L) {
                    return 11;
                }
                if ((active0 & 0x1f6ceL) != 0L) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos  = 4;
                    return 11;
                }
                return -1;
            }
            case 5 -> {
                if ((active0 & 0x404L) != 0L) {
                    return 11;
                }
                if ((active0 & 0x1f2caL) != 0L) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos  = 5;
                    return 11;
                }
                return -1;
            }
            case 6 -> {
                if ((active0 & 0xf2c2L) != 0L) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos  = 6;
                    return 11;
                }
                if ((active0 & 0x10008L) != 0L) {
                    return 11;
                }
                return -1;
            }
            case 7 -> {
                if ((active0 & 0xd240L) != 0L) {
                    return 11;
                }
                if ((active0 & 0x2082L) != 0L) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos  = 7;
                    return 11;
                }
                return -1;
            }
            case 8 -> {
                if ((active0 & 0x2000L) != 0L) {
                    this.jjmatchedKind = 38;
                    this.jjmatchedPos  = 8;
                    return 11;
                }
                if ((active0 & 0x82L) != 0L) {
                    return 11;
                }
                return -1;
            }
            default -> {
                return -1;
            }
        }
    }

    private int jjStartNfa_0 (final int pos, final long active0) {
        return this.jjMoveNfa_0(this.jjStopStringLiteralDfa_0(pos, active0), pos + 1);
    }

    private int jjStopAtPos (final int pos, final int kind) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos  = pos;
        return pos + 1;
    }

    private int jjMoveStringLiteralDfa0_0 () {
        switch (this.curChar) {
            case 12 -> {
                return this.jjStartNfaWithStates_0(0, 43, 2);
            }
            case 13 -> {
                return this.jjStartNfaWithStates_0(0, 42, 2);
            }
            case 40 -> {
                return this.jjStopAtPos(0, 34);
            }
            case 41 -> {
                return this.jjStopAtPos(0, 35);
            }
            case 42 -> {
                return this.jjStartNfaWithStates_0(0, 30, 2);
            }
            case 43 -> {
                return this.jjStopAtPos(0, 33);
            }
            case 45 -> {
                return this.jjStopAtPos(0, 32);
            }
            case 47 -> {
                this.jjmatchedKind = 31;
                return this.jjMoveStringLiteralDfa1_0(0x3000000000000L);
            }
            case 58 -> {
                return this.jjStopAtPos(0, 51);
            }
            case 67 -> {
                return this.jjMoveStringLiteralDfa1_0(0x1080L);
            }
            case 68 -> {
                return this.jjMoveStringLiteralDfa1_0(0x2048L);
            }
            case 69 -> {
                return this.jjMoveStringLiteralDfa1_0(0x20L);
            }
            case 70 -> {
                return this.jjMoveStringLiteralDfa1_0(0x800L);
            }
            case 73 -> {
                return this.jjMoveStringLiteralDfa1_0(0x200L);
            }
            case 82 -> {
                return this.jjMoveStringLiteralDfa1_0(0xc400L);
            }
            case 83 -> {
                return this.jjMoveStringLiteralDfa1_0(0x6L);
            }
            case 84 -> {
                return this.jjMoveStringLiteralDfa1_0(0x10110L);
            }
            case 85 -> {
                return this.jjMoveStringLiteralDfa1_0(0x20000L);
            }
            case 91 -> {
                this.jjmatchedKind = 52;
                return this.jjMoveStringLiteralDfa1_0(0x3ffc0000L);
            }
            case 93 -> {
                return this.jjStopAtPos(0, 53);
            }
            default -> {
                return this.jjMoveNfa_0(3, 0);
            }
        }
    }

    private int jjMoveStringLiteralDfa1_0 (final long active0) {
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            this.jjStopStringLiteralDfa_0(0, active0);
            return 1;
        }
        switch (this.curChar) {
            case 42 -> {
                if ((active0 & 0x1000000000000L) != 0L) {
                    return this.jjStopAtPos(1, 48);
                }
            }
            case 47 -> {
                if ((active0 & 0x2000000000000L) != 0L) {
                    return this.jjStopAtPos(1, 49);
                }
            }
            case 65 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x10410L);
            }
            case 67 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x2L);
            }
            case 69 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0xe000L);
            }
            case 73 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x940L);
            }
            case 76 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x2000000L);
            }
            case 77 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x20000000L);
            }
            case 78 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x10300220L);
            }
            case 79 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x80L);
            }
            case 80 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x1400000L);
            }
            case 82 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x1000L);
            }
            case 83 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x20000L);
            }
            case 84 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0xc8c0004L);
            }
            case 89 -> {
                return this.jjMoveStringLiteralDfa2_0(active0, 0x8L);
            }
            default -> {
            }
        }
        return this.jjStartNfa_0(0, active0);
    }

    private int jjMoveStringLiteralDfa2_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return this.jjStartNfa_0(0, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            this.jjStopStringLiteralDfa_0(1, active0);
            return 2;
        }
        switch (this.curChar) {
            case 65 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x4L);
            }
            case 67 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0xecc2000L);
            }
            case 69 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x21000L);
            }
            case 70 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x20000800L);
            }
            case 72 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x2L);
            }
            case 77 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x180L);
            }
            case 78 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x408L);
            }
            case 80 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x1000000L);
            }
            case 83 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x1c050L);
            }
            case 84 -> {
                return this.jjMoveStringLiteralDfa3_0(active0, 0x10300220L);
            }
            default -> {
            }
        }
        return this.jjStartNfa_0(1, active0);
    }

    private int jjMoveStringLiteralDfa3_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return this.jjStartNfa_0(1, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            this.jjStopStringLiteralDfa_0(2, active0);
            return 3;
        }
        switch (this.curChar) {
            case 65 -> {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x8L);
            }
            case 67 -> {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x200000L);
            }
            case 68 -> {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x400L);
            }
            case 69 -> {
                if ((active0 & 0x100L) != 0L) {
                    return this.jjStartNfaWithStates_0(3, 8, 11);
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x30000202L);
            }
            case 75 -> {
                if ((active0 & 0x10L) != 0L) {
                    this.jjmatchedKind = 4;
                    this.jjmatchedPos  = 3;
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x10000L);
            }
            case 77 -> {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x8000000L);
            }
            case 79 -> {
                if ((active0 & 0x800L) != 0L) {
                    return this.jjStartNfaWithStates_0(3, 11, 11);
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x4000L);
            }
            case 80 -> {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x400c0L);
            }
            case 82 -> {
                if ((active0 & 0x20000L) != 0L) {
                    return this.jjStartNfaWithStates_0(3, 17, 11);
                }
                return this.jjMoveStringLiteralDfa4_0(active0, 0x802020L);
            }
            case 83 -> {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x101000L);
            }
            case 84 -> {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x4008004L);
            }
            case 85 -> {
                return this.jjMoveStringLiteralDfa4_0(active0, 0x400000L);
            }
            case 93 -> {
                if ((active0 & 0x80000L) != 0L) {
                    return this.jjStopAtPos(3, 19);
                } else if ((active0 & 0x1000000L) != 0L) {
                    return this.jjStopAtPos(3, 24);
                } else if ((active0 & 0x2000000L) != 0L) {
                    return this.jjStopAtPos(3, 25);
                }
            }
            default -> {
            }
        }
        return this.jjStartNfa_0(2, active0);
    }

    private int jjMoveStringLiteralDfa4_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return this.jjStartNfa_0(2, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            this.jjStopStringLiteralDfa_0(3, active0);
            return 4;
        }
        switch (this.curChar) {
            case 65 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x40L);
            }
            case 67 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x1000L);
            }
            case 68 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x2L);
            }
            case 69 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x2000L);
            }
            case 73 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x4L);
            }
            case 76 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x80L);
            }
            case 77 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x8L);
            }
            case 79 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x400L);
            }
            case 80 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x10000L);
            }
            case 82 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x8200L);
            }
            case 84 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x8000000L);
            }
            case 85 -> {
                return this.jjMoveStringLiteralDfa5_0(active0, 0x4000L);
            }
            case 89 -> {
                if ((active0 & 0x20L) != 0L) {
                    return this.jjStartNfaWithStates_0(4, 5, 11);
                }
            }
            case 93 -> {
                if ((active0 & 0x40000L) != 0L) {
                    return this.jjStopAtPos(4, 18);
                } else if ((active0 & 0x100000L) != 0L) {
                    return this.jjStopAtPos(4, 20);
                } else if ((active0 & 0x200000L) != 0L) {
                    return this.jjStopAtPos(4, 21);
                } else if ((active0 & 0x400000L) != 0L) {
                    return this.jjStopAtPos(4, 22);
                } else if ((active0 & 0x800000L) != 0L) {
                    return this.jjStopAtPos(4, 23);
                } else if ((active0 & 0x4000000L) != 0L) {
                    return this.jjStopAtPos(4, 26);
                } else if ((active0 & 0x10000000L) != 0L) {
                    return this.jjStopAtPos(4, 28);
                } else if ((active0 & 0x20000000L) != 0L) {
                    return this.jjStopAtPos(4, 29);
                }
            }
            default -> {
            }
        }
        return this.jjStartNfa_0(3, active0);
    }

    private int jjMoveStringLiteralDfa5_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return this.jjStartNfa_0(3, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            this.jjStopStringLiteralDfa_0(4, active0);
            return 5;
        }
        switch (this.curChar) {
            case 65 -> {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x2000L);
            }
            case 67 -> {
                if ((active0 & 0x4L) != 0L) {
                    return this.jjStartNfaWithStates_0(5, 2, 11);
                }
                return this.jjMoveStringLiteralDfa6_0(active0, 0x40L);
            }
            case 69 -> {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x11080L);
            }
            case 73 -> {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x8008L);
            }
            case 77 -> {
                if ((active0 & 0x400L) != 0L) {
                    return this.jjStartNfaWithStates_0(5, 10, 11);
                }
            }
            case 82 -> {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x4000L);
            }
            case 85 -> {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x2L);
            }
            case 86 -> {
                return this.jjMoveStringLiteralDfa6_0(active0, 0x200L);
            }
            case 93 -> {
                if ((active0 & 0x8000000L) != 0L) {
                    return this.jjStopAtPos(5, 27);
                }
            }
            default -> {
            }
        }
        return this.jjStartNfa_0(4, active0);
    }

    private int jjMoveStringLiteralDfa6_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return this.jjStartNfa_0(4, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            this.jjStopStringLiteralDfa_0(5, active0);
            return 6;
        }
        switch (this.curChar) {
            case 65 -> {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x200L);
            }
            case 67 -> {
                if ((active0 & 0x8L) != 0L) {
                    return this.jjStartNfaWithStates_0(6, 3, 11);
                }
                return this.jjMoveStringLiteralDfa7_0(active0, 0xc000L);
            }
            case 76 -> {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x2L);
            }
            case 78 -> {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x1000L);
            }
            case 82 -> {
                if ((active0 & 0x10000L) != 0L) {
                    return this.jjStartNfaWithStates_0(6, 16, 11);
                }
            }
            case 83 -> {
                return this.jjMoveStringLiteralDfa7_0(active0, 0x2000L);
            }
            case 84 -> {
                return this.jjMoveStringLiteralDfa7_0(active0, 0xc0L);
            }
            default -> {
            }
        }
        return this.jjStartNfa_0(5, active0);
    }

    private int jjMoveStringLiteralDfa7_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return this.jjStartNfa_0(5, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            this.jjStopStringLiteralDfa_0(6, active0);
            return 7;
        }
        switch (this.curChar) {
            case 69 -> {
                if ((active0 & 0x4000L) != 0L) {
                    return this.jjStartNfaWithStates_0(7, 14, 11);
                }
                return this.jjMoveStringLiteralDfa8_0(active0, 0x82L);
            }
            case 72 -> {
                if ((active0 & 0x40L) != 0L) {
                    return this.jjStartNfaWithStates_0(7, 6, 11);
                }
            }
            case 73 -> {
                return this.jjMoveStringLiteralDfa8_0(active0, 0x2000L);
            }
            case 76 -> {
                if ((active0 & 0x200L) != 0L) {
                    return this.jjStartNfaWithStates_0(7, 9, 11);
                }
            }
            case 84 -> {
                if ((active0 & 0x1000L) != 0L) {
                    return this.jjStartNfaWithStates_0(7, 12, 11);
                } else if ((active0 & 0x8000L) != 0L) {
                    return this.jjStartNfaWithStates_0(7, 15, 11);
                }
            }
            default -> {
            }
        }
        return this.jjStartNfa_0(6, active0);
    }

    private int jjMoveStringLiteralDfa8_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return this.jjStartNfa_0(6, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            this.jjStopStringLiteralDfa_0(7, active0);
            return 8;
        }
        switch (this.curChar) {
            case 68 -> {
                if ((active0 & 0x80L) != 0L) {
                    return this.jjStartNfaWithStates_0(8, 7, 11);
                }
            }
            case 78 -> {
                return this.jjMoveStringLiteralDfa9_0(active0, 0x2000L);
            }
            case 82 -> {
                if ((active0 & 0x2L) != 0L) {
                    return this.jjStartNfaWithStates_0(8, 1, 11);
                }
            }
            default -> {
            }
        }
        return this.jjStartNfa_0(7, active0);
    }

    private int jjMoveStringLiteralDfa9_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return this.jjStartNfa_0(7, old0);
        }
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            this.jjStopStringLiteralDfa_0(8, active0);
            return 9;
        }
        if (this.curChar == 71) {
            if ((active0 & 0x2000L) != 0L) {
                return this.jjStartNfaWithStates_0(9, 13, 11);
            }
        }
        return this.jjStartNfa_0(8, active0);
    }

    private int jjStartNfaWithStates_0 (final int pos, final int kind, final int state) {
        this.jjmatchedKind = kind;
        this.jjmatchedPos  = pos;
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            return pos + 1;
        }
        return this.jjMoveNfa_0(state, pos + 1);
    }

    private int jjMoveNfa_0 (final int startState, int curPos) {
        this.jjnewStateCnt = 11;
        this.jjstateSet[0] = startState;
        var kind     = 0x7fffffff;
        var i        = 1;
        var startsAt = 0;
        while (true) {
            ++this.jjround;
            if (this.jjround == 0x7fffffff) {
                this.ReInitRounds();
            }
            if (this.curChar < 64) {
                final var l = 1L << this.curChar;
                do {
                    switch (this.jjstateSet[--i]) {
                        case 3 -> {
                            if ((0xd0000484fffff8ffL & l) != 0L) {
                                if (kind > 50) {
                                    kind = 50;
                                }
                                this.jjCheckNAdd(2);
                            } else if ((0x3ff000000000000L & l) != 0L) {
                                if (kind > 36) {
                                    kind = 36;
                                }
                                this.jjCheckNAddStates(0, 4);
                            }
                        }
                        case 11, 1 -> {
                            if ((0x3ff000000000000L & l) == 0L) {
                                break;
                            }
                            if (kind > 38) {
                                kind = 38;
                            }
                            this.jjCheckNAdd(1);
                        }
                        case 2 -> {
                            if ((0xd0000484fffff8ffL & l) == 0L) {
                                break;
                            }
                            kind = 50;
                            this.jjCheckNAdd(2);
                        }
                        case 4 -> {
                            if ((0x3ff000000000000L & l) == 0L) {
                                break;
                            }
                            if (kind > 36) {
                                kind = 36;
                            }
                            this.jjCheckNAdd(4);
                        }
                        case 5 -> {
                            if ((0x3ff000000000000L & l) != 0L) {
                                this.jjCheckNAddTwoStates(5, 6);
                            }
                        }
                        case 6 -> {
                            if (this.curChar == 46) {
                                this.jjCheckNAdd(7);
                            }
                        }
                        case 7 -> {
                            if ((0x3ff000000000000L & l) == 0L) {
                                break;
                            }
                            if (kind > 37) {
                                kind = 37;
                            }
                            this.jjCheckNAdd(7);
                        }
                        case 8 -> {
                            if ((0x3ff000000000000L & l) != 0L) {
                                this.jjCheckNAddTwoStates(8, 9);
                            }
                        }
                        case 9 -> {
                            if (this.curChar == 44) {
                                this.jjCheckNAdd(10);
                            }
                        }
                        case 10 -> {
                            if ((0x3ff000000000000L & l) == 0L) {
                                break;
                            }
                            if (kind > 37) {
                                kind = 37;
                            }
                            this.jjCheckNAdd(10);
                        }
                        default -> {
                        }
                    }
                } while (i != startsAt);
            } else if (this.curChar < 128) {
                final var l = 1L << (this.curChar & 077);
                do {
                    switch (this.jjstateSet[--i]) {
                        case 3 -> {
                            if ((0x7fffffe07fffffeL & l) != 0L) {
                                if (kind > 38) {
                                    kind = 38;
                                }
                                this.jjCheckNAddTwoStates(0, 1);
                            } else if ((0x8000000100000000L & l) != 0L) {
                                if (kind > 50) {
                                    kind = 50;
                                }
                                this.jjCheckNAdd(2);
                            }
                        }
                        case 11 -> {
                            if ((0x7fffffe07fffffeL & l) != 0L) {
                                if (kind > 38) {
                                    kind = 38;
                                }
                                this.jjCheckNAdd(1);
                            }
                            if ((0x7fffffe07fffffeL & l) != 0L) {
                                if (kind > 38) {
                                    kind = 38;
                                }
                                this.jjCheckNAddTwoStates(0, 1);
                            }
                        }
                        case 0 -> {
                            if ((0x7fffffe07fffffeL & l) == 0L) {
                                break;
                            }
                            if (kind > 38) {
                                kind = 38;
                            }
                            this.jjCheckNAddTwoStates(0, 1);
                        }
                        case 1 -> {
                            if ((0x7fffffe07fffffeL & l) == 0L) {
                                break;
                            }
                            if (kind > 38) {
                                kind = 38;
                            }
                            this.jjCheckNAdd(1);
                        }
                        case 2 -> {
                            if ((0x8000000100000000L & l) == 0L) {
                                break;
                            }
                            kind = 50;
                            this.jjCheckNAdd(2);
                        }
                        default -> {
                        }
                    }
                } while (i != startsAt);
            } else {
                final var i2 = (this.curChar & 0xff) >> 6;
                final var l2 = 1L << (this.curChar & 077);
                do {
                    switch (this.jjstateSet[--i]) {
                        case 3, 2 -> {
                            if ((InterpretadorTokenManager.jjbitVec0[i2] & l2) == 0L) {
                                break;
                            }
                            if (kind > 50) {
                                kind = 50;
                            }
                            this.jjCheckNAdd(2);
                        }
                        default -> {
                        }
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos  = curPos;
                kind               = 0x7fffffff;
            }
            ++curPos;
            if ((i = this.jjnewStateCnt) == (startsAt = 11 - (this.jjnewStateCnt = startsAt))) {
                return curPos;
            }
            try {
                this.curChar = this.input_stream.readChar();
            } catch (final IOException e) {
                return curPos;
            }
        }
    }

    private int jjMoveStringLiteralDfa0_2 () {
        return this.jjMoveNfa_2(0, 0);
    }

    private int jjMoveNfa_2 (final int startState, int curPos) {
        this.jjnewStateCnt = 1;
        this.jjstateSet[0] = startState;
        var kind     = 0x7fffffff;
        var i        = 1;
        var startsAt = 0;
        while (true) {
            ++this.jjround;
            if (this.jjround == 0x7fffffff) {
                this.ReInitRounds();
            }
            if (this.curChar < 64) {
                final var l = 1L << this.curChar;
                do {
                    --i;
                    if (this.jjstateSet[i] == 0) {
                        if ((0x2400L & l) != 0L) {
                            kind = 46;
                        }
                    }
                } while (i != startsAt);
            } else if (this.curChar < 128) {
                do {
                    --i;
                } while (i != startsAt);
            } else {
                do {
                    --i;
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                this.jjmatchedKind = kind;
                this.jjmatchedPos  = curPos;
                kind               = 0x7fffffff;
            }
            ++curPos;
            if ((i = this.jjnewStateCnt) == (startsAt = 1 - (this.jjnewStateCnt = startsAt))) {
                return curPos;
            }
            try {
                this.curChar = this.input_stream.readChar();
            } catch (final IOException e) {
                return curPos;
            }
        }
    }

    private int jjMoveStringLiteralDfa0_1 () {
        if (this.curChar == 42) {
            return this.jjMoveStringLiteralDfa1_1(0x100000000000L);
        }
        return 1;
    }

    private int jjMoveStringLiteralDfa1_1 (final long active0) {
        try {
            this.curChar = this.input_stream.readChar();
        } catch (final IOException e) {
            return 1;
        }
        if (this.curChar == 47) {
            if ((active0 & 0x100000000000L) != 0L) {
                return this.jjStopAtPos(1, 44);
            }
        } else {
            return 2;
        }
        return 2;
    }

    private void ReInitRounds () {
        this.jjround = 0x80000001;
        for (var i = 11; i > 0; ) {
            i--;
            this.jjrounds[i] = 0x80000000;
        }
    }

    private Token jjFillToken () {
        final var im            = InterpretadorTokenManager.jjstrLiteralImages[this.jjmatchedKind];
        final var curTokenImage = (im == null) ? this.input_stream.GetImage() : im;
        final var beginLine     = this.input_stream.getBeginLine();
        final var beginColumn   = this.input_stream.getBeginColumn();
        final var endLine       = this.input_stream.getEndLine();
        final var endColumn     = this.input_stream.getEndColumn();
        final var t             = new Token(this.jjmatchedKind, curTokenImage);

        t.beginLine   = beginLine;
        t.endLine     = endLine;
        t.beginColumn = beginColumn;
        t.endColumn   = endColumn;

        return t;
    }

    /**
     * Get the next Token.
     */
    public Token getNextToken () {
        Token specialToken = null;
        var   curPos       = 0;

        while (true) {
            try {
                this.curChar = this.input_stream.BeginToken();
            } catch (final IOException ignored) {
                this.jjmatchedKind = 0;
                final var matchedToken = this.jjFillToken();
                matchedToken.specialToken = specialToken;
                return matchedToken;
            }
            this.image = this.jjimage;
            this.image.setLength(0);
            this.jjimageLen = 0;

            switch (this.curLexState) {
                case 0 -> {
                    try {
                        this.input_stream.backup(0);
                        while (this.curChar <= 32 && (0x100000600L & (1L << this.curChar)) != 0L) {
                            this.curChar = this.input_stream.BeginToken();
                        }
                    } catch (final IOException e1) {
                        continue;
                    }
                    this.jjmatchedKind = 0x7fffffff;
                    this.jjmatchedPos  = 0;
                    curPos             = this.jjMoveStringLiteralDfa0_0();
                }
                case 1 -> {
                    this.jjmatchedKind = 0x7fffffff;
                    this.jjmatchedPos  = 0;
                    curPos             = this.jjMoveStringLiteralDfa0_1();
                    if (this.jjmatchedPos == 0 && this.jjmatchedKind > 45) {
                        this.jjmatchedKind = 45;
                    }
                }
                case 2 -> {
                    this.jjmatchedKind = 0x7fffffff;
                    this.jjmatchedPos  = 0;
                    curPos             = this.jjMoveStringLiteralDfa0_2();
                    if (this.jjmatchedPos == 0 && this.jjmatchedKind > 47) {
                        this.jjmatchedKind = 47;
                    }
                }
            }
            if (this.jjmatchedKind != 0x7fffffff) {
                if (this.jjmatchedPos + 1 < curPos) {
                    this.input_stream.backup(curPos - this.jjmatchedPos - 1);
                }
                if ((
                        InterpretadorTokenManager.jjtoToken[this.jjmatchedKind >> 6] & (
                            1L
                            << (
                                this.jjmatchedKind
                                & 077
                            )
                        )
                    ) != 0L) {
                    final var matchedToken = this.jjFillToken();
                    matchedToken.specialToken = specialToken;
                    if (InterpretadorTokenManager.jjnewLexState[this.jjmatchedKind] != -1) {
                        this.curLexState =
                            InterpretadorTokenManager.jjnewLexState[this.jjmatchedKind];
                    }
                    return matchedToken;
                } else {
                    if ((
                            InterpretadorTokenManager.jjtoSpecial[this.jjmatchedKind >> 6] & (
                                1L << (
                                    this.jjmatchedKind
                                    & 0b111111
                                )
                            )
                        ) != 0L) {
                        final var matchedToken = this.jjFillToken();
                        if (specialToken == null) {
                            specialToken = matchedToken;
                        } else {
                            matchedToken.specialToken = specialToken;
                            specialToken              = (specialToken.next = matchedToken);
                        }
                        this.SkipLexicalActions();
                    } else {
                        this.SkipLexicalActions();
                    }
                    if (InterpretadorTokenManager.jjnewLexState[this.jjmatchedKind] != -1) {
                        this.curLexState =
                            InterpretadorTokenManager.jjnewLexState[this.jjmatchedKind];
                    }
                    continue;
                }
            }
            var    error_line   = this.input_stream.getEndLine();
            var    error_column = this.input_stream.getEndColumn();
            String error_after  = null;
            var    EOFSeen      = false;
            try {
                this.input_stream.readChar();
                this.input_stream.backup(1);
            } catch (final IOException ignored) {
                EOFSeen     = true;
                error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
                if (this.curChar == '\n' || this.curChar == '\r') {
                    error_line++;
                    error_column = 0;
                } else {
                    error_column++;
                }
            }
            if (!EOFSeen) {
                this.input_stream.backup(1);
                error_after = curPos <= 1 ? "" : this.input_stream.GetImage();
            }
            throw new TokenMgrError(EOFSeen, error_line, error_column, error_after, this.curChar);
        }
    }

    private void SkipLexicalActions () {
        if (this.jjmatchedKind == 50) {
            this.image.append(this.input_stream.GetSuffix(this.jjimageLen + this.jjmatchedPos + 1));
            this.addErro("Erro na linha "
                         + this.input_stream.getEndLine()
                         + ". Caracter "
                         + this.image
                         + " não é aceito.");
        }
    }

    private void jjCheckNAdd (final int state) {
        if (this.jjrounds[state] == this.jjround) {
            return;
        }

        this.jjstateSet[this.jjnewStateCnt] = state;
        this.jjnewStateCnt++;
        this.jjrounds[state] = this.jjround;
    }

    private void jjCheckNAddTwoStates (final int state1, final int state2) {
        this.jjCheckNAdd(state1);
        this.jjCheckNAdd(state2);
    }

    private void jjCheckNAddStates (int start, final int end) {
        do {
            start++;
            this.jjCheckNAdd(InterpretadorTokenManager.jjnextStates[start]);
        } while (start != end);
    }
}

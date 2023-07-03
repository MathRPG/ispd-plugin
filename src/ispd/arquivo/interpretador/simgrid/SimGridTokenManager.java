package ispd.arquivo.interpretador.simgrid;

/**
 * Token Manager.
 */
public class SimGridTokenManager {

    /**
     * Token literal values.
     */
    private static final String[] jjstrLiteralImages = {
        "",
        "xml",
        "cpu",
        "name",
        "bandwidth",
        "latency",
        "route",
        "src",
        "dst",
        "route_element",
        "process",
        "network_link",
        "host",
        "function",
        "argument",
        "value",
        "power",
        "master",
        "slave",
        "tasksource",
        "slavecomm",
        "reloadhost",
        "forwarderscheduler",
        "forwardernode",
        "forwardercomm",
        "platform_description",
        "version",
        null,
        null,
        ".",
        "/",
        "!",
        "?",
        "<",
        ">",
        "=",
        "'",
        "\"",
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
        -1,
        1,
        0,
        -1,
    };

    private static final long[] jjbitVec0 = {
        0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
    };

    private static final long[] jjtoToken = {
        0x7fffffffffL,
    };

    private static final long[] jjtoSpecial = {
        0x8000000000L,
    };

    private static final int[] jjrounds = new int[4];

    private static final int[] jjstateSet = new int[8];

    private static final StringBuilder jjimage = new StringBuilder();

    private static final int defaultLexState = 0;

    protected static char curChar;

    private static SimpleCharStream input_stream;

    private static int curLexState = 0;

    private static int jjnewStateCnt;

    private static int jjround;

    private static int jjmatchedPos;

    private static int jjmatchedKind;

    private static int contaErrosLex = 0;

    private static String errosLex = "\u005cnErros l\u00e9xicos:\u005cn";

    private static StringBuilder image = SimGridTokenManager.jjimage;

    private static int jjimageLen;

    /**
     * Constructor.
     */
    public SimGridTokenManager (final SimpleCharStream stream) {
        if (SimGridTokenManager.input_stream != null) {
            throw new TokenMgrError(
                "ERROR: Second call to constructor of static lexer. You must use ReInit() to initialize the static variables."
            );
        }
        SimGridTokenManager.input_stream = stream;
    }

    public static int contaErrosLex () {
        return SimGridTokenManager.contaErrosLex;
    }

    public static void addErroLex (final String msg) {
        SimGridTokenManager.errosLex = SimGridTokenManager.errosLex + "\u005cn" + msg;
        SimGridTokenManager.contaErrosLex++;
    }

    public static String getErrosLex () {
        return SimGridTokenManager.errosLex + "\u005cn";
    }

    public static void reset () {
        SimGridTokenManager.contaErrosLex = 0;
        SimGridTokenManager.errosLex      = "\u005cnErros l\u00e9xicos:\u005cn";
    }

    private static int jjStopStringLiteralDfa_0 (final int pos, final long active0) {
        switch (pos) {
            case 0:
                if ((active0 & 0x7fffffeL) != 0L) {
                    SimGridTokenManager.jjmatchedKind = 27;
                    return -1;
                }
                return -1;
            case 1:
                if ((active0 & 0x7fffffeL) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 2:
                if ((active0 & 0x7fffffeL) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 3:
                if ((active0 & 0x7fffe78L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 4:
                if ((active0 & 0x7ffee70L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 5:
                if ((active0 & 0x7fa6e30L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 6:
                if ((active0 & 0x7f86e30L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 7:
                if ((active0 & 0x3f86a10L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 8:
                if ((active0 & 0x3f80a10L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 9:
                if ((active0 & 0x3e80a00L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 10:
                if ((active0 & 0x3c00a00L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 11:
                if ((active0 & 0x3c00a00L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 12:
                if ((active0 & 0x3c00200L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 13:
                if ((active0 & 0x2400000L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 14:
                if ((active0 & 0x2400000L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 15:
                if ((active0 & 0x2400000L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 16:
                if ((active0 & 0x2400000L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 17:
                if ((active0 & 0x2400000L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            case 18:
                if ((active0 & 0x2000000L) != 0L) {
                    if (SimGridTokenManager.jjmatchedPos == 0) {
                        SimGridTokenManager.jjmatchedKind = 27;
                        SimGridTokenManager.jjmatchedPos  = 0;
                    }
                    return -1;
                }
                return -1;
            default:
                return -1;
        }
    }

    private static int jjStartNfa_0 (final int pos, final long active0) {
        return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
    }

    private static int jjStopAtPos (final int pos, final int kind) {
        SimGridTokenManager.jjmatchedKind = kind;
        SimGridTokenManager.jjmatchedPos  = pos;
        return pos + 1;
    }

    private static int jjMoveStringLiteralDfa0_0 () {
        switch (SimGridTokenManager.curChar) {
            case 33:
                return jjStopAtPos(0, 31);
            case 34:
                return jjStopAtPos(0, 37);
            case 39:
                return jjStopAtPos(0, 36);
            case 46:
                return jjStopAtPos(0, 29);
            case 47:
                return jjStopAtPos(0, 30);
            case 60:
                SimGridTokenManager.jjmatchedKind = 33;
                return jjMoveStringLiteralDfa1_0(0x200000000000L);
            case 61:
                return jjStopAtPos(0, 35);
            case 62:
                return jjStopAtPos(0, 34);
            case 63:
                return jjStopAtPos(0, 32);
            case 97:
                return jjMoveStringLiteralDfa1_0(0x4000L);
            case 98:
                return jjMoveStringLiteralDfa1_0(0x10L);
            case 99:
                return jjMoveStringLiteralDfa1_0(0x4L);
            case 100:
                return jjMoveStringLiteralDfa1_0(0x100L);
            case 102:
                return jjMoveStringLiteralDfa1_0(0x1c02000L);
            case 104:
                return jjMoveStringLiteralDfa1_0(0x1000L);
            case 108:
                return jjMoveStringLiteralDfa1_0(0x20L);
            case 109:
                return jjMoveStringLiteralDfa1_0(0x20000L);
            case 110:
                return jjMoveStringLiteralDfa1_0(0x808L);
            case 112:
                return jjMoveStringLiteralDfa1_0(0x2010400L);
            case 114:
                return jjMoveStringLiteralDfa1_0(0x200240L);
            case 115:
                return jjMoveStringLiteralDfa1_0(0x140080L);
            case 116:
                return jjMoveStringLiteralDfa1_0(0x80000L);
            case 118:
                return jjMoveStringLiteralDfa1_0(0x4008000L);
            case 120:
                return jjMoveStringLiteralDfa1_0(0x2L);
            default:
                return jjMoveNfa_0(0, 0);
        }
    }

    private static int jjMoveStringLiteralDfa1_0 (final long active0) {
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(0, active0);
            return 1;
        }
        switch (SimGridTokenManager.curChar) {
            case 33:
                if ((active0 & 0x200000000000L) != 0L) {
                    return jjStopAtPos(1, 45);
                }
                break;
            case 97:
                return jjMoveStringLiteralDfa2_0(active0, 0xa8038L);
            case 101:
                return jjMoveStringLiteralDfa2_0(active0, 0x4200800L);
            case 108:
                return jjMoveStringLiteralDfa2_0(active0, 0x2140000L);
            case 109:
                return jjMoveStringLiteralDfa2_0(active0, 0x2L);
            case 111:
                return jjMoveStringLiteralDfa2_0(active0, 0x1c11240L);
            case 112:
                return jjMoveStringLiteralDfa2_0(active0, 0x4L);
            case 114:
                return jjMoveStringLiteralDfa2_0(active0, 0x4480L);
            case 115:
                return jjMoveStringLiteralDfa2_0(active0, 0x100L);
            case 117:
                return jjMoveStringLiteralDfa2_0(active0, 0x2000L);
            default:
                break;
        }
        return jjStartNfa_0(0, active0);
    }

    private static int jjMoveStringLiteralDfa2_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(0, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(1, active0);
            return 2;
        }
        switch (SimGridTokenManager.curChar) {
            case 97:
                return jjMoveStringLiteralDfa3_0(active0, 0x2140000L);
            case 99:
                if ((active0 & 0x80L) != 0L) {
                    return jjStopAtPos(2, 7);
                }
                break;
            case 103:
                return jjMoveStringLiteralDfa3_0(active0, 0x4000L);
            case 108:
                if ((active0 & 0x2L) != 0L) {
                    return jjStopAtPos(2, 1);
                }
                return jjMoveStringLiteralDfa3_0(active0, 0x208000L);
            case 109:
                return jjMoveStringLiteralDfa3_0(active0, 0x8L);
            case 110:
                return jjMoveStringLiteralDfa3_0(active0, 0x2010L);
            case 111:
                return jjMoveStringLiteralDfa3_0(active0, 0x400L);
            case 114:
                return jjMoveStringLiteralDfa3_0(active0, 0x5c00000L);
            case 115:
                return jjMoveStringLiteralDfa3_0(active0, 0xa1000L);
            case 116:
                if ((active0 & 0x100L) != 0L) {
                    return jjStopAtPos(2, 8);
                }
                return jjMoveStringLiteralDfa3_0(active0, 0x820L);
            case 117:
                if ((active0 & 0x4L) != 0L) {
                    return jjStopAtPos(2, 2);
                }
                return jjMoveStringLiteralDfa3_0(active0, 0x240L);
            case 119:
                return jjMoveStringLiteralDfa3_0(active0, 0x10000L);
            default:
                break;
        }
        return jjStartNfa_0(1, active0);
    }

    private static int jjMoveStringLiteralDfa3_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(1, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(2, active0);
            return 3;
        }
        switch (SimGridTokenManager.curChar) {
            case 99:
                return jjMoveStringLiteralDfa4_0(active0, 0x2400L);
            case 100:
                return jjMoveStringLiteralDfa4_0(active0, 0x10L);
            case 101:
                if ((active0 & 0x8L) != 0L) {
                    return jjStopAtPos(3, 3);
                }
                return jjMoveStringLiteralDfa4_0(active0, 0x10020L);
            case 107:
                return jjMoveStringLiteralDfa4_0(active0, 0x80000L);
            case 111:
                return jjMoveStringLiteralDfa4_0(active0, 0x200000L);
            case 115:
                return jjMoveStringLiteralDfa4_0(active0, 0x4000000L);
            case 116:
                if ((active0 & 0x1000L) != 0L) {
                    return jjStopAtPos(3, 12);
                }
                return jjMoveStringLiteralDfa4_0(active0, 0x2020240L);
            case 117:
                return jjMoveStringLiteralDfa4_0(active0, 0xc000L);
            case 118:
                return jjMoveStringLiteralDfa4_0(active0, 0x140000L);
            case 119:
                return jjMoveStringLiteralDfa4_0(active0, 0x1c00800L);
            default:
                break;
        }
        return jjStartNfa_0(2, active0);
    }

    private static int jjMoveStringLiteralDfa4_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(2, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(3, active0);
            return 4;
        }
        switch (SimGridTokenManager.curChar) {
            case 97:
                return jjMoveStringLiteralDfa5_0(active0, 0x1e00000L);
            case 101:
                if ((active0 & 0x40L) != 0L) {
                    SimGridTokenManager.jjmatchedKind = 6;
                    SimGridTokenManager.jjmatchedPos  = 4;
                } else if ((active0 & 0x8000L) != 0L) {
                    return jjStopAtPos(4, 15);
                } else if ((active0 & 0x40000L) != 0L) {
                    SimGridTokenManager.jjmatchedKind = 18;
                    SimGridTokenManager.jjmatchedPos  = 4;
                }
                return jjMoveStringLiteralDfa5_0(active0, 0x120600L);
            case 102:
                return jjMoveStringLiteralDfa5_0(active0, 0x2000000L);
            case 105:
                return jjMoveStringLiteralDfa5_0(active0, 0x4000000L);
            case 109:
                return jjMoveStringLiteralDfa5_0(active0, 0x4000L);
            case 110:
                return jjMoveStringLiteralDfa5_0(active0, 0x20L);
            case 111:
                return jjMoveStringLiteralDfa5_0(active0, 0x800L);
            case 114:
                if ((active0 & 0x10000L) != 0L) {
                    return jjStopAtPos(4, 16);
                }
                break;
            case 115:
                return jjMoveStringLiteralDfa5_0(active0, 0x80000L);
            case 116:
                return jjMoveStringLiteralDfa5_0(active0, 0x2000L);
            case 119:
                return jjMoveStringLiteralDfa5_0(active0, 0x10L);
            default:
                break;
        }
        return jjStartNfa_0(3, active0);
    }

    private static int jjMoveStringLiteralDfa5_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(3, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(4, active0);
            return 5;
        }
        switch (SimGridTokenManager.curChar) {
            case 95:
                return jjMoveStringLiteralDfa6_0(active0, 0x200L);
            case 99:
                return jjMoveStringLiteralDfa6_0(active0, 0x100020L);
            case 100:
                return jjMoveStringLiteralDfa6_0(active0, 0x200000L);
            case 101:
                return jjMoveStringLiteralDfa6_0(active0, 0x4000L);
            case 105:
                return jjMoveStringLiteralDfa6_0(active0, 0x2010L);
            case 111:
                return jjMoveStringLiteralDfa6_0(active0, 0x6080000L);
            case 114:
                if ((active0 & 0x20000L) != 0L) {
                    return jjStopAtPos(5, 17);
                }
                return jjMoveStringLiteralDfa6_0(active0, 0x1c00800L);
            case 115:
                return jjMoveStringLiteralDfa6_0(active0, 0x400L);
            default:
                break;
        }
        return jjStartNfa_0(4, active0);
    }

    private static int jjMoveStringLiteralDfa6_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(4, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(5, active0);
            return 6;
        }
        switch (SimGridTokenManager.curChar) {
            case 100:
                return jjMoveStringLiteralDfa7_0(active0, 0x1c00010L);
            case 101:
                return jjMoveStringLiteralDfa7_0(active0, 0x200L);
            case 104:
                return jjMoveStringLiteralDfa7_0(active0, 0x200000L);
            case 107:
                return jjMoveStringLiteralDfa7_0(active0, 0x800L);
            case 110:
                if ((active0 & 0x4000000L) != 0L) {
                    return jjStopAtPos(6, 26);
                }
                return jjMoveStringLiteralDfa7_0(active0, 0x4000L);
            case 111:
                return jjMoveStringLiteralDfa7_0(active0, 0x102000L);
            case 114:
                return jjMoveStringLiteralDfa7_0(active0, 0x2000000L);
            case 115:
                if ((active0 & 0x400L) != 0L) {
                    return jjStopAtPos(6, 10);
                }
                break;
            case 117:
                return jjMoveStringLiteralDfa7_0(active0, 0x80000L);
            case 121:
                if ((active0 & 0x20L) != 0L) {
                    return jjStopAtPos(6, 5);
                }
                break;
            default:
                break;
        }
        return jjStartNfa_0(5, active0);
    }

    private static int jjMoveStringLiteralDfa7_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(5, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(6, active0);
            return 7;
        }
        switch (SimGridTokenManager.curChar) {
            case 95:
                return jjMoveStringLiteralDfa8_0(active0, 0x800L);
            case 101:
                return jjMoveStringLiteralDfa8_0(active0, 0x1c00000L);
            case 108:
                return jjMoveStringLiteralDfa8_0(active0, 0x200L);
            case 109:
                return jjMoveStringLiteralDfa8_0(active0, 0x2100000L);
            case 110:
                if ((active0 & 0x2000L) != 0L) {
                    return jjStopAtPos(7, 13);
                }
                break;
            case 111:
                return jjMoveStringLiteralDfa8_0(active0, 0x200000L);
            case 114:
                return jjMoveStringLiteralDfa8_0(active0, 0x80000L);
            case 116:
                if ((active0 & 0x4000L) != 0L) {
                    return jjStopAtPos(7, 14);
                }
                return jjMoveStringLiteralDfa8_0(active0, 0x10L);
            default:
                break;
        }
        return jjStartNfa_0(6, active0);
    }

    private static int jjMoveStringLiteralDfa8_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(6, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(7, active0);
            return 8;
        }
        switch (SimGridTokenManager.curChar) {
            case 95:
                return jjMoveStringLiteralDfa9_0(active0, 0x2000000L);
            case 99:
                return jjMoveStringLiteralDfa9_0(active0, 0x80000L);
            case 101:
                return jjMoveStringLiteralDfa9_0(active0, 0x200L);
            case 104:
                if ((active0 & 0x10L) != 0L) {
                    return jjStopAtPos(8, 4);
                }
                break;
            case 108:
                return jjMoveStringLiteralDfa9_0(active0, 0x800L);
            case 109:
                if ((active0 & 0x100000L) != 0L) {
                    return jjStopAtPos(8, 20);
                }
                break;
            case 114:
                return jjMoveStringLiteralDfa9_0(active0, 0x1c00000L);
            case 115:
                return jjMoveStringLiteralDfa9_0(active0, 0x200000L);
            default:
                break;
        }
        return jjStartNfa_0(7, active0);
    }

    private static int jjMoveStringLiteralDfa9_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(7, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(8, active0);
            return 9;
        }
        switch (SimGridTokenManager.curChar) {
            case 99:
                return jjMoveStringLiteralDfa10_0(active0, 0x1000000L);
            case 100:
                return jjMoveStringLiteralDfa10_0(active0, 0x2000000L);
            case 101:
                if ((active0 & 0x80000L) != 0L) {
                    return jjStopAtPos(9, 19);
                }
                break;
            case 105:
                return jjMoveStringLiteralDfa10_0(active0, 0x800L);
            case 109:
                return jjMoveStringLiteralDfa10_0(active0, 0x200L);
            case 110:
                return jjMoveStringLiteralDfa10_0(active0, 0x800000L);
            case 115:
                return jjMoveStringLiteralDfa10_0(active0, 0x400000L);
            case 116:
                if ((active0 & 0x200000L) != 0L) {
                    return jjStopAtPos(9, 21);
                }
                break;
            default:
                break;
        }
        return jjStartNfa_0(8, active0);
    }

    private static int jjMoveStringLiteralDfa10_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(8, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(9, active0);
            return 10;
        }
        switch (SimGridTokenManager.curChar) {
            case 99:
                return jjMoveStringLiteralDfa11_0(active0, 0x400000L);
            case 101:
                return jjMoveStringLiteralDfa11_0(active0, 0x2000200L);
            case 110:
                return jjMoveStringLiteralDfa11_0(active0, 0x800L);
            case 111:
                return jjMoveStringLiteralDfa11_0(active0, 0x1800000L);
            default:
                break;
        }
        return jjStartNfa_0(9, active0);
    }

    private static int jjMoveStringLiteralDfa11_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(9, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(10, active0);
            return 11;
        }
        switch (SimGridTokenManager.curChar) {
            case 100:
                return jjMoveStringLiteralDfa12_0(active0, 0x800000L);
            case 104:
                return jjMoveStringLiteralDfa12_0(active0, 0x400000L);
            case 107:
                if ((active0 & 0x800L) != 0L) {
                    return jjStopAtPos(11, 11);
                }
                break;
            case 109:
                return jjMoveStringLiteralDfa12_0(active0, 0x1000000L);
            case 110:
                return jjMoveStringLiteralDfa12_0(active0, 0x200L);
            case 115:
                return jjMoveStringLiteralDfa12_0(active0, 0x2000000L);
            default:
                break;
        }
        return jjStartNfa_0(10, active0);
    }

    private static int jjMoveStringLiteralDfa12_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(10, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(11, active0);
            return 12;
        }
        switch (SimGridTokenManager.curChar) {
            case 99:
                return jjMoveStringLiteralDfa13_0(active0, 0x2000000L);
            case 101:
                if ((active0 & 0x800000L) != 0L) {
                    return jjStopAtPos(12, 23);
                }
                return jjMoveStringLiteralDfa13_0(active0, 0x400000L);
            case 109:
                if ((active0 & 0x1000000L) != 0L) {
                    return jjStopAtPos(12, 24);
                }
                break;
            case 116:
                if ((active0 & 0x200L) != 0L) {
                    return jjStopAtPos(12, 9);
                }
                break;
            default:
                break;
        }
        return jjStartNfa_0(11, active0);
    }

    private static int jjMoveStringLiteralDfa13_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(11, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(12, active0);
            return 13;
        }
        switch (SimGridTokenManager.curChar) {
            case 100:
                return jjMoveStringLiteralDfa14_0(active0, 0x400000L);
            case 114:
                return jjMoveStringLiteralDfa14_0(active0, 0x2000000L);
            default:
                break;
        }
        return jjStartNfa_0(12, active0);
    }

    private static int jjMoveStringLiteralDfa14_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(12, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(13, active0);
            return 14;
        }
        switch (SimGridTokenManager.curChar) {
            case 105:
                return jjMoveStringLiteralDfa15_0(active0, 0x2000000L);
            case 117:
                return jjMoveStringLiteralDfa15_0(active0, 0x400000L);
            default:
                break;
        }
        return jjStartNfa_0(13, active0);
    }

    private static int jjMoveStringLiteralDfa15_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(13, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(14, active0);
            return 15;
        }
        switch (SimGridTokenManager.curChar) {
            case 108:
                return jjMoveStringLiteralDfa16_0(active0, 0x400000L);
            case 112:
                return jjMoveStringLiteralDfa16_0(active0, 0x2000000L);
            default:
                break;
        }
        return jjStartNfa_0(14, active0);
    }

    private static int jjMoveStringLiteralDfa16_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(14, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(15, active0);
            return 16;
        }
        switch (SimGridTokenManager.curChar) {
            case 101:
                return jjMoveStringLiteralDfa17_0(active0, 0x400000L);
            case 116:
                return jjMoveStringLiteralDfa17_0(active0, 0x2000000L);
            default:
                break;
        }
        return jjStartNfa_0(15, active0);
    }

    private static int jjMoveStringLiteralDfa17_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(15, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(16, active0);
            return 17;
        }
        switch (SimGridTokenManager.curChar) {
            case 105:
                return jjMoveStringLiteralDfa18_0(active0, 0x2000000L);
            case 114:
                if ((active0 & 0x400000L) != 0L) {
                    return jjStopAtPos(17, 22);
                }
                break;
            default:
                break;
        }
        return jjStartNfa_0(16, active0);
    }

    private static int jjMoveStringLiteralDfa18_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(16, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(17, active0);
            return 18;
        }
        if (SimGridTokenManager.curChar == 111) {
            return jjMoveStringLiteralDfa19_0(active0, 0x2000000L);
        }
        return jjStartNfa_0(17, active0);
    }

    private static int jjMoveStringLiteralDfa19_0 (final long old0, long active0) {
        if (((active0 &= old0)) == 0L) {
            return jjStartNfa_0(17, old0);
        }
        try {
            SimGridTokenManager.curChar = SimpleCharStream.readChar();
        } catch (final java.io.IOException e) {
            jjStopStringLiteralDfa_0(18, active0);
            return 19;
        }
        if (SimGridTokenManager.curChar == 110) {
            if ((active0 & 0x2000000L) != 0L) {
                return jjStopAtPos(19, 25);
            }
        }
        return jjStartNfa_0(18, active0);
    }

    private static int jjMoveNfa_0 (final int startState, int curPos) {
        SimGridTokenManager.jjnewStateCnt = 4;
        SimGridTokenManager.jjstateSet[0] = startState;
        var kind     = 0x7fffffff;
        var i        = 1;
        var startsAt = 0;
        while (true) {
            ++SimGridTokenManager.jjround;
            if (SimGridTokenManager.jjround == 0x7fffffff) {
                ReInitRounds();
            }
            if (SimGridTokenManager.curChar < 64) {
                final var l = 1L << SimGridTokenManager.curChar;
                do {
                    switch (SimGridTokenManager.jjstateSet[--i]) {
                        case 0:
                            if ((0x80000400ffffc8ffL & l) != 0L) {
                                if (kind > 39) {
                                    kind = 39;
                                }
                            } else if ((0x2c00fb7a00000000L & l) != 0L) {
                                if (kind > 38) {
                                    kind = 38;
                                }
                            } else if ((0x3ff000000000000L & l) != 0L) {
                                if (kind > 28) {
                                    kind = 28;
                                }
                            }
                            break;
                        case 1:
                            if ((0x3ff000000000000L & l) != 0L) {
                                kind = 28;
                            }
                            break;
                        case 2:
                            if ((0x2c00fb7a00000000L & l) != 0L) {
                                kind = 38;
                            }
                            break;
                        case 3:
                            if ((0x80000400ffffc8ffL & l) != 0L) {
                                kind = 39;
                            }
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else if (SimGridTokenManager.curChar < 128) {
                final var l = 1L << (SimGridTokenManager.curChar & 077);
                do {
                    switch (SimGridTokenManager.jjstateSet[--i]) {
                        case 0:
                            if ((0x7fffffe07fffffeL & l) != 0L) {
                                if (kind > 27) {
                                    kind = 27;
                                }
                            } else if ((0x78000000e8000001L & l) != 0L) {
                                if (kind > 38) {
                                    kind = 38;
                                }
                            } else if ((0x8000000100000000L & l) != 0L) {
                                if (kind > 39) {
                                    kind = 39;
                                }
                            }
                            break;
                        case 2:
                            if ((0x78000000e8000001L & l) != 0L) {
                                kind = 38;
                            }
                            break;
                        case 3:
                            if ((0x8000000100000000L & l) != 0L) {
                                kind = 39;
                            }
                            break;
                        default:
                            break;
                    }
                } while (i != startsAt);
            } else {
                final var i2 = (SimGridTokenManager.curChar & 0xff) >> 6;
                final var l2 = 1L << (SimGridTokenManager.curChar & 077);
                do {
                    --i;
                    if (SimGridTokenManager.jjstateSet[i] == 0) {
                        if ((SimGridTokenManager.jjbitVec0[i2] & l2) != 0L && kind > 39) {
                            kind = 39;
                        }
                    }
                } while (i != startsAt);
            }
            if (kind != 0x7fffffff) {
                SimGridTokenManager.jjmatchedKind = kind;
                SimGridTokenManager.jjmatchedPos  = curPos;
                kind                              = 0x7fffffff;
            }
            ++curPos;
            if ((i = SimGridTokenManager.jjnewStateCnt) == (
                startsAt = 4 - (SimGridTokenManager.jjnewStateCnt = startsAt)
            )) {
                return curPos;
            }
            try {
                SimGridTokenManager.curChar = SimpleCharStream.readChar();
            } catch (final java.io.IOException e) {
                return curPos;
            }
        }
    }

    private static int jjMoveStringLiteralDfa0_1 () {
        if (SimGridTokenManager.curChar == 62) {
            return jjStopAtPos(0, 46);
        }
        return 1;
    }

    /**
     * Reinitialise parser.
     */
    public static void ReInit (final SimpleCharStream stream) {
        SimGridTokenManager.jjmatchedPos = SimGridTokenManager.jjnewStateCnt = 0;
        SimGridTokenManager.curLexState  = SimGridTokenManager.defaultLexState;
        SimGridTokenManager.input_stream = stream;
        ReInitRounds();
    }

    private static void ReInitRounds () {
        SimGridTokenManager.jjround = 0x80000001;
        int i;
        for (i = 4; i > 0; ) {
            i--;
            SimGridTokenManager.jjrounds[i] = 0x80000000;
        }
    }

    private static Token jjFillToken () {
        final var im =
            SimGridTokenManager.jjstrLiteralImages[SimGridTokenManager.jjmatchedKind];
        final var curTokenImage = (im == null) ? SimpleCharStream.GetImage() : im;
        final var beginLine     = SimpleCharStream.getBeginLine();
        final var beginColumn   = SimpleCharStream.getBeginColumn();
        final var endLine       = SimpleCharStream.getEndLine();
        final var endColumn     = SimpleCharStream.getEndColumn();
        final var t             = Token.newToken(SimGridTokenManager.jjmatchedKind, curTokenImage);

        t.beginLine   = beginLine;
        t.endLine     = endLine;
        t.beginColumn = beginColumn;
        t.endColumn   = endColumn;

        return t;
    }

    /**
     * Get the next Token.
     */
    public static Token getNextToken () {
        Token specialToken = null;
        var   curPos       = 0;

        while (true) {
            final Token matchedToken;
            try {
                SimGridTokenManager.curChar = SimpleCharStream.BeginToken();
            } catch (final java.io.IOException e) {
                SimGridTokenManager.jjmatchedKind = 0;
                matchedToken                      = jjFillToken();
                matchedToken.specialToken         = specialToken;
                return matchedToken;
            }
            SimGridTokenManager.image = SimGridTokenManager.jjimage;
            SimGridTokenManager.image.setLength(0);
            SimGridTokenManager.jjimageLen = 0;

            switch (SimGridTokenManager.curLexState) {
                case 0:
                    try {
                        SimpleCharStream.backup(0);
                        while (SimGridTokenManager.curChar <= 32
                               && (0x100003600L & (1L << SimGridTokenManager.curChar)) != 0L) {
                            SimGridTokenManager.curChar = SimpleCharStream.BeginToken();
                        }
                    } catch (final java.io.IOException e1) {
                        continue;
                    }
                    SimGridTokenManager.jjmatchedKind = 0x7fffffff;
                    SimGridTokenManager.jjmatchedPos = 0;
                    curPos = jjMoveStringLiteralDfa0_0();
                    break;
                case 1:
                    SimGridTokenManager.jjmatchedKind = 0x7fffffff;
                    SimGridTokenManager.jjmatchedPos = 0;
                    curPos = jjMoveStringLiteralDfa0_1();
                    if (SimGridTokenManager.jjmatchedPos == 0
                        && SimGridTokenManager.jjmatchedKind > 47) {
                        SimGridTokenManager.jjmatchedKind = 47;
                    }
                    break;
            }
            if (SimGridTokenManager.jjmatchedKind != 0x7fffffff) {
                if (SimGridTokenManager.jjmatchedPos + 1 < curPos) {
                    SimpleCharStream.backup(curPos - SimGridTokenManager.jjmatchedPos - 1);
                }
                if ((
                        SimGridTokenManager.jjtoToken[SimGridTokenManager.jjmatchedKind >> 6] & (
                            1L
                            << (
                                SimGridTokenManager.jjmatchedKind
                                & 077
                            )
                        )
                    ) != 0L) {
                    matchedToken              = jjFillToken();
                    matchedToken.specialToken = specialToken;
                    if (SimGridTokenManager.jjnewLexState[SimGridTokenManager.jjmatchedKind]
                        != -1) {
                        SimGridTokenManager.curLexState =
                            SimGridTokenManager.jjnewLexState[SimGridTokenManager.jjmatchedKind];
                    }
                    return matchedToken;
                } else {
                    if ((
                            SimGridTokenManager.jjtoSpecial[SimGridTokenManager.jjmatchedKind >> 6]
                            & (
                                1L << (
                                    SimGridTokenManager.jjmatchedKind
                                    & 077
                                )
                            )
                        ) != 0L) {
                        matchedToken = jjFillToken();
                        if (specialToken == null) {
                            specialToken = matchedToken;
                        } else {
                            matchedToken.specialToken = specialToken;
                            specialToken              = (specialToken.next = matchedToken);
                        }
                        SkipLexicalActions();
                    } else {
                        SkipLexicalActions();
                    }
                    if (SimGridTokenManager.jjnewLexState[SimGridTokenManager.jjmatchedKind]
                        != -1) {
                        SimGridTokenManager.curLexState =
                            SimGridTokenManager.jjnewLexState[SimGridTokenManager.jjmatchedKind];
                    }
                    continue;
                }
            }
            var    error_line   = SimpleCharStream.getEndLine();
            var    error_column = SimpleCharStream.getEndColumn();
            String error_after  = null;
            var    EOFSeen      = false;
            try {
                SimpleCharStream.readChar();
                SimpleCharStream.backup(1);
            } catch (final java.io.IOException e1) {
                EOFSeen     = true;
                error_after = curPos <= 1 ? "" : SimpleCharStream.GetImage();
                if (SimGridTokenManager.curChar == '\n' || SimGridTokenManager.curChar == '\r') {
                    error_line++;
                    error_column = 0;
                } else {
                    error_column++;
                }
            }
            if (!EOFSeen) {
                SimpleCharStream.backup(1);
                error_after = curPos <= 1 ? "" : SimpleCharStream.GetImage();
            }
            throw new TokenMgrError(
                EOFSeen,
                error_line,
                error_column,
                error_after,
                SimGridTokenManager.curChar
            );
        }
    }

    private static void SkipLexicalActions () {
        if (SimGridTokenManager.jjmatchedKind == 39) {
            SimGridTokenManager.image.append(SimpleCharStream.GetSuffix(
                SimGridTokenManager.jjimageLen + SimGridTokenManager.jjmatchedPos + 1));
            addErroLex(InterpretadorSimGrid.getFileName()
                       + ": Erro na linha "
                       + SimpleCharStream.getEndLine()
                       + ", coluna "
                       + SimpleCharStream.getEndColumn()
                       + ". Caracter \u005c""
                       + SimGridTokenManager.image
                       + "\u005c" n\u00e3o \u00e9 aceito.");
        }
    }
}

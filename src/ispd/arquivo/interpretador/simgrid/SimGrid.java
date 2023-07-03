package ispd.arquivo.interpretador.simgrid;

import ispd.arquivo.xml.IconicoXML;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

public class SimGrid {

    private static final int[] jj_la1 = new int[22];

    private static final JJCalls[] jj_2_rtns = new JJCalls[36];

    private static final LookaheadSuccess jj_ls = new LookaheadSuccess();

    private static final int[] jj_la1_0 = new int[] {
        0x10000000,
        0xf8000000,
        0xf8000000,
        0xf8000000,
        0x0,
        0x0,
        0x0,
        0x18000000,
        0x18000000,
        0x18000000,
        0x18000000,
        0x18000000,
        0x2000000,
        0x3000,
        0x0,
        0x3000,
        0x10008,
        0x30,
        0x18,
        0x28,
        0x38,
        0x180,
    };

    private static final int[] jj_la1_1 = new int[] {
        0x0,
        0x5f,
        0x5f,
        0x5f,
        0x2,
        0x30,
        0x30,
        0x48,
        0x40,
        0x40,
        0x40,
        0x40,
        0x1,
        0x0,
        0x2,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
    };

    private static final List<int[]> jj_expentries = new ArrayList<>();

    private static final int[] jj_lasttokens = new int[100];

    /**
     * Current token.
     */
    public static Token token = null;

    /**
     * Next token.
     */
    private static Token jj_nt = null;

    private static SimpleCharStream jj_input_stream = null;

    private static int net = 0;

    private static SimGrid parser = null;

    private static Collection<Server> servers = new ArrayList<>();

    private static Collection<Master> masters = new ArrayList<>();

    private static Collection<Network> networks = new ArrayList<>();

    private static Collection<Route> routes = new ArrayList<>();

    private static int contaErrosSint = 0;

    private static int contaErrosSem = 0;

    private static String errosSint = "Erros sint\u00e1ticos:\u005cn";

    private static String errosSem = "Erros sem\u00e2nticos:\u005cn";

    private static boolean jj_initialized_once = false;

    private static int jj_ntk = 0;

    private static Token jj_scanpos = null;

    private static Token jj_lastpos = null;

    private static int jj_la = 0;

    private static int jj_gen = 0;

    private static boolean jj_rescan = false;

    private static int jj_gc = 0;

    private static int[] jj_expentry = null;

    private static int jj_kind = -1;

    private static int jj_endpos = 0;

    private String erros = "";

    /**
     * Constructor with InputStream.
     */
    private SimGrid (final InputStream stream) {
        this(stream, null);
    }

    /**
     * Constructor with InputStream and supplied encoding
     */
    private SimGrid (final InputStream stream, final String encoding) {
        if (SimGrid.jj_initialized_once) {
            System.out.println("ERROR: Second call to constructor of static parser.  ");
            System.out.println(
                "       You must either use ReInit() or set the JavaCC option STATIC to false");
            System.out.println("       during parser generation.");
            throw new Error();
        }
        SimGrid.jj_initialized_once = true;
        try {
            SimGrid.jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        new SimGridTokenManager(SimGrid.jj_input_stream);
        SimGrid.token  = new Token();
        SimGrid.jj_ntk = -1;
        SimGrid.jj_gen = 0;
        for (var i = 0; i < 22; i++) {
            SimGrid.jj_la1[i] = -1;
        }
        for (var i = 0; i < SimGrid.jj_2_rtns.length; i++) {
            SimGrid.jj_2_rtns[i] = new JJCalls();
        }
    }

    public static SimGrid getInstance (final FileInputStream file) {
        if (SimGrid.parser == null) {
            SimGrid.parser = new SimGrid(file);
        }
        return SimGrid.parser;
    }

    private static void addErroSint (final String msg) {
        SimGrid.errosSint = SimGrid.errosSint + "\u005cn" + msg;
        SimGrid.contaErrosSint++;
    }

    private static void addErroSem (final String msg) {
        SimGrid.errosSem = SimGrid.errosSem + "\u005cn" + msg;
        SimGrid.contaErrosSem++;
    }

    private static String inteiro ()
        throws ParseException {
        try {
            var t = "";
            while (true) {
                jj_consume_token(SimGridConstants.digito);
                t += SimGrid.token.image;
                if (((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk)
                    == SimGridConstants.digito) {
                } else {
                    SimGrid.jj_la1[0] = SimGrid.jj_gen;
                    break;
                }
            }
            return t;
        } catch (final ParseException e) {
            final var t1 = getToken(1);
            addErroSem(InterpretadorSimGrid.getFileName()
                       + ": Erro na linha "
                       + t1.endLine
                       + ", coluna "
                       + t1.endColumn
                       + ". Valor atribu\u00eddo inv\u00e1lido.");
        }
        throw new Error("Missing return statement in function");
    }

    private static String real ()
        throws ParseException {
        try {
            var t1 = inteiro();
            jj_consume_token(SimGridConstants.ponto);
            t1 += SimGrid.token.image;
            final var t2 = inteiro();
            t1 += t2;
            return t1;
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSem(InterpretadorSimGrid.getFileName()
                       + ": Erro na linha "
                       + t.endLine
                       + ", coluna "
                       + t.endColumn
                       + ". Valor atribu\u00eddo inv\u00e1lido.");
        }
        throw new Error("Missing return statement in function");
    }

    private static String num_tarefas ()
        throws ParseException {
        return inteiro();
    }

    private static String max_comp_tam_tarefa ()
        throws ParseException {
        String t;
        if (jj_2_1(2147483647)) {
            t = real();
            return t;
        } else if (jj_2_2(2147483647)) {
            t = inteiro();
            t += ".0";
            return t;
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    private static String min_comp_tam_tarefa ()
        throws ParseException {
        String t;
        if (jj_2_3(2147483647)) {
            t = real();
            return t;
        } else if (jj_2_4(2147483647)) {
            t = inteiro();
            t += ".0";
            return t;
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    private static String max_comm_tam_tarefa ()
        throws ParseException {
        String t;
        if (jj_2_5(2147483647)) {
            t = real();
            return t;
        } else if (jj_2_6(2147483647)) {
            t = inteiro();
            t += ".0";
            return t;
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    private static String min_comm_tam_tarefa ()
        throws ParseException {
        String t;
        if (jj_2_7(2147483647)) {
            t = real();
            return t;
        } else if (jj_2_8(2147483647)) {
            t = inteiro();
            t += ".0";
            return t;
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    private static String capacidade_processamento ()
        throws ParseException {
        String t;
        if (jj_2_9(2147483647)) {
            t = real();
            return t;
        } else if (jj_2_10(2147483647)) {
            t = inteiro();
            t += ".0";
            return t;
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    private static String largura_banda ()
        throws ParseException {
        String t;
        if (jj_2_11(2147483647)) {
            t = real();
            return t;
        } else if (jj_2_12(2147483647)) {
            t = inteiro();
            t += ".0";
            return t;
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    private static String latencia ()
        throws ParseException {
        String t;
        if (jj_2_13(2147483647)) {
            t = real();
            return t;
        } else if (jj_2_14(2147483647)) {
            t = inteiro();
            t += ".0";
            return t;
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    private static String identificador () {
        try {
            var   erro = false;
            Token t1;
            var   t    = "";
            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                case SimGridConstants.letra:
                    jj_consume_token(SimGridConstants.letra);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    break;
                case SimGridConstants.digito:
                    jj_consume_token(SimGridConstants.digito);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                case SimGridConstants.especiais:
                    jj_consume_token(SimGridConstants.especiais);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                case SimGridConstants.ponto:
                    jj_consume_token(SimGridConstants.ponto);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                case SimGridConstants.barra:
                    jj_consume_token(SimGridConstants.barra);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                case SimGridConstants.exclamacao:
                    jj_consume_token(SimGridConstants.exclamacao);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                case SimGridConstants.interrogacao:
                    jj_consume_token(SimGridConstants.interrogacao);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                case SimGridConstants.menor:
                    jj_consume_token(SimGridConstants.menor);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                case SimGridConstants.maior:
                    jj_consume_token(SimGridConstants.maior);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                case SimGridConstants.igual:
                    jj_consume_token(SimGridConstants.igual);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                case SimGridConstants.apostrofo:
                    jj_consume_token(SimGridConstants.apostrofo);
                    t1 = getToken(1);
                    t += SimGrid.token.image;
                    erro = true;
                    break;
                default:
                    SimGrid.jj_la1[1] = SimGrid.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            label_2:
            while (true) {
                switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                    case SimGridConstants.letra:
                    case SimGridConstants.digito:
                    case SimGridConstants.ponto:
                    case SimGridConstants.barra:
                    case SimGridConstants.exclamacao:
                    case SimGridConstants.interrogacao:
                    case SimGridConstants.menor:
                    case SimGridConstants.maior:
                    case SimGridConstants.igual:
                    case SimGridConstants.apostrofo:
                    case SimGridConstants.especiais:
                        break;
                    default:
                        SimGrid.jj_la1[2] = SimGrid.jj_gen;
                        break label_2;
                }
                switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                    case SimGridConstants.letra:
                        jj_consume_token(SimGridConstants.letra);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        break;
                    case SimGridConstants.digito:
                        jj_consume_token(SimGridConstants.digito);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        break;
                    case SimGridConstants.especiais:
                        jj_consume_token(SimGridConstants.especiais);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        break;
                    case SimGridConstants.ponto:
                        jj_consume_token(SimGridConstants.ponto);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        erro = true;
                        break;
                    case SimGridConstants.barra:
                        jj_consume_token(SimGridConstants.barra);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        erro = true;
                        break;
                    case SimGridConstants.exclamacao:
                        jj_consume_token(SimGridConstants.exclamacao);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        erro = true;
                        break;
                    case SimGridConstants.interrogacao:
                        jj_consume_token(SimGridConstants.interrogacao);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        erro = true;
                        break;
                    case SimGridConstants.menor:
                        jj_consume_token(SimGridConstants.menor);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        erro = true;
                        break;
                    case SimGridConstants.maior:
                        jj_consume_token(SimGridConstants.maior);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        erro = true;
                        break;
                    case SimGridConstants.igual:
                        jj_consume_token(SimGridConstants.igual);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        erro = true;
                        break;
                    case SimGridConstants.apostrofo:
                        jj_consume_token(SimGridConstants.apostrofo);
                        t1 = getToken(1);
                        t += SimGrid.token.image;
                        erro = true;
                        break;
                    default:
                        SimGrid.jj_la1[3] = SimGrid.jj_gen;
                        jj_consume_token(-1);
                        throw new ParseException();
                }
            }
            if (erro) {
                SimGridTokenManager.addErroLex(InterpretadorSimGrid.getFileName()
                                               + ": Erro na linha "
                                               + t1.endLine
                                               + ", coluna "
                                               + t1.endColumn
                                               + ". Identificador "
                                               + t
                                               + " declarado incorretamente.");
            }
            return t;
        } catch (final ParseException e) {
            final var t2 = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t2.endLine
                        + ", coluna "
                        + t2.endColumn
                        + ".");
        }
        throw new Error("Missing return statement in function");
    }

    private static String servidor_ID ()
        throws ParseException {
        return identificador();
    }

    private static String rede_ID ()
        throws ParseException {
        return identificador();
    }

    public static void modelo ()
        throws ParseException {
        inicio_xml_plataforma();
        if (jj_2_15(2)) {
            application_file();
        } else {
            if (((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) == SimGridConstants.menor) {
                plataform_file();
            } else {
                SimGrid.jj_la1[4] = SimGrid.jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
            }
        }
        fim_plataforma();
    }

    private static void application_file ()
        throws ParseException {
        while (true) {
            processos();
            if (jj_2_16(2)) {
            } else {
                break;
            }
        }
    }

    private static void plataform_file ()
        throws ParseException {
        while (true) {
            plataforma();
            if (jj_2_17(2)) {
            } else {
                break;
            }
        }
    }

    private static void plataforma ()
        throws ParseException {
        if (jj_2_18(2)) {
            servidor();
        } else if (jj_2_19(2)) {
            rede();
        } else if (jj_2_20(2)) {
            roteamento();
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    private static void inicio_xml_plataforma () {
        try {
            jj_consume_token(SimGridConstants.menor);
            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                case SimGridConstants.interrogacao:
                    jj_consume_token(SimGridConstants.interrogacao);
                    jj_consume_token(SimGridConstants.XML);
                    jj_consume_token(SimGridConstants.VERSION);
                    jj_consume_token(SimGridConstants.igual);
                    switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                        case SimGridConstants.apostrofo:
                            jj_consume_token(SimGridConstants.apostrofo);
                            break;
                        case SimGridConstants.aspas:
                            jj_consume_token(SimGridConstants.aspas);
                            break;
                        default:
                            SimGrid.jj_la1[5] = SimGrid.jj_gen;
                            jj_consume_token(-1);
                            throw new ParseException();
                    }
                    real();
                    switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                        case SimGridConstants.apostrofo:
                            jj_consume_token(SimGridConstants.apostrofo);
                            break;
                        case SimGridConstants.aspas:
                            jj_consume_token(SimGridConstants.aspas);
                            break;
                        default:
                            SimGrid.jj_la1[6] = SimGrid.jj_gen;
                            jj_consume_token(-1);
                            throw new ParseException();
                    }
                    label_5:
                    while (true) {
                        switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                            case SimGridConstants.letra:
                            case SimGridConstants.digito:
                            case SimGridConstants.igual:
                            case SimGridConstants.especiais:
                                break;
                            default:
                                SimGrid.jj_la1[7] = SimGrid.jj_gen;
                                break label_5;
                        }
                        label_6:
                        while (true) {
                            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                                case SimGridConstants.letra:
                                case SimGridConstants.digito:
                                case SimGridConstants.especiais:
                                    break;
                                default:
                                    SimGrid.jj_la1[8] = SimGrid.jj_gen;
                                    break label_6;
                            }
                            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                                case SimGridConstants.letra:
                                    jj_consume_token(SimGridConstants.letra);
                                    break;
                                case SimGridConstants.digito:
                                    jj_consume_token(SimGridConstants.digito);
                                    break;
                                case SimGridConstants.especiais:
                                    jj_consume_token(SimGridConstants.especiais);
                                    break;
                                default:
                                    SimGrid.jj_la1[9] = SimGrid.jj_gen;
                                    jj_consume_token(-1);
                                    throw new ParseException();
                            }
                        }
                        jj_consume_token(SimGridConstants.igual);
                        jj_consume_token(SimGridConstants.aspas);
                        label_7:
                        while (true) {
                            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                                case SimGridConstants.letra:
                                case SimGridConstants.digito:
                                case SimGridConstants.especiais:
                                    break;
                                default:
                                    SimGrid.jj_la1[10] = SimGrid.jj_gen;
                                    break label_7;
                            }
                            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                                case SimGridConstants.letra:
                                    jj_consume_token(SimGridConstants.letra);
                                    break;
                                case SimGridConstants.digito:
                                    jj_consume_token(SimGridConstants.digito);
                                    break;
                                case SimGridConstants.especiais:
                                    jj_consume_token(SimGridConstants.especiais);
                                    break;
                                default:
                                    SimGrid.jj_la1[11] = SimGrid.jj_gen;
                                    jj_consume_token(-1);
                                    throw new ParseException();
                            }
                        }
                        jj_consume_token(SimGridConstants.aspas);
                    }
                    jj_consume_token(SimGridConstants.interrogacao);
                    jj_consume_token(SimGridConstants.maior);
                    jj_consume_token(SimGridConstants.menor);
                    jj_consume_token(SimGridConstants.PLATFORM_DESCRIPTION);
                    jj_consume_token(SimGridConstants.VERSION);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    inteiro();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.maior);
                    break;
                case SimGridConstants.PLATFORM_DESCRIPTION:
                    jj_consume_token(SimGridConstants.PLATFORM_DESCRIPTION);
                    jj_consume_token(SimGridConstants.VERSION);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    inteiro();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.maior);
                    break;
                default:
                    SimGrid.jj_la1[12] = SimGrid.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t.endLine
                        + ", coluna "
                        + t.endColumn
                        + ".");
        }
    }

    private static void fim_plataforma () {
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.PLATFORM_DESCRIPTION);
            jj_consume_token(SimGridConstants.maior);
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t.endLine
                        + ", coluna "
                        + t.endColumn
                        + ".");
        }
    }

    private static void processos ()
        throws ParseException {
        if (jj_2_21(2147483647)) {
            master();
        } else if (jj_2_22(2147483647)) {
            master();
        } else if (jj_2_23(2147483647)) {
            slave();
        } else if (jj_2_24(2147483647)) {
            slave();
        } else if (jj_2_25(2147483647)) {
            outros();
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    public static void master ()
        throws ParseException {
        final var m = new Master();
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.PROCESS);
            final String t1;
            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                case SimGridConstants.HOST:
                    jj_consume_token(SimGridConstants.HOST);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t1 = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.FUNCTION);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.MASTER);
                    jj_consume_token(SimGridConstants.aspas);
                    break;
                case SimGridConstants.FUNCTION:
                    jj_consume_token(SimGridConstants.FUNCTION);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.MASTER);
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.HOST);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t1 = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    break;
                default:
                    SimGrid.jj_la1[13] = SimGrid.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            jj_consume_token(SimGridConstants.maior);
            final var t     = getToken(1);
            var       found = false;
            for (final var testem : SimGrid.masters) {
                if (t1.equals(testem.getID())) {
                    addErroSem(InterpretadorSimGrid.getFileName()
                               + ": Erro na linha "
                               + t.endLine
                               + ", coluna "
                               + t.endColumn
                               + ". Servidor \u005c""
                               + t1
                               + "\u005c" j\u00e1 foi declarado.");
                    found = true;
                }
            }
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.ARGUMENT);
            jj_consume_token(SimGridConstants.VALUE);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            final var t2 = num_tarefas();
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.maior);
            final String t4;
            final String t3;
            if (jj_2_26(2147483647)) {
                jj_consume_token(SimGridConstants.menor);
                jj_consume_token(SimGridConstants.ARGUMENT);
                jj_consume_token(SimGridConstants.VALUE);
                jj_consume_token(SimGridConstants.igual);
                jj_consume_token(SimGridConstants.aspas);
                t3 = max_comp_tam_tarefa();
                jj_consume_token(SimGridConstants.aspas);
                jj_consume_token(SimGridConstants.barra);
                jj_consume_token(SimGridConstants.maior);
                jj_consume_token(SimGridConstants.menor);
                jj_consume_token(SimGridConstants.ARGUMENT);
                jj_consume_token(SimGridConstants.VALUE);
                jj_consume_token(SimGridConstants.igual);
                jj_consume_token(SimGridConstants.aspas);
                t4 = min_comp_tam_tarefa();
                jj_consume_token(SimGridConstants.aspas);
                jj_consume_token(SimGridConstants.barra);
                jj_consume_token(SimGridConstants.maior);
                jj_consume_token(SimGridConstants.menor);
                jj_consume_token(SimGridConstants.ARGUMENT);
                jj_consume_token(SimGridConstants.VALUE);
                jj_consume_token(SimGridConstants.igual);
                jj_consume_token(SimGridConstants.aspas);
                final var t5 = max_comm_tam_tarefa();
                jj_consume_token(SimGridConstants.aspas);
                jj_consume_token(SimGridConstants.barra);
                jj_consume_token(SimGridConstants.maior);
                jj_consume_token(SimGridConstants.menor);
                jj_consume_token(SimGridConstants.ARGUMENT);
                jj_consume_token(SimGridConstants.VALUE);
                jj_consume_token(SimGridConstants.igual);
                jj_consume_token(SimGridConstants.aspas);
                final var t6 = min_comm_tam_tarefa();
                jj_consume_token(SimGridConstants.aspas);
                jj_consume_token(SimGridConstants.barra);
                jj_consume_token(SimGridConstants.maior);
                m.setCarga(t2, t3, t4, t5, t6);
            } else {
                if (((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk)
                    == SimGridConstants.menor) {
                    jj_consume_token(SimGridConstants.menor);
                    jj_consume_token(SimGridConstants.ARGUMENT);
                    jj_consume_token(SimGridConstants.VALUE);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t3 = max_comp_tam_tarefa();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.barra);
                    jj_consume_token(SimGridConstants.maior);
                    jj_consume_token(SimGridConstants.menor);
                    jj_consume_token(SimGridConstants.ARGUMENT);
                    jj_consume_token(SimGridConstants.VALUE);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t4 = max_comm_tam_tarefa();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.barra);
                    jj_consume_token(SimGridConstants.maior);
                    m.setCarga(t2, t3, t4);
                } else {
                    SimGrid.jj_la1[14] = SimGrid.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
                }
            }
            final List<String> slaves = new ArrayList<>();
            while (true) {
                jj_consume_token(SimGridConstants.menor);
                jj_consume_token(SimGridConstants.ARGUMENT);
                jj_consume_token(SimGridConstants.VALUE);
                jj_consume_token(SimGridConstants.igual);
                jj_consume_token(SimGridConstants.aspas);
                final var t7 = servidor_ID();
                final var tt = getToken(1);
                if (slaves.contains(t7)) {
                    addErroSem(InterpretadorSimGrid.getFileName()
                               + ": Erro na linha "
                               + tt.endLine
                               + ", coluna "
                               + tt.endColumn
                               + ". Servidor \u005c""
                               + t7
                               + "\u005c" j\u00e1 foi declarado.");
                    found = true;
                } else {
                    slaves.add(t7);
                }
                jj_consume_token(SimGridConstants.aspas);
                jj_consume_token(SimGridConstants.barra);
                jj_consume_token(SimGridConstants.maior);
                if (jj_2_27(2)) {
                } else {
                    break;
                }
            }
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.maior);
            if (!found) {
                m.setID(t1);
                m.setSlaves(slaves);
                SimGrid.masters.add(m);
            }
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t.endLine
                        + ", coluna "
                        + t.endColumn
                        + ".");
        }
    }

    public static void slave ()
        throws ParseException {
        final var s = new Server();
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.PROCESS);
            final String t;
            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                case SimGridConstants.HOST:
                    jj_consume_token(SimGridConstants.HOST);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.FUNCTION);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.SLAVE);
                    jj_consume_token(SimGridConstants.aspas);
                    break;
                case SimGridConstants.FUNCTION:
                    jj_consume_token(SimGridConstants.FUNCTION);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.SLAVE);
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.HOST);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    break;
                default:
                    SimGrid.jj_la1[15] = SimGrid.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.maior);
            final var t1    = getToken(1);
            var       found = false;
            for (final var testes : SimGrid.servers) {
                if (t.equals(testes.getID())) {
                    addErroSem(InterpretadorSimGrid.getFileName()
                               + ": Erro na linha "
                               + t1.endLine
                               + ", coluna "
                               + t1.endColumn
                               + ". Servidor \u005c""
                               + t
                               + "\u005c" j\u00e1 foi declarado.");
                    found = true;
                }
            }
            if (!found) {
                s.setID(t);
                SimGrid.servers.add(s);
            }
        } catch (final ParseException e) {
            final var t2 = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t2.endLine
                        + ", coluna "
                        + t2.endColumn
                        + ".");
        }
    }

    private static void outros ()
        throws ParseException {
        if (jj_2_28(2147483647)) {
            tasksource();
        } else if (jj_2_29(2147483647)) {
            slavecomm();
        } else if (jj_2_30(2147483647)) {
            reloadhost();
        } else if (jj_2_31(2147483647)) {
            forwarderscheduler();
        } else if (jj_2_32(2147483647)) {
            forwardernode();
        } else if (jj_2_33(2147483647)) {
            forwardercomm();
        } else {
            jj_consume_token(-1);
            throw new ParseException();
        }
    }

    public static void servidor ()
        throws ParseException {
        try {
            final var t = getToken(1);
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.CPU);
            final String t2;
            final String t1;
            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                case SimGridConstants.NAME:
                    jj_consume_token(SimGridConstants.NAME);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t1 = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.POWER);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t2 = capacidade_processamento();
                    jj_consume_token(SimGridConstants.aspas);
                    break;
                case SimGridConstants.POWER:
                    jj_consume_token(SimGridConstants.POWER);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t2 = capacidade_processamento();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.NAME);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t1 = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    break;
                default:
                    SimGrid.jj_la1[16] = SimGrid.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.maior);
            var found = false;
            for (final var s : SimGrid.servers) {
                if (t1.equals(s.getID())) {
                    s.setPower(t2);
                    found = true;
                }
            }
            if (!found) {
                for (final var m : SimGrid.masters) {
                    if (t1.equals(m.getID())) {
                        m.setPower(t2);
                        found = true;
                    }
                }
            }
            if (!found) {
                addErroSem(InterpretadorSimGrid.getFileName()
                           + ": Erro na linha "
                           + t.endLine
                           + ", coluna "
                           + t.endColumn
                           + ". Servidor \u005c""
                           + t1
                           + "\u005c" n\u00e3o foi declarado.");
            }
        } catch (final ParseException e) {
            final var t3 = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t3.endLine
                        + ", coluna "
                        + t3.endColumn
                        + ".");
        }
    }

    public static void rede ()
        throws ParseException {
        final var n = new Network();
        try {
            final var t = getToken(1);
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.NETWORK_LINK);
            final String t3;
            final String t2;
            final String t1;
            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                case SimGridConstants.NAME:
                    jj_consume_token(SimGridConstants.NAME);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t1 = rede_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                        case SimGridConstants.BANDWIDTH:
                            jj_consume_token(SimGridConstants.BANDWIDTH);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t2 = largura_banda();
                            jj_consume_token(SimGridConstants.aspas);
                            jj_consume_token(SimGridConstants.LATENCY);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t3 = latencia();
                            jj_consume_token(SimGridConstants.aspas);
                            break;
                        case SimGridConstants.LATENCY:
                            jj_consume_token(SimGridConstants.LATENCY);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t3 = latencia();
                            jj_consume_token(SimGridConstants.aspas);
                            jj_consume_token(SimGridConstants.BANDWIDTH);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t2 = largura_banda();
                            jj_consume_token(SimGridConstants.aspas);
                            break;
                        default:
                            SimGrid.jj_la1[17] = SimGrid.jj_gen;
                            jj_consume_token(-1);
                            throw new ParseException();
                    }
                    break;
                case SimGridConstants.LATENCY:
                    jj_consume_token(SimGridConstants.LATENCY);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t3 = latencia();
                    jj_consume_token(SimGridConstants.aspas);
                    switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                        case SimGridConstants.BANDWIDTH:
                            jj_consume_token(SimGridConstants.BANDWIDTH);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t2 = largura_banda();
                            jj_consume_token(SimGridConstants.aspas);
                            jj_consume_token(SimGridConstants.NAME);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t1 = rede_ID();
                            jj_consume_token(SimGridConstants.aspas);
                            break;
                        case SimGridConstants.NAME:
                            jj_consume_token(SimGridConstants.NAME);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t1 = rede_ID();
                            jj_consume_token(SimGridConstants.aspas);
                            jj_consume_token(SimGridConstants.BANDWIDTH);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t2 = largura_banda();
                            jj_consume_token(SimGridConstants.aspas);
                            break;
                        default:
                            SimGrid.jj_la1[18] = SimGrid.jj_gen;
                            jj_consume_token(-1);
                            throw new ParseException();
                    }
                    break;
                case SimGridConstants.BANDWIDTH:
                    jj_consume_token(SimGridConstants.BANDWIDTH);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t2 = largura_banda();
                    jj_consume_token(SimGridConstants.aspas);
                    switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                        case SimGridConstants.NAME:
                            jj_consume_token(SimGridConstants.NAME);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t1 = rede_ID();
                            jj_consume_token(SimGridConstants.aspas);
                            jj_consume_token(SimGridConstants.LATENCY);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t3 = latencia();
                            jj_consume_token(SimGridConstants.aspas);
                            break;
                        case SimGridConstants.LATENCY:
                            jj_consume_token(SimGridConstants.LATENCY);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t3 = latencia();
                            jj_consume_token(SimGridConstants.aspas);
                            jj_consume_token(SimGridConstants.NAME);
                            jj_consume_token(SimGridConstants.igual);
                            jj_consume_token(SimGridConstants.aspas);
                            t1 = rede_ID();
                            jj_consume_token(SimGridConstants.aspas);
                            break;
                        default:
                            SimGrid.jj_la1[19] = SimGrid.jj_gen;
                            jj_consume_token(-1);
                            throw new ParseException();
                    }
                    break;
                default:
                    SimGrid.jj_la1[20] = SimGrid.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.maior);
            var found = false;
            for (final var testen : SimGrid.networks) {
                if (t1.equals(testen.getID())) {
                    addErroSem(InterpretadorSimGrid.getFileName()
                               + ": Erro na linha "
                               + t.endLine
                               + ", coluna "
                               + t.endColumn
                               + ". Link \u005c""
                               + t1
                               + "\u005c" j\u00e1 foi declarado.");
                    found = true;
                }
            }
            if (!found) {
                n.setAtributos(t1, t2, t3);
                SimGrid.networks.add(n);
            }
        } catch (final ParseException e) {
            final var t4 = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t4.endLine
                        + ", coluna "
                        + t4.endColumn
                        + ".");
        }
    }

    private static void roteamento () {
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.ROUTE);
            final String t2;
            final String t1;
            switch ((SimGrid.jj_ntk == -1) ? jj_ntk() : SimGrid.jj_ntk) {
                case SimGridConstants.SRC:
                    jj_consume_token(SimGridConstants.SRC);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t1 = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.DST);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t2 = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    break;
                case SimGridConstants.DST:
                    jj_consume_token(SimGridConstants.DST);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t2 = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    jj_consume_token(SimGridConstants.SRC);
                    jj_consume_token(SimGridConstants.igual);
                    jj_consume_token(SimGridConstants.aspas);
                    t1 = servidor_ID();
                    jj_consume_token(SimGridConstants.aspas);
                    break;
                default:
                    SimGrid.jj_la1[21] = SimGrid.jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
            }
            final var t     = getToken(1);
            var       found = false;
            for (final var s : SimGrid.servers) {
                if (t1.equals(s.getID())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (final var m : SimGrid.masters) {
                    if (t1.equals(m.getID())) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                addErroSem(InterpretadorSimGrid.getFileName()
                           + ": Erro na linha "
                           + t.endLine
                           + ", coluna "
                           + t.endColumn
                           + ". Servidor \u005c""
                           + t1
                           + "\u005c" n\u00e3o foi declarado.");
            }
            found = false;
            final var tt = getToken(1);
            for (final var m : SimGrid.masters) {
                if (t2.equals(m.getID())) {
                    found = true;
                    break;
                }
            }
            for (final var s : SimGrid.servers) {
                if (t2.equals(s.getID())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                addErroSem(InterpretadorSimGrid.getFileName()
                           + ": Erro na linha "
                           + tt.endLine
                           + ", coluna "
                           + tt.endColumn
                           + ". Servidor \u005c""
                           + t2
                           + "\u005c" n\u00e3o foi declarado.");
            }
            found = false;
            final var ttt = getToken(1);
            jj_consume_token(SimGridConstants.maior);
            var inicio = t1;
            while (true) {
                jj_consume_token(SimGridConstants.menor);
                jj_consume_token(SimGridConstants.ROUTE_ELEMENT);
                jj_consume_token(SimGridConstants.NAME);
                jj_consume_token(SimGridConstants.igual);
                jj_consume_token(SimGridConstants.aspas);
                final var t3 = rede_ID();
                for (final var n : SimGrid.networks) {
                    if (t3.equals(n.getID())) {
                        Route achou = null;
                        for (final var rot : SimGrid.routes) {
                            if (rot.getID().equals(t3)) {
                                achou = rot;
                            }
                        }
                        if (achou != null) {
                            if (achou.getSrc().equals(inicio)) {
                                achou.setRoute(achou.getNet(), inicio, t2);
                            } else if (achou.getDst().equals(inicio)) {
                                SimGrid.net++;
                                final var novo = new Route();
                                novo.setAtributos(t3, n.getBand(), n.getLatency());
                                novo.setRoute(SimGrid.net, inicio, t2);
                                SimGrid.routes.add(novo);
                            } else {
                                achou.setInternet(true);
                            }
                        } else {
                            SimGrid.net++;
                            final var novo = new Route();
                            novo.setAtributos(t3, n.getBand(), n.getLatency());
                            novo.setRoute(SimGrid.net, inicio, t2);
                            SimGrid.routes.add(novo);
                        }
                        for (final var rot : SimGrid.routes) {
                            if (rot.getID().equals(inicio)) {
                                rot.setRoute(rot.getNet(), rot.getSrc(), t3);
                            }
                        }
                        inicio = t3;
                        found  = true;
                    }
                }
                if (!found) {
                    addErroSem(InterpretadorSimGrid.getFileName()
                               + ": Erro na linha "
                               + ttt.endLine
                               + ", coluna "
                               + ttt.endColumn
                               + ". Link \u005c""
                               + t3
                               + "\u005c" n\u00e3o foi declarado.");
                }
                jj_consume_token(SimGridConstants.aspas);
                jj_consume_token(SimGridConstants.barra);
                jj_consume_token(SimGridConstants.maior);
                if (jj_2_34(2)) {
                } else {
                    break;
                }
            }
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.ROUTE);
            jj_consume_token(SimGridConstants.maior);
            final List<String> itemRede = new ArrayList<>();
            for (final var net : SimGrid.networks) {
                itemRede.add(net.getID());
            }
            for (final var net : SimGrid.routes) {
                //Testando se origem é uma máquina
                if (!itemRede.contains(net.getSrc()) && itemRede.contains(net.getDst())) {
                    for (final var destino : SimGrid.routes) {
                        if (destino.getID().equals(net.getDst())) {
                            destino.setInternet(true);
                        }
                    }
                }
                //Testando se destino é uma máquina
                if (itemRede.contains(net.getSrc()) && !itemRede.contains(net.getDst())) {
                    for (final var origem : SimGrid.routes) {
                        if (origem.getID().equals(net.getSrc())) {
                            origem.setInternet(true);
                        }
                    }
                }
            }
            for (final var net : SimGrid.routes) {
                for (final var linkOrigem : SimGrid.routes) {
                    if (!linkOrigem.isInternet() && net.getSrc().equals(linkOrigem.getID())) {
                        for (final var linkDestino : SimGrid.routes) {
                            if (!linkDestino.isInternet() && net
                                .getDst()
                                .equals(linkDestino.getID())) {
                                net.setInternet(true);
                            }
                        }
                    }
                }
            }
        } catch (final ParseException e) {
            final var t4 = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t4.endLine
                        + ", coluna "
                        + t4.endColumn
                        + ".");
        }
    }

    private static void tasksource () {
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.HOST);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            servidor_ID();
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.FUNCTION);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.TASKSOURCE);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.maior);
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.ARGUMENT);
            jj_consume_token(SimGridConstants.VALUE);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            inteiro();
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.maior);
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.maior);
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t.endLine
                        + ", coluna "
                        + t.endColumn
                        + ".");
        }
    }

    private static void slavecomm () {
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.HOST);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            servidor_ID();
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.FUNCTION);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.SLAVECOMM);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.maior);
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t.endLine
                        + ", coluna "
                        + t.endColumn
                        + ".");
        }
    }

    private static void reloadhost () {
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.HOST);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            servidor_ID();
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.FUNCTION);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.RELOADHOST);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.maior);
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t.endLine
                        + ", coluna "
                        + t.endColumn
                        + ".");
        }
    }

    private static void forwarderscheduler () {
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.HOST);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            servidor_ID();
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.FUNCTION);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.FORWARDERSCHEDULER);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.maior);
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.ARGUMENT);
            jj_consume_token(SimGridConstants.VALUE);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            servidor_ID();
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.maior);
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.maior);
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t.endLine
                        + ", coluna "
                        + t.endColumn
                        + ".");
        }
    }

    private static void forwardernode () {
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.HOST);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            servidor_ID();
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.FUNCTION);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.FORWARDERNODE);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.maior);
            while (true) {
                jj_consume_token(SimGridConstants.menor);
                jj_consume_token(SimGridConstants.ARGUMENT);
                jj_consume_token(SimGridConstants.VALUE);
                jj_consume_token(SimGridConstants.igual);
                jj_consume_token(SimGridConstants.aspas);
                servidor_ID();
                jj_consume_token(SimGridConstants.aspas);
                jj_consume_token(SimGridConstants.barra);
                jj_consume_token(SimGridConstants.maior);
                if (jj_2_35(2)) {
                } else {
                    break;
                }
            }
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.maior);
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t.endLine
                        + ", coluna "
                        + t.endColumn
                        + ".");
        }
    }

    private static void forwardercomm () {
        try {
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.HOST);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            servidor_ID();
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.FUNCTION);
            jj_consume_token(SimGridConstants.igual);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.FORWARDERCOMM);
            jj_consume_token(SimGridConstants.aspas);
            jj_consume_token(SimGridConstants.maior);
            while (true) {
                jj_consume_token(SimGridConstants.menor);
                jj_consume_token(SimGridConstants.ARGUMENT);
                jj_consume_token(SimGridConstants.VALUE);
                jj_consume_token(SimGridConstants.igual);
                jj_consume_token(SimGridConstants.aspas);
                servidor_ID();
                jj_consume_token(SimGridConstants.aspas);
                jj_consume_token(SimGridConstants.barra);
                jj_consume_token(SimGridConstants.maior);
                if (jj_2_36(2)) {
                } else {
                    break;
                }
            }
            jj_consume_token(SimGridConstants.menor);
            jj_consume_token(SimGridConstants.barra);
            jj_consume_token(SimGridConstants.PROCESS);
            jj_consume_token(SimGridConstants.maior);
        } catch (final ParseException e) {
            final var t = getToken(1);
            addErroSint(InterpretadorSimGrid.getFileName()
                        + ": Erro sint\u00e1tico na linha "
                        + t.endLine
                        + ", coluna "
                        + t.endColumn
                        + ".");
        }
    }

    private static boolean jj_2_1 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_1();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(0, xla);
        }
    }

    private static boolean jj_2_2 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_2();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(1, xla);
        }
    }

    private static boolean jj_2_3 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_3();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(2, xla);
        }
    }

    private static boolean jj_2_4 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_4();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(3, xla);
        }
    }

    private static boolean jj_2_5 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_5();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(4, xla);
        }
    }

    private static boolean jj_2_6 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_6();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(5, xla);
        }
    }

    private static boolean jj_2_7 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_7();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(6, xla);
        }
    }

    private static boolean jj_2_8 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_8();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(7, xla);
        }
    }

    private static boolean jj_2_9 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_9();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(8, xla);
        }
    }

    private static boolean jj_2_10 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_10();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(9, xla);
        }
    }

    private static boolean jj_2_11 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_11();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(10, xla);
        }
    }

    private static boolean jj_2_12 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_12();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(11, xla);
        }
    }

    private static boolean jj_2_13 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_13();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(12, xla);
        }
    }

    private static boolean jj_2_14 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_14();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(13, xla);
        }
    }

    private static boolean jj_2_15 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_15();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(14, xla);
        }
    }

    private static boolean jj_2_16 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_16();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(15, xla);
        }
    }

    private static boolean jj_2_17 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_17();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(16, xla);
        }
    }

    private static boolean jj_2_18 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_18();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(17, xla);
        }
    }

    private static boolean jj_2_19 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_19();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(18, xla);
        }
    }

    private static boolean jj_2_20 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_20();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(19, xla);
        }
    }

    private static boolean jj_2_21 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_21();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(20, xla);
        }
    }

    private static boolean jj_2_22 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_22();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(21, xla);
        }
    }

    private static boolean jj_2_23 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_23();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(22, xla);
        }
    }

    private static boolean jj_2_24 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_24();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(23, xla);
        }
    }

    private static boolean jj_2_25 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_25();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(24, xla);
        }
    }

    private static boolean jj_2_26 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_26();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(25, xla);
        }
    }

    private static boolean jj_2_27 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_27();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(26, xla);
        }
    }

    private static boolean jj_2_28 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_28();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(27, xla);
        }
    }

    private static boolean jj_2_29 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_29();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(28, xla);
        }
    }

    private static boolean jj_2_30 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_30();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(29, xla);
        }
    }

    private static boolean jj_2_31 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_31();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(30, xla);
        }
    }

    private static boolean jj_2_32 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_32();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(31, xla);
        }
    }

    private static boolean jj_2_33 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_33();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(32, xla);
        }
    }

    private static boolean jj_2_34 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_34();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(33, xla);
        }
    }

    private static boolean jj_2_35 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_35();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(34, xla);
        }
    }

    private static boolean jj_2_36 (final int xla) {
        SimGrid.jj_la      = xla;
        SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.token;
        try {
            return !jj_3_36();
        } catch (final LookaheadSuccess ls) {
            return true;
        } finally {
            jj_save(35, xla);
        }
    }

    private static boolean jj_3_15 () {
        return jj_3R_14();
    }

    private static boolean jj_3_18 () {
        return jj_3R_17();
    }

    private static boolean jj_3R_16 () {
        final var xsp = SimGrid.jj_scanpos;
        if (jj_3_18()) {
            SimGrid.jj_scanpos = xsp;
            if (jj_3_19()) {
                SimGrid.jj_scanpos = xsp;
                return jj_3_20();
            }
        }
        return false;
    }

    private static boolean jj_3_17 () {
        return jj_3R_16();
    }

    private static boolean jj_3_16 () {
        return jj_3R_15();
    }

    private static boolean jj_3R_14 () {
        if (jj_3_16()) {
            return true;
        }
        while (true) {
            final var xsp = SimGrid.jj_scanpos;
            if (jj_3_16()) {
                SimGrid.jj_scanpos = xsp;
                break;
            }
        }
        return false;
    }

    private static boolean jj_3_33 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.HOST)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_20()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.FORWARDERCOMM);
    }

    private static boolean jj_3_32 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.HOST)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_20()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.FORWARDERNODE);
    }

    private static boolean jj_3R_17 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.CPU);
    }

    private static boolean jj_3_31 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.HOST)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_20()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.FORWARDERSCHEDULER);
    }

    private static boolean jj_3_30 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.HOST)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_20()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.RELOADHOST);
    }

    private static boolean jj_3_29 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.HOST)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_20()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.SLAVECOMM);
    }

    private static boolean jj_3_28 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.HOST)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_20()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.TASKSOURCE);
    }

    private static boolean jj_3R_20 () {
        return jj_3R_31();
    }

    private static boolean jj_3_14 () {
        return jj_3R_13();
    }

    private static boolean jj_3R_60 () {
        return jj_3R_77();
    }

    private static boolean jj_3R_59 () {
        return jj_3R_76();
    }

    private static boolean jj_3R_58 () {
        return jj_3R_75();
    }

    private static boolean jj_3R_57 () {
        return jj_3R_74();
    }

    private static boolean jj_3R_56 () {
        return jj_3R_73();
    }

    private static boolean jj_3R_55 () {
        return jj_3R_72();
    }

    private static boolean jj_3_12 () {
        return jj_3R_13();
    }

    private static boolean jj_3R_42 () {
        final var xsp = SimGrid.jj_scanpos;
        if (jj_3R_55()) {
            SimGrid.jj_scanpos = xsp;
            if (jj_3R_56()) {
                SimGrid.jj_scanpos = xsp;
                if (jj_3R_57()) {
                    SimGrid.jj_scanpos = xsp;
                    if (jj_3R_58()) {
                        SimGrid.jj_scanpos = xsp;
                        if (jj_3R_59()) {
                            SimGrid.jj_scanpos = xsp;
                            return jj_3R_60();
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean jj_3R_71 () {
        return jj_scan_token(SimGridConstants.apostrofo);
    }

    private static boolean jj_3R_70 () {
        return jj_scan_token(SimGridConstants.igual);
    }

    private static boolean jj_3R_69 () {
        return jj_scan_token(SimGridConstants.maior);
    }

    private static boolean jj_3R_68 () {
        return jj_scan_token(SimGridConstants.menor);
    }

    private static boolean jj_3R_67 () {
        return jj_scan_token(SimGridConstants.interrogacao);
    }

    private static boolean jj_3_10 () {
        return jj_3R_13();
    }

    private static boolean jj_3R_66 () {
        return jj_scan_token(SimGridConstants.exclamacao);
    }

    private static boolean jj_3R_65 () {
        return jj_scan_token(SimGridConstants.barra);
    }

    private static boolean jj_3R_64 () {
        return jj_scan_token(SimGridConstants.ponto);
    }

    private static boolean jj_3R_63 () {
        return jj_scan_token(SimGridConstants.especiais);
    }

    private static boolean jj_3R_62 () {
        return jj_scan_token(SimGridConstants.digito);
    }

    private static boolean jj_3R_61 () {
        return jj_scan_token(SimGridConstants.letra);
    }

    private static boolean jj_3R_54 () {
        final var xsp = SimGrid.jj_scanpos;
        if (jj_3R_61()) {
            SimGrid.jj_scanpos = xsp;
            if (jj_3R_62()) {
                SimGrid.jj_scanpos = xsp;
                if (jj_3R_63()) {
                    SimGrid.jj_scanpos = xsp;
                    if (jj_3R_64()) {
                        SimGrid.jj_scanpos = xsp;
                        if (jj_3R_65()) {
                            SimGrid.jj_scanpos = xsp;
                            if (jj_3R_66()) {
                                SimGrid.jj_scanpos = xsp;
                                if (jj_3R_67()) {
                                    SimGrid.jj_scanpos = xsp;
                                    if (jj_3R_68()) {
                                        SimGrid.jj_scanpos = xsp;
                                        if (jj_3R_69()) {
                                            SimGrid.jj_scanpos = xsp;
                                            if (jj_3R_70()) {
                                                SimGrid.jj_scanpos = xsp;
                                                return jj_3R_71();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean jj_3R_53 () {
        return jj_scan_token(SimGridConstants.apostrofo);
    }

    private static boolean jj_3R_52 () {
        return jj_scan_token(SimGridConstants.igual);
    }

    private static boolean jj_3_8 () {
        return jj_3R_13();
    }

    private static boolean jj_3R_51 () {
        return jj_scan_token(SimGridConstants.maior);
    }

    private static boolean jj_3R_50 () {
        return jj_scan_token(SimGridConstants.menor);
    }

    private static boolean jj_3R_49 () {
        return jj_scan_token(SimGridConstants.interrogacao);
    }

    private static boolean jj_3R_48 () {
        return jj_scan_token(SimGridConstants.exclamacao);
    }

    private static boolean jj_3R_47 () {
        return jj_scan_token(SimGridConstants.barra);
    }

    private static boolean jj_3R_46 () {
        return jj_scan_token(SimGridConstants.ponto);
    }

    private static boolean jj_3R_43 () {
        return jj_scan_token(SimGridConstants.letra);
    }

    private static boolean jj_3R_45 () {
        return jj_scan_token(SimGridConstants.especiais);
    }

    private static boolean jj_3_34 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.ROUTE_ELEMENT);
    }

    private static boolean jj_3_6 () {
        return jj_3R_13();
    }

    private static boolean jj_3R_44 () {
        return jj_scan_token(SimGridConstants.digito);
    }

    private static boolean jj_3R_39 () {
        return jj_3R_13();
    }

    private static boolean jj_3R_41 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.PROCESS);
    }

    private static boolean jj_3_4 () {
        return jj_3R_13();
    }

    private static boolean jj_3R_37 () {
        return jj_3R_13();
    }

    private static boolean jj_3_13 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_31 () {
        var xsp = SimGrid.jj_scanpos;
        if (jj_3R_43()) {
            SimGrid.jj_scanpos = xsp;
            if (jj_3R_44()) {
                SimGrid.jj_scanpos = xsp;
                if (jj_3R_45()) {
                    SimGrid.jj_scanpos = xsp;
                    if (jj_3R_46()) {
                        SimGrid.jj_scanpos = xsp;
                        if (jj_3R_47()) {
                            SimGrid.jj_scanpos = xsp;
                            if (jj_3R_48()) {
                                SimGrid.jj_scanpos = xsp;
                                if (jj_3R_49()) {
                                    SimGrid.jj_scanpos = xsp;
                                    if (jj_3R_50()) {
                                        SimGrid.jj_scanpos = xsp;
                                        if (jj_3R_51()) {
                                            SimGrid.jj_scanpos = xsp;
                                            if (jj_3R_52()) {
                                                SimGrid.jj_scanpos = xsp;
                                                if (jj_3R_53()) {
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        while (true) {
            xsp = SimGrid.jj_scanpos;
            if (jj_3R_54()) {
                SimGrid.jj_scanpos = xsp;
                break;
            }
        }
        return false;
    }

    private static boolean jj_3_2 () {
        return jj_3R_13();
    }

    private static boolean jj_3R_35 () {
        return jj_3R_13();
    }

    private static boolean jj_3_11 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_33 () {
        return jj_3R_13();
    }

    private static boolean jj_3_9 () {
        return jj_3R_12();
    }

    private static boolean jj_3_27 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.ARGUMENT);
    }

    private static boolean jj_3_26 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.ARGUMENT)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.VALUE)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_21()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.barra)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.maior)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.ARGUMENT)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.VALUE)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_22()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.barra)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.maior)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.ARGUMENT)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.VALUE)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_23()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.barra)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.maior)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.ARGUMENT)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.VALUE)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_24()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.barra)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.maior);
    }

    private static boolean jj_3_7 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_77 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.PROCESS);
    }

    private static boolean jj_3_5 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_24 () {
        final var xsp = SimGrid.jj_scanpos;
        if (jj_3R_38()) {
            SimGrid.jj_scanpos = xsp;
            return jj_3R_39();
        }
        return false;
    }

    private static boolean jj_3R_38 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_19 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.ROUTE);
    }

    private static boolean jj_3_3 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_23 () {
        final var xsp = SimGrid.jj_scanpos;
        if (jj_3R_36()) {
            SimGrid.jj_scanpos = xsp;
            return jj_3R_37();
        }
        return false;
    }

    private static boolean jj_3R_36 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_76 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.PROCESS);
    }

    private static boolean jj_3_1 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_22 () {
        final var xsp = SimGrid.jj_scanpos;
        if (jj_3R_34()) {
            SimGrid.jj_scanpos = xsp;
            return jj_3R_35();
        }
        return false;
    }

    private static boolean jj_3R_34 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_75 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.PROCESS);
    }

    private static boolean jj_3R_21 () {
        final var xsp = SimGrid.jj_scanpos;
        if (jj_3R_32()) {
            SimGrid.jj_scanpos = xsp;
            return jj_3R_33();
        }
        return false;
    }

    private static boolean jj_3R_32 () {
        return jj_3R_12();
    }

    private static boolean jj_3R_40 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.PROCESS);
    }

    private static boolean jj_3_25 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.HOST)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_20()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        final var xsp = SimGrid.jj_scanpos;
        if (jj_scan_token(19)) {
            SimGrid.jj_scanpos = xsp;
            if (jj_scan_token(20)) {
                SimGrid.jj_scanpos = xsp;
                if (jj_scan_token(21)) {
                    SimGrid.jj_scanpos = xsp;
                    if (jj_scan_token(22)) {
                        SimGrid.jj_scanpos = xsp;
                        if (jj_scan_token(23)) {
                            SimGrid.jj_scanpos = xsp;
                            return jj_scan_token(24);
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean jj_3_24 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.SLAVE);
    }

    private static boolean jj_3_23 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.HOST)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_20()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.SLAVE);
    }

    private static boolean jj_3_22 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.MASTER);
    }

    private static boolean jj_3_21 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.PROCESS)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.HOST)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_3R_20()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.FUNCTION)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.igual)) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.aspas)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.MASTER);
    }

    private static boolean jj_3_20 () {
        return jj_3R_19();
    }

    private static boolean jj_3R_30 () {
        return jj_3R_42();
    }

    private static boolean jj_3R_29 () {
        return jj_3R_41();
    }

    private static boolean jj_3R_74 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.PROCESS);
    }

    private static boolean jj_3R_28 () {
        return jj_3R_41();
    }

    private static boolean jj_3R_27 () {
        return jj_3R_40();
    }

    private static boolean jj_3R_15 () {
        final var xsp = SimGrid.jj_scanpos;
        if (jj_3R_26()) {
            SimGrid.jj_scanpos = xsp;
            if (jj_3R_27()) {
                SimGrid.jj_scanpos = xsp;
                if (jj_3R_28()) {
                    SimGrid.jj_scanpos = xsp;
                    if (jj_3R_29()) {
                        SimGrid.jj_scanpos = xsp;
                        return jj_3R_30();
                    }
                }
            }
        }
        return false;
    }

    private static boolean jj_3R_26 () {
        return jj_3R_40();
    }

    private static boolean jj_3R_12 () {
        if (jj_3R_13()) {
            return true;
        }
        if (jj_scan_token(SimGridConstants.ponto)) {
            return true;
        }
        return jj_3R_13();
    }

    private static boolean jj_3R_73 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.PROCESS);
    }

    private static boolean jj_3R_25 () {
        return jj_scan_token(SimGridConstants.digito);
    }

    private static boolean jj_3_36 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.ARGUMENT);
    }

    private static boolean jj_3_19 () {
        return jj_3R_18();
    }

    private static boolean jj_3R_13 () {
        if (jj_3R_25()) {
            return true;
        }
        while (true) {
            final var xsp = SimGrid.jj_scanpos;
            if (jj_3R_25()) {
                SimGrid.jj_scanpos = xsp;
                break;
            }
        }
        return false;
    }

    private static boolean jj_3R_18 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.NETWORK_LINK);
    }

    private static boolean jj_3R_72 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.PROCESS);
    }

    private static boolean jj_3_35 () {
        if (jj_scan_token(SimGridConstants.menor)) {
            return true;
        }
        return jj_scan_token(SimGridConstants.ARGUMENT);
    }

    /**
     * Reinitialise.
     */
    public static void ReInit (final InputStream stream) {
        ReInit(stream, null);
    }

    /**
     * Reinitialise.
     */
    private static void ReInit (final InputStream stream, final String encoding) {
        try {
            SimGrid.jj_input_stream.ReInit(stream, encoding, 1, 1);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        SimGridTokenManager.ReInit(SimGrid.jj_input_stream);
        SimGrid.token  = new Token();
        SimGrid.jj_ntk = -1;
        SimGrid.jj_gen = 0;
        for (var i = 0; i < 22; i++) {
            SimGrid.jj_la1[i] = -1;
        }
        for (var i = 0; i < SimGrid.jj_2_rtns.length; i++) {
            SimGrid.jj_2_rtns[i] = new JJCalls();
        }
    }

    private static Token jj_consume_token (final int kind)
        throws ParseException {
        final Token oldToken;
        if ((oldToken = SimGrid.token).next != null) {
            SimGrid.token = SimGrid.token.next;
        } else {
            SimGrid.token = SimGrid.token.next = SimGridTokenManager.getNextToken();
        }
        SimGrid.jj_ntk = -1;
        if (SimGrid.token.kind == kind) {
            SimGrid.jj_gen++;
            ++SimGrid.jj_gc;
            if (SimGrid.jj_gc > 100) {
                SimGrid.jj_gc = 0;
                for (var i = 0; i < SimGrid.jj_2_rtns.length; i++) {
                    var c = SimGrid.jj_2_rtns[i];
                    while (c != null) {
                        if (c.gen < SimGrid.jj_gen) {
                            c.first = null;
                        }
                        c = c.next;
                    }
                }
            }
            return SimGrid.token;
        }
        SimGrid.token   = oldToken;
        SimGrid.jj_kind = kind;
        throw generateParseException();
    }

    private static boolean jj_scan_token (final int kind) {
        if (SimGrid.jj_scanpos == SimGrid.jj_lastpos) {
            SimGrid.jj_la--;
            if (SimGrid.jj_scanpos.next == null) {
                SimGrid.jj_lastpos      = SimGrid.jj_scanpos =
                SimGrid.jj_scanpos.next = SimGridTokenManager.getNextToken();
            } else {
                SimGrid.jj_lastpos = SimGrid.jj_scanpos = SimGrid.jj_scanpos.next;
            }
        } else {
            SimGrid.jj_scanpos = SimGrid.jj_scanpos.next;
        }
        if (SimGrid.jj_rescan) {
            var i   = 0;
            var tok = SimGrid.token;
            while (tok != null && tok != SimGrid.jj_scanpos) {
                i++;
                tok = tok.next;
            }
            if (tok != null) {
                jj_add_error_token(kind, i);
            }
        }
        if (SimGrid.jj_scanpos.kind != kind) {
            return true;
        }
        if (SimGrid.jj_la == 0 && SimGrid.jj_scanpos == SimGrid.jj_lastpos) {
            throw SimGrid.jj_ls;
        }
        return false;
    }

    /**
     * Get the next Token.
     */
    private static Token getNextToken () {
        if (SimGrid.token.next != null) {
            SimGrid.token = SimGrid.token.next;
        } else {
            SimGrid.token = SimGrid.token.next = SimGridTokenManager.getNextToken();
        }
        SimGrid.jj_ntk = -1;
        SimGrid.jj_gen++;
        return SimGrid.token;
    }

    /**
     * Get the specific Token.
     */
    private static Token getToken (final int index) {
        var t = SimGrid.token;
        for (var i = 0; i < index; i++) {
            if (t.next != null) {
                t = t.next;
            } else {
                t = t.next = SimGridTokenManager.getNextToken();
            }
        }
        return t;
    }

    private static int jj_ntk () {
        if ((SimGrid.jj_nt = SimGrid.token.next) == null) {
            return (
                SimGrid.jj_ntk = (SimGrid.token.next = SimGridTokenManager.getNextToken()).kind
            );
        } else {
            return (SimGrid.jj_ntk = SimGrid.jj_nt.kind);
        }
    }

    private static void jj_add_error_token (final int kind, final int pos) {
        if (pos >= 100) {
            return;
        }
        if (pos == SimGrid.jj_endpos + 1) {
            SimGrid.jj_lasttokens[SimGrid.jj_endpos] = kind;
            SimGrid.jj_endpos++;
        } else if (SimGrid.jj_endpos != 0) {
            SimGrid.jj_expentry = new int[SimGrid.jj_endpos];
            System.arraycopy(SimGrid.jj_lasttokens, 0, SimGrid.jj_expentry, 0, SimGrid.jj_endpos);
            jj_entries_loop:
            for (Iterator<?> it = SimGrid.jj_expentries.iterator(); it.hasNext(); ) {
                final var oldentry = (int[]) (it.next());
                if (oldentry.length == SimGrid.jj_expentry.length) {
                    for (var i = 0; i < SimGrid.jj_expentry.length; i++) {
                        if (oldentry[i] != SimGrid.jj_expentry[i]) {
                            continue jj_entries_loop;
                        }
                    }
                    SimGrid.jj_expentries.add(SimGrid.jj_expentry);
                    break;
                }
            }
            if (pos != 0) {
                SimGrid.jj_lasttokens[(SimGrid.jj_endpos = pos) - 1] = kind;
            }
        }
    }

    /**
     * Generate ParseException.
     */
    public static ParseException generateParseException () {
        SimGrid.jj_expentries.clear();
        final var la1tokens = new boolean[48];
        if (SimGrid.jj_kind >= 0) {
            la1tokens[SimGrid.jj_kind] = true;
            SimGrid.jj_kind            = -1;
        }
        for (var i = 0; i < 22; i++) {
            if (SimGrid.jj_la1[i] == SimGrid.jj_gen) {
                for (var j = 0; j < 32; j++) {
                    if ((SimGrid.jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((SimGrid.jj_la1_1[i] & (1 << j)) != 0) {
                        la1tokens[32 + j] = true;
                    }
                }
            }
        }
        for (var i = 0; i < 48; i++) {
            if (la1tokens[i]) {
                SimGrid.jj_expentry    = new int[1];
                SimGrid.jj_expentry[0] = i;
                SimGrid.jj_expentries.add(SimGrid.jj_expentry);
            }
        }
        SimGrid.jj_endpos = 0;
        jj_rescan_token();
        jj_add_error_token(0, 0);
        final var exptokseq = new int[SimGrid.jj_expentries.size()][];
        for (var i = 0; i < SimGrid.jj_expentries.size(); i++) {
            exptokseq[i] = SimGrid.jj_expentries.get(i);
        }
        return new ParseException(SimGrid.token, exptokseq, SimGridConstants.tokenImage);
    }

    private static void jj_rescan_token () {
        SimGrid.jj_rescan = true;
        for (var i = 0; i < 36; i++) {
            try {
                var p = SimGrid.jj_2_rtns[i];
                do {
                    if (p.gen > SimGrid.jj_gen) {
                        SimGrid.jj_la      = p.arg;
                        SimGrid.jj_lastpos = SimGrid.jj_scanpos = p.first;
                        switch (i) {
                            case 0:
                                jj_3_1();
                                break;
                            case 1:
                                jj_3_2();
                                break;
                            case 2:
                                jj_3_3();
                                break;
                            case 3:
                                jj_3_4();
                                break;
                            case 4:
                                jj_3_5();
                                break;
                            case 5:
                                jj_3_6();
                                break;
                            case 6:
                                jj_3_7();
                                break;
                            case 7:
                                jj_3_8();
                                break;
                            case 8:
                                jj_3_9();
                                break;
                            case 9:
                                jj_3_10();
                                break;
                            case 10:
                                jj_3_11();
                                break;
                            case 11:
                                jj_3_12();
                                break;
                            case 12:
                                jj_3_13();
                                break;
                            case 13:
                                jj_3_14();
                                break;
                            case 14:
                                jj_3_15();
                                break;
                            case 15:
                                jj_3_16();
                                break;
                            case 16:
                                jj_3_17();
                                break;
                            case 17:
                                jj_3_18();
                                break;
                            case 18:
                                jj_3_19();
                                break;
                            case 19:
                                jj_3_20();
                                break;
                            case 20:
                                jj_3_21();
                                break;
                            case 21:
                                jj_3_22();
                                break;
                            case 22:
                                jj_3_23();
                                break;
                            case 23:
                                jj_3_24();
                                break;
                            case 24:
                                jj_3_25();
                                break;
                            case 25:
                                jj_3_26();
                                break;
                            case 26:
                                jj_3_27();
                                break;
                            case 27:
                                jj_3_28();
                                break;
                            case 28:
                                jj_3_29();
                                break;
                            case 29:
                                jj_3_30();
                                break;
                            case 30:
                                jj_3_31();
                                break;
                            case 31:
                                jj_3_32();
                                break;
                            case 32:
                                jj_3_33();
                                break;
                            case 33:
                                jj_3_34();
                                break;
                            case 34:
                                jj_3_35();
                                break;
                            case 35:
                                jj_3_36();
                                break;
                        }
                    }
                    p = p.next;
                } while (p != null);
            } catch (final LookaheadSuccess ls) {
            }
        }
        SimGrid.jj_rescan = false;
    }

    private static void jj_save (final int index, final int xla) {
        var p = SimGrid.jj_2_rtns[index];
        while (p.gen > SimGrid.jj_gen) {
            if (p.next == null) {
                p = p.next = new JJCalls();
                break;
            }
            p = p.next;
        }
        p.gen   = SimGrid.jj_gen + xla - SimGrid.jj_la;
        p.first = SimGrid.token;
        p.arg   = xla;
    }

    public boolean resultadoParser () {
        if (SimGridTokenManager.contaErrosLex() > 0
            || this.contaErrosSint() > 0
            || this.contaErrosSem() > 0) {
            this.addErro("Foram encontrados "
                         + SimGridTokenManager.contaErrosLex()
                         + " erros l\u00e9xicos.");
            this.addErro("Foram encontrados "
                         + this.contaErrosSint()
                         + " erros sint\u00e1ticos.");
            this.addErro("Foram encontrados "
                         + this.contaErrosSem()
                         + " erros sem\u00e2nticos.");
            if (SimGridTokenManager.contaErrosLex() > 0) {
                this.addErro(SimGridTokenManager.getErrosLex());
            }
            if (this.contaErrosSint() > 0) {
                this.addErro(this.getErrosSint());
            }
            if (this.contaErrosSem() > 0) {
                this.addErro(this.getErrosSem());
            }
            JOptionPane.showMessageDialog(null, this.erros, "Erro!", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showOptionDialog(
                null,
                "Modelo SimGrid reconhecido com sucesso.",
                "Modelo SimGrid reconhecido",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null
            );
            return false;
        }
        return true;
    }

    public IconicoXML getModelo () {
        final var  xml      = new IconicoXML();
        final List usuarios = new ArrayList();
        usuarios.add("user1");
        final Map<String, Double> perfil = new HashMap<>();
        perfil.put("user1", 100.0);
        xml.addUsers(usuarios, perfil);
        final var x      = 100;
        final var y      = 100;
        var       local  = 0;
        var       global = 0;
        final var maq    = new HashMap<String, Integer>();
        for (final var s : SimGrid.servers) {
            maq.put(s.getID(), global);
            xml.addMachine(
                x, y, local, global, s.getID(),
                Double.valueOf(s.getPower()), 0.0,
                "---", "user1",
                1, 0.0, 0.0,
                false, null, 0.0
            );
            local++;
            global++;
        }
        for (final var r : SimGrid.routes) {
            if (r.isInternet()) {
                maq.put(r.getID(), global);
                xml.addInternet(x, y, local, global, r.getID(),
                                Double.valueOf(r.getBand()), 0, Double.valueOf(r.getLatency())
                );
                local++;
                global++;
            }
        }
        for (final var m : SimGrid.masters) {
            maq.put(m.getID(), global);
            local++;
            global++;
        }
        for (final var m : SimGrid.masters) {
            final Collection<Integer> slv = new ArrayList<>();
            for (final var escravo : m.getEscravos()) {
                slv.add(maq.get(escravo));
            }
            xml.addMachine(
                x, y, maq.get(m.getID()), maq.get(m.getID()), m.getID(),
                Double.valueOf(m.getPower()), 0.0, "RoundRobin",
                "user1", 1, 0.0, 0.0, true, slv, 0.0
            );
        }
        for (final var r : SimGrid.routes) {
            if (!r.isInternet()) {
                xml.addLink(x, y, x, y, local, global, r.getID(),
                            Double.valueOf(r.getBand()), 0, Double.valueOf(r.getLatency()),
                            maq.get(r.getSrc()), maq.get(r.getDst())
                );
                local++;
                global++;
            }
        }
        for (final var m : SimGrid.masters) {
            xml.addLoadNo("app0", "user1", m.getID(), Integer.valueOf(m.getNumtarefas()),
                          Double.valueOf(m.getMaxcomp()), Double.valueOf(m.getMincomp()),
                          Double.valueOf(m.getMaxcomm()), Double.valueOf(m.getMincomm())
            );
        }
        return xml;
    }

    private int contaErrosSint () {
        return SimGrid.contaErrosSint;
    }

    private int contaErrosSem () {
        return SimGrid.contaErrosSem;
    }

    private void addErro (final String msg) {
        this.erros = this.erros + msg + "\u005cn";
    }

    private String getErrosSint () {
        return SimGrid.errosSint + "\u005cn";
    }

    private String getErrosSem () {
        return SimGrid.errosSem + "\u005cn";
    }

    public void reset () {
        Formatter file = null;
        SimGrid.net            = 0;
        SimGrid.contaErrosSint = 0;
        SimGrid.contaErrosSem  = 0;
        SimGrid.errosSint      = "\u005cnErros sint\u00e1ticos:\u005cn";
        SimGrid.errosSem       = "\u005cnErros sem\u00e2nticos:\u005cn";
        this.erros             = "";
        SimGrid.masters        = new ArrayList<>();
        SimGrid.servers        = new ArrayList<>();
        SimGrid.networks       = new ArrayList<>();
        SimGrid.routes         = new ArrayList<>();
        SimGridTokenManager.reset();
    }

    private static class Server {

        private String id = "";

        private String power = "0.0";

        public String getID () {
            return this.id;
        }

        public void setID (final String id) {
            this.id = id;
        }

        public String getPower () {
            return this.power;
        }

        public void setPower (final String power) {
            this.power = power;
        }
    }

    private static class Master extends Server {

        private String numtarefas = "0";

        private String maxcomp = "0.0";

        private String mincomp = "0.0";

        private String maxcomm = "0.0";

        private String mincomm = "0.0";

        private List<String> slaves = new ArrayList<>();

        private void setSlaves (final List<String> slaves) {
            this.slaves = slaves;
        }

        private void setCarga (
            final String numtarefas,
            final String maxcomp,
            final String mincomp,
            final String maxcomm,
            final String mincomm
        ) {
            this.numtarefas = numtarefas;
            this.maxcomp    = maxcomp;
            this.mincomp    = mincomp;
            this.maxcomm    = maxcomm;
            this.mincomm    = mincomm;
        }

        private void setCarga (
            final String numtarefas,
            final String maxcomp,
            final String maxcomm
        ) {
            this.numtarefas = numtarefas;
            this.maxcomp    = maxcomp;
            this.maxcomm    = maxcomm;
        }

        private List<String> getEscravos () {
            return this.slaves;
        }

        private String getNumtarefas () {
            return this.numtarefas;
        }

        private String getMaxcomp () {
            return this.maxcomp;
        }

        private String getMincomp () {
            return this.mincomp;
        }

        private String getMaxcomm () {
            return this.maxcomm;
        }

        private String getMincomm () {
            return this.mincomm;
        }
    }

    private static class Network {

        private String id;

        private String band;

        private String latency;

        private Network () {
            this.id      = "";
            this.band    = "0.0";
            this.latency = "0.0";
        }

        public void setAtributos (final String id, final String band, final String latency) {
            this.id      = id;
            this.band    = band;
            this.latency = latency;
        }

        public String getID () {
            return this.id;
        }

        public String getBand () {
            return this.band;
        }

        public String getLatency () {
            return this.latency;
        }
    }

    private static class Route extends Network {

        private int net;

        private String src;

        private String dst;

        private boolean internet;

        private Route () {
            super();
            this.net      = 0;
            this.src      = "";
            this.dst      = "";
            this.internet = false;
        }

        private boolean isInternet () {
            return this.internet;
        }

        private void setInternet (final boolean internet) {
            this.internet = internet;
        }

        private void setRoute (final int net, final String src, final String dst) {
            this.net = net;
            this.src = src;
            this.dst = dst;
        }

        private int getNet () {
            return this.net;
        }

        private String getSrc () {
            return this.src;
        }

        private String getDst () {
            return this.dst;
        }
    }

    private static final class LookaheadSuccess extends Error {

    }

    private static final class JJCalls {

        private int gen = 0;

        private Token first = null;

        private int arg = 0;

        private JJCalls next = null;
    }
}

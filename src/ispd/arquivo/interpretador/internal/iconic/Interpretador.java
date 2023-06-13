package ispd.arquivo.interpretador.internal.iconic;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.swing.JOptionPane;

public class Interpretador {

    private static final int[] jj_la1_0 =
        new int[] { 0x4164, 0x4164, 0x2400, 0x200000, 0x4008a, 0x200000, 0x10a00, 0xa1000, };

    private final int[] jj_la1 = new int[8];

    private final Collection<DescreveIconePrivado> listaIcones = new HashSet<>();

    private final Collection<String> listaNomes = new HashSet<>();

    private final Collection<String> listaEscravos = new HashSet<>();

    private final Collection<String> listaConexoes = new HashSet<>();

    private final List<String> escravos2 = new ArrayList<>();

    private final List<int[]> jj_expentries = new ArrayList<>();

    private boolean verbose = false;

    private boolean erroEncontrado = false;

    private InterpretadorTokenManager token_source;

    private Token token = new Token();

    private Token jj_nt = null;

    private SimpleCharStream jj_input_stream;

    private String textoVerbose = "Saida do Verbose:";

    private String algoritmo = null;

    private String erros = "Erros encontrados durante o parser do Modelo Iconico:";

    private Integer cargasTipoConfiguracao = -1;

    private String cargasConfiguracao = "";

    private int numeroConexoes = 0;

    private int jj_ntk = -1;

    private int jj_gen = 0;

    private int jj_kind = -1;

    /**
     * Constructor with InputStream.
     */
    public Interpretador (final InputStream stream) {
        this(stream, null);
    }

    /**
     * Constructor with InputStream and supplied encoding.
     */
    private Interpretador (final InputStream stream, final String encoding) {
        try {
            this.jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        this.token_source = new InterpretadorTokenManager(this.jj_input_stream);
        Arrays.fill(this.jj_la1, -1);
    }

    public void printv (final String msg) {
        this.textoVerbose = this.textoVerbose + "\u005cn>" + msg;
    }

    private void addErro (final String msg) {
        this.erros = this.erros + "\u005cn" + msg;
    }

    private void resuladoParser () {
        if (this.erroEncontrado) {
            final var saida = new MostraSaida("Found Errors", this.erros);
            saida.setVisible(true);
        } else {
            if (this.verbose) {
                final var saida = new MostraSaida("Saida do Reconhecimento", this.textoVerbose);
                saida.setVisible(true);
            }
        }
    }

    private void verificaLabels () {
        for (final var temp : this.listaEscravos) {
            if (!this.listaNomes.contains(temp)) {
                this.erroEncontrado = true;
                this.addErro("\u005c'" + temp + "\u005c' does not exist!");
            }
        }
        for (final var temp : this.listaConexoes) {
            if (!this.listaNomes.contains(temp)) {
                this.erroEncontrado = true;
                this.addErro("\u005c'" + temp + "\u005c' does not exist!");
            }
        }
    }

    public void escreveArquivo () {
        try {
            final var file   = new File("modelosimulavel");
            final var writer = new FileWriter(file, StandardCharsets.UTF_8);
            final var saida  = new PrintWriter(writer, true);
            saida.println("MODELO");
            saida.println("TAREFA");
            switch (this.cargasTipoConfiguracao) {
                case 0:
                    saida.println("RANDOM " + this.cargasConfiguracao);
                    break;
                case 1:
                    saida.println("MAQUINA " + this.cargasConfiguracao);
                    break;
                case 2:
                    saida.println("TRACE " + this.cargasConfiguracao);
                    break;
            }
            saida.println("FIM_TAREFA");
            saida.println("CENTROS_DE_SERVICOS");
            for (final var icone : this.listaIcones) {
                switch (icone.getTipoIcone()) {
                    case 1:
                        if (icone.getMestre()) {
                            saida.printf(
                                "CS_0 %s 1 1 FILAS fila_%s SERVIDORES serv_%s 0 %f %f ",
                                icone.getNome(),
                                icone.getNome(),
                                icone.getNome(),
                                icone.getPoderComputacional(),
                                icone.getTaxaOcupacao()
                            );
                            saida.print("MESTRE " + icone.getAlgoritmo() + " LMAQ ");
                            final var nos = icone.getEscravos();
                            for (final var no : nos) {
                                saida.print(no + " ");
                            }
                            saida.println("");
                        } else {
                            saida.printf(
                                "CS_0 %s 1 1 FILAS fila_%s SERVIDORES serv_%s 0 %f %f ",
                                icone.getNome(),
                                icone.getNome(),
                                icone.getNome(),
                                icone.getPoderComputacional(),
                                icone.getTaxaOcupacao()
                            );
                            saida.print("ESCRAVO");
                            saida.println("");
                        }
                        break;
                    case 2:
                        saida.printf(
                            "CS_2 %s 1 1 FILAS fila_%s SERVIDORES serv_%s 1 %f %f %f\u005cn",
                            icone.getNome(),
                            icone.getNome(),
                            icone.getNome(),
                            icone.getBanda(),
                            icone.getTaxaOcupacao(),
                            icone.getLatencia()
                        );
                        break;
                    case 4:
                        saida.printf(
                            "CS_3 %s 1 1 FILAS fila_%s SERVIDORES serv_%s 1 %f %f %f\u005cn",
                            icone.getNome(),
                            icone.getNome(),
                            icone.getNome(),
                            icone.getBanda(),
                            icone.getTaxaOcupacao(),
                            icone.getLatencia()
                        );
                        break;
                    case 3:
                        saida.print("CS_1 "
                                    + icone.getNome()
                                    + " 2 "
                                    + icone.getNumeroEscravos()
                                    + " "
                                    + icone.getAlgoritmo()
                                    + " FILAS fila_0_"
                                    + icone.getNome()
                                    + " fila_1_"
                                    + icone.getNome()
                                    + " SERVIDORES ");
                        saida.printf(
                            "serv_%s 0 %f %f %f ",
                            icone.getNome(),
                            icone.getPoderComputacional(),
                            icone.getBanda(),
                            icone.getLatencia()
                        );
                        saida.println("");
                        break;
                }
            }
            saida.println("FIM_CENTROS_DE_SERVICOS");
            if (this.numeroConexoes > 0) {
                saida.println("CONEXOES");
                for (final var icone : this.listaIcones) {
                    if (icone.getTipoIcone() == 2) {
                        saida.printf("%s\u005ct%s\u005cn", icone.getSNoOrigem(), icone.getNome());
                        saida.printf("%s\u005ct%s\u005cn", icone.getNome(), icone.getSNoDestino());
                    }
                }
                saida.println("FIM_CONEXOES");
            }
            saida.println("FIM_MODELO");
            saida.close();
            writer.close();
        }
        // em caso de erro apresenta mensagem abaixo
        catch (final Exception e) {
            JOptionPane.showMessageDialog(
                null,
                e.getMessage(),
                "Warning",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void consomeTokens () {
        var t = this.getToken(1);
        while (t.kind != InterpretadorConstants.MAQ
               && t.kind != InterpretadorConstants.REDE
               && t.kind != InterpretadorConstants.INET
               && t.kind != InterpretadorConstants.CLUSTER
               && t.kind != InterpretadorConstants.CARGA
               && t.kind != InterpretadorConstants.EOF) {
            this.getNextToken();
            t = this.getToken(1);
        }
    }

    private void resetaObjetosParser () {
        this.textoVerbose   = "";
        this.erroEncontrado = false;
        this.listaIcones.clear();
        this.listaNomes.clear();
        this.listaEscravos.clear();
        this.listaConexoes.clear();
        this.escravos2.clear();
        this.cargasTipoConfiguracao = -1;
        this.cargasConfiguracao     = "";
        this.numeroConexoes         = 0;
    }

    public final void Modelo ()
        throws ParseException {
        this.resetaObjetosParser();
        try {
            this.Icones();
            this.jj_consume_token(0);
            this.printv("Reconheceu Modelo()");

            this.verificaLabels();

            this.resuladoParser();
        } catch (final ParseException e) {
            final var t = this.getToken(1);
            this.addErro("Erro semantico encontrado na linha "
                         + t.endLine
                         + ", coluna "
                         + t.endColumn);
            this.erroEncontrado = true;
            this.consomeTokens();
            this.resuladoParser();
        }
    }

    private final void Icones ()
        throws ParseException {
        label_1:
        while (true) {
            this.Icone();
            switch (this.getAToken()) {
                case InterpretadorConstants.CLUSTER,
                    InterpretadorConstants.REDE,
                    InterpretadorConstants.INET,
                    InterpretadorConstants.CARGA,
                    InterpretadorConstants.MAQ:
                    break;
                default:
                    this.jj_la1[0] = this.jj_gen;
                    break label_1;
            }
        }
        this.printv("Reconheceu Icones");
    }

    private int getAToken () {
        return (this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk;
    }

    private final void Icone ()
        throws ParseException {
        switch (this.getAToken()) {
            case InterpretadorConstants.MAQ:
                this.No();
                this.printv("Reconheceu No()");
                break;
            case InterpretadorConstants.CLUSTER:
                this.Cluster();
                this.printv("Reconheceu Cluster()");
                break;
            case InterpretadorConstants.REDE:
                this.Link();
                this.printv("Reconheceu Link()");
                break;
            case InterpretadorConstants.INET:
                this.Inet();
                this.printv("Reconheceu Inet()");
                break;
            case InterpretadorConstants.CARGA:
                this.Carga();
                this.printv("Reconheceu Carga()");
                break;
            default:
                this.jj_la1[1] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final boolean NoTipo ()
        throws ParseException {
        switch (this.getAToken()) {
            case InterpretadorConstants.MESTRE: {
                this.jj_consume_token(InterpretadorConstants.MESTRE);
                this.algoritmo = this.ClusterAlg();
                this.jj_consume_token(InterpretadorConstants.LMAQ);
                this.NoLista();
                return true;
            }
            case InterpretadorConstants.ESCRAVO: {
                this.jj_consume_token(InterpretadorConstants.ESCRAVO);
                return false;
            }
            default: {
                this.jj_la1[2] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    private final void NoLista ()
        throws ParseException {
        this.escravos2.clear();
        while (true) {
            final var t = this.jj_consume_token(InterpretadorConstants.nome);
            final var s = t.image;
            this.escravos2.add(s);
            this.listaEscravos.add(s);
            this.printv("Reconheceu nome no escravo");
            if (this.getAToken() != InterpretadorConstants.nome) {
                this.jj_la1[3] = this.jj_gen;
                break;
            }
        }
    }

    public final void No ()
        throws ParseException {
        final var icone = new DescreveIconePrivado();
        try {
            this.jj_consume_token(InterpretadorConstants.MAQ);
            final var     t2     = this.jj_consume_token(InterpretadorConstants.nome);
            final var     t3     = this.jj_consume_token(InterpretadorConstants.flutuante);
            final var     t4     = this.jj_consume_token(InterpretadorConstants.flutuante);
            final Boolean mestre = this.NoTipo();
            if (this.listaNomes.contains(t2.image)) {
                this.addErro("There's already an icon named \u005c'" + t2.image + "\u005c'.");
                this.erroEncontrado = true;
            } else {
                this.listaNomes.add(t2.image);
            }

            if (mestre) {
                icone.setAtributosNo(
                    1,
                    t2.image,
                    Double.parseDouble(t3.image),
                    Double.parseDouble(t4.image),
                    true,
                    this.algoritmo,
                    this.escravos2
                );
                this.listaIcones.add(icone);

                this.algoritmo = null;
            } else {
                final List<String> escravos = new ArrayList<>();
                icone.setAtributosNo(
                    1,
                    t2.image,
                    Double.parseDouble(t3.image),
                    Double.parseDouble(t4.image),
                    false,
                    this.algoritmo,
                    escravos
                );
                this.listaIcones.add(icone);
            }
        } catch (final ParseException e) {
            final var t = this.getToken(1);
            this.addErro("Erro semantico encontrado na linha "
                         + t.endLine
                         + ", coluna "
                         + t.endColumn);
            this.erroEncontrado = true;
            this.consomeTokens();
        }
    }

    public final void Link ()
        throws ParseException {
        final var icone = new DescreveIconePrivado();
        try {
            this.jj_consume_token(InterpretadorConstants.REDE);
            final var t2 = this.jj_consume_token(InterpretadorConstants.nome);
            final var t3 = this.jj_consume_token(InterpretadorConstants.flutuante);
            final var t4 = this.jj_consume_token(InterpretadorConstants.flutuante);
            final var t5 = this.jj_consume_token(InterpretadorConstants.flutuante);
            this.jj_consume_token(InterpretadorConstants.CONECTA);
            final var t6 = this.jj_consume_token(InterpretadorConstants.nome);
            final var t7 = this.jj_consume_token(InterpretadorConstants.nome);
            if (this.listaNomes.contains(t2.image)) {
                this.addErro("There's already an icon named \u005c'" + t2.image + "\u005c'.");
                this.erroEncontrado = true;
            } else {
                this.listaNomes.add(t2.image);
            }

            this.listaConexoes.add(t6.image);
            this.listaConexoes.add(t7.image);
            icone.setAtributosLink(
                2,
                t2.image,
                Double.parseDouble(t5.image),
                Double.parseDouble(t4.image),
                Double.parseDouble(t3.image),
                t6.image,
                t7.image
            );
            this.listaIcones.add(icone);

            this.numeroConexoes++;
        } catch (final ParseException e) {
            final var t = this.getToken(1);
            this.addErro("Erro semantico encontrado na linha "
                         + t.endLine
                         + ", coluna "
                         + t.endColumn);
            this.erroEncontrado = true;
            this.consomeTokens();
        }
    }

    private final String ClusterAlg ()
        throws ParseException {
        final Token  t;
        final String s;
        switch (this.getAToken()) {
            case InterpretadorConstants.RR:
                t = this.jj_consume_token(InterpretadorConstants.RR);
                s = t.image;
            {
                if (true) {
                    return s;
                }
            }
            break;
            case InterpretadorConstants.WORKQUEUE:
                t = this.jj_consume_token(InterpretadorConstants.WORKQUEUE);
                s = t.image;
            {
                if (true) {
                    return s;
                }
            }
            break;
            case InterpretadorConstants.FPLTF:
                t = this.jj_consume_token(InterpretadorConstants.FPLTF);
                s = t.image;
            {
                if (true) {
                    return s;
                }
            }
            break;
            case InterpretadorConstants.VAZIO:
                t = this.jj_consume_token(InterpretadorConstants.VAZIO);
                s = t.image;
            {
                if (true) {
                    return s;
                }
            }
            break;
            default:
                this.jj_la1[4] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
        throw new Error("Missing return statement in function");
    }

    public final void Cluster ()
        throws ParseException {
        final var icone = new DescreveIconePrivado();
        try {
            this.jj_consume_token(InterpretadorConstants.CLUSTER);
            final var t2 = this.jj_consume_token(InterpretadorConstants.nome);
            final var t3 = this.jj_consume_token(InterpretadorConstants.inteiro);
            final var t4 = this.jj_consume_token(InterpretadorConstants.flutuante);
            final var t5 = this.jj_consume_token(InterpretadorConstants.flutuante);
            final var t6 = this.jj_consume_token(InterpretadorConstants.flutuante);
            final var s  = this.ClusterAlg();
            if (this.listaNomes.contains(t2.image)) {
                this.addErro("There's already an icon named \u005c'" + t2.image + "\u005c'.");
                this.erroEncontrado = true;
            } else {
                this.listaNomes.add(t2.image);
            }
            icone.setAtributosCluster(
                3,
                t2.image,
                Double.parseDouble(t4.image),
                Double.parseDouble(t6.image),
                Double.parseDouble(t5.image),
                s,
                Integer.parseInt(t3.image)
            );
            this.listaIcones.add(icone);
        } catch (final ParseException e) {
            final var t = this.getToken(1);
            this.addErro("Erro semantico encontrado na linha "
                         + t.endLine
                         + ", coluna "
                         + t.endColumn);
            this.erroEncontrado = true;
            this.consomeTokens();
        }
    }

    private final void Inet () {
        final var icone = new DescreveIconePrivado();
        try {
            this.jj_consume_token(InterpretadorConstants.INET);
            final var t2 = this.jj_consume_token(InterpretadorConstants.nome);
            final var t3 = this.jj_consume_token(InterpretadorConstants.flutuante);
            final var t4 = this.jj_consume_token(InterpretadorConstants.flutuante);
            final var t5 = this.jj_consume_token(InterpretadorConstants.flutuante);
            if (this.listaNomes.contains(t2.image)) {
                this.addErro("There's already an icon named \u005c'" + t2.image + "\u005c'.");
                this.erroEncontrado = true;
            } else {
                this.listaNomes.add(t2.image);
            }
            icone.setAtributosInet(
                4,
                t2.image,
                Double.parseDouble(t5.image),
                Double.parseDouble(t4.image),
                Double.parseDouble(t3.image)
            );
            this.listaIcones.add(icone);
        } catch (final ParseException e) {
            final var t = this.getToken(1);
            this.addErro("Erro semantico encontrado na linha "
                         + t.endLine
                         + ", coluna "
                         + t.endColumn);
            this.erroEncontrado = true;
            this.consomeTokens();
        }
    }

    private final void Carga () {
        try {
            this.jj_consume_token(InterpretadorConstants.CARGA);
            this.TipoCarga();
        } catch (final ParseException e) {
            final var t = this.getToken(1);
            this.addErro("Erro semantico encontrado na linha "
                         + t.endLine
                         + ", coluna "
                         + t.endColumn);
            this.erroEncontrado = true;
            this.consomeTokens();
        }
    }

    private final void TipoCarga ()
        throws ParseException {
        switch (this.getAToken()) {
            case InterpretadorConstants.RANDOM:
                this.jj_consume_token(InterpretadorConstants.RANDOM);
                this.TipoRandom();
                break;
            case InterpretadorConstants.MAQUINA:
                this.jj_consume_token(InterpretadorConstants.MAQUINA);
                while (true) {
                    this.TipoMaquina();
                    if (this.getAToken()
                        == InterpretadorConstants.nome) {
                    } else {
                        this.jj_la1[5] = this.jj_gen;
                        break;
                    }
                }
                break;
            case InterpretadorConstants.TRACE:
                this.jj_consume_token(InterpretadorConstants.TRACE);
                this.TipoTrace();
                break;
            default:
                this.jj_la1[6] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final void TipoRandom ()
        throws ParseException {
        String dist;
        this.cargasTipoConfiguracao = 0;
        final var t1  = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t2  = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t3  = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t4  = this.jj_consume_token(InterpretadorConstants.flutuante);
        final var t5  = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t6  = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t7  = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t8  = this.jj_consume_token(InterpretadorConstants.flutuante);
        final var t9  = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t10 = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t11 = this.jj_consume_token(InterpretadorConstants.inteiro);
        this.cargasConfiguracao = t1.image
                                  + " "
                                  + t2.image
                                  + " "
                                  + t3.image
                                  + " "
                                  + t4.image
                                  + "\u005cn"
                                  + t5.image
                                  + " "
                                  + t6.image
                                  + " "
                                  + t7.image
                                  + " "
                                  + t8.image
                                  + "\u005cn"
                                  + t9.image
                                  + " "
                                  + t10.image
                                  + " "
                                  + t11.image;
    }

    private final void TipoMaquina ()
        throws ParseException {
        String dist;
        this.cargasTipoConfiguracao = 1;
        final var t1 = this.jj_consume_token(InterpretadorConstants.nome);
        final var t2 = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t3 = this.jj_consume_token(InterpretadorConstants.flutuante);
        final var t4 = this.jj_consume_token(InterpretadorConstants.flutuante);
        final var t5 = this.jj_consume_token(InterpretadorConstants.flutuante);
        final var t6 = this.jj_consume_token(InterpretadorConstants.flutuante);
        this.cargasConfiguracao += t1.image
                                   + " "
                                   + t2.image
                                   + " "
                                   + t3.image
                                   + " "
                                   + t4.image
                                   + " "
                                   + t5.image
                                   + " "
                                   + t6.image
                                   + "\u005cn";
    }

    private final void TipoTrace ()
        throws ParseException {
        this.cargasTipoConfiguracao = 2;
        final var t = this.jj_consume_token(InterpretadorConstants.nome);
        this.cargasConfiguracao = t.image + "\u005cn";
    }

    private Token jj_consume_token (final int kind)
        throws ParseException {
        final Token oldToken;
        if ((oldToken = this.token).next != null) {
            this.token = this.token.next;
        } else {
            this.token = this.token.next = this.token_source.getNextToken();
        }
        this.jj_ntk = -1;
        if (this.token.kind == kind) {
            this.jj_gen++;
            return this.token;
        }
        this.token   = oldToken;
        this.jj_kind = kind;
        throw this.generateParseException();
    }

    /**
     * Get the next Token.
     */
    private final Token getNextToken () {
        if (this.token.next != null) {
            this.token = this.token.next;
        } else {
            this.token = this.token.next = this.token_source.getNextToken();
        }
        this.jj_ntk = -1;
        this.jj_gen++;
        return this.token;
    }

    /**
     * Get the specific Token.
     */
    private final Token getToken (final int index) {
        var t = this.token;
        for (var i = 0; i < index; i++) {
            if (t.next != null) {
                t = t.next;
            } else {
                t = t.next = this.token_source.getNextToken();
            }
        }
        return t;
    }

    private int jj_ntk () {
        if ((this.jj_nt = this.token.next) == null) {
            return (this.jj_ntk = (this.token.next = this.token_source.getNextToken()).kind);
        } else {
            return (this.jj_ntk = this.jj_nt.kind);
        }
    }

    /**
     * Generate ParseException.
     */
    private ParseException generateParseException () {
        this.jj_expentries.clear();
        final var la1tokens = new boolean[30];
        if (this.jj_kind >= 0) {
            la1tokens[this.jj_kind] = true;
            this.jj_kind            = -1;
        }
        for (var i = 0; i < 8; i++) {
            if (this.jj_la1[i] == this.jj_gen) {
                for (var j = 0; j < 32; j++) {
                    if ((Interpretador.jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                }
            }
        }
        for (var i = 0; i < 30; i++) {
            if (la1tokens[i]) {
                final var jj_expentry = new int[1];
                jj_expentry[0] = i;
                this.jj_expentries.add(jj_expentry);
            }
        }
        final var exptokseq = new int[this.jj_expentries.size()][];
        for (var i = 0; i < this.jj_expentries.size(); i++) {
            exptokseq[i] = this.jj_expentries.get(i);
        }
        return new ParseException(this.token, exptokseq, InterpretadorConstants.tokenImage);
    }

    public void setVerbose (final boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isErroEncontrado () {
        return this.erroEncontrado;
    }

    public void setErroEncontrado (final boolean erroEncontrado) {
        this.erroEncontrado = erroEncontrado;
    }

    private class DescreveIconePrivado {

        private int tipoIcone = 0;

        private String nome = null;

        private double poderComputacional = 0.0;

        private double taxaOcupacao = 0.0;

        private double latencia = 0.0;

        private double banda = 0.0;

        private int numeroEscravos = 0;

        private boolean mestre = false;

        private String algoritmoEscalonamento = null;

        private List<String> escravos = null;

        private String snoOrigem = null;

        private String snoDestino = null;

        private void setAtributosNo (
            final int tipoIcone,
            final String nome,
            final double poderComputacional,
            final double taxaOcupacao,
            final boolean mestre,
            final String algoritmoEscalonamento,
            final List<String> escravos
        ) {

            this.tipoIcone              = tipoIcone;
            this.nome                   = nome;
            this.poderComputacional     = poderComputacional;
            this.taxaOcupacao           = taxaOcupacao;
            this.latencia               = 0.0;
            this.banda                  = 0.0;
            this.mestre                 = mestre;
            this.escravos               = escravos;
            this.algoritmoEscalonamento = algoritmoEscalonamento;
            this.numeroEscravos         = 0;
            this.snoOrigem              = null;
            this.snoDestino             = null;
        }

        private void setAtributosLink (
            final int tipoIcone,
            final String nome,
            final double taxaOcupacao,
            final double latencia,
            final double banda,
            final String snoOrigem,
            final String snoDestino
        ) {

            this.tipoIcone              = tipoIcone;
            this.nome                   = nome;
            this.poderComputacional     = 0.0;
            this.taxaOcupacao           = taxaOcupacao;
            this.latencia               = latencia;
            this.banda                  = banda;
            this.mestre                 = false;
            this.escravos               = new ArrayList<>();
            this.algoritmoEscalonamento = null;
            this.numeroEscravos         = 0;
            this.snoOrigem              = snoOrigem;
            this.snoDestino             = snoDestino;
        }

        private void setAtributosCluster (
            final int tipoIcone,
            final String nome,
            final double poderComputacional,
            final double latencia,
            final double banda,
            final String algoritmoEscalonamento,
            final int numeroEscravos
        ) {

            this.tipoIcone              = tipoIcone;
            this.nome                   = nome;
            this.poderComputacional     = poderComputacional;
            this.taxaOcupacao           = 0.0;
            this.latencia               = latencia;
            this.banda                  = banda;
            this.mestre                 = false;
            this.escravos               = new ArrayList<>();
            this.algoritmoEscalonamento = algoritmoEscalonamento;
            this.numeroEscravos         = numeroEscravos;
            this.snoOrigem              = null;
            this.snoDestino             = null;
        }

        private void setAtributosInet (
            final int tipoIcone,
            final String nome,
            final double taxaOcupacao,
            final double latencia,
            final double banda
        ) {

            this.tipoIcone              = tipoIcone;
            this.nome                   = nome;
            this.poderComputacional     = 0.0;
            this.taxaOcupacao           = taxaOcupacao;
            this.latencia               = latencia;
            this.banda                  = banda;
            this.mestre                 = false;
            this.escravos               = new ArrayList<>();
            this.algoritmoEscalonamento = null;
            this.numeroEscravos         = 0;
            this.snoOrigem              = null;
            this.snoDestino             = null;
        }

        private int getTipoIcone () {
            return this.tipoIcone;
        }

        private String getNome () {
            return this.nome;
        }

        private double getPoderComputacional () {
            return this.poderComputacional;
        }

        private double getTaxaOcupacao () {
            return this.taxaOcupacao;
        }

        private double getLatencia () {
            return this.latencia;
        }

        private double getBanda () {
            return this.banda;
        }

        private boolean getMestre () {
            return this.mestre;
        }

        private List<String> getEscravos () {
            return this.escravos;
        }

        private String getAlgoritmo () {
            return this.algoritmoEscalonamento;
        }

        private int getNumeroEscravos () {
            return this.numeroEscravos;
        }

        private String getSNoOrigem () {
            return this.snoOrigem;
        }

        private String getSNoDestino () {
            return this.snoDestino;
        }
    }
}

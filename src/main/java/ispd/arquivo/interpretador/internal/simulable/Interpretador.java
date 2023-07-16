package ispd.arquivo.interpretador.internal.simulable;

import ispd.motor.queueNetworks.RedesDeFilas;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Interpretador {

    private static final int[] jj_la1_0 = new int[] {
        0x2022000,
        0x2022000,
        0x10000000,
        0x10900,
        0x844000,
        0x3c,
        0x3c,
        0x10000000,
        0x10000000,
        0x8400,
        0x10000000,
        0x10000000,
    };

    private static final int[] jj_la1_1 = new int[] {
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
    };

    private final int[] jj_la1 = new int[12];

    private final String char253 = String.valueOf((char) 253);

    private final String char252 = String.valueOf((char) 252);

    private final String char254 = String.valueOf((char) 254);

    private final List<int[]> jj_expentries = new ArrayList<>();

    private final SimpleCharStream jj_input_stream;

    private boolean verbose = false;

    private boolean erroEncontrado = false;

    /**
     * Generated Token Manager.
     */
    private InterpretadorTokenManager token_source;

    /**
     * Current token.
     */
    private Token token = new Token();

    private String textoVerbose = "Saida do Verbose:";

    private String erros = "Erros Encontrados durante o parser do Modelo Simulavel:";

    private RedesDeFilas redeFilas = new RedesDeFilas();

    private String tarefas = "";

    private int idCs = 0;

    private List<String> listaCS = new ArrayList<>();

    private Collection<String> listaConecta = new ArrayList<>();

    private Collection<Escravos> listaEscravos = new ArrayList<>();

    private int jj_ntk = -1;

    private int jj_gen = 0;

    private int jj_kind = -1;

    public Interpretador (final InputStream stream) {
        this(stream, null);
    }

    /**
     * Constructor with InputStream and supplied encoding
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

    private void consomeTokens () {
        var t = this.getToken(1);
        while (t.kind != InterpretadorConstants.CS0
               && t.kind != InterpretadorConstants.CS1
               && t.kind != InterpretadorConstants.CS2
               && t.kind != InterpretadorConstants.CS3
               && t.kind != InterpretadorConstants.TAREFA
               && t.kind != InterpretadorConstants.CENTROS_DE_SERVICOS
               && t.kind != InterpretadorConstants.CONEXOES
               && t.kind != InterpretadorConstants.FILAS
               && t.kind != InterpretadorConstants.SERVIDORES
               && t.kind != InterpretadorConstants.EOF) {
            if (this.token.next != null) {
                this.token = this.token.next;
            } else {
                this.token = this.token.next = this.token_source.getNextToken();
            }
            this.jj_ntk = -1;
            this.jj_gen++;
            t = this.getToken(1);
        }
    }

    private void resetaObjetosParser () {
        this.textoVerbose   = "";
        this.erroEncontrado = false;
        this.redeFilas      = new RedesDeFilas();
        this.listaCS        = new ArrayList<>();
        this.listaConecta   = new ArrayList<>();
        this.listaEscravos  = new ArrayList<>();
    }

    private void converterEscravos () {
        this.addErro("Durante convers\u00c3\u00a3o de escravos:");
        for (final var temp : this.listaEscravos) {
            final var id       = temp.getNumCs();
            final var maquinas = temp.getMaquinas();
            final var maqInt   = new int[maquinas.size()];
            var       posicao  = 0;
            for (final var tempMaq : maquinas) {
                if (!this.listaCS.contains(tempMaq)) {
                    this.addErro("Nome de centro de servi\u00c3\u00a7o n\u00c3\u00a3o encontrado: "
                                 + tempMaq);
                    this.erroEncontrado = true;
                }
                maqInt[posicao] = (this.listaCS.indexOf(tempMaq));
                posicao++;
            }
            this.redeFilas.setVetorEscravos(maqInt, id);
        }
    }

    private void constroiMatrizRedeFilas () {
        this.redeFilas.instanciaMatrizVetor(this.listaConecta.size());
        for (final var temp : this.listaConecta) {
            final var lista = temp.split(this.char254);
            if (!this.listaCS.contains(lista[0])) {
                this.addErro("Nome de centro de servi\u00c3\u00a7o n\u00c3\u00a3o encontrado: "
                             + lista[0]);
                this.erroEncontrado = true;
            }
            if (!this.listaCS.contains(lista[1])) {
                this.addErro("Nome de centro de servi\u00c3\u00a7o n\u00c3\u00a3o encontrado: "
                             + lista[1]);
                this.erroEncontrado = true;
            }
            this.redeFilas.inteligaCSs(
                this.listaCS.indexOf(lista[0]),
                this.listaCS.indexOf(lista[1])
            );
        }
    }

    private void converterTarefas () {
        this.addErro("Durante convers\u00c3\u00a3o de tarefas:");
        final var vetor = this.tarefas.split(this.char252);
        if (Integer.parseInt(vetor[0]) == 1) {
            final var vetor2  = vetor[1].split(this.char254);
            final var tamanho = vetor2.length;
            for (var i = 0; i < tamanho; i++) {
                final var vetor3 = vetor2[i].split(this.char253);
                if (!this.listaCS.contains(vetor3[0])) {
                    this.addErro("Nome de centro de servi\u00c3\u00a7o n\u00c3\u00a3o encontrado: "
                                 + vetor3[0]);
                    this.erroEncontrado = true;
                }
                vetor3[0] = String.valueOf(this.listaCS.indexOf(vetor3[0]));
                vetor2[i] = vetor3[0]
                            + this.char253
                            + vetor3[1]
                            + this.char253
                            + vetor3[2]
                            + this.char253
                            + vetor3[3]
                            + this.char253
                            + vetor3[4]
                            + this.char253
                            + vetor3[5]
                            + this.char253
                            + vetor3[6]
                            + this.char253
                            + vetor3[7];
            }
            vetor[1] = "";
            for (var i = 0; i < tamanho; i++) {
                vetor[1] = vetor[1] + vetor2[i] + this.char254;
            }
        }
        this.tarefas = vetor[0] + this.char252 + vetor[1];
        if (Integer.parseInt(vetor[0]) == 0) {
            final var vetor2 = vetor[1].split(this.char253);
            this.tarefas += this.char252 + vetor2[10];
        }
    }

    public String getTarefas () {
        return this.tarefas;
    }

    public final void Modelo ()
        throws ParseException {
        this.resetaObjetosParser();
        try {
            this.jj_consume_token(InterpretadorConstants.MODELO);
            label_1:
            while (true) {
                this.PartesModelo();
                switch (this.getAToken()) {
                    case InterpretadorConstants.TAREFA:
                    case InterpretadorConstants.CONEXOES:
                    case InterpretadorConstants.CENTROS_DE_SERVICOS:
                        break;
                    default:
                        this.jj_la1[0] = this.jj_gen;
                        break label_1;
                }
            }
            this.jj_consume_token(InterpretadorConstants.FIM_MODELO);
            this.jj_consume_token(0);
            this.printv("Reconheceu Modelo()");
            this.converterEscravos();
            this.converterTarefas();
            this.constroiMatrizRedeFilas();
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

    private final void PartesModelo ()
        throws ParseException {
        switch (this.getAToken()) {
            case InterpretadorConstants.TAREFA:
                this.Tarefa();
                this.printv("Reconheceu Tarefa");
                break;
            case InterpretadorConstants.CENTROS_DE_SERVICOS:
                this.Centros();
                this.printv("Reconheceu Centros");
                break;
            case InterpretadorConstants.CONEXOES:
                this.Conexao();
                this.printv("Reconheceu Conexao");
                break;
            default:
                this.jj_la1[1] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    public final void Tarefa ()
        throws ParseException {
        try {
            this.jj_consume_token(InterpretadorConstants.TAREFA);
            this.TipoCarga();
            this.jj_consume_token(InterpretadorConstants.FIM_TAREFA);
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
                this.tarefas = "0" + this.char252;
                this.TipoRandom();
                break;
            case InterpretadorConstants.MAQUINA:
                this.jj_consume_token(InterpretadorConstants.MAQUINA);
                this.tarefas = "1" + this.char252;
                while (true) {
                    this.TipoMaquina();
                    if (this.getAToken()
                        == InterpretadorConstants.nome) {
                    } else {
                        this.jj_la1[2] = this.jj_gen;
                        break;
                    }
                }
                break;
            case InterpretadorConstants.TRACE:
                this.jj_consume_token(InterpretadorConstants.TRACE);
                this.tarefas = "2" + this.char252;
                this.TipoTrace();
                break;
            default:
                this.jj_la1[3] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final void TipoRandom ()
        throws ParseException {
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
        this.tarefas = this.tarefas
                       + t1.image
                       + this.char253
                       + t2.image
                       + this.char253
                       + t3.image
                       + this.char253
                       + t4.image
                       + this.char253
                       + t5.image
                       + this.char253
                       + t6.image
                       + this.char253
                       + t7.image
                       + this.char253
                       + t8.image
                       + this.char253
                       + t9.image
                       + this.char253
                       + t10.image
                       + this.char253
                       + t11.image;
    }

    private final void TipoMaquina ()
        throws ParseException {
        final var t1        = this.jj_consume_token(InterpretadorConstants.nome);
        final var t2        = this.jj_consume_token(InterpretadorConstants.inteiro);
        final var t3        = this.jj_consume_token(InterpretadorConstants.flutuante);
        final var t4        = this.jj_consume_token(InterpretadorConstants.flutuante);
        final var t5        = this.jj_consume_token(InterpretadorConstants.flutuante);
        final var t6        = this.jj_consume_token(InterpretadorConstants.flutuante);
        final var minComp   = Double.parseDouble(t3.image);
        final var maxComp   = Double.parseDouble(t4.image);
        final var minComc   = Double.parseDouble(t5.image);
        final var maxComc   = Double.parseDouble(t6.image);
        final var mediaComp = (minComp + maxComp) / 2;
        final var mediaComc = (minComc + maxComc) / 2;
        this.tarefas = this.tarefas
                       + t1.image
                       + this.char253
                       + t2.image
                       + this.char253
                       + minComp
                       + this.char253
                       + maxComp
                       + this.char253
                       + mediaComp
                       + this.char253
                       + minComc
                       + this.char253
                       + maxComc
                       + this.char253
                       + mediaComc
                       + this.char254;
    }

    private final void TipoTrace ()
        throws ParseException {
        this.jj_consume_token(InterpretadorConstants.nome);
    }

    private final void Centros () {
        try {
            this.jj_consume_token(InterpretadorConstants.CENTROS_DE_SERVICOS);
            label_3:
            while (true) {
                this.CentrosServ();
                switch (this.getAToken()) {
                    case InterpretadorConstants.CS0:
                    case InterpretadorConstants.CS1:
                    case InterpretadorConstants.CS2:
                    case InterpretadorConstants.CS3:
                        break;
                    default:
                        this.jj_la1[5] = this.jj_gen;
                        break label_3;
                }
            }
            this.jj_consume_token(InterpretadorConstants.FIM_CENTROS_DE_SERVICOS);
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

    private final void CentrosServ ()
        throws ParseException {
        try {
            final var   temp = new int[] { -1 };
            final Token t1;
            final Token t3;
            switch (this.getAToken()) {
                case InterpretadorConstants.CS0:
                    this.jj_consume_token(InterpretadorConstants.CS0);
                    t1 = this.jj_consume_token(InterpretadorConstants.nome);
                    this.jj_consume_token(InterpretadorConstants.inteiro);
                    t3 = this.jj_consume_token(InterpretadorConstants.inteiro);
                    this.idCs =
                        this.redeFilas.adicionaCentroServico(0, Integer.parseInt(t3.image), temp);
                    if (this.listaCS.contains(t1.image)) {
                        this.addErro("Nome de centro de servi\u00c3\u00a7o duplicado na linha "
                                     + t1.endLine
                                     + ", coluna "
                                     + t1.endColumn);
                        this.erroEncontrado = true;
                    }
                    this.listaCS.add(this.idCs, t1.image);
                    this.Filas();
                    this.Servidores0();

                    break;
                case InterpretadorConstants.CS1:
                    this.jj_consume_token(InterpretadorConstants.CS1);
                    t1 = this.jj_consume_token(InterpretadorConstants.nome);
                    this.jj_consume_token(InterpretadorConstants.inteiro);
                    t3 = this.jj_consume_token(InterpretadorConstants.inteiro);
                    this.jj_consume_token(InterpretadorConstants.nome);
                    this.idCs =
                        this.redeFilas.adicionaCentroServico(1, Integer.parseInt(t3.image), temp);
                    if (this.listaCS.contains(t1.image)) {
                        this.addErro("Nome de centro de servi\u00c3\u00a7o duplicado na linha "
                                     + t1.endLine
                                     + ", coluna "
                                     + t1.endColumn);
                        this.erroEncontrado = true;
                    }
                    this.listaCS.add(this.idCs, t1.image);
                    this.Filas();
                    this.Servidores1();

                    break;
                case InterpretadorConstants.CS2:
                    this.jj_consume_token(InterpretadorConstants.CS2);
                    t1 = this.jj_consume_token(InterpretadorConstants.nome);
                    this.jj_consume_token(InterpretadorConstants.inteiro);
                    t3 = this.jj_consume_token(InterpretadorConstants.inteiro);
                    this.idCs = this.redeFilas.adicionaCentroServico(2, Integer.parseInt(t3.image));
                    if (this.listaCS.contains(t1.image)) {
                        this.addErro("Nome de centro de servi\u00c3\u00a7o duplicado na linha "
                                     + t1.endLine
                                     + ", coluna "
                                     + t1.endColumn);
                        this.erroEncontrado = true;
                    }
                    this.listaCS.add(this.idCs, t1.image);
                    this.Filas();
                    this.Servidores();

                    break;
                case InterpretadorConstants.CS3:
                    this.jj_consume_token(InterpretadorConstants.CS3);
                    t1 = this.jj_consume_token(InterpretadorConstants.nome);
                    this.jj_consume_token(InterpretadorConstants.inteiro);
                    t3 = this.jj_consume_token(InterpretadorConstants.inteiro);
                    this.idCs = this.redeFilas.adicionaCentroServico(3, Integer.parseInt(t3.image));
                    if (this.listaCS.contains(t1.image)) {
                        this.addErro("Nome de centro de servi\u00c3\u00a7o duplicado na linha "
                                     + t1.endLine
                                     + ", coluna "
                                     + t1.endColumn);
                        this.erroEncontrado = true;
                    }
                    this.listaCS.add(this.idCs, t1.image);
                    this.Filas();
                    this.Servidores();

                    break;
                default:
                    this.jj_la1[6] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
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

    private final void Filas ()
        throws ParseException {
        try {
            this.jj_consume_token(InterpretadorConstants.FILAS);
            while (true) {
                this.jj_consume_token(InterpretadorConstants.nome);
                this.redeFilas.adicionaFila(this.idCs);
                if (this.getAToken()
                    == InterpretadorConstants.nome) {
                } else {
                    this.jj_la1[7] = this.jj_gen;
                    break;
                }
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

    private final void Servidores0 ()
        throws ParseException {
        try {
            this.jj_consume_token(InterpretadorConstants.SERVIDORES);
            this.jj_consume_token(InterpretadorConstants.nome);
            this.jj_consume_token(InterpretadorConstants.inteiro);
            this.jj_consume_token(InterpretadorConstants.flutuante);
            this.jj_consume_token(InterpretadorConstants.flutuante);
            final var mestre = this.Mestre();
            this.redeFilas.adicionaServidorProcto(this.idCs, mestre);
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

    private final void Servidores ()
        throws ParseException {
        try {
            this.jj_consume_token(InterpretadorConstants.SERVIDORES);
            while (true) {
                this.jj_consume_token(InterpretadorConstants.nome);
                this.jj_consume_token(InterpretadorConstants.inteiro);
                this.jj_consume_token(InterpretadorConstants.flutuante);
                this.jj_consume_token(InterpretadorConstants.flutuante);
                this.jj_consume_token(InterpretadorConstants.flutuante);
                this.redeFilas.adicionaServidorCom(this.idCs);
                if (this.getAToken() != InterpretadorConstants.nome) {
                    this.jj_la1[8] = this.jj_gen;
                    break;
                }
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

    private int getAToken () {
        return (this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk;
    }

    private final void Servidores1 ()
        throws ParseException {
        try {
            this.jj_consume_token(InterpretadorConstants.SERVIDORES);
            this.jj_consume_token(InterpretadorConstants.nome);
            this.jj_consume_token(InterpretadorConstants.inteiro);
            this.jj_consume_token(InterpretadorConstants.flutuante);
            this.jj_consume_token(InterpretadorConstants.flutuante);
            this.jj_consume_token(InterpretadorConstants.flutuante);
            this.redeFilas.adicionaServidoresClr(this.idCs);
        } catch (final ParseException ignored) {
            final var t = this.getToken(1);
            this.addErro("Erro semantico encontrado na linha "
                         + t.endLine
                         + ", coluna "
                         + t.endColumn);
            this.erroEncontrado = true;
            this.consomeTokens();
        }
    }

    private final boolean Mestre ()
        throws ParseException {
        try {
            switch (this.getAToken()) {
                case InterpretadorConstants.MESTRE: {
                    this.jj_consume_token(InterpretadorConstants.MESTRE);
                    this.jj_consume_token(InterpretadorConstants.nome);
                    this.jj_consume_token(InterpretadorConstants.LMAQ);
                    this.NomesEscravos();
                    return true;
                }
                case InterpretadorConstants.ESCRAVO: {
                    this.jj_consume_token(InterpretadorConstants.ESCRAVO);
                    return false;
                }
                default:
                    this.jj_la1[9] = this.jj_gen;
                    this.jj_consume_token(-1);
                    throw new ParseException();
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
        throw new Error("Missing return statement in function");
    }

    private final void NomesEscravos ()
        throws ParseException {
        final List<String> temp = new ArrayList<>();
        while (true) {
            final var t = this.jj_consume_token(InterpretadorConstants.nome);
            temp.add(t.image);
            if (this.getAToken()
                == InterpretadorConstants.nome) {
            } else {
                this.jj_la1[10] = this.jj_gen;
                break;
            }
        }
        final var esc = new Escravos(this.idCs, temp);
        this.listaEscravos.add(esc);
    }

    private final void Conexao () {
        try {
            this.jj_consume_token(InterpretadorConstants.CONEXOES);
            while (true) {
                this.Conexoes();
                if (this.getAToken()
                    == InterpretadorConstants.nome) {
                } else {
                    this.jj_la1[11] = this.jj_gen;
                    break;
                }
            }
            this.jj_consume_token(InterpretadorConstants.FIM_CONEXOES);
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

    private final void Conexoes ()
        throws ParseException {
        try {
            final var t1 = this.jj_consume_token(InterpretadorConstants.nome);
            final var t2 = this.jj_consume_token(InterpretadorConstants.nome);
            this.listaConecta.add(t1.image + this.char254 + t2.image);
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
        final Token jj_nt = this.token.next;
        if (jj_nt != null) {
            this.jj_ntk = jj_nt.kind;
        } else {
            this.token.next = this.token_source.getNextToken();
            this.jj_ntk     = this.token.next.kind;
        }
        return this.jj_ntk;
    }

    /**
     * Generate ParseException.
     */
    private ParseException generateParseException () {
        this.jj_expentries.clear();
        final var la1tokens = new boolean[37];
        if (this.jj_kind >= 0) {
            la1tokens[this.jj_kind] = true;
            this.jj_kind            = -1;
        }
        for (var i = 0; i < 12; i++) {
            if (this.jj_la1[i] == this.jj_gen) {
                for (var j = 0; j < 32; j++) {
                    if ((Interpretador.jj_la1_0[i] & (1 << j)) != 0) {
                        la1tokens[j] = true;
                    }
                    if ((Interpretador.jj_la1_1[i] & (1 << j)) != 0) {
                        la1tokens[32 + j] = true;
                    }
                }
            }
        }
        for (var i = 0; i < 37; i++) {
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

    private class Escravos {

        private final int numCs;

        private final List<String> maquinas;

        private Escravos (final int numCs, final List<String> maquinas) {
            this.numCs    = numCs;
            this.maquinas = maquinas;
        }

        private int getNumCs () {
            return this.numCs;
        }

        private List<String> getMaquinas () {
            return this.maquinas;
        }
    }
}

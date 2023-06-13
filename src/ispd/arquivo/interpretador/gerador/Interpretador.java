package ispd.arquivo.interpretador.gerador;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class Interpretador {

    private static final int[] jj_la1_0 = {
        0x2,
        0x8000,
        0xc,
        0x24000,
        0xe0,
        0x0,
        0x110,
        0x3c00,
        0xc0000000,
        0x0,
        0x0,
        0x3ffc0000,
        0xc0000000,
        0x3ffc0000,
        0x0,
    };

    private static final int[] jj_la1_1 = {
        0x0,
        0x0,
        0x0,
        0x0,
        0x0,
        0x30,
        0x0,
        0x0,
        0x3,
        0x3,
        0x3,
        0x100004,
        0x3,
        0x0,
        0x30,
    };

    private final int[] jj_la1 = new int[15];

    private final String rota = """
                                @Override
                                public List<CentroServico> escalonarRota(CentroServico destino) {
                                    int index = escravos.indexOf(destino);
                                    return new ArrayList<CentroServico>((List<CentroServico>) caminhoEscravo.get(index));
                                }

                                """;

    private final List<int[]> jj_expentries = new ArrayList<>();

    private boolean verbose = false;

    private boolean erroEncontrado = false;

    private InterpretadorTokenManager token_source;

    private Token token;

    private Token jj_nt = null;

    private SimpleCharStream jj_input_stream = null;

    private String textoVerbose = "Saida do Verbose:";

    private String erros = "Erros encontrados durante o parser do Gerador:";

    private boolean dinamico = false;

    private boolean tarefaCrescente = true;

    private boolean recursoCrescente = true;

    private String arquivoNome = null;

    private String imports =
        """
        import ispd.policy.scheduling.grid.GridSchedulingPolicy;
        import ispd.motor.filas.Tarefa;
        import ispd.motor.filas.servidores.CS_Processamento;
        import ispd.motor.filas.servidores.CentroServico;
        import java.util.List;
        import java.util.ArrayList;

        """;

    private String declaracao = null;

    private String variavel = "private Tarefa tarefaSelecionada = null;\n";

    private String construtor = "";

    private String caracteristica = "";

    private String iniciar = "";

    private String tarefa = "";

    private String tarefaExpressao = "";

    private String declararVariaveisTarefa = "";

    private String carregarVariaveisTarefa = "";

    private String recurso = "";

    private String recursoExpressao = "";

    private String declararVariaveisRecurso = "";

    private String carregarVariaveisRecurso = "";

    private String escalonar =
        """
            tarefaSelecionada = escalonarTarefa();
            if(tarefaSelecionada != null){
        """;

    private String ifEscalonar =
        """
                CentroServico rec = escalonarRecurso();
                tarefaSelecionada.setLocalProcessamento(rec);
                tarefaSelecionada.setCaminho(escalonarRota(rec));
                mestre.sendTask(tarefaSelecionada);
        """;

    private String decAddTarefaConcluida = "";

    private String addTarefaConcluida = "";

    private String fimAddTarefaConcluida = "";

    private String adicionarTarefa = "";

    private String getTempoAtualizar = "";

    private String metodosPrivate = "";

    private int jj_ntk;

    private int jj_gen;

    private int jj_kind = -1;

    /**
     * Constructor with InputStream.
     */
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
        this.token        = new Token();
        this.jj_ntk       = -1;
        this.jj_gen       = 0;
        for (var i = 0; i < 15; i++) {
            this.jj_la1[i] = -1;
        }
    }

    private void resetaObjetosParser () {
        this.textoVerbose   = "";
        this.erroEncontrado = false;
    }

    public void printv (final String msg) {
        this.textoVerbose = this.textoVerbose + "\n>" + msg;
    }

    private void addErro (final String msg) {
        this.erros = this.erros + "\n" + msg;
    }

    private void resuladoParser () {
        if (this.erroEncontrado) {
            JOptionPane.showMessageDialog(null,
                                          this.erros, "Found Errors", JOptionPane.ERROR_MESSAGE
            );
        } else if (this.verbose) {
            JOptionPane.showMessageDialog(
                null,
                this.textoVerbose,
                "Saida do Reconhecimento",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void consomeTokens () {
        var t = this.getToken(1);
        while (t.kind != InterpretadorConstants.SCHEDULER
               && t.kind != InterpretadorConstants.STATIC
               && t.kind != InterpretadorConstants.DYNAMIC
               && t.kind != InterpretadorConstants.TASK
               && t.kind != InterpretadorConstants.RESOURCE
               && t.kind != InterpretadorConstants.EOF) {
            this.getNextToken();
            t = this.getToken(1);
        }
    }

    private void escreverNome (final String text) {
        this.arquivoNome = text;
        this.declaracao  = "public class " + text + " extends GridSchedulingPolicy{\n\n";
        this.construtor  = "public " + text + "() {\n"
                           + "    this.tarefas = new ArrayList<Tarefa>();\n"
                           + "    this.escravos = new ArrayList<CS_Processamento>();\n"
                           + "}\n\n";
    }

    private void estatico () {
        this.caracteristica = "";
        this.dinamico       = false;
    }

    private void dinamico (final String tipo) {
        if ("in".equals(tipo)) {
            this.adicionarTarefa = """
                                   @Override
                                   public void adicionarTarefa(Tarefa tarefa){
                                       super.adicionarTarefa(tarefa);
                                       for(CS_Processamento maq : this.getEscravos()){
                                           mestre.updateSubordinate(maq);
                                       }
                                   }

                                   """;
        }
        if ("out".equals(tipo)) {
            this.ifEscalonar = """
                                   for(CS_Processamento maq : this.getEscravos()){
                                       mestre.updateSubordinate(maq);
                                   }
                               """;
        }
        if ("end".equals(tipo)) {
            this.decAddTarefaConcluida = """
                                         @Override
                                         public void addTarefaConcluida(Tarefa tarefa) {
                                             super.addTarefaConcluida(tarefa);
                                         """;
            this.addTarefaConcluida += """
                                           for(CS_Processamento maq : this.getEscravos()){
                                               mestre.updateSubordinate(maq);
                                           }
                                       """;
            this.fimAddTarefaConcluida = "}\n\n";
        }
    }

    private void dinamicoIntervalo (final String text) {
        this.getTempoAtualizar = "@Override\n"
                                 + "public Double getTempoAtualizar(){\n"
                                 + "    return (double) " + text + ";\n"
                                 + "}\n\n";
    }

    private void formulaTarefa (final String valor) {
        if ("random".equals(valor)) {
            if (!this.imports.contains("import java.util.Random;")) {
                this.imports = "import java.util.Random;\n" + this.imports;
            }
            if (!this.variavel.contains("private Random sorteio = new Random();")) {
                this.variavel += "private Random sorteio = new Random();\n";
            }
            this.tarefa = """
                            if (!tarefas.isEmpty()) {
                                int tar = sorteio.nextInt(tarefas.size());
                                return tarefas.remove(tar);
                            }
                            return null;
                          """;
        } else if ("fifo".equals(valor)) {
            this.tarefa = """
                            if (!tarefas.isEmpty()) {
                                return tarefas.remove(0);
                            }
                            return null;
                          """;
        } else if ("formula".equals(valor)) {
            var ordenac = " < ";
            if (this.tarefaCrescente) {
                ordenac = " > ";
            }
            this.tarefa = "if(!tarefas.isEmpty()){\n"
                          + this.declararVariaveisTarefa
                          + "  double resultado = " + this.tarefaExpressao + ";\n"
                          + "  int tar = 0;\n"
                          + "  for(int i = 0; i < tarefas.size(); i++){\n"
                          + this.carregarVariaveisTarefa
                          + "    double expressao = " + this.tarefaExpressao + ";\n"
                          + "    if(resultado " + ordenac + " expressao){\n"
                          + "       resultado = expressao;\n"
                          + "       tar = i;\n"
                          + "    }\n"
                          + "  }\n"
                          + "return tarefas.remove(tar);\n"
                          + "}\n"
                          + "return null;\n";
        }
    }

    private void formulaRecurso (final String valor) {
        if ("random".equals(valor)) {
            if (!this.imports.contains("import java.util.Random;")) {
                this.imports = "import java.util.Random;\n" + this.imports;
            }
            if (!this.variavel.contains("private Random sorteio = new Random();")) {
                this.variavel += "Random sorteio = new Random();\n";
            }
            this.recurso = """
                             int rec = sorteio.nextInt(escravos.size());
                             return escravos.get(rec);
                           """;
        } else if ("fifo".equals(valor)) {
            if (!this.imports.contains("import java.util.ListIterator;")) {
                this.imports = "import java.util.ListIterator;\n" + this.imports;
            }
            if (!this.variavel.contains("private ListIterator<CS_Processamento> recursos;")) {
                this.variavel += "private ListIterator<CS_Processamento> recursos;\n";
            }
            if (!this.iniciar.contains("recursos = escravos.listIterator(0);")) {
                this.iniciar += "    recursos = escravos.listIterator(0);\n";
            }
            this.recurso = """
                             if(!escravos.isEmpty()){
                                 if (recursos.hasNext()) {
                                     return recursos.next();
                                 }else{
                                     recursos = escravos.listIterator(0);
                                     return recursos.next();
                                 }
                             }
                             return null;
                           """;
        } else if ("formula".equals(valor)) {
            var ordenac = " < ";
            if (this.recursoCrescente) {
                ordenac = " > ";
            }
            this.recurso = "if(!escravos.isEmpty()){\n"
                           + this.declararVariaveisRecurso
                           + "  double resultado = " + this.recursoExpressao + ";\n"
                           + "  int rec = 0;\n"
                           + "  for(int i = 0; i < escravos.size(); i++){\n"
                           + this.carregarVariaveisRecurso
                           + "    double expressao = " + this.recursoExpressao + ";\n"
                           + "    if(resultado " + ordenac + " expressao){\n"
                           + "       resultado = expressao;\n"
                           + "       rec = i;\n"
                           + "    }\n"
                           + "  }\n"
                           + "return escravos.get(rec);\n"
                           + "}\n"
                           + "return null;\n";
        }
    }

    private void addConstanteTarefa (final String valor) {
        this.tarefaExpressao += valor;
    }

    private void addConstanteRecurso (final String valor) {
        this.recursoExpressao += valor;
    }

    private void limite (final String valorInteiro, final boolean porRecurso) {
        if (porRecurso) {
            this.metodosPrivate += """
                                   private boolean condicoesEscalonamento() {
                                       int cont = 1;
                                       for (List tarefasNoRecurso : tarExecRec) {
                                           if (tarefasNoRecurso.size() > 1) {
                                               cont++;
                                           }
                                       }
                                       if(cont >= tarExecRec.size()){
                                           mestre.setSchedulingConditions(PolicyConditions.WHEN_RECEIVES_RESULT);
                                           return false;
                                       }
                                       mestre.setSchedulingConditions(PolicyConditions.WHILE_MUST_DISTRIBUTE);
                                       return true;
                                   }

                                   """;
            if (!this.variavel.contains("tarExecRec")) {
                this.variavel += "private List<List> tarExecRec;\n";
            }
            if (!this.iniciar.contains("tarExecRec")) {
                this.iniciar += """
                                    tarExecRec = new ArrayList<List>(escravos.size());
                                    for (int i = 0; i < escravos.size(); i++) {
                                        tarExecRec.add(new ArrayList<Tarefa>());
                                    }
                                """;
            }
            if (!this.metodosPrivate.contains("private void addTarefasEnviadas(){")) {
                this.metodosPrivate += """
                                       private void addTarefasEnviadas(){
                                           if(tarefaSelecionada != null){
                                               int index = escravos.indexOf(tarefaSelecionada.getLocalProcessamento());
                                               tarExecRec.get(index).add(tarefaSelecionada);
                                           }
                                       }

                                       """;
            }
            if (!this.escalonar.contains("if (condicoesEscalonamento())")) {
                this.escalonar = "tarefaSelecionada = null;\n"
                                 + "if (condicoesEscalonamento())\n"
                                 + this.escalonar;
            }
            if (!this.ifEscalonar.contains("addTarefasEnviadas();")) {
                this.ifEscalonar += "addTarefasEnviadas();\n";
            }
            this.decAddTarefaConcluida = """
                                         @Override
                                         public void addTarefaConcluida(Tarefa tarefa) {
                                             super.addTarefaConcluida(tarefa);
                                         """;
            this.addTarefaConcluida    = this.addTarefaConcluida
                                         + "    for (int i = 0; i < escravos.size(); i++) {\n"
                                         + "        if (tarExecRec.get(i).contains(tarefa)) {\n"
                                         + "            tarExecRec.get(i).remove(tarefa);\n"
                                         + "        }\n"
                                         + "    }\n";
            this.fimAddTarefaConcluida = "}\n\n";
        } else {
            this.metodosPrivate += "private boolean  condicoesEscalonamento() {\n"
                                   + "    int cont = 1;\n"
                                   + "    for (String usuario : metricaUsuarios.getUsuarios()) {\n"
                                   + "        if( (metricaUsuarios.getSizeTarefasSubmetidas(usuario) - metricaUsuarios.getSizeTarefasConcluidas(usuario) ) > "
                                   + valorInteiro
                                   + "){\n"
                                   + "            cont++;\n"
                                   + "        }\n"
                                   + "    }\n"
                                   + "    if(cont >= metricaUsuarios.getUsuarios().size()){\n"
                                   + "        mestre.setSchedulingConditions(PolicyConditions.WHEN_RECEIVES_RESULT);\n"
                                   + "        return false;\n"
                                   + "    }\n"
                                   + "    mestre.setSchedulingConditions(PolicyConditions.WHILE_MUST_DISTRIBUTE);\n"
                                   + "    return true;\n"
                                   + "}\n\n";
        }
    }

    private void addExpressaoTarefa (final int tipoToken) {
        switch (tipoToken) {
            case InterpretadorConstants.add:
                this.tarefaExpressao += " + ";
                break;
            case InterpretadorConstants.sub:
                this.tarefaExpressao += " - ";
                break;
            case InterpretadorConstants.div:
                this.tarefaExpressao += " / ";
                break;
            case InterpretadorConstants.mult:
                this.tarefaExpressao += " * ";
                break;
            case InterpretadorConstants.lparen:
                this.tarefaExpressao += " ( ";
                break;
            case InterpretadorConstants.rparen:
                this.tarefaExpressao += " ) ";
                break;
            case InterpretadorConstants.tTamComp:
                this.tarefaExpressao += "tTamComp";
                if (!this.declararVariaveisTarefa.contains("tTamComp")) {
                    this.declararVariaveisTarefa +=
                        "double tTamComp = tarefas.get(0).getTamProcessamento();\n";
                }
                if (!this.carregarVariaveisTarefa.contains("tTamComp")) {
                    this.carregarVariaveisTarefa +=
                        "tTamComp = tarefas.get(i).getTamProcessamento();\n";
                }
                break;
            case InterpretadorConstants.tTamComu:
                this.tarefaExpressao += "tTamComu";
                if (!this.declararVariaveisTarefa.contains("tTamComu")) {
                    this.declararVariaveisTarefa +=
                        "double tTamComu = tarefas.get(0).getTamComunicacao();\n";
                }
                if (!this.carregarVariaveisTarefa.contains("tTamComu")) {
                    this.carregarVariaveisTarefa +=
                        "tTamComu = tarefas.get(i).getTamComunicacao();\n";
                }
                break;
            case InterpretadorConstants.tTempSubm:
                this.tarefaExpressao += "tTempSubm";
                if (!this.declararVariaveisTarefa.contains("tTempSubm")) {
                    this.declararVariaveisTarefa +=
                        "double tTempSubm = tarefas.get(0).getTimeCriacao();\n";
                }
                if (!this.carregarVariaveisTarefa.contains("tTempSubm")) {
                    this.carregarVariaveisTarefa +=
                        "tTempSubm = tarefas.get(i).getTimeCriacao();\n";
                }
                break;
            case InterpretadorConstants.tNumTarSub:
                this.tarefaExpressao += "tNumTarSub";
                if (!this.declararVariaveisTarefa.contains("tNumTarSub")) {
                    if (this.dinamico) {
                        this.declararVariaveisTarefa +=
                            "int tNumTarSub = mestre.getSimulation().getRedeDeFilas().getMetricasUsuarios().getSizeTarefasSubmetidas(tarefas.get(0).getProprietario());\n";
                    } else {
                        this.declararVariaveisTarefa +=
                            "int tNumTarSub = metricaUsuarios.getSizeTarefasSubmetidas(tarefas.get(0).getProprietario());\n";
                    }
                }
                if (!this.carregarVariaveisTarefa.contains("tNumTarSub")) {
                    if (this.dinamico) {
                        this.carregarVariaveisTarefa +=
                            "tNumTarSub = mestre.getSimulation().getRedeDeFilas().getMetricasUsuarios().getSizeTarefasSubmetidas(tarefas.get(i).getProprietario());\n";
                    } else {
                        this.carregarVariaveisTarefa +=
                            "tNumTarSub = metricaUsuarios.getSizeTarefasSubmetidas(tarefas.get(i).getProprietario());\n";
                    }
                }
                break;
            case InterpretadorConstants.tNumTarConc:
                this.tarefaExpressao += "tNumTarConc";
                if (!this.declararVariaveisTarefa.contains("tNumTarConc")) {
                    if (this.dinamico) {
                        this.declararVariaveisTarefa +=
                            "int tNumTarConc = mestre.getSimulation().getRedeDeFilas().getMetricasUsuarios().getSizeTarefasConcluidas(tarefas.get(0).getProprietario());\n";
                    } else {
                        this.declararVariaveisTarefa +=
                            "int tNumTarConc = metricaUsuarios.getSizeTarefasConcluidas(tarefas.get(0).getProprietario());\n";
                    }
                }
                if (!this.carregarVariaveisTarefa.contains("tNumTarConc")) {
                    if (this.dinamico) {
                        this.carregarVariaveisTarefa +=
                            "tNumTarConc = mestre.getSimulation().getRedeDeFilas().getMetricasUsuarios().getSizeTarefasConcluidas(tarefas.get(i).getProprietario());\n";
                    } else {
                        this.carregarVariaveisTarefa +=
                            "tNumTarConc = metricaUsuarios.getSizeTarefasConcluidas(tarefas.get(i).getProprietario());\n";
                    }
                }
                break;
            case InterpretadorConstants.tPoderUser:
                this.tarefaExpressao += "tPoderUser";
                if (!this.declararVariaveisTarefa.contains("tPoderUser")) {
                    this.declararVariaveisTarefa +=
                        "double tPoderUser = metricaUsuarios.getPoderComputacional(tarefas.get(0).getProprietario());\n";
                }
                if (!this.carregarVariaveisTarefa.contains("tPoderUser")) {
                    this.carregarVariaveisTarefa +=
                        "tPoderUser = metricaUsuarios.getPoderComputacional(tarefas.get(i).getProprietario());\n";
                }
                break;
            default:
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

    private void addExpressaoRecurso (final int tipoToken) {
        switch (tipoToken) {
            case InterpretadorConstants.add:
                this.recursoExpressao += " + ";
                break;
            case InterpretadorConstants.sub:
                this.recursoExpressao += " - ";
                break;
            case InterpretadorConstants.div:
                this.recursoExpressao += " / ";
                break;
            case InterpretadorConstants.mult:
                this.recursoExpressao += " * ";
                break;
            case InterpretadorConstants.lparen:
                this.recursoExpressao += " ( ";
                break;
            case InterpretadorConstants.rparen:
                this.recursoExpressao += " ) ";
                break;
            case InterpretadorConstants.rPodeProc:
                this.recursoExpressao += "rPodeProc";
                if (!this.declararVariaveisRecurso.contains("rPodeProc")) {
                    this.declararVariaveisRecurso +=
                        "double rPodeProc = escravos.get(0).getPoderComputacional();\n";
                }
                if (!this.carregarVariaveisRecurso.contains("rPodeProc")) {
                    this.carregarVariaveisRecurso +=
                        "rPodeProc = escravos.get(i).getPoderComputacional();\n";
                }
                break;
            case InterpretadorConstants.rLinkComu:
                this.recursoExpressao += "rLinkComu";
                if (!this.imports.contains("import ispd.motor.filas.servidores.CS_Comunicacao;")) {
                    this.imports =
                        "import ispd.motor.filas.servidores.CS_Comunicacao;\n" + this.imports;
                }
                if (!this.declararVariaveisRecurso.contains("rLinkComu")) {
                    this.declararVariaveisRecurso +=
                        "double rLinkComu = calcularBandaLink(escravos.get(0));\n";
                }
                if (!this.carregarVariaveisRecurso.contains("rLinkComu")) {
                    this.carregarVariaveisRecurso +=
                        "rLinkComu = calcularBandaLink(escravos.get(i));\n";
                }
                if (!this.metodosPrivate.contains(
                    "private double calcularBandaLink(CS_Processamento get)")) {
                    this.metodosPrivate +=
                        """
                        private double calcularBandaLink(CS_Processamento get) {
                        double total = 0;
                        int conec = 0;
                        for (CentroServico cs : escalonarRota(get)) {
                            if(cs instanceof CS_Comunicacao){
                                 CS_Comunicacao comu = (CS_Comunicacao) cs;
                                 total += comu.getLarguraBanda();
                                 conec++;
                            }
                        }
                        return total / conec;
                        }

                        """;
                }
                break;
            case InterpretadorConstants.rtamCompTar:
                this.recursoExpressao += "rtamCompTar";
                if (!this.declararVariaveisRecurso.contains("rtamCompTar")) {
                    this.declararVariaveisRecurso +=
                        "double rtamCompTar = tarefaSelecionada.getTamProcessamento();\n";
                }
                break;
            case InterpretadorConstants.rtamComuTar:
                this.recursoExpressao += "rtamComuTar";
                if (!this.declararVariaveisRecurso.contains("rtamComuTar")) {
                    this.declararVariaveisRecurso +=
                        "double rtamComuTar = tarefaSelecionada.getTamComunicacao();\n";
                }
                break;
            case InterpretadorConstants.numTarExec:
                this.recursoExpressao += "numTarExec";
                if (!this.variavel.contains("numTarExecRec")) {
                    this.variavel += "private List<Integer> numTarExecRec;\n";
                }
                if (!this.variavel.contains("tarExecRec")) {
                    this.variavel += "private List<List> tarExecRec;\n";
                }
                if (!this.metodosPrivate.contains("private void addTarefasEnviadasNum(){")) {
                    this.metodosPrivate += """
                                           private void addTarefasEnviadasNum(){
                                               if(tarefaSelecionada != null){
                                                   int index = escravos.indexOf(tarefaSelecionada.getLocalProcessamento());
                                                   numTarExecRec.set(index,numTarExecRec.get(index)+1);
                                               }
                                           }

                                           """;
                }
                if (!this.metodosPrivate.contains("private void addTarefasEnviadas(){")) {
                    this.metodosPrivate += """
                                           private void addTarefasEnviadas(){
                                               if(tarefaSelecionada != null){
                                                   int index = escravos.indexOf(tarefaSelecionada.getLocalProcessamento());
                                                   tarExecRec.get(index).add(tarefaSelecionada);
                                               }
                                           }

                                           """;
                }
                if (!this.ifEscalonar.contains("addTarefasEnviadasNum();")) {
                    this.ifEscalonar += "addTarefasEnviadasNum();\n";
                }
                if (!this.ifEscalonar.contains("addTarefasEnviadas();")) {
                    this.ifEscalonar += "addTarefasEnviadas();\n";
                }
                if (!this.imports.contains("import java.util.ArrayList;")) {
                    this.imports = "import java.util.ArrayList;\n" + this.imports;
                }
                if (!this.iniciar.contains("numTarExecRec")) {
                    this.iniciar += """
                                        numTarExecRec = new ArrayList<Integer>(escravos.size());
                                        for (int i = 0; i < escravos.size(); i++) {
                                            numTarExecRec.add(0);
                                        }
                                    """;
                }
                if (!this.iniciar.contains("tarExecRec")) {
                    this.iniciar += """
                                        tarExecRec = new ArrayList<List>(escravos.size());
                                        for (int i = 0; i < escravos.size(); i++) {
                                            tarExecRec.add(new ArrayList<Tarefa>());
                                        }
                                    """;
                }
                if (!this.addTarefaConcluida.contains("numTarExecRec")) {
                    this.decAddTarefaConcluida = """
                                                 @Override
                                                 public void addTarefaConcluida(Tarefa tarefa) {
                                                     super.addTarefaConcluida(tarefa);
                                                 """;
                    this.addTarefaConcluida    =
                        "    int index = escravos.indexOf(tarefa.getLocalProcessamento());\n"
                        + "    if(index != -1){\n"
                        + "        numTarExecRec.set(index, numTarExecRec.get(index) - 1);\n"
                        + "    } else {\n"
                        + "        for(int i = 0; i < escravos.size(); i++){\n"
                        + "            if (tarExecRec.get(i).contains(tarefa)) {\n"
                        + "                numTarExecRec.set(i, numTarExecRec.get(i) - 1);\n"
                        + "                tarExecRec.get(i).remove(tarefa);\n"
                        + "            }\n"
                        + "        }\n"
                        + "    }\n" + this.addTarefaConcluida;
                    this.fimAddTarefaConcluida = "}\n\n";
                }
                if (!this.declararVariaveisRecurso.contains("numTarExec")) {
                    if (this.dinamico) {
                        this.declararVariaveisRecurso +=
                            "int numTarExec = numTarExecRec.get(0) + escravos.get(0).getInformacaoDinamicaFila().size();\n";
                    } else {
                        this.declararVariaveisRecurso += "int numTarExec = numTarExecRec.get(0);\n";
                    }
                }
                if (!this.carregarVariaveisRecurso.contains("numTarExec")) {
                    if (this.dinamico) {
                        this.carregarVariaveisRecurso +=
                            "numTarExec = numTarExecRec.get(i) + escravos.get(i).getInformacaoDinamicaFila().size();\n";
                    } else {
                        this.carregarVariaveisRecurso += "numTarExec = numTarExecRec.get(i);\n";
                    }
                }
                break;
            case InterpretadorConstants.mflopProce:
                this.recursoExpressao += "mflopProce";
                if (!this.variavel.contains("mflopProceRec")) {
                    this.variavel += "private List<Double> mflopProceRec;\n";
                }
                if (!this.variavel.contains("tarExecRec")) {
                    this.variavel += "private List<List> tarExecRec;\n";
                }
                if (!this.ifEscalonar.contains("addTarefasEnviadasMflop();")) {
                    this.ifEscalonar += "addTarefasEnviadasMflop();\n";
                }
                if (!this.ifEscalonar.contains("addTarefasEnviadas();")) {
                    this.ifEscalonar += "addTarefasEnviadas();\n";
                }
                if (!this.iniciar.contains("mflopProceRec")) {
                    this.iniciar += """
                                        mflopProceRec = new ArrayList<Double>(escravos.size());
                                        for (int i = 0; i < escravos.size(); i++) {
                                            mflopProceRec.add(0.0);
                                        }
                                    """;
                }
                if (!this.iniciar.contains("tarExecRec")) {
                    this.iniciar += """
                                        tarExecRec = new ArrayList<List>(escravos.size());
                                        for (int i = 0; i < escravos.size(); i++) {
                                            tarExecRec.add(new ArrayList<Tarefa>());
                                        }
                                    """;
                }
                if (!this.metodosPrivate.contains("private void addTarefasEnviadasMflop(){")) {
                    this.metodosPrivate += """
                                           private void addTarefasEnviadasMflop(){
                                               if(tarefaSelecionada != null){
                                                   int index = escravos.indexOf(tarefaSelecionada.getLocalProcessamento());
                                                   mflopProceRec.set(index,mflopProceRec.get(index)+tarefaSelecionada.getTamProcessamento());
                                               }
                                           }

                                           """;
                }
                if (!this.metodosPrivate.contains("private void addTarefasEnviadas(){")) {
                    this.metodosPrivate += """
                                           private void addTarefasEnviadas(){
                                               if(tarefaSelecionada != null){
                                                   int index = escravos.indexOf(tarefaSelecionada.getLocalProcessamento());
                                                   tarExecRec.get(index).add(tarefaSelecionada);
                                               }
                                           }

                                           """;
                }
                if (!this.addTarefaConcluida.contains("mflopProceRec")) {
                    this.decAddTarefaConcluida = """
                                                 @Override
                                                 public void addTarefaConcluida(Tarefa tarefa) {
                                                     super.addTarefaConcluida(tarefa);
                                                 """;
                    this.addTarefaConcluida    =
                        "    int index2 = escravos.indexOf(tarefa.getLocalProcessamento());\n"
                        + "    if(index2 != -1){\n"
                        + "        mflopProceRec.set(index2, mflopProceRec.get(index2) - tarefa.getTamProcessamento());\n"
                        + "    } else {\n"
                        + "        for(int i = 0; i < escravos.size(); i++){\n"
                        + "            if (tarExecRec.get(i).contains(tarefa)) {\n"
                        + "                mflopProceRec.set(i, mflopProceRec.get(i) - tarefa.getTamProcessamento());\n"
                        + "            }\n"
                        + "        }\n"
                        + "    }\n" + this.addTarefaConcluida;
                    this.fimAddTarefaConcluida = "}\n\n";
                }
                if (this.dinamico && !this.metodosPrivate.contains(
                    "private Double mflopsNoRecIndex(int index)")) {
                    this.metodosPrivate += """
                                           private double mflopsNoRecIndex(int index) {
                                               double mflops = 0;
                                               for(Object tar : escravos.get(index).getInformacaoDinamicaFila()){
                                                   Tarefa tarefa = (Tarefa) tar;
                                                   mflops += tarefa.getTamProcessamento();
                                               }
                                               return mflops;
                                           }

                                           """;
                }
                if (!this.declararVariaveisRecurso.contains("mflopProce")) {
                    if (this.dinamico) {
                        this.declararVariaveisRecurso +=
                            "double mflopProce = mflopProceRec.get(0) + mflopsNoRecIndex(0);\n";
                    } else {
                        this.declararVariaveisRecurso +=
                            "double mflopProce = mflopProceRec.get(0);\n";
                    }
                }
                if (!this.carregarVariaveisRecurso.contains("mflopProce")) {
                    if (this.dinamico) {
                        this.carregarVariaveisRecurso +=
                            "mflopProce = mflopProceRec.get(i) + mflopsNoRecIndex(i);\n";
                    } else {
                        this.carregarVariaveisRecurso += "mflopProce = mflopProceRec.get(i);\n";
                    }
                }
                break;
            default:
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

    public String getCodigo () {
        if (!"".equals(this.recursoExpressao)) {
            this.formulaRecurso("formula");
        }
        if (!"".equals(this.tarefaExpressao)) {
            this.formulaTarefa("formula");
        }
        final var pacote     = "package ispd.policy.externo;\n\n";
        final var decIniciar = "@Override\npublic void iniciar() {\n";
        final var decTarefa = """
                              @Override
                              public Tarefa escalonarTarefa() {
                              """;
        final var decRecurso = """
                               @Override
                               public CS_Processamento escalonarRecurso() {
                               """;
        final var decEscalonar = """
                                 @Override
                                 public void escalonar() {
                                 """;
        final var txt = pacote
                        + this.imports
                        + this.declaracao
                        + this.variavel + "\n"
                        + this.construtor
                        + this.caracteristica
                        + decIniciar
                        + this.iniciar + "}\n\n"
                        + decTarefa
                        + this.tarefa + "}\n\n"
                        + decRecurso
                        + this.recurso + "}\n\n"
                        + decEscalonar
                        + this.escalonar
                        + this.ifEscalonar + "    }\n}\n\n"
                        //+ decResultadoAtualizar
                        //+ resultadoAtualizar
                        //+ fimResultadoAtualizar
                        + this.decAddTarefaConcluida
                        + this.addTarefaConcluida
                        + this.fimAddTarefaConcluida
                        + this.adicionarTarefa
                        + this.getTempoAtualizar
                        + this.metodosPrivate
                        + this.rota + "}";
        return txt;
    }

    public String getArquivoNome () {
        return this.arquivoNome;
    }

    public final void Escalonador ()
        throws ParseException {
        this.resetaObjetosParser();
        try {
            this.Partes();
            this.jj_consume_token(0);
            this.printv("Escalonador reconhecido");

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

    private final void Partes ()
        throws ParseException {
        while (true) {
            this.Parte();
            if (((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk)
                == InterpretadorConstants.SCHEDULER) {
            } else {
                this.jj_la1[0] = this.jj_gen;
                break;
            }
        }
        this.printv("Componentes reconhecidos");
    }

    private final void Parte ()
        throws ParseException {
        this.Nome();
        this.printv("Reconheceu nome do escaonador");
        this.Caracteristica();
        this.printv("Reconheceu caracteristicas");
        this.EscalonadorTarefa();
        this.printv("Reconheceu politica de escalonamento das tarefas");
        this.EscalonadorRecurso();
        this.printv("Reconheceu politica de escalonamento dos recursos");
    }

    private final void Nome ()
        throws ParseException {
        final Token t;
        this.jj_consume_token(InterpretadorConstants.SCHEDULER);
        t = this.jj_consume_token(InterpretadorConstants.nome);
        this.escreverNome(t.image);
        this.printv("Reconheceu nome no escravo");
    }

    private final void Caracteristica ()
        throws ParseException {
        if (((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk)
            == InterpretadorConstants.RESTRICT) {
            this.limite_tarefas();
        } else {
            this.jj_la1[1] = this.jj_gen;
        }
        switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
            case InterpretadorConstants.STATIC:
                this.jj_consume_token(InterpretadorConstants.STATIC);
                this.estatico();
                break;
            case InterpretadorConstants.DYNAMIC:
                this.jj_consume_token(InterpretadorConstants.DYNAMIC);
                this.tipo_atualizacao();
                break;
            default:
                this.jj_la1[2] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final void limite_tarefas ()
        throws ParseException {
        final Token t;
        this.jj_consume_token(InterpretadorConstants.RESTRICT);
        t = this.jj_consume_token(InterpretadorConstants.inteiro);
        this.jj_consume_token(InterpretadorConstants.TASKPER);
        switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
            case InterpretadorConstants.RESOURCE:
                this.jj_consume_token(InterpretadorConstants.RESOURCE);
                this.limite(t.image, true);
                break;
            case InterpretadorConstants.USER:
                this.jj_consume_token(InterpretadorConstants.USER);
                this.limite(t.image, false);
                break;
            default:
                this.jj_la1[3] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final void tipo_atualizacao ()
        throws ParseException {
        final Token t;
        switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
            case InterpretadorConstants.TASK:
                this.jj_consume_token(InterpretadorConstants.TASK);
                switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
                    case InterpretadorConstants.ENTRY:
                        this.jj_consume_token(InterpretadorConstants.ENTRY);
                        this.dinamico("in");
                        break;
                    case InterpretadorConstants.DISPACTH:
                        this.jj_consume_token(InterpretadorConstants.DISPACTH);
                        this.dinamico("out");
                        break;
                    case InterpretadorConstants.COMPLETED:
                        this.jj_consume_token(InterpretadorConstants.COMPLETED);
                        this.dinamico("end");
                        break;
                    default:
                        this.jj_la1[4] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                }
                break;
            case InterpretadorConstants.TIME:
                this.jj_consume_token(InterpretadorConstants.TIME);
                this.jj_consume_token(InterpretadorConstants.INTERVAL);
                switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
                    case InterpretadorConstants.real:
                        t = this.jj_consume_token(InterpretadorConstants.real);
                        break;
                    case InterpretadorConstants.inteiro:
                        t = this.jj_consume_token(InterpretadorConstants.inteiro);
                        break;
                    default:
                        this.jj_la1[5] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                }
                this.dinamicoIntervalo(t.image);
                break;
            default:
                this.jj_la1[6] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final void EscalonadorTarefa ()
        throws ParseException {
        this.jj_consume_token(InterpretadorConstants.TASK);
        this.jj_consume_token(InterpretadorConstants.SCHEDULER);
        this.jj_consume_token(51);
        this.formula(true);
    }

    private final void EscalonadorRecurso ()
        throws ParseException {
        this.jj_consume_token(InterpretadorConstants.RESOURCE);
        this.jj_consume_token(InterpretadorConstants.SCHEDULER);
        this.jj_consume_token(51);
        this.formula(false);
    }

    private final void formula (final boolean tarefa)
        throws ParseException {
        switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
            case InterpretadorConstants.RANDOM:
                this.jj_consume_token(InterpretadorConstants.RANDOM);
                if (tarefa) {
                    this.formulaTarefa("random");
                } else {
                    this.formulaRecurso("random");
                }
                break;
            case InterpretadorConstants.FIFO:
                this.jj_consume_token(InterpretadorConstants.FIFO);
                if (tarefa) {
                    this.formulaTarefa("fifo");
                } else {
                    this.formulaRecurso("fifo");
                }
                break;
            case InterpretadorConstants.CRESCENT:
                this.jj_consume_token(InterpretadorConstants.CRESCENT);
                if (tarefa) {
                    this.tarefaCrescente = true;
                } else {
                    this.recursoCrescente = true;
                }
                this.jj_consume_token(InterpretadorConstants.lparen);
                this.expressao(tarefa);
                this.jj_consume_token(InterpretadorConstants.rparen);
                break;
            case InterpretadorConstants.DECREASING:
                this.jj_consume_token(InterpretadorConstants.DECREASING);
                if (tarefa) {
                    this.tarefaCrescente = false;
                } else {
                    this.recursoCrescente = false;
                }
                this.jj_consume_token(InterpretadorConstants.lparen);
                this.expressao(tarefa);
                this.jj_consume_token(InterpretadorConstants.rparen);
                break;
            default:
                this.jj_la1[7] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final void expressao (final boolean tarefa)
        throws ParseException {
        this.operando(tarefa);
        label_2:
        while (true) {
            switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
                case InterpretadorConstants.mult:
                case InterpretadorConstants.div:
                case InterpretadorConstants.sub:
                case InterpretadorConstants.add:
                    break;
                default:
                    this.jj_la1[8] = this.jj_gen;
                    break label_2;
            }
            this.operador(tarefa);
            this.operando(tarefa);
        }
    }

    private final void operando (final boolean tarefa)
        throws ParseException {
        switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
            case InterpretadorConstants.sub:
            case InterpretadorConstants.add:
                switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
                    case InterpretadorConstants.add:
                        this.jj_consume_token(InterpretadorConstants.add);
                        if (tarefa) {
                            this.addExpressaoTarefa(InterpretadorConstants.add);
                        } else {
                            this.addExpressaoRecurso(InterpretadorConstants.add);
                        }
                        break;
                    case InterpretadorConstants.sub:
                        this.jj_consume_token(InterpretadorConstants.sub);
                        if (tarefa) {
                            this.addExpressaoTarefa(InterpretadorConstants.sub);
                        } else {
                            this.addExpressaoRecurso(InterpretadorConstants.sub);
                        }
                        break;
                    default:
                        this.jj_la1[9] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                }
                break;
            default:
                this.jj_la1[10] = this.jj_gen;
        }
        switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
            case InterpretadorConstants.tTamComp:
            case InterpretadorConstants.tTamComu:
            case InterpretadorConstants.tNumTarSub:
            case InterpretadorConstants.tNumTarConc:
            case InterpretadorConstants.tPoderUser:
            case InterpretadorConstants.tTempSubm:
            case InterpretadorConstants.rPodeProc:
            case InterpretadorConstants.rLinkComu:
            case InterpretadorConstants.rtamCompTar:
            case InterpretadorConstants.rtamComuTar:
            case InterpretadorConstants.numTarExec:
            case InterpretadorConstants.mflopProce:
                this.variavel(tarefa);
                break;
            case 52:
                this.constante(tarefa);
                break;
            case InterpretadorConstants.lparen:
                this.jj_consume_token(InterpretadorConstants.lparen);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.lparen);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.lparen);
                }
                this.expressao(tarefa);
                this.jj_consume_token(InterpretadorConstants.rparen);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.rparen);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.rparen);
                }
                break;
            default:
                this.jj_la1[11] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final void operador (final boolean tarefa)
        throws ParseException {
        switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
            case InterpretadorConstants.div:
                this.jj_consume_token(InterpretadorConstants.div);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.div);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.div);
                }
                break;
            case InterpretadorConstants.mult:
                this.jj_consume_token(InterpretadorConstants.mult);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.mult);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.mult);
                }
                break;
            case InterpretadorConstants.add:
                this.jj_consume_token(InterpretadorConstants.add);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.add);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.add);
                }
                break;
            case InterpretadorConstants.sub:
                this.jj_consume_token(InterpretadorConstants.sub);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.sub);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.sub);
                }
                break;
            default:
                this.jj_la1[12] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final void variavel (final boolean tarefa)
        throws ParseException {
        switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
            case InterpretadorConstants.tTamComp:
                this.jj_consume_token(InterpretadorConstants.tTamComp);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tTamComp);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tTamComp);
                }
                break;
            case InterpretadorConstants.tTamComu:
                this.jj_consume_token(InterpretadorConstants.tTamComu);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tTamComu);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tTamComu);
                }
                break;
            case InterpretadorConstants.tNumTarSub:
                this.jj_consume_token(InterpretadorConstants.tNumTarSub);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tNumTarSub);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tNumTarSub);
                }
                break;
            case InterpretadorConstants.tNumTarConc:
                this.jj_consume_token(InterpretadorConstants.tNumTarConc);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tNumTarConc);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tNumTarConc);
                }
                break;
            case InterpretadorConstants.tPoderUser:
                this.jj_consume_token(InterpretadorConstants.tPoderUser);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tPoderUser);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tPoderUser);
                }
                break;
            case InterpretadorConstants.tTempSubm:
                this.jj_consume_token(InterpretadorConstants.tTempSubm);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tTempSubm);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tTempSubm);
                }
                break;
            case InterpretadorConstants.rPodeProc:
                this.jj_consume_token(InterpretadorConstants.rPodeProc);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.rPodeProc);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.rPodeProc);
                }
                break;
            case InterpretadorConstants.rLinkComu:
                this.jj_consume_token(InterpretadorConstants.rLinkComu);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.rLinkComu);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.rLinkComu);
                }
                break;
            case InterpretadorConstants.rtamCompTar:
                this.jj_consume_token(InterpretadorConstants.rtamCompTar);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.rtamCompTar);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.rtamCompTar);
                }
                break;
            case InterpretadorConstants.rtamComuTar:
                this.jj_consume_token(InterpretadorConstants.rtamComuTar);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.rtamComuTar);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.rtamComuTar);
                }
                break;
            case InterpretadorConstants.numTarExec:
                this.jj_consume_token(InterpretadorConstants.numTarExec);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.numTarExec);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.numTarExec);
                }
                break;
            case InterpretadorConstants.mflopProce:
                this.jj_consume_token(InterpretadorConstants.mflopProce);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.mflopProce);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.mflopProce);
                }
                break;
            default:
                this.jj_la1[13] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
    }

    private final void constante (final boolean tarefa)
        throws ParseException {
        final Token t;
        this.jj_consume_token(52);
        switch ((this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk) {
            case InterpretadorConstants.inteiro:
                t = this.jj_consume_token(InterpretadorConstants.inteiro);
                if (tarefa) {
                    this.addConstanteTarefa(t.image);
                } else {
                    this.addConstanteRecurso(t.image);
                }
                break;
            case InterpretadorConstants.real:
                t = this.jj_consume_token(InterpretadorConstants.real);
                if (tarefa) {
                    this.addConstanteTarefa(t.image);
                } else {
                    this.addConstanteRecurso(t.image);
                }
                break;
            default:
                this.jj_la1[14] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
        }
        this.jj_consume_token(53);
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
        final var la1tokens = new boolean[54];
        if (this.jj_kind >= 0) {
            la1tokens[this.jj_kind] = true;
            this.jj_kind            = -1;
        }
        for (var i = 0; i < 15; i++) {
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
        for (var i = 0; i < 54; i++) {
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
}

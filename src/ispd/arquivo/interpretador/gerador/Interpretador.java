package ispd.arquivo.interpretador.gerador;

import java.io.InputStream;
import java.text.MessageFormat;
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

    private final InterpretadorTokenManager token_source;

    private boolean erroEncontrado = false;

    private Token token = new Token();

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

    private int jj_ntk = -1;

    private int jj_gen = 0;

    private int jj_kind = -1;

    /**
     * Constructor with InputStream.
     */
    public Interpretador (final InputStream stream) {
        this.token_source = new InterpretadorTokenManager(new SimpleCharStream(stream));
        for (var i = 0; i < 15; i++) {
            this.jj_la1[i] = -1;
        }
    }

    private void resuladoParser () {
        if (this.erroEncontrado) {
            JOptionPane.showMessageDialog(
                null, this.erros, "Found Errors", JOptionPane.ERROR_MESSAGE
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
        this.declaracao  = MessageFormat.format(
            """
            public class {0} extends GridSchedulingPolicy'{'

            """,
            text
        );
        this.construtor  = MessageFormat.format(
            """
            public {0}() '{'
                this.tarefas = new ArrayList<Tarefa>();
                this.escravos = new ArrayList<CS_Processamento>();
            '}'

            """,
            text
        );
    }

    private void estatico () {
        this.caracteristica = "";
        this.dinamico       = false;
    }

    private void dinamico (final String tipo) {
        switch (tipo) {
            case "in" -> this.adicionarTarefa = """
                                                @Override
                                                public void adicionarTarefa(Tarefa tarefa){
                                                    super.adicionarTarefa(tarefa);
                                                    for(CS_Processamento maq : this.getEscravos()){
                                                        mestre.updateSubordinate(maq);
                                                    }
                                                }

                                                """;
            case "out" -> this.ifEscalonar = """
                                                 for(CS_Processamento maq : this.getEscravos()){
                                                     mestre.updateSubordinate(maq);
                                                 }
                                             """;
            case "end" -> {
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
                this.fimAddTarefaConcluida = """
                                             }

                                             """;
            }
        }
    }

    private void dinamicoIntervalo (final String text) {
        this.getTempoAtualizar = """
                                 @Override
                                 public Double getTempoAtualizar(){
                                     return (double) %s;
                                 }

                                 """.formatted(text);
    }

    private void formulaTarefa (final String valor) {
        switch (valor) {
            case "random" -> {
                this.addImportIfMissing("import java.util.Random;");
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
            }
            case "fifo" -> this.tarefa = """
                                           if (!tarefas.isEmpty()) {
                                               return tarefas.remove(0);
                                           }
                                           return null;
                                         """;
            case "formula" -> this.tarefa = MessageFormat.format(
                """
                if(!tarefas.isEmpty())'{'
                {0}  double resultado = {1};
                  int tar = 0;
                  for(int i = 0; i < tarefas.size(); i++)'{'
                {2}    double expressao = {3};
                    if(resultado {4} expressao)'{'
                       resultado = expressao;
                       tar = i;
                    '}'
                  '}'
                return tarefas.remove(tar);
                '}'
                return null;
                """,
                this.declararVariaveisTarefa,
                this.tarefaExpressao,
                this.carregarVariaveisTarefa,
                this.tarefaExpressao,
                this.tarefaCrescente ? " > " : " < "
            );
        }
    }

    private void formulaRecurso (final String valor) {
        switch (valor) {
            case "random" -> {
                this.addImportIfMissing("import java.util.Random;");
                if (!this.variavel.contains("private Random sorteio = new Random();")) {
                    this.variavel += "Random sorteio = new Random();\n";
                }
                this.recurso = """
                                 int rec = sorteio.nextInt(escravos.size());
                                 return escravos.get(rec);
                               """;
            }
            case "fifo" -> {
                this.addImportIfMissing("import java.util.ListIterator;");
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
            }
            case "formula" -> this.recurso = MessageFormat.format(
                """
                if(!escravos.isEmpty())'{'
                {0}  double resultado = {1};
                  int rec = 0;
                  for(int i = 0; i < escravos.size(); i++)'{'
                {2}    double expressao = {3};
                    if(resultado {4} expressao)'{'
                       resultado = expressao;
                       rec = i;
                    '}'
                  '}'
                return escravos.get(rec);
                '}'
                return null;
                """,
                this.declararVariaveisRecurso,
                this.recursoExpressao,
                this.carregarVariaveisRecurso,
                this.recursoExpressao,
                this.recursoCrescente ? " > " : " < "
            );
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
                this.escalonar = MessageFormat.format(
                    """
                    tarefaSelecionada = null;
                    if (condicoesEscalonamento())
                    {0}""",
                    this.escalonar
                );
            }
            if (!this.ifEscalonar.contains("addTarefasEnviadas();")) {
                this.ifEscalonar += "addTarefasEnviadas();\n";
            }
            this.decAddTarefaConcluida = """
                                         @Override
                                         public void addTarefaConcluida(Tarefa tarefa) {
                                             super.addTarefaConcluida(tarefa);
                                         """;
            this.addTarefaConcluida    = MessageFormat.format(
                """
                {0}    for (int i = 0; i < escravos.size(); i++) '{'
                        if (tarExecRec.get(i).contains(tarefa)) '{'
                            tarExecRec.get(i).remove(tarefa);
                        '}'
                    '}'
                """,
                this.addTarefaConcluida
            );
            this.fimAddTarefaConcluida = """
                                         }

                                         """;
        } else {
            this.metodosPrivate += MessageFormat.format(
                """
                private boolean  condicoesEscalonamento() '{'
                    int cont = 1;
                    for (String usuario : metricaUsuarios.getUsuarios()) '{'
                        if( (metricaUsuarios.getSizeTarefasSubmetidas(usuario) - metricaUsuarios.getSizeTarefasConcluidas(usuario) ) > {0})'{'
                            cont++;
                        '}'
                    '}'
                    if(cont >= metricaUsuarios.getUsuarios().size())'{'
                        mestre.setSchedulingConditions(PolicyConditions.WHEN_RECEIVES_RESULT);
                        return false;
                    '}'
                    mestre.setSchedulingConditions(PolicyConditions.WHILE_MUST_DISTRIBUTE);
                    return true;
                '}'

                """,
                valorInteiro
            );
        }
    }

    private void addExpressaoTarefa (final int tipoToken) {
        switch (tipoToken) {
            case InterpretadorConstants.add -> this.tarefaExpressao += " + ";
            case InterpretadorConstants.sub -> this.tarefaExpressao += " - ";
            case InterpretadorConstants.div -> this.tarefaExpressao += " / ";
            case InterpretadorConstants.mult -> this.tarefaExpressao += " * ";
            case InterpretadorConstants.lparen -> this.tarefaExpressao += " ( ";
            case InterpretadorConstants.rparen -> this.tarefaExpressao += " ) ";
            case InterpretadorConstants.tTamComp -> {
                this.tarefaExpressao += "tTamComp";
                if (!this.declararVariaveisTarefa.contains("tTamComp")) {
                    this.declararVariaveisTarefa +=
                        "double tTamComp = tarefas.get(0).getTamProcessamento();\n";
                }
                if (!this.carregarVariaveisTarefa.contains("tTamComp")) {
                    this.carregarVariaveisTarefa +=
                        "tTamComp = tarefas.get(i).getTamProcessamento();\n";
                }
            }
            case InterpretadorConstants.tTamComu -> {
                this.tarefaExpressao += "tTamComu";
                if (!this.declararVariaveisTarefa.contains("tTamComu")) {
                    this.declararVariaveisTarefa +=
                        "double tTamComu = tarefas.get(0).getTamComunicacao();\n";
                }
                if (!this.carregarVariaveisTarefa.contains("tTamComu")) {
                    this.carregarVariaveisTarefa +=
                        "tTamComu = tarefas.get(i).getTamComunicacao();\n";
                }
            }
            case InterpretadorConstants.tTempSubm -> {
                this.tarefaExpressao += "tTempSubm";
                if (!this.declararVariaveisTarefa.contains("tTempSubm")) {
                    this.declararVariaveisTarefa +=
                        "double tTempSubm = tarefas.get(0).getTimeCriacao();\n";
                }
                if (!this.carregarVariaveisTarefa.contains("tTempSubm")) {
                    this.carregarVariaveisTarefa +=
                        "tTempSubm = tarefas.get(i).getTimeCriacao();\n";
                }
            }
            case InterpretadorConstants.tNumTarSub -> {
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
            }
            case InterpretadorConstants.tNumTarConc -> {
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
            }
            case InterpretadorConstants.tPoderUser -> {
                this.tarefaExpressao += "tPoderUser";
                if (!this.declararVariaveisTarefa.contains("tPoderUser")) {
                    this.declararVariaveisTarefa +=
                        "double tPoderUser = metricaUsuarios.getPoderComputacional(tarefas.get(0).getProprietario());\n";
                }
                if (!this.carregarVariaveisTarefa.contains("tPoderUser")) {
                    this.carregarVariaveisTarefa +=
                        "tPoderUser = metricaUsuarios.getPoderComputacional(tarefas.get(i).getProprietario());\n";
                }
            }
            default -> {
                this.addSemanticError(this.getToken(1));
                this.erroEncontrado = true;
                this.consomeTokens();
                this.resuladoParser();
            }
        }
    }

    private void addSemanticError (final Token t) {
        this.erros += MessageFormat.format(
            "\nErro semantico encontrado na linha {0}, coluna {1}", t.endLine, t.endColumn
        );
    }

    private void addExpressaoRecurso (final int tipoToken) {
        switch (tipoToken) {
            case InterpretadorConstants.add -> this.recursoExpressao += " + ";
            case InterpretadorConstants.sub -> this.recursoExpressao += " - ";
            case InterpretadorConstants.div -> this.recursoExpressao += " / ";
            case InterpretadorConstants.mult -> this.recursoExpressao += " * ";
            case InterpretadorConstants.lparen -> this.recursoExpressao += " ( ";
            case InterpretadorConstants.rparen -> this.recursoExpressao += " ) ";
            case InterpretadorConstants.rPodeProc -> {
                this.recursoExpressao += "rPodeProc";
                if (!this.declararVariaveisRecurso.contains("rPodeProc")) {
                    this.declararVariaveisRecurso +=
                        "double rPodeProc = escravos.get(0).getPoderComputacional();\n";
                }
                if (!this.carregarVariaveisRecurso.contains("rPodeProc")) {
                    this.carregarVariaveisRecurso +=
                        "rPodeProc = escravos.get(i).getPoderComputacional();\n";
                }
            }
            case InterpretadorConstants.rLinkComu -> {
                this.recursoExpressao += "rLinkComu";
                this.addImportIfMissing("import ispd.motor.filas.servidores.CS_Comunicacao;");
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
            }
            case InterpretadorConstants.rtamCompTar -> {
                this.recursoExpressao += "rtamCompTar";
                if (!this.declararVariaveisRecurso.contains("rtamCompTar")) {
                    this.declararVariaveisRecurso +=
                        "double rtamCompTar = tarefaSelecionada.getTamProcessamento();\n";
                }
            }
            case InterpretadorConstants.rtamComuTar -> {
                this.recursoExpressao += "rtamComuTar";
                if (!this.declararVariaveisRecurso.contains("rtamComuTar")) {
                    this.declararVariaveisRecurso +=
                        "double rtamComuTar = tarefaSelecionada.getTamComunicacao();\n";
                }
            }
            case InterpretadorConstants.numTarExec -> {
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
                this.addImportIfMissing("import java.util.ArrayList;");
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
                        MessageFormat.format(
                            """
                                int index = escravos.indexOf(tarefa.getLocalProcessamento());
                                if(index != -1)'{'
                                    numTarExecRec.set(index, numTarExecRec.get(index) - 1);
                                '}' else '{'
                                    for(int i = 0; i < escravos.size(); i++)'{'
                                        if (tarExecRec.get(i).contains(tarefa)) '{'
                                            numTarExecRec.set(i, numTarExecRec.get(i) - 1);
                                            tarExecRec.get(i).remove(tarefa);
                                        '}'
                                    '}'
                                '}'
                            {0}""",
                            this.addTarefaConcluida
                        );
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
            }
            case InterpretadorConstants.mflopProce -> {
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
                        MessageFormat.format(
                            """
                                int index2 = escravos.indexOf(tarefa.getLocalProcessamento());
                                if(index2 != -1)'{'
                                    mflopProceRec.set(index2, mflopProceRec.get(index2) - tarefa.getTamProcessamento());
                                '}' else '{'
                                    for(int i = 0; i < escravos.size(); i++)'{'
                                        if (tarExecRec.get(i).contains(tarefa)) '{'
                                            mflopProceRec.set(i, mflopProceRec.get(i) - tarefa.getTamProcessamento());
                                        '}'
                                    '}'
                                '}'
                            {0}""",
                            this.addTarefaConcluida
                        );
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
            }
            default -> {
                this.addSemanticError(this.getToken(1));
                this.erroEncontrado = true;
                this.consomeTokens();
                this.resuladoParser();
            }
        }
    }

    private void addImportIfMissing (final String importStatement) {
        if (this.imports.contains(importStatement)) {
            return;
        }

        this.imports = MessageFormat.format("{0}\n{1}", importStatement, this.imports);
    }

    public String getCodigo () {
        if (!"".equals(this.recursoExpressao)) {
            this.formulaRecurso("formula");
        }
        if (!"".equals(this.tarefaExpressao)) {
            this.formulaTarefa("formula");
        }
        final var pacote = """
                           package ispd.policy.externo;

                           """;
        final var decIniciar = """
                               @Override
                               public void iniciar() {
                               """;
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
        return MessageFormat.format(
            """
            {0}{1}{2}{3}
            {4}{5}{6}{7}'}'

            {8}{9}'}'

            {10}{11}'}'

            {12}{13}{14}    '}'
            '}'

            {15}{16}{17}{18}{19}{20}{21}'}'""",
            pacote,
            this.imports,
            this.declaracao,
            this.variavel,
            this.construtor,
            this.caracteristica,
            decIniciar,
            this.iniciar,
            decTarefa,
            this.tarefa,
            decRecurso,
            this.recurso,
            decEscalonar,
            this.escalonar,
            this.ifEscalonar,
            this.decAddTarefaConcluida,
            this.addTarefaConcluida,
            this.fimAddTarefaConcluida,
            this.adicionarTarefa,
            this.getTempoAtualizar,
            this.metodosPrivate,
            this.rota
        );
    }

    public String getArquivoNome () {
        return this.arquivoNome;
    }

    public final void Escalonador () throws ParseException {
        this.erroEncontrado = false;
        try {
            this.Partes();
            this.jj_consume_token(0);

            this.resuladoParser();
        } catch (final ParseException ignored) {
            this.addSemanticError(this.getToken(1));
            this.erroEncontrado = true;
            this.consomeTokens();
            this.resuladoParser();
        }
    }

    private final void Partes () throws ParseException {
        while (true) {
            this.Parte();
            if (this.getAToken() != InterpretadorConstants.SCHEDULER) {
                this.jj_la1[0] = this.jj_gen;
                break;
            }
        }
    }

    private final void Parte () throws ParseException {
        this.Nome();
        this.Caracteristica();
        this.EscalonadorTarefa();
        this.EscalonadorRecurso();
    }

    private final void Nome () throws ParseException {
        this.jj_consume_token(InterpretadorConstants.SCHEDULER);
        final var t = this.jj_consume_token(InterpretadorConstants.nome);
        this.escreverNome(t.image);
    }

    private final void Caracteristica () throws ParseException {
        if (this.getAToken()
            == InterpretadorConstants.RESTRICT) {
            this.limite_tarefas();
        } else {
            this.jj_la1[1] = this.jj_gen;
        }
        switch (this.getAToken()) {
            case InterpretadorConstants.STATIC -> {
                this.jj_consume_token(InterpretadorConstants.STATIC);
                this.estatico();
            }
            case InterpretadorConstants.DYNAMIC -> {
                this.jj_consume_token(InterpretadorConstants.DYNAMIC);
                this.tipo_atualizacao();
            }
            default -> {
                this.jj_la1[2] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    private final void limite_tarefas () throws ParseException {
        this.jj_consume_token(InterpretadorConstants.RESTRICT);
        final var t = this.jj_consume_token(InterpretadorConstants.inteiro);
        this.jj_consume_token(InterpretadorConstants.TASKPER);
        switch (this.getAToken()) {
            case InterpretadorConstants.RESOURCE -> {
                this.jj_consume_token(InterpretadorConstants.RESOURCE);
                this.limite(t.image, true);
            }
            case InterpretadorConstants.USER -> {
                this.jj_consume_token(InterpretadorConstants.USER);
                this.limite(t.image, false);
            }
            default -> {
                this.jj_la1[3] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    private final void tipo_atualizacao () throws ParseException {
        switch (this.getAToken()) {
            case InterpretadorConstants.TASK -> {
                this.jj_consume_token(InterpretadorConstants.TASK);
                switch (this.getAToken()) {
                    case InterpretadorConstants.ENTRY -> {
                        this.jj_consume_token(InterpretadorConstants.ENTRY);
                        this.dinamico("in");
                    }
                    case InterpretadorConstants.DISPACTH -> {
                        this.jj_consume_token(InterpretadorConstants.DISPACTH);
                        this.dinamico("out");
                    }
                    case InterpretadorConstants.COMPLETED -> {
                        this.jj_consume_token(InterpretadorConstants.COMPLETED);
                        this.dinamico("end");
                    }
                    default -> {
                        this.jj_la1[4] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                }
            }
            case InterpretadorConstants.TIME -> {
                this.jj_consume_token(InterpretadorConstants.TIME);
                this.jj_consume_token(InterpretadorConstants.INTERVAL);
                final Token t;
                switch (this.getAToken()) {
                    case InterpretadorConstants.real -> t =
                        this.jj_consume_token(InterpretadorConstants.real);
                    case InterpretadorConstants.inteiro -> t =
                        this.jj_consume_token(InterpretadorConstants.inteiro);
                    default -> {
                        this.jj_la1[5] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                }
                this.dinamicoIntervalo(t.image);
            }
            default -> {
                this.jj_la1[6] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    private final void EscalonadorTarefa () throws ParseException {
        this.jj_consume_token(InterpretadorConstants.TASK);
        this.jj_consume_token(InterpretadorConstants.SCHEDULER);
        this.jj_consume_token(51);
        this.formula(true);
    }

    private final void EscalonadorRecurso () throws ParseException {
        this.jj_consume_token(InterpretadorConstants.RESOURCE);
        this.jj_consume_token(InterpretadorConstants.SCHEDULER);
        this.jj_consume_token(51);
        this.formula(false);
    }

    private final void formula (final boolean tarefa) throws ParseException {
        switch (this.getAToken()) {
            case InterpretadorConstants.RANDOM -> {
                this.jj_consume_token(InterpretadorConstants.RANDOM);
                if (tarefa) {
                    this.formulaTarefa("random");
                } else {
                    this.formulaRecurso("random");
                }
            }
            case InterpretadorConstants.FIFO -> {
                this.jj_consume_token(InterpretadorConstants.FIFO);
                if (tarefa) {
                    this.formulaTarefa("fifo");
                } else {
                    this.formulaRecurso("fifo");
                }
            }
            case InterpretadorConstants.CRESCENT -> {
                this.jj_consume_token(InterpretadorConstants.CRESCENT);
                if (tarefa) {
                    this.tarefaCrescente = true;
                } else {
                    this.recursoCrescente = true;
                }
                this.jj_consume_token(InterpretadorConstants.lparen);
                this.expressao(tarefa);
                this.jj_consume_token(InterpretadorConstants.rparen);
            }
            case InterpretadorConstants.DECREASING -> {
                this.jj_consume_token(InterpretadorConstants.DECREASING);
                if (tarefa) {
                    this.tarefaCrescente = false;
                } else {
                    this.recursoCrescente = false;
                }
                this.jj_consume_token(InterpretadorConstants.lparen);
                this.expressao(tarefa);
                this.jj_consume_token(InterpretadorConstants.rparen);
            }
            default -> {
                this.jj_la1[7] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    private final void expressao (final boolean tarefa) throws ParseException {
        this.operando(tarefa);
        label_2:
        while (true) {
            switch (this.getAToken()) {
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

    private final void operando (final boolean tarefa) throws ParseException {
        switch (this.getAToken()) {
            case InterpretadorConstants.sub, InterpretadorConstants.add -> {
                switch (this.getAToken()) {
                    case InterpretadorConstants.add -> {
                        this.jj_consume_token(InterpretadorConstants.add);
                        if (tarefa) {
                            this.addExpressaoTarefa(InterpretadorConstants.add);
                        } else {
                            this.addExpressaoRecurso(InterpretadorConstants.add);
                        }
                    }
                    case InterpretadorConstants.sub -> {
                        this.jj_consume_token(InterpretadorConstants.sub);
                        if (tarefa) {
                            this.addExpressaoTarefa(InterpretadorConstants.sub);
                        } else {
                            this.addExpressaoRecurso(InterpretadorConstants.sub);
                        }
                    }
                    default -> {
                        this.jj_la1[9] = this.jj_gen;
                        this.jj_consume_token(-1);
                        throw new ParseException();
                    }
                }
            }
            default -> this.jj_la1[10] = this.jj_gen;
        }
        switch (this.getAToken()) {
            case InterpretadorConstants.numTarExec, InterpretadorConstants.mflopProce, InterpretadorConstants.tTamComu, InterpretadorConstants.tNumTarSub, InterpretadorConstants.tNumTarConc, InterpretadorConstants.tPoderUser, InterpretadorConstants.tTempSubm, InterpretadorConstants.rPodeProc, InterpretadorConstants.rLinkComu, InterpretadorConstants.tTamComp, InterpretadorConstants.rtamCompTar, InterpretadorConstants.rtamComuTar -> this.variavel(
                tarefa);
            case 52 -> this.constante(tarefa);
            case InterpretadorConstants.lparen -> {
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
            }
            default -> {
                this.jj_la1[11] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    private final void operador (final boolean tarefa) throws ParseException {
        switch (this.getAToken()) {
            case InterpretadorConstants.div -> {
                this.jj_consume_token(InterpretadorConstants.div);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.div);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.div);
                }
            }
            case InterpretadorConstants.mult -> {
                this.jj_consume_token(InterpretadorConstants.mult);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.mult);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.mult);
                }
            }
            case InterpretadorConstants.add -> {
                this.jj_consume_token(InterpretadorConstants.add);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.add);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.add);
                }
            }
            case InterpretadorConstants.sub -> {
                this.jj_consume_token(InterpretadorConstants.sub);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.sub);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.sub);
                }
            }
            default -> {
                this.jj_la1[12] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    private final void variavel (final boolean tarefa) throws ParseException {
        switch (this.getAToken()) {
            case InterpretadorConstants.tTamComp -> {
                this.jj_consume_token(InterpretadorConstants.tTamComp);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tTamComp);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tTamComp);
                }
            }
            case InterpretadorConstants.tTamComu -> {
                this.jj_consume_token(InterpretadorConstants.tTamComu);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tTamComu);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tTamComu);
                }
            }
            case InterpretadorConstants.tNumTarSub -> {
                this.jj_consume_token(InterpretadorConstants.tNumTarSub);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tNumTarSub);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tNumTarSub);
                }
            }
            case InterpretadorConstants.tNumTarConc -> {
                this.jj_consume_token(InterpretadorConstants.tNumTarConc);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tNumTarConc);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tNumTarConc);
                }
            }
            case InterpretadorConstants.tPoderUser -> {
                this.jj_consume_token(InterpretadorConstants.tPoderUser);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tPoderUser);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tPoderUser);
                }
            }
            case InterpretadorConstants.tTempSubm -> {
                this.jj_consume_token(InterpretadorConstants.tTempSubm);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.tTempSubm);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.tTempSubm);
                }
            }
            case InterpretadorConstants.rPodeProc -> {
                this.jj_consume_token(InterpretadorConstants.rPodeProc);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.rPodeProc);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.rPodeProc);
                }
            }
            case InterpretadorConstants.rLinkComu -> {
                this.jj_consume_token(InterpretadorConstants.rLinkComu);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.rLinkComu);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.rLinkComu);
                }
            }
            case InterpretadorConstants.rtamCompTar -> {
                this.jj_consume_token(InterpretadorConstants.rtamCompTar);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.rtamCompTar);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.rtamCompTar);
                }
            }
            case InterpretadorConstants.rtamComuTar -> {
                this.jj_consume_token(InterpretadorConstants.rtamComuTar);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.rtamComuTar);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.rtamComuTar);
                }
            }
            case InterpretadorConstants.numTarExec -> {
                this.jj_consume_token(InterpretadorConstants.numTarExec);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.numTarExec);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.numTarExec);
                }
            }
            case InterpretadorConstants.mflopProce -> {
                this.jj_consume_token(InterpretadorConstants.mflopProce);
                if (tarefa) {
                    this.addExpressaoTarefa(InterpretadorConstants.mflopProce);
                } else {
                    this.addExpressaoRecurso(InterpretadorConstants.mflopProce);
                }
            }
            default -> {
                this.jj_la1[13] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
    }

    private final void constante (final boolean tarefa) throws ParseException {
        this.jj_consume_token(52);
        switch (this.getAToken()) {
            case InterpretadorConstants.inteiro -> {
                final var t = this.jj_consume_token(InterpretadorConstants.inteiro);
                if (tarefa) {
                    this.addConstanteTarefa(t.image);
                } else {
                    this.addConstanteRecurso(t.image);
                }
            }
            case InterpretadorConstants.real -> {
                final var t = this.jj_consume_token(InterpretadorConstants.real);
                if (tarefa) {
                    this.addConstanteTarefa(t.image);
                } else {
                    this.addConstanteRecurso(t.image);
                }
            }
            default -> {
                this.jj_la1[14] = this.jj_gen;
                this.jj_consume_token(-1);
                throw new ParseException();
            }
        }
        this.jj_consume_token(53);
    }

    private int getAToken () {
        return (this.jj_ntk == -1) ? this.jj_ntk() : this.jj_ntk;
    }

    private Token jj_consume_token (final int kind) throws ParseException {
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
        final var jj_nt = this.token.next;
        if (jj_nt == null) {
            return (this.jj_ntk = (this.token.next = this.token_source.getNextToken()).kind);
        } else {
            return (this.jj_ntk = jj_nt.kind);
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

    public boolean isErroEncontrado () {
        return this.erroEncontrado;
    }
}

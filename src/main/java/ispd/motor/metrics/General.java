package ispd.motor.metrics;

import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.*;
import ispd.motor.queues.task.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

public class General implements Serializable {

    /**
     * Armazena métricas obtidas da simulação
     */
    private final Global global;

    private final Iterable<String> usuarios;

    private int numeroDeSimulacoes;

    private Map<String, CommunicationMetrics> metricasComunicacao = null;

    private Map<String, ProcessingMetrics> metricasProcessamento = null;

    private Map<String, Allocation> metricasAlocacao = null;

    private Map<String, Cost> metricasCusto = null;

    private Map<String, Double> metricasSatisfacao = null;

    private Map<String, Integer> tarefasConcluidas = null;

    private double tempoMedioFilaComunicacao = 0;

    private double tempoMedioComunicacao = 0;

    private double tempoMedioFilaProcessamento = 0;

    private double tempoMedioProcessamento = 0;

    private double MflopsDesperdicio = 0;

    private int numTarefasCanceladas = 0;

    private int numTarefas = 0;

    /**
     * Satisfação geral do usuário considerando beta calculado com tempo de simulação
     */
    private Map<String, Double> satisfacaoGeralSim = null;

    /**
     * Satisfação geral do usuário considerando beta calculado com tempo de uso
     */
    private Map<String, Double> satisfacaoGeralUso = null;

    /**
     * Consumo de energia em Joules, total do usuário
     */
    private Map<String, Double> consumoEnergiaTotalUsuario = null;

    /**
     * Satisfação sobre desempenho, idêntica à utilizada no HOSEP
     */
    private Map<String, Double> satisfacaoDesempenho = null;

    /**
     * Número de tarefas que sofreram preempção
     */
    private Map<String, Integer> tarefasPreemp = null;

    /**
     * Consumo da porção de cada usuário
     */
    private Map<String, Double> consumoLocal = null;

    /**
     * Consumo total do sistema
     */
    private double consumoTotalSistema = 0.0;

    /**
     * Consumo máximo da porção de cada usuário, como se a porção ficasse ativa por completo durante
     * toda a simulação
     */
    private Map<String, Double> consumoMaxLocal = null;

    /**
     * Limite de consumo em Joules, considerando o tempo total de simulação
     */
    private Map<String, Double> limitesConsumoTempoUso = null;

    /**
     * Energia consumida na porção com tarefas de usuários não proprietários
     */
    private Map<String, Double> consumoLocalEstrangeiro = null;

    /**
     * Energia consumida na porção com tarefas do usuário proprietário
     */
    private Map<String, Double> consumoLocalProprio = null;

    /**
     * Energia desperdiçada para cada usuário
     */
    private Map<String, Double> energiaDespercicada = null;

    /**
     * Limite de consumo em Joules, considerando em o usuário teve tarefas no sistema
     */
    private Map<String, Double> limitesConsumoTempoSim = null;

    /**
     * Energia desperdiçada para cada usuário
     */
    private Map<String, Double> alphaUsuarios = null;

    /**
     * Energia desperdiçada para cada usuário
     */
    private Map<String, Double> betaTempoSim = null;

    /**
     * Energia desperdiçada para cada usuário
     */
    private Map<String, Double> betaTempoUso = null;

    private Map<String, Double> tempoInicialExec = null;

    private Map<String, Double> tempoFinalExec = null;

    private Map<String, Double> turnaroundTime = null;

    private double tempoSIM = 0.0;

    private List<Map<String, Double>> historicoConsumoTotalUsuario = null;

    private List<Map<String, Double>> historicoSatisfacaoGeralTempoSim = null;

    private List<Map<String, Double>> historicoSatisfacaoGeralTempoUso = null;

    private List<Map<String, Double>> historicoConsumoLocal = null;

    private List<Map<String, Double>> historicoSatisfacaoDesempenho = null;

    private List<Map<String, Integer>> historicoTarefasPreemp = null;

    private List<Map<String, Double>> historicoConsumoMaxLocal = null;

    private Collection<Double> historicoConsumoTotalSistema = null;

    private List<Map<String, Double>> historicoLimitesConsumoTempoSim = null;

    private List<Map<String, Double>> historicoLimitesConsumoTempoUso = null;

    private List<Map<String, Double>> historicoEnergiaDeperdicada = null;

    private List<Map<String, Double>> historicoAlpha = null;

    private List<Map<String, Double>> historicoBetaTempoSim = null;

    private List<Map<String, Double>> historicoBetaTempoUso = null;

    private List<Map<String, Double>> historicoConsumoLocalProprio = null;

    private List<Map<String, Double>> historicoConsumoLocalEstrangeiro = null;

    private Collection<Map<String, Double>> historicoTempoInicial = null;

    private Collection<Map<String, Double>> historicoTempoFinal = null;

    private List<Map<String, Double>> historicoTurnaroundTime = null;

    private List<Double> historicoTempoSim = null;

    public General (final List<String> usuarios) {

        this.numeroDeSimulacoes = 0;
        this.global = new Global();
        this.usuarios           = usuarios;
        this.tempoSIM           = 0.0;

        //Maps
        this.satisfacaoGeralSim         = new HashMap<>();
        this.satisfacaoGeralUso         = new HashMap<>();
        this.consumoEnergiaTotalUsuario = new HashMap<>();
        this.consumoLocal               = new HashMap<>();
        this.limitesConsumoTempoSim     = new HashMap<>();
        this.limitesConsumoTempoUso     = new HashMap<>();
        this.satisfacaoDesempenho       = new HashMap<>();
        this.tarefasPreemp              = new HashMap<>();
        this.consumoMaxLocal            = new HashMap<>();
        this.consumoLocalEstrangeiro    = new HashMap<>();
        this.consumoLocalProprio        = new HashMap<>();
        this.energiaDespercicada        = new HashMap<>();
        this.alphaUsuarios              = new HashMap<>();
        this.betaTempoSim               = new HashMap<>();
        this.betaTempoUso               = new HashMap<>();
        this.tempoInicialExec = new HashMap<>();
        this.tempoFinalExec   = new HashMap<>();
        this.turnaroundTime   = new HashMap<>();

        //Historicos
        this.historicoSatisfacaoGeralTempoSim = new ArrayList<>();
        this.historicoSatisfacaoGeralTempoUso = new ArrayList<>();
        this.historicoConsumoTotalUsuario     = new ArrayList<>();
        this.historicoConsumoLocal            = new ArrayList<>();
        this.historicoSatisfacaoDesempenho    = new ArrayList<>();
        this.historicoTarefasPreemp           = new ArrayList<>();
        this.historicoConsumoMaxLocal         = new ArrayList<>();
        this.historicoConsumoTotalSistema     = new ArrayList<>();
        this.historicoLimitesConsumoTempoSim  = new ArrayList<>();
        this.historicoLimitesConsumoTempoUso  = new ArrayList<>();
        this.historicoConsumoLocalEstrangeiro = new ArrayList<>();
        this.historicoConsumoLocalProprio     = new ArrayList<>();
        this.historicoEnergiaDeperdicada      = new ArrayList<>();
        this.historicoAlpha                   = new ArrayList<>();
        this.historicoBetaTempoSim            = new ArrayList<>();
        this.historicoBetaTempoUso            = new ArrayList<>();
        this.historicoTempoInicial   = new ArrayList<>();
        this.historicoTempoFinal     = new ArrayList<>();
        this.historicoTurnaroundTime = new ArrayList<>();
        this.historicoTempoSim                = new ArrayList<>();
    }

    public General (
        final GridQueueNetwork gridQueueNetwork,
        final double time,
        final List<GridTask> tarefas
    ) {
        this.numeroDeSimulacoes = 1;
        this.global = new Global(gridQueueNetwork, time, tarefas);
        this.tarefasConcluidas  = new HashMap<>();
        this.usuarios        = gridQueueNetwork.getUsuarios();

        //Maps
        this.metricasSatisfacao         = new HashMap<>();
        this.satisfacaoGeralSim         = new HashMap<>();
        this.satisfacaoGeralUso         = new HashMap<>();
        this.consumoEnergiaTotalUsuario = new HashMap<>();
        this.consumoLocal               = new HashMap<>();
        this.satisfacaoDesempenho       = new HashMap<>();
        this.consumoMaxLocal            = new HashMap<>();
        this.limitesConsumoTempoSim     = new HashMap<>();
        this.limitesConsumoTempoUso     = new HashMap<>();
        this.tarefasPreemp              = new HashMap<>();
        this.consumoLocalEstrangeiro    = new HashMap<>();
        this.consumoLocalProprio        = new HashMap<>();
        this.energiaDespercicada        = new HashMap<>();
        this.alphaUsuarios              = new HashMap<>();
        this.betaTempoSim               = new HashMap<>();
        this.betaTempoUso               = new HashMap<>();
        this.tempoInicialExec = new HashMap<>();
        this.tempoFinalExec   = new HashMap<>();
        this.turnaroundTime   = new HashMap<>();
        this.tempoSIM                   = 0.0;

        //Historicos
        this.historicoSatisfacaoGeralTempoSim = new ArrayList<>();
        this.historicoSatisfacaoGeralTempoUso = new ArrayList<>();
        this.historicoLimitesConsumoTempoSim  = new ArrayList<>();
        this.historicoLimitesConsumoTempoUso  = new ArrayList<>();
        this.historicoConsumoTotalUsuario     = new ArrayList<>();
        this.historicoConsumoLocal            = new ArrayList<>();
        this.historicoSatisfacaoDesempenho    = new ArrayList<>();
        this.historicoTarefasPreemp           = new ArrayList<>();
        this.historicoConsumoMaxLocal         = new ArrayList<>();
        this.historicoConsumoTotalSistema     = new ArrayList<>();
        this.historicoConsumoLocalEstrangeiro = new ArrayList<>();
        this.historicoConsumoLocalProprio     = new ArrayList<>();
        this.historicoEnergiaDeperdicada      = new ArrayList<>();
        this.historicoAlpha                   = new ArrayList<>();
        this.historicoBetaTempoSim            = new ArrayList<>();
        this.historicoBetaTempoUso            = new ArrayList<>();
        this.historicoTempoInicial   = new ArrayList<>();
        this.historicoTempoFinal     = new ArrayList<>();
        this.historicoTurnaroundTime = new ArrayList<>();
        this.historicoTempoSim                = new ArrayList<>();
        this.consumoTotalSistema              = 0.0;

        for (final var user : this.usuarios) {
            this.satisfacaoDesempenho.put(user, 0.0);
            this.satisfacaoGeralSim.put(user, 0.0);
            this.satisfacaoGeralUso.put(user, 0.0);
            this.consumoEnergiaTotalUsuario.put(user, 0.0);
            this.consumoLocal.put(user, 0.0);
            this.consumoMaxLocal.put(user, 0.0);
            this.limitesConsumoTempoSim.put(user, 0.0);
            this.limitesConsumoTempoUso.put(user, 0.0);
            this.energiaDespercicada.put(user, 0.0);
            this.tarefasPreemp.put(user, 0);
            this.consumoLocalEstrangeiro.put(user, 0.0);
            this.consumoLocalProprio.put(user, 0.0);
            this.alphaUsuarios.put(user, 0.0);
            this.betaTempoSim.put(user, 0.0);
            this.betaTempoUso.put(user, 0.0);
            this.tempoInicialExec.put(user, 0.0);
            this.tempoFinalExec.put(user, 0.0);
            this.turnaroundTime.put(user, 0.0);
        }

        for (final Processing maq : gridQueueNetwork.getMaquinas()) {
            final var propMaq = maq.getProprietario();
            this.consumoMaxLocal.put(
                propMaq,
                this.consumoMaxLocal.get(propMaq) + maq.getConsumoEnergia()
            );

            this.limitesConsumoTempoSim.put(
                propMaq,
                this.limitesConsumoTempoSim.get(propMaq)
                + maq.getConsumoEnergia()
            );
        }

        final var limits = gridQueueNetwork.getLimites();

        for (final var user : this.usuarios) {
            if (!limits.containsKey(user)) {
                throw new IllegalArgumentException("Unexpected user " + user);
            }

            // Limite de consumo em porcentagem do consumo total da porção do usuário
            final var porcentLimite = limits.get(user) / 100;
            this.limitesConsumoTempoSim.put(
                user,
                this.limitesConsumoTempoSim.get(user) * porcentLimite
            );
            this.limitesConsumoTempoUso.put(user, this.limitesConsumoTempoSim.get(user));
        }

        this.getMetricaFilaTarefa(tarefas, gridQueueNetwork);
        this.getMetricaComunicacao(gridQueueNetwork);
        this.getMetricaProcessamento(gridQueueNetwork);

        //Historicos
        this.historicoSatisfacaoDesempenho.add(this.satisfacaoDesempenho);
        this.historicoTarefasPreemp.add(this.tarefasPreemp);
        this.historicoSatisfacaoGeralTempoSim.add(this.satisfacaoGeralSim);
        this.historicoSatisfacaoGeralTempoUso.add(this.satisfacaoGeralUso);
        this.historicoConsumoTotalUsuario.add(this.consumoEnergiaTotalUsuario);
        this.historicoConsumoLocal.add(this.consumoLocal);
        this.historicoConsumoMaxLocal.add(this.consumoMaxLocal);
        this.historicoConsumoTotalSistema.add(this.consumoTotalSistema);
        this.historicoLimitesConsumoTempoSim.add(this.limitesConsumoTempoSim);
        this.historicoLimitesConsumoTempoUso.add(this.limitesConsumoTempoUso);
        this.historicoConsumoLocalEstrangeiro.add(this.consumoLocalEstrangeiro);
        this.historicoConsumoLocalProprio.add(this.consumoLocalProprio);
        this.historicoEnergiaDeperdicada.add(this.energiaDespercicada);
        this.historicoAlpha.add(this.alphaUsuarios);
        this.historicoBetaTempoSim.add(this.betaTempoSim);
        this.historicoBetaTempoUso.add(this.betaTempoUso);
        this.historicoTempoInicial.add(this.tempoInicialExec);
        this.historicoTempoFinal.add(this.tempoFinalExec);
        this.historicoTurnaroundTime.add(this.turnaroundTime);
        this.historicoTempoSim.add(this.tempoSIM);
    }

    public General (
        final CloudQueueNetwork redeDeFilas, final double time, final List<GridTask> tarefas
    ) {
        this.numeroDeSimulacoes = 1;
        this.global = new Global(redeDeFilas, time, tarefas);
        this.metricasSatisfacao = new HashMap<>();
        this.tarefasConcluidas  = new HashMap<>();
        this.usuarios           = redeDeFilas.getUsuarios();
        for (final var user : this.usuarios) {
            this.metricasSatisfacao.put(user, 0.0);
            this.tarefasConcluidas.put(user, 0);
        }
        this.getMetricaFilaTarefaCloud(tarefas, redeDeFilas);
        this.getMetricaComunicacao(redeDeFilas);
        this.getMetricaProcessamentoCloud(redeDeFilas);
        this.getMetricaAlocacao(redeDeFilas);
        this.getMetricaCusto(redeDeFilas);
    }

    private void getMetricaFilaTarefa (
        final Collection<? extends GridTask> tarefas, final GridQueueNetwork rede
    ) {
        this.tempoMedioFilaComunicacao   = 0;
        this.tempoMedioComunicacao       = 0;
        this.tempoMedioFilaProcessamento = 0;
        this.tempoMedioProcessamento     = 0;
        this.numTarefasCanceladas        = 0;
        this.MflopsDesperdicio           = 0;
        this.numTarefas                  = 0;

        final double mediaPoder = rede
            .getMaquinas()
            .stream()
            .collect(Collectors.averagingDouble(Processing::getPoderComputacional));

        for (final var user : this.usuarios) {
            this.tarefasPreemp.put(user, 0);
            this.tarefasConcluidas.put(user, 0);
        }

        Double satis;
        for (final GridTask tar : tarefas) {
            if (tar.getEstado() == TaskState.DONE) {

                this.tempoMedioFilaComunicacao += tar.getMetricas().getTempoEsperaComu();
                this.tempoMedioComunicacao += tar.getMetricas().getTempoComunicacao();
                this.tempoMedioFilaProcessamento = tar.getMetricas().getTempoEsperaProc();
                this.tempoMedioProcessamento     = tar.getMetricas().getTempoProcessamento();
                this.numTarefas++;

                final var propTar = tar.getProprietario();
                this.tarefasConcluidas.put(propTar, this.tarefasConcluidas.get(propTar) + 1);

                // Instante de tempo de submissão da tarefa
                final Double tempoInicio = tar.getTimeCriacao();

                // Instante de tempo em que a terafa é concluída
                final var tempoFinal = tar.lastFinalizationTime();

                // Intervalo entre término e submissão
                final var intervaloReal = tempoFinal - tempoInicio;

                // Tempo de execução esperado pelo usuário, em que não há espera nem preempção, com a tarefa
                //  executando em uma máquina média do sistema
                final var intervaloIdeal = tar.getTamProcessamento() / mediaPoder;

                // Satisfação em relação a desempenho do usuário i, sobre a tarefa j submetida por i
                // Dividir os intervalos e multiplicar o resultado por 100
                final Double suij = (intervaloIdeal / intervaloReal) * 100.0;

                if (this.satisfacaoGeralSim.putIfAbsent(propTar, suij) != null) {
                    //Faz a primeira incersão testando se o hashmap está vazio e, se não estiver, entra no
                    // corpo no if

                    satis = this.satisfacaoGeralSim.get(propTar);
                    this.satisfacaoGeralSim.put(propTar, satis + suij);
                    this.satisfacaoGeralUso.put(propTar, this.satisfacaoGeralSim.get(propTar));
                }

                if (this.satisfacaoDesempenho.putIfAbsent(propTar, suij) != null) {
                    //Faz a primeira incersão testando se o hashmap está vazio e, se não estiver, entra no
                    // corpo no if

                    satis = this.satisfacaoDesempenho.get(propTar);
                    this.satisfacaoDesempenho.put(propTar, satis + suij);
                }

                int i;
                for (i = 0; i < tar.getHistoricoProcessamento().size(); i++) {

                    //Consumo da máquina corrente no histórico multiplicado pelo tempo em que permaneceu na máquina
                    final Double consumo =
                        tar.getHistoricoProcessamento().get(i).getConsumoEnergia() * (
                            tar.getTempoFinal().get(i) - tar.getTempoInicial().get(i)
                        );

                    final var propMaq = tar.getHistoricoProcessamento().get(i).getProprietario();

                    this.consumoTotalSistema += consumo;

                    final Double temp = this.consumoEnergiaTotalUsuario.get(propTar) + consumo;
                    this.consumoEnergiaTotalUsuario.put(propTar, temp);

                    this.consumoLocal.put(propMaq, this.consumoLocal.get(propMaq) + consumo);

                    if (propTar.equals(propMaq)) {
                        this.consumoLocalProprio.put(
                            propMaq,
                            this.consumoLocalProprio.get(propMaq) + consumo
                        );
                    } else {
                        this.consumoLocalEstrangeiro.put(
                            propMaq,
                            this.consumoLocalEstrangeiro.get(propMaq)
                            + consumo
                        );
                    }
                }

                if (tar.getHistoricoProcessamento().size() > 1) {
                    //Se a tarefa passou por mais de uma máquina, houve preempção dela
                    if (this.tarefasPreemp.putIfAbsent(propTar, 1) != null) {
                        //Faz a primeira incersão testando se o hashmap está vazio e, se não estiver, entra no
                        // corpo no if
                        this.tarefasPreemp.put(propTar, this.tarefasPreemp.get(propTar) + 1);
                    }

                    var mflopsProcessado = 0.0;
                    //Calcular Desperdício
                    for (i = 0; i < tar.getHistoricoProcessamento().size(); i++) {
                        mflopsProcessado +=
                            (tar.getTempoFinal().get(i) - tar.getTempoInicial().get(i)) * tar
                                .getHistoricoProcessamento()
                                .get(i)
                                .getPoderComputacional();
                        final Double tempoDesperdicio = mflopsProcessado / tar
                            .getHistoricoProcessamento()
                            .get(i)
                            .getPoderComputacional();
                        this.energiaDespercicada.put(
                            propTar,
                            this.energiaDespercicada.get(propTar) + (
                                tempoDesperdicio * tar
                                    .getHistoricoProcessamento()
                                    .get(i)
                                    .getConsumoEnergia()
                            )
                        );
                    }
                }
            } else if (tar.getEstado() == TaskState.CANCELLED) {
                this.MflopsDesperdicio += tar.getTamProcessamento() * tar.getMflopsProcessado();
                this.numTarefasCanceladas++;
            }
            //Rever, se for informação pertinente adicionar nas métricas da tarefa ou Processing e calcula
            // durante a simulação
            final var temp = (Processing) tar.getLocalProcessamento();
            if (temp != null) {
                for (var i = 0; i < tar.getTempoInicial().size(); i++) {
                    temp.setTempoProcessamento(
                        tar.getTempoInicial().get(i),
                        tar.getTempoFinal().get(i)
                    );
                }
            }
        }

        this.tempoSIM =
            tarefas.stream().mapToDouble(GridTask::lastFinalizationTime).max().orElse(-1.0);

        for (final var user : this.usuarios) {
            satis = this.satisfacaoGeralSim.get(user) / this.tarefasConcluidas.get(user);
            final var consMaxLocal  = this.consumoMaxLocal.get(user);
            final var limiteConsSim = this.limitesConsumoTempoSim.get(user);
            final var limiteConsUso = this.limitesConsumoTempoUso.get(user);

            this.satisfacaoGeralSim.put(user, satis);
            this.satisfacaoGeralUso.put(user, satis);
            this.satisfacaoDesempenho.put(user, satis);
            this.consumoMaxLocal.put(user, consMaxLocal * this.global.getTempoSimulacao());
            this.limitesConsumoTempoSim.put(
                user,
                limiteConsSim * this.global.getTempoSimulacao()
            );

            Double inicio = -1.0;
            Double fim    = -1.0;
            for (final GridTask tarefa : tarefas) {
                if (tarefa.getProprietario().equals(user)) {
                    if (inicio == -1.0 || fim == -1.0) {
                        inicio = tarefa.getTempoInicial().get(0);
                        fim = tarefa.lastFinalizationTime();
                    } else {
                        if (tarefa.getTempoInicial().get(0) < inicio) {
                            inicio = tarefa.getTempoInicial().get(0);
                        }
                        if (tarefa.lastFinalizationTime() > fim) {
                            fim = tarefa.lastFinalizationTime();
                        }
                    }

                    this.turnaroundTime.put(user, this.turnaroundTime.get(user) + (
                        tarefa.lastFinalizationTime() - tarefa.getTimeCriacao()
                    ));
                }
            }

            this.limitesConsumoTempoUso.put(user, limiteConsUso * (fim - inicio));
            this.tempoFinalExec.put(user, fim);
            this.tempoInicialExec.put(user, inicio);

            final double alpha;
            if (this.consumoLocal.get(user).compareTo(0.0) == 0) {
                alpha = 0.0;
            } else {
                alpha = this.consumoMaxLocal.get(user) / this.consumoLocal.get(user);
            }

            final double betaSim;
            if (this.limitesConsumoTempoSim.get(user).compareTo(0.0) == 0) {
                betaSim = 0.0;
            } else {
                betaSim = 1.0 + (
                    (
                        this.consumoTotalSistema - this.consumoEnergiaTotalUsuario.get(user)
                    ) / this.limitesConsumoTempoSim.get(user)
                );
            }

            this.alphaUsuarios.put(user, alpha);
            this.betaTempoSim.put(user, betaSim);

            final Double satisGeralSim = this.satisfacaoGeralSim.get(user) * (alpha * betaSim);

            this.satisfacaoGeralSim.put(user, satisGeralSim);
            this.betaTempoUso.put(user, betaSim);

            final Double satisGeralUso = this.satisfacaoGeralUso.get(user) * (alpha * betaSim);

            this.satisfacaoGeralUso.put(user, satisGeralUso);
            this.turnaroundTime.put(
                user,
                this.turnaroundTime.get(user) / this.tarefasConcluidas.get(user)
            );
        }

        this.tempoMedioFilaComunicacao   = this.tempoMedioFilaComunicacao / this.numTarefas;
        this.tempoMedioComunicacao       = this.tempoMedioComunicacao / this.numTarefas;
        this.tempoMedioFilaProcessamento = this.tempoMedioFilaProcessamento / this.numTarefas;
        this.tempoMedioProcessamento     = this.tempoMedioProcessamento / this.numTarefas;
    }

    private void getMetricaComunicacao (final GridQueueNetwork gridQueueNetwork) {
        this.metricasComunicacao = new HashMap<>();
        for (final Communication link : gridQueueNetwork.getInternets()) {
            this.metricasComunicacao.put(link.id(), link.getMetrica());
        }
        for (final var link : gridQueueNetwork.getLinks()) {
            this.metricasComunicacao.put(link.id(), link.getMetrica());
        }
    }

    private void getMetricaProcessamento (final GridQueueNetwork gridQueueNetwork) {
        this.metricasProcessamento = new HashMap<>();
        for (final var maq : gridQueueNetwork.getMestres()) {
            this.metricasProcessamento.put(maq.id() + maq.getnumeroMaquina(), maq.getMetrica());
        }
        for (final Processing maq : gridQueueNetwork.getMaquinas()) {
            this.metricasProcessamento.put(maq.id() + maq.getnumeroMaquina(), maq.getMetrica());
        }
    }

    private void getMetricaFilaTarefaCloud (
        final Iterable<GridTask> tarefas, final CloudQueueNetwork rede
    ) {
        this.tempoMedioFilaComunicacao   = 0;
        this.tempoMedioComunicacao       = 0;
        this.tempoMedioFilaProcessamento = 0;
        this.tempoMedioProcessamento     = 0;
        this.numTarefasCanceladas        = 0;
        this.MflopsDesperdicio           = 0;
        this.numTarefas                  = 0;

        final var mediaPoder = rede
            .getVMs()
            .stream()
            .collect(Collectors.averagingDouble(Processing::getPoderComputacional));

        for (final var no : tarefas) {
            if (no.getEstado() == TaskState.DONE) {

                final Double suij = (
                                        no.getTamProcessamento() / mediaPoder / (
                                            no.lastFinalizationTime() - no.getTimeCriacao()
                                        )
                                    ) * 100;
                this.metricasSatisfacao.put(
                    no.getProprietario(),
                    suij + this.metricasSatisfacao.get(no.getProprietario())
                );
                this.tarefasConcluidas.put(
                    no.getProprietario(),
                    1 + this.tarefasConcluidas.get(no.getProprietario())
                );
            }
            if (no.getEstado() == TaskState.DONE) {
                this.tempoMedioFilaComunicacao += no.getMetricas().getTempoEsperaComu();
                this.tempoMedioComunicacao += no.getMetricas().getTempoComunicacao();
                this.tempoMedioFilaProcessamento = no.getMetricas().getTempoEsperaProc();
                this.tempoMedioProcessamento     = no.getMetricas().getTempoProcessamento();
                this.numTarefas++;
            } else if (no.getEstado() == TaskState.CANCELLED) {
                this.MflopsDesperdicio += no.getTamProcessamento() * no.getMflopsProcessado();
                this.numTarefasCanceladas++;
            }
            //Rever, se for informação pertinente adicionar nas métricas da tarefa ou Processing e calcula
            // durante a simulação
            final var temp = (Processing) no.getLocalProcessamento();
            if (temp != null) {
                for (var i = 0; i < no.getTempoInicial().size(); i++) {
                    temp.setTempoProcessamento(
                        no.getTempoInicial().get(i),
                        no.getTempoFinal().get(i)
                    );
                }
            }
        }

        for (final var entry : this.metricasSatisfacao.entrySet()) {
            final var string = entry.getKey();
            entry.setValue(entry.getValue() / this.tarefasConcluidas.get(string));
        }

        this.tempoMedioFilaComunicacao   = this.tempoMedioFilaComunicacao / this.numTarefas;
        this.tempoMedioComunicacao       = this.tempoMedioComunicacao / this.numTarefas;
        this.tempoMedioFilaProcessamento = this.tempoMedioFilaProcessamento / this.numTarefas;
        this.tempoMedioProcessamento     = this.tempoMedioProcessamento / this.numTarefas;
    }

    private void getMetricaProcessamentoCloud (final CloudQueueNetwork redeDeFilas) {
        this.metricasProcessamento = new HashMap<>();
        for (final var maq : redeDeFilas.getMestres()) {
            this.metricasProcessamento.put(maq.id() + maq.getnumeroMaquina(), maq.getMetrica());
        }
        for (final Processing maq : redeDeFilas.getVMs()) {
            this.metricasProcessamento.put(maq.id() + maq.getnumeroMaquina(), maq.getMetrica());
        }
    }

    private void getMetricaAlocacao (final CloudQueueNetwork redeDeFilas) {
        this.metricasAlocacao = new HashMap<>();
        //percorre as máquinas recolhendo as métricas de alocação
        for (final var maq : redeDeFilas.getMaquinasCloud()) {
            this.metricasAlocacao.put(maq.id() + maq.getnumeroMaquina(), maq.getMetricaAloc());
        }
        //insere nas métricas as VMs que não foram alocadas
        final var mtRej = new Allocation("Rejected");
        for (final var mst : redeDeFilas.getMestres()) {
            final var aux = (CloudMaster) mst;
            IntStream
                .range(0, aux.getAlocadorVM().getVMsRejeitadas().size())
                .forEach(i -> mtRej.incVMsAlocadas());
        }
        this.metricasAlocacao.put("Rejected", mtRej);
    }

    private void getMetricaCusto (final CloudQueueNetwork redeDeFilas) {
        this.metricasCusto = new HashMap<>();
        //percorre as vms inserindo as métricas de custo
        for (final var vm : redeDeFilas.getVMs()) {
            if (vm.getStatus() == VirtualMachineState.DESTROYED) {
                this.metricasCusto.put(vm.id() + vm.getnumeroMaquina(), vm.getMetricaCusto());
            }
        }
    }

    public void addMetrica (final General metrica) {
        this.addMetricasGlobais(metrica.global);
        this.addMetricaFilaTarefa(metrica);
        this.addMetricaComunicacao(metrica.metricasComunicacao);
        this.addMetricaProcessamento(metrica.metricasProcessamento);
        this.numeroDeSimulacoes += metrica.numeroDeSimulacoes;

        this.addMetricaSatisfacaoGeralTempoSim(metrica.satisfacaoGeralSim);
        this.addMetricaConsumo(metrica.consumoEnergiaTotalUsuario);
        this.historicoSatisfacaoGeralTempoUso.add(metrica.satisfacaoGeralUso);
        this.historicoTarefasPreemp.add(metrica.tarefasPreemp);
        this.historicoSatisfacaoDesempenho.add(metrica.satisfacaoDesempenho);
        this.historicoConsumoLocal.add(metrica.consumoLocal);
        this.historicoConsumoMaxLocal.add(metrica.consumoMaxLocal);
        this.historicoLimitesConsumoTempoSim.add(metrica.limitesConsumoTempoSim);
        this.historicoLimitesConsumoTempoUso.add(metrica.limitesConsumoTempoUso);
        this.historicoConsumoTotalSistema.add(metrica.consumoTotalSistema);
        this.historicoConsumoLocalEstrangeiro.add(metrica.consumoLocalEstrangeiro);
        this.historicoConsumoLocalProprio.add(metrica.consumoLocalProprio);
        this.historicoEnergiaDeperdicada.add(metrica.energiaDespercicada);
        this.historicoAlpha.add(metrica.alphaUsuarios);
        this.historicoBetaTempoSim.add(metrica.betaTempoSim);
        this.historicoBetaTempoUso.add(metrica.betaTempoUso);
        this.historicoTempoFinal.add(metrica.tempoFinalExec);
        this.historicoTempoInicial.add(metrica.tempoInicialExec);
        this.historicoTurnaroundTime.add(metrica.turnaroundTime);
        this.historicoTempoSim.add(metrica.tempoSIM);
    }

    private void addMetricasGlobais (final Global global) {
        this.global.setTempoSimulacao(this.global.getTempoSimulacao()
                                      + global.getTempoSimulacao());
        this.global.setSatisfacaoMedia(this.global.getSatisfacaoMedia()
                                       + global.getSatisfacaoMedia());
        this.global.setOciosidadeComputacao(this.global.getOciosidadeComputacao()
                                            + global.getOciosidadeComputacao());
        this.global.setOciosidadeComunicacao(this.global.getOciosidadeComunicacao()
                                             + global.getOciosidadeComunicacao());
        this.global.setEficiencia(this.global.getEficiencia()
                                  + global.getEficiencia());
    }

    private void addMetricaFilaTarefa (final General metrica) {
        this.tempoMedioFilaComunicacao += metrica.tempoMedioFilaComunicacao;
        this.tempoMedioComunicacao += metrica.tempoMedioComunicacao;
        this.tempoMedioFilaProcessamento += metrica.tempoMedioFilaProcessamento;
        this.tempoMedioProcessamento += metrica.tempoMedioFilaProcessamento;
        this.MflopsDesperdicio += metrica.MflopsDesperdicio;
        this.numTarefasCanceladas += metrica.numTarefasCanceladas;
    }

    private void addMetricaComunicacao (final Map<String, CommunicationMetrics> metricasComunicacao) {
        if (this.numeroDeSimulacoes == 0) {
            this.metricasComunicacao = metricasComunicacao;
        } else {
            for (final var entry : metricasComunicacao.entrySet()) {
                final var key  = entry.getKey();
                final var item = entry.getValue();
                final var base = this.metricasComunicacao.get(key);
                base.incMbitsTransmitidos(item.getMbitsTransmitidos());
                base.incSegundosDeTransmissao(item.getSegundosDeTransmissao());
            }
        }
    }

    private void addMetricaProcessamento (final Map<String, ProcessingMetrics> metricasProcessamento) {
        if (this.numeroDeSimulacoes == 0) {
            this.metricasProcessamento = metricasProcessamento;
        } else {
            for (final var entry : metricasProcessamento.entrySet()) {
                final var key  = entry.getKey();
                final var item = entry.getValue();
                final var base = this.metricasProcessamento.get(key);
                base.incMflopsProcessados(item.getMFlopsProcessados());
                base.incSegundosDeProcessamento(item.getSegundosDeProcessamento());
            }
        }
    }

    private void addMetricaSatisfacaoGeralTempoSim (final Map<String, Double> metricasSatisfacao) {
        this.historicoSatisfacaoGeralTempoSim.add(metricasSatisfacao);
    }

    private void addMetricaConsumo (final Map<String, Double> metricasConsumo) {
        this.historicoConsumoTotalUsuario.add(metricasConsumo);
    }

    public Global getMetricasGlobais () {
        return this.global;
    }

    public Map<String, CommunicationMetrics> getMetricasComunicacao () {
        return this.metricasComunicacao;
    }

    public Map<String, ProcessingMetrics> getMetricasProcessamento () {
        return this.metricasProcessamento;
    }

    /**
     * It creates the resources table. The resources table contains results of performed computation
     * for each machine adn performed communication for each network link.
     *
     * @return a table containing information about performed computation for each machine and
     * performed communication for each network link.
     */
    public Object[][] makeResourceTable () {
        final var table = new ArrayList<Object[]>();

        /* Add table entries for processing metrics */
        if (this.metricasProcessamento != null) {
            for (final var processingMetrics : this.metricasProcessamento.values()) {
                final String name;

                if (processingMetrics.getnumeroMaquina() == 0) {
                    name = processingMetrics.getId();
                } else {
                    name =
                        processingMetrics.getId() + " node " + processingMetrics.getnumeroMaquina();
                }

                table.add(Arrays.asList(
                    name,
                    processingMetrics.getProprietario(),
                    processingMetrics.getSegundosDeProcessamento(),
                    0.0d
                ).toArray());
            }
        }

        /* Add table entries for communication metrics */
        if (this.metricasComunicacao != null) {
            for (final var communicationMetrics : this.metricasComunicacao.values()) {

                table.add(Arrays.asList(
                    communicationMetrics.getId(),
                    "---",
                    0.0d,
                    communicationMetrics.getSegundosDeTransmissao()
                ).toArray());
            }
        }

        return table.toArray(Object[][]::new);
    }

    public Map<String, Allocation> getMetricasAlocacao () {
        return this.metricasAlocacao;
    }

    public Map<String, Cost> getMetricasCusto () {
        return this.metricasCusto;
    }

    public Map<String, Double> getMetricasSatisfacao () {
        return this.metricasSatisfacao;
    }

    public double getTempoMedioFilaComunicacao () {
        return this.tempoMedioFilaComunicacao;
    }

    public double getTempoMedioComunicacao () {
        return this.tempoMedioComunicacao;
    }

    public double getTempoMedioFilaProcessamento () {
        return this.tempoMedioFilaProcessamento;
    }

    public double getTempoMedioProcessamento () {
        return this.tempoMedioProcessamento;
    }

    public double getMflopsDesperdicio () {
        return this.MflopsDesperdicio;
    }

    public int getNumTarefasCanceladas () {
        return this.numTarefasCanceladas;
    }

    public void calculaMedia () {
        //Média das Metricas Globais
        this.global.setTempoSimulacao(this.global.getTempoSimulacao()
                                      / this.numeroDeSimulacoes);
        this.global.setSatisfacaoMedia(this.global.getSatisfacaoMedia()
                                       / this.numeroDeSimulacoes);
        this.global.setOciosidadeComputacao(this.global.getOciosidadeComputacao()
                                            / this.numeroDeSimulacoes);
        this.global.setOciosidadeComunicacao(this.global.getOciosidadeComunicacao()
                                             / this.numeroDeSimulacoes);
        this.global.setEficiencia(this.global.getEficiencia()
                                  / this.numeroDeSimulacoes);
        //Média das Metricas da rede de filas
        this.tempoMedioFilaComunicacao   = this.tempoMedioFilaComunicacao / this.numeroDeSimulacoes;
        this.tempoMedioComunicacao       = this.tempoMedioComunicacao / this.numeroDeSimulacoes;
        this.tempoMedioFilaProcessamento =
            this.tempoMedioFilaProcessamento / this.numeroDeSimulacoes;
        this.tempoMedioProcessamento     =
            this.tempoMedioFilaProcessamento / this.numeroDeSimulacoes;
        this.MflopsDesperdicio           = this.MflopsDesperdicio / this.numeroDeSimulacoes;
        this.numTarefasCanceladas        = this.numTarefasCanceladas / this.numeroDeSimulacoes;
        //Média das Metricas de Comunicação
        for (final var entry : this.metricasComunicacao.entrySet()) {
            final var item = entry.getValue();
            item.setMbitsTransmitidos(item.getMbitsTransmitidos() / this.numeroDeSimulacoes);
            item.setSegundosDeTransmissao(item.getSegundosDeTransmissao()
                                          / this.numeroDeSimulacoes);
        }
        //Média das Metricas de Processamento
        for (final var entry : this.metricasProcessamento.entrySet()) {
            final var item = entry.getValue();
            item.setMflopsProcessados(item.getMFlopsProcessados() / this.numeroDeSimulacoes);
            item.setSegundosDeProcessamento(item.getSegundosDeProcessamento()
                                            / this.numeroDeSimulacoes);
        }

        System.out.printf(
            "Usuário \t SatisfaçãoGeralTempoSim\tSatisfaçãoGeralTempoUso\tSatisfaçãoDesempenho\tConsumoTotal\tLimiteConsumoTempoSimulado\tLimiteConsumoTempoUso\tConsumoLocal\tConsumoLocalProprio\tConsumoLocalEstrangeiro\tConsumoMaxLocal\tTarefasPreemp\tAlpha\tBetaTempoSim\tBetaTempoUso\tDesperdicio\tConsumoKJS\tTurnaroundTime\tTempoSIM%n");

        for (final var usuario : this.usuarios) {
            for (var i = 0; i < this.numeroDeSimulacoes; i++) {
                System.out.printf(
                    "%s\t%.2f\t%.2f\t%.2f\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%.0f\t%.0f\t%.0f\t%s\t%s\t%s\t%s%n",
                    usuario,
                    this.historicoSatisfacaoGeralTempoSim.get(i).get(usuario),
                    this.historicoSatisfacaoGeralTempoUso.get(i).get(usuario),
                    this.historicoSatisfacaoDesempenho.get(i).get(usuario),
                    String.format(
                        "%.2f",
                        this.historicoConsumoTotalUsuario.get(i).get(usuario) / 1_000_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoLimitesConsumoTempoSim.get(i).get(usuario)
                        / 1_000_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoLimitesConsumoTempoUso.get(i).get(usuario)
                        / 1_000_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoLocal.get(i).get(usuario)
                        / this.historicoTempoSim.get(i)
                        / 1_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoLocalProprio.get(i).get(usuario)
                        / this.historicoTempoSim.get(i)
                        / 1000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoLocalEstrangeiro.get(i).get(usuario)
                        / this.historicoTempoSim.get(i)
                        / 1_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoMaxLocal.get(i).get(usuario) / 1_000_000
                    ),
                    this.historicoTarefasPreemp.get(i).get(usuario),
                    this.historicoAlpha.get(i).get(usuario),
                    this.historicoBetaTempoSim.get(i).get(usuario),
                    this.historicoBetaTempoUso.get(i).get(usuario),
                    String.format(
                        "%.2f",
                        this.historicoEnergiaDeperdicada.get(i).get(usuario) / 1_000_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoTotalUsuario.get(i).get(usuario)
                        / this.historicoTempoSim.get(i)
                        / 1_000
                    ),
                    this.historicoTurnaroundTime.get(i).get(usuario),
                    this.historicoTempoSim.get(i)
                );
            }
            System.out.println();
        }
        System.out.println("\n\n");
    }

    public boolean hasCancelledTasks () {
        return this.numTarefasCanceladas > 0;
    }
}

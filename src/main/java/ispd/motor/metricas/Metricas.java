package ispd.motor.metricas;

import ispd.motor.filas.*;
import ispd.motor.filas.servidores.*;
import ispd.motor.filas.servidores.implementacao.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.util.stream.*;

public class Metricas implements Serializable {

    /**
     * Armazena métricas obtidas da simulação
     */
    private final MetricasGlobais metricasGlobais;

    private final List<String> usuarios;

    private int numeroDeSimulacoes;

    private Map<String, MetricasComunicacao> metricasComunicacao = null;

    private Map<String, MetricasProcessamento> metricasProcessamento = null;

    private Map<String, MetricasAlocacao> metricasAlocacao = null;

    private Map<String, MetricasCusto> metricasCusto = null;

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
    private Map<String, BigDecimal> satisfacaoGeralSim = null;

    /**
     * Satisfação geral do usuário considerando beta calculado com tempo de uso
     */
    private Map<String, BigDecimal> satisfacaoGeralUso = null;

    /**
     * Consumo de energia em Joules, total do usuário
     */
    private Map<String, BigDecimal> consumoEnergiaTotalUsuario = null;

    /**
     * Satisfação sobre desempenho, idêntica à utilizada no HOSEP
     */
    private Map<String, BigDecimal> satisfacaoDesempenho = null;

    /**
     * Número de tarefas que sofreram preempção
     */
    private Map<String, Integer> tarefasPreemp = null;

    /**
     * Consumo da porção de cada usuário
     */
    private Map<String, BigDecimal> consumoLocal = null;

    /**
     * Consumo total do sistema
     */
    private BigDecimal consumoTotalSistema = null;

    /**
     * Consumo máximo da porção de cada usuário, como se a porção ficasse ativa por completo durante
     * toda a simulação
     */
    private Map<String, BigDecimal> consumoMaxLocal = null;

    /**
     * Limite de consumo em Joules, considerando o tempo total de simulação
     */
    private Map<String, BigDecimal> limitesConsumoTempoUso = null;

    /**
     * Energia consumida na porção com tarefas de usuários não proprietários
     */
    private Map<String, BigDecimal> consumoLocalEstrangeiro = null;

    /**
     * Energia consumida na porção com tarefas do usuário proprietário
     */
    private Map<String, BigDecimal> consumoLocalProprio = null;

    /**
     * Energia desperdiçada para cada usuário
     */
    private Map<String, BigDecimal> energiaDespercicada = null;

    /**
     * Limite de consumo em Joules, considerando em o usuário teve tarefas no sistema
     */
    private Map<String, BigDecimal> limitesConsumoTempoSim = null;

    /**
     * Energia desperdiçada para cada usuário
     */
    private Map<String, BigDecimal> alphaUsuarios = null;

    /**
     * Energia desperdiçada para cada usuário
     */
    private Map<String, BigDecimal> betaTempoSim = null;

    /**
     * Energia desperdiçada para cada usuário
     */
    private Map<String, BigDecimal> betaTempoUso = null;

    private Map<String, BigDecimal> tempoInicialExec = null;

    private Map<String, BigDecimal> tempoFinalExec = null;

    private Map<String, BigDecimal> turnaroundTime = null;

    private Double tempoSIM = null;

    private List<Map<String, BigDecimal>> historicoConsumoTotalUsuario = null;

    private List<Map<String, BigDecimal>> historicoSatisfacaoGeralTempoSim = null;

    private List<Map<String, BigDecimal>> historicoSatisfacaoGeralTempoUso = null;

    private List<Map<String, BigDecimal>> historicoConsumoLocal = null;

    private List<Map<String, BigDecimal>> historicoSatisfacaoDesempenho = null;

    private List<Map<String, Integer>> historicoTarefasPreemp = null;

    private List<Map<String, BigDecimal>> historicoConsumoMaxLocal = null;

    private List<BigDecimal> historicoConsumoTotalSistema = null;

    private List<Map<String, BigDecimal>> historicoLimitesConsumoTempoSim = null;

    private List<Map<String, BigDecimal>> historicoLimitesConsumoTempoUso = null;

    private List<Map<String, BigDecimal>> historicoEnergiaDeperdicada = null;

    private List<Map<String, BigDecimal>> historicoAlpha = null;

    private List<Map<String, BigDecimal>> historicoBetaTempoSim = null;

    private List<Map<String, BigDecimal>> historicoBetaTempoUso = null;

    private List<Map<String, BigDecimal>> historicoConsumoLocalProprio = null;

    private List<Map<String, BigDecimal>> historicoConsumoLocalEstrangeiro = null;

    private List<Map<String, BigDecimal>> historicoTempoInicial = null;

    private List<Map<String, BigDecimal>> historicoTempoFinal = null;

    private List<Map<String, BigDecimal>> historicoTurnaroundTime = null;

    private List<Double> historicoTempoSim = null;

    public Metricas (final List<String> usuarios) {

        this.numeroDeSimulacoes = 0;
        this.metricasGlobais    = new MetricasGlobais();
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
        this.tempoInicialExec           = new HashMap<>();
        this.tempoFinalExec             = new HashMap<>();
        this.turnaroundTime             = new HashMap<>();

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
        this.historicoTempoInicial            = new ArrayList<>();
        this.historicoTempoFinal              = new ArrayList<>();
        this.historicoTurnaroundTime          = new ArrayList<>();
        this.historicoTempoSim                = new ArrayList<>();
    }

    public Metricas (final RedeDeFilas redeDeFilas, final double time, final List<Tarefa> tarefas) {
        this.numeroDeSimulacoes = 1;
        this.metricasGlobais    = new MetricasGlobais(redeDeFilas, time, tarefas);
        this.tarefasConcluidas  = new HashMap<>();
        this.usuarios           = redeDeFilas.getUsuarios();

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
        this.tempoInicialExec           = new HashMap<>();
        this.tempoFinalExec             = new HashMap<>();
        this.turnaroundTime             = new HashMap<>();
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
        this.historicoTempoInicial            = new ArrayList<>();
        this.historicoTempoFinal              = new ArrayList<>();
        this.historicoTurnaroundTime          = new ArrayList<>();
        this.historicoTempoSim                = new ArrayList<>();
        this.consumoTotalSistema              = BigDecimal.ZERO;

        for (final var user : this.usuarios) {
            this.satisfacaoDesempenho.put(user, BigDecimal.ZERO);
            this.satisfacaoGeralSim.put(user, BigDecimal.ZERO);
            this.satisfacaoGeralUso.put(user, BigDecimal.ZERO);
            this.consumoEnergiaTotalUsuario.put(user, BigDecimal.ZERO);
            this.consumoLocal.put(user, BigDecimal.ZERO);
            this.consumoMaxLocal.put(user, BigDecimal.ZERO);
            this.limitesConsumoTempoSim.put(user, BigDecimal.ZERO);
            this.limitesConsumoTempoUso.put(user, BigDecimal.ZERO);
            this.energiaDespercicada.put(user, BigDecimal.ZERO);
            this.tarefasPreemp.put(user, 0);
            this.consumoLocalEstrangeiro.put(user, BigDecimal.ZERO);
            this.consumoLocalProprio.put(user, BigDecimal.ZERO);
            this.alphaUsuarios.put(user, BigDecimal.ZERO);
            this.betaTempoSim.put(user, BigDecimal.ZERO);
            this.betaTempoUso.put(user, BigDecimal.ZERO);
            this.tempoInicialExec.put(user, BigDecimal.ZERO);
            this.tempoFinalExec.put(user, BigDecimal.ZERO);
            this.turnaroundTime.put(user, BigDecimal.ZERO);
        }

        for (final CS_Processamento maq : redeDeFilas.getMaquinas()) {
            final var propMaq = maq.getProprietario();
            this.consumoMaxLocal.put(
                propMaq,
                this.consumoMaxLocal.get(propMaq).add(BigDecimal.valueOf(maq.getConsumoEnergia()))
            );

            this.limitesConsumoTempoSim.put(
                propMaq,
                this.limitesConsumoTempoSim
                    .get(propMaq)
                    .add(BigDecimal.valueOf(maq.getConsumoEnergia()))
            );
        }

        final var limits = redeDeFilas.getLimites();

        for (final var user : this.usuarios) {
            if (!limits.containsKey(user)) {
                throw new IllegalArgumentException("Unexpected user " + user);
            }

            // Limite de consumo em porcentagem do consumo total da porção do usuário
            final var porcentLimite = limits.get(user) / 100;
            this.limitesConsumoTempoSim.put(
                user,
                this.limitesConsumoTempoSim.get(user).multiply(BigDecimal.valueOf(porcentLimite))
            );
            this.limitesConsumoTempoUso.put(user, this.limitesConsumoTempoSim.get(user));
        }

        this.getMetricaFilaTarefa(tarefas, redeDeFilas);
        this.getMetricaComunicacao(redeDeFilas);
        this.getMetricaProcessamento(redeDeFilas);

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

    public Metricas (
        final RedeDeFilasCloud redeDeFilas,
        final double time,
        final List<Tarefa> tarefas
    ) {
        this.numeroDeSimulacoes = 1;
        this.metricasGlobais    = new MetricasGlobais(redeDeFilas, time, tarefas);
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
        final List<? extends Tarefa> tarefas,
        final RedeDeFilas rede
    ) {
        this.tempoMedioFilaComunicacao   = 0;
        this.tempoMedioComunicacao       = 0;
        this.tempoMedioFilaProcessamento = 0;
        this.tempoMedioProcessamento     = 0;
        this.numTarefasCanceladas        = 0;
        this.MflopsDesperdicio           = 0;
        this.numTarefas                  = 0;

        final double mediaPoder = rede.getMaquinas().stream()
            .collect(Collectors.averagingDouble(CS_Processamento::getPoderComputacional));

        for (final var user : this.usuarios) {
            this.tarefasPreemp.put(user, 0);
            this.tarefasConcluidas.put(user, 0);
        }

        BigDecimal satis;
        for (final Tarefa tar : tarefas) {
            if (tar.getEstado() == TaskState.DONE) {

                this.tempoMedioFilaComunicacao += tar.getMetricas().getTempoEsperaComu();
                this.tempoMedioComunicacao += tar.getMetricas().getTempoComunicacao();
                this.tempoMedioFilaProcessamento = tar.getMetricas().getTempoEsperaProc();
                this.tempoMedioProcessamento     = tar.getMetricas().getTempoProcessamento();
                this.numTarefas++;

                final var propTar = tar.getProprietario();
                this.tarefasConcluidas.put(propTar, this.tarefasConcluidas.get(propTar) + 1);

                // Instante de tempo de submissão da tarefa
                final var tempoInicio = BigDecimal.valueOf(tar.getTimeCriacao());

                // Instante de tempo em que a terafa é concluída
                final var tempoFinal =
                    BigDecimal.valueOf(tar.getTempoFinal().get(tar.getTempoFinal().size() - 1));

                // Intervalo entre término e submissão
                final var intervaloReal = tempoFinal.subtract(tempoInicio);

                // Tempo de execução esperado pelo usuário, em que não há espera nem preempção, com a tarefa
                //  executando em uma máquina média do sistema
                final var intervaloIdeal =
                    BigDecimal.valueOf(tar.getTamProcessamento() / mediaPoder);

                // Satisfação em relação a desempenho do usuário i, sobre a tarefa j submetida por i
                // Dividir os intervalos e multiplicar o resultado por 100
                final var suij =
                    (intervaloIdeal.divide(intervaloReal, 2, RoundingMode.DOWN)).multiply(
                        BigDecimal.valueOf(100.0));

                if (this.satisfacaoGeralSim.putIfAbsent(propTar, suij) != null) {
                    //Faz a primeira incersão testando se o hashmap está vazio e, se não estiver, entra no
                    // corpo no if

                    satis = this.satisfacaoGeralSim.get(propTar);
                    this.satisfacaoGeralSim.put(propTar, satis.add(suij));
                    this.satisfacaoGeralUso.put(propTar, this.satisfacaoGeralSim.get(propTar));
                }

                if (this.satisfacaoDesempenho.putIfAbsent(propTar, suij) != null) {
                    //Faz a primeira incersão testando se o hashmap está vazio e, se não estiver, entra no
                    // corpo no if

                    satis = this.satisfacaoDesempenho.get(propTar);
                    this.satisfacaoDesempenho.put(propTar, satis.add(suij));
                }

                int i;
                for (i = 0; i < tar.getHistoricoProcessamento().size(); i++) {

                    //Consumo da máquina corrente no histórico multiplicado pelo tempo em que permaneceu na máquina
                    final var consumo = BigDecimal.valueOf(tar
                                                               .getHistoricoProcessamento()
                                                               .get(i)
                                                               .getConsumoEnergia()
                                                           * (
                                                               tar.getTempoFinal().get(i) - tar
                                                                   .getTempoInicial()
                                                                   .get(i)
                                                           )
                    );

                    final var propMaq = tar.getHistoricoProcessamento().get(i).getProprietario();

                    this.consumoTotalSistema = this.consumoTotalSistema.add(consumo);

                    final var temp =
                        this.consumoEnergiaTotalUsuario.get(propTar).add(consumo);
                    this.consumoEnergiaTotalUsuario.put(propTar, temp);

                    this.consumoLocal.put(propMaq, this.consumoLocal.get(propMaq).add(consumo));

                    if (propTar.equals(propMaq)) {
                        this.consumoLocalProprio.put(
                            propMaq,
                            this.consumoLocalProprio
                                .get(propMaq)
                                .add(consumo)
                        );
                    } else {
                        this.consumoLocalEstrangeiro.put(
                            propMaq, this.consumoLocalEstrangeiro.get(propMaq).add(consumo));
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
                            (tar.getTempoFinal().get(i) - tar.getTempoInicial().get(i)) *
                            tar.getHistoricoProcessamento().get(i).getPoderComputacional();
                        final Double tempoDesperdicio = (
                            mflopsProcessado / tar
                                .getHistoricoProcessamento()
                                .get(i)
                                .getPoderComputacional()
                        );
                        this.energiaDespercicada.put(
                            propTar,
                            this.energiaDespercicada
                                .get(propTar)
                                .add(BigDecimal.valueOf(tempoDesperdicio * tar
                                    .getHistoricoProcessamento().get(i).getConsumoEnergia()))
                        );
                    }
                }
            } else if (tar.getEstado() == TaskState.CANCELLED) {
                this.MflopsDesperdicio += tar.getTamProcessamento() * tar.getMflopsProcessado();
                this.numTarefasCanceladas++;
            }
            //Rever, se for informação pertinente adicionar nas métricas da tarefa ou CS_Processamento e calcula
            // durante a simulação
            final var temp = (CS_Processamento) tar.getLocalProcessamento();
            if (temp != null) {
                for (var i = 0; i < tar.getTempoInicial().size(); i++) {
                    temp.setTempoProcessamento(
                        tar.getTempoInicial().get(i),
                        tar.getTempoFinal().get(i)
                    );
                }
            }
        }

        this.tempoSIM = -1.0;
        for (final Tarefa tarefa : tarefas) {
            if (this.tempoSIM == -1.0) {
                this.tempoSIM =
                    tarefa.getTempoFinal().get(tarefa.getTempoFinal().size() - 1);
            } else {
                if (tarefa.getTempoFinal().get(tarefa.getTempoFinal().size() - 1)
                    > this.tempoSIM) {
                    this.tempoSIM = tarefa
                        .getTempoFinal()
                        .get(tarefa.getTempoFinal().size() - 1);
                }
            }
        }

        for (final var user : this.usuarios) {
            satis = this.satisfacaoGeralSim.get(user)
                .divide(
                    BigDecimal.valueOf(this.tarefasConcluidas.get(user)), 2,
                    RoundingMode.DOWN
                );
            final var consMaxLocal  = this.consumoMaxLocal.get(user);
            final var limiteConsSim = this.limitesConsumoTempoSim.get(user);
            final var limiteConsUso = this.limitesConsumoTempoUso.get(user);

            this.satisfacaoGeralSim.put(user, satis);
            this.satisfacaoGeralUso.put(user, satis);
            this.satisfacaoDesempenho.put(user, satis);
            this.consumoMaxLocal.put(
                user,
                consMaxLocal.multiply(BigDecimal.valueOf(this.metricasGlobais.getTempoSimulacao()))
            );
            this.limitesConsumoTempoSim.put(
                user,
                limiteConsSim.multiply(BigDecimal.valueOf(this.metricasGlobais.getTempoSimulacao()))
            );

            Double inicio = -1.0;
            Double fim    = -1.0;
            for (final Tarefa tarefa : tarefas) {
                if (tarefa.getProprietario().equals(user)) {
                    if (inicio == -1.0 || fim == -1.0) {
                        inicio = tarefa.getTempoInicial().get(0);
                        fim    = tarefa
                            .getTempoFinal()
                            .get(tarefa.getTempoFinal().size() - 1);
                    } else {
                        if (tarefa.getTempoInicial().get(0) < inicio) {
                            inicio = tarefa.getTempoInicial().get(0);
                        }
                        if (tarefa
                                .getTempoFinal()
                                .get(tarefa.getTempoFinal().size() - 1) > fim) {
                            fim = tarefa
                                .getTempoFinal()
                                .get(tarefa.getTempoFinal().size() - 1);
                        }
                    }

                    this.turnaroundTime.put(
                        user,
                        this.turnaroundTime.get(user).add(BigDecimal.valueOf(
                            tarefa
                                .getTempoFinal()
                                .get(tarefa.getTempoFinal().size() - 1)
                            -
                            tarefa.getTimeCriacao()))
                    );
                }
            }

            this.limitesConsumoTempoUso.put(
                user,
                limiteConsUso.multiply(BigDecimal.valueOf(fim - inicio))
            );
            this.tempoFinalExec.put(user, BigDecimal.valueOf(fim));
            this.tempoInicialExec.put(user, BigDecimal.valueOf(inicio));

            final BigDecimal alpha;
            if (this.consumoLocal.get(user).compareTo(BigDecimal.ZERO) == 0) {
                alpha = BigDecimal.ZERO;
            } else {
                alpha = this.consumoMaxLocal
                    .get(user)
                    .divide(this.consumoLocal.get(user), 2, RoundingMode.DOWN);
            }

            final BigDecimal betaSim;
            if (this.limitesConsumoTempoSim.get(user).compareTo(BigDecimal.ZERO) == 0) {
                betaSim = BigDecimal.ZERO;
            } else {
                betaSim = (
                    (
                        (
                            this.consumoEnergiaTotalUsuario
                                .get(user)
                                .negate()
                        ).add(this.consumoTotalSistema)
                    ).divide(
                        this.limitesConsumoTempoSim.get(user), 2, RoundingMode.DOWN)
                ).add(BigDecimal.ONE);
            }

            this.alphaUsuarios.put(user, alpha);
            this.betaTempoSim.put(user, betaSim);

            final var satisGeralSim =
                (this.satisfacaoGeralSim.get(user)).multiply((alpha.multiply(betaSim)));

            this.satisfacaoGeralSim.put(user, satisGeralSim);
            this.betaTempoUso.put(user, betaSim);

            final var satisGeralUso =
                (this.satisfacaoGeralUso.get(user)).multiply((alpha.multiply(betaSim)));

            this.satisfacaoGeralUso.put(user, satisGeralUso);
            this.turnaroundTime.put(user, this.turnaroundTime.get(user).divide(
                BigDecimal.valueOf(this.tarefasConcluidas.get(user)),
                RoundingMode.UP
            ));
        }

        this.tempoMedioFilaComunicacao   = this.tempoMedioFilaComunicacao / this.numTarefas;
        this.tempoMedioComunicacao       = this.tempoMedioComunicacao / this.numTarefas;
        this.tempoMedioFilaProcessamento = this.tempoMedioFilaProcessamento / this.numTarefas;
        this.tempoMedioProcessamento     = this.tempoMedioProcessamento / this.numTarefas;
    }

    private void getMetricaComunicacao (final RedeDeFilas redeDeFilas) {
        this.metricasComunicacao = new HashMap<>();
        for (final CS_Comunicacao link : redeDeFilas.getInternets()) {
            this.metricasComunicacao.put(link.getId(), link.getMetrica());
        }
        for (final var link : redeDeFilas.getLinks()) {
            this.metricasComunicacao.put(link.getId(), link.getMetrica());
        }
    }

    private void getMetricaProcessamento (final RedeDeFilas redeDeFilas) {
        this.metricasProcessamento = new HashMap<>();
        for (final var maq : redeDeFilas.getMestres()) {
            this.metricasProcessamento.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetrica());
        }
        for (final CS_Processamento maq : redeDeFilas.getMaquinas()) {
            this.metricasProcessamento.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetrica());
        }
    }

    private void getMetricaFilaTarefaCloud (
        final List<Tarefa> tarefas,
        final RedeDeFilasCloud rede
    ) {
        this.tempoMedioFilaComunicacao   = 0;
        this.tempoMedioComunicacao       = 0;
        this.tempoMedioFilaProcessamento = 0;
        this.tempoMedioProcessamento     = 0;
        this.numTarefasCanceladas        = 0;
        this.MflopsDesperdicio           = 0;
        this.numTarefas                  = 0;

        final var mediaPoder = rede.getVMs().stream()
            .collect(Collectors.averagingDouble(CS_Processamento::getPoderComputacional));

        for (final var no : tarefas) {
            if (no.getEstado() == TaskState.DONE) {

                final Double suij = (
                                        no.getTamProcessamento() / mediaPoder /
                                        (
                                            no.getTempoFinal().get(no.getTempoFinal().size() - 1) -
                                            no.getTimeCriacao()
                                        )
                                    ) * 100;
                this.metricasSatisfacao.put(
                    no.getProprietario(), suij + this.metricasSatisfacao.get(no.getProprietario()));
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
            //Rever, se for informação pertinente adicionar nas métricas da tarefa ou CS_Processamento e calcula
            // durante a simulação
            final var temp = (CS_Processamento) no.getLocalProcessamento();
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

    private void getMetricaProcessamentoCloud (final RedeDeFilasCloud redeDeFilas) {
        this.metricasProcessamento = new HashMap<>();
        for (final var maq : redeDeFilas.getMestres()) {
            this.metricasProcessamento.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetrica());
        }
        for (final CS_Processamento maq : redeDeFilas.getVMs()) {
            this.metricasProcessamento.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetrica());
        }
    }

    private void getMetricaAlocacao (final RedeDeFilasCloud redeDeFilas) {
        this.metricasAlocacao = new HashMap<>();
        //percorre as máquinas recolhendo as métricas de alocação
        for (final var maq : redeDeFilas.getMaquinasCloud()) {
            this.metricasAlocacao.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetricaAloc());
        }
        //insere nas métricas as VMs que não foram alocadas
        final var mtRej = new MetricasAlocacao("Rejected");
        for (final var mst : redeDeFilas.getMestres()) {
            final var aux = (CS_VMM) mst;
            IntStream
                .range(0, aux.getAlocadorVM().getVMsRejeitadas().size())
                .forEach(i -> mtRej.incVMsAlocadas());
        }
        this.metricasAlocacao.put("Rejected", mtRej);
    }

    private void getMetricaCusto (final RedeDeFilasCloud redeDeFilas) {
        this.metricasCusto = new HashMap<>();
        //percorre as vms inserindo as métricas de custo
        for (final var vm : redeDeFilas.getVMs()) {
            if (vm.getStatus() == VirtualMachineState.DESTROYED) {
                this.metricasCusto.put(vm.getId() + vm.getnumeroMaquina(), vm.getMetricaCusto());
            }
        }
    }

    public void addMetrica (final Metricas metrica) {
        this.addMetricasGlobais(metrica.metricasGlobais);
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

    private void addMetricasGlobais (final MetricasGlobais global) {
        this.metricasGlobais.setTempoSimulacao(this.metricasGlobais.getTempoSimulacao()
                                               + global.getTempoSimulacao());
        this.metricasGlobais.setSatisfacaoMedia(
            this.metricasGlobais.getSatisfacaoMedia() + global.getSatisfacaoMedia());
        this.metricasGlobais.setOciosidadeComputacao(
            this.metricasGlobais.getOciosidadeComputacao() + global.getOciosidadeComputacao());
        this.metricasGlobais.setOciosidadeComunicacao(
            this.metricasGlobais.getOciosidadeComunicacao() + global.getOciosidadeComunicacao());
        this.metricasGlobais.setEficiencia(this.metricasGlobais.getEficiencia()
                                           + global.getEficiencia());
    }

    private void addMetricaFilaTarefa (final Metricas metrica) {
        this.tempoMedioFilaComunicacao += metrica.tempoMedioFilaComunicacao;
        this.tempoMedioComunicacao += metrica.tempoMedioComunicacao;
        this.tempoMedioFilaProcessamento += metrica.tempoMedioFilaProcessamento;
        this.tempoMedioProcessamento += metrica.tempoMedioFilaProcessamento;
        this.MflopsDesperdicio += metrica.MflopsDesperdicio;
        this.numTarefasCanceladas += metrica.numTarefasCanceladas;
    }

    private void addMetricaComunicacao (final Map<String, MetricasComunicacao> metricasComunicacao) {
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

    private void addMetricaProcessamento (final Map<String, MetricasProcessamento> metricasProcessamento) {
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

    private void addMetricaSatisfacaoGeralTempoSim (final Map<String, BigDecimal> metricasSatisfacao) {
        this.historicoSatisfacaoGeralTempoSim.add(metricasSatisfacao);
    }

    private void addMetricaConsumo (final Map<String, BigDecimal> metricasConsumo) {
        this.historicoConsumoTotalUsuario.add(metricasConsumo);
    }

    public MetricasGlobais getMetricasGlobais () {
        return this.metricasGlobais;
    }

    public Map<String, MetricasComunicacao> getMetricasComunicacao () {
        return this.metricasComunicacao;
    }

    public Map<String, MetricasProcessamento> getMetricasProcessamento () {
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

    public Map<String, MetricasAlocacao> getMetricasAlocacao () {
        return this.metricasAlocacao;
    }

    public Map<String, MetricasCusto> getMetricasCusto () {
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
        this.metricasGlobais.setTempoSimulacao(this.metricasGlobais.getTempoSimulacao()
                                               / this.numeroDeSimulacoes);
        this.metricasGlobais.setSatisfacaoMedia(this.metricasGlobais.getSatisfacaoMedia()
                                                / this.numeroDeSimulacoes);
        this.metricasGlobais.setOciosidadeComputacao(
            this.metricasGlobais.getOciosidadeComputacao() / this.numeroDeSimulacoes);
        this.metricasGlobais.setOciosidadeComunicacao(
            this.metricasGlobais.getOciosidadeComunicacao() / this.numeroDeSimulacoes);
        this.metricasGlobais.setEficiencia(this.metricasGlobais.getEficiencia()
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
                    "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s%n",
                    usuario,
                    this.historicoSatisfacaoGeralTempoSim.get(i).get(usuario),
                    this.historicoSatisfacaoGeralTempoUso.get(i).get(usuario),
                    this.historicoSatisfacaoDesempenho.get(i).get(usuario),
                    String.format(
                        "%.2f",
                        this.historicoConsumoTotalUsuario
                            .get(i)
                            .get(usuario)
                            .doubleValue()
                        / 1_000_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoLimitesConsumoTempoSim
                            .get(i)
                            .get(usuario)
                            .doubleValue()
                        / 1_000_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoLimitesConsumoTempoUso
                            .get(i)
                            .get(usuario)
                            .doubleValue()
                        / 1_000_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoLocal.get(i).get(usuario)
                            .divide(
                                BigDecimal.valueOf(this.historicoTempoSim.get(i)),
                                RoundingMode.UP
                            ).doubleValue() / 1_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoLocalProprio.get(i).get(usuario)
                            .divide(
                                BigDecimal.valueOf(
                                    this.historicoTempoSim.get(i)),
                                RoundingMode.UP
                            ).doubleValue() / 1000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoLocalEstrangeiro.get(i).get(usuario)
                            .divide(
                                BigDecimal.valueOf(
                                    this.historicoTempoSim.get(i)),
                                RoundingMode.UP
                            ).doubleValue() / 1_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoMaxLocal.get(i).get(usuario).doubleValue()
                        / 1_000_000
                    ),
                    this.historicoTarefasPreemp.get(i).get(usuario),
                    this.historicoAlpha.get(i).get(usuario),
                    this.historicoBetaTempoSim.get(i).get(usuario),
                    this.historicoBetaTempoUso.get(i).get(usuario),
                    String.format(
                        "%.2f",
                        this.historicoEnergiaDeperdicada
                            .get(i)
                            .get(usuario)
                            .doubleValue()
                        / 1_000_000
                    ),
                    String.format(
                        "%.2f",
                        this.historicoConsumoTotalUsuario.get(i).get(usuario)
                            .divide(
                                BigDecimal.valueOf(
                                    this.historicoTempoSim.get(i)),
                                RoundingMode.UP
                            ).doubleValue() / 1_000
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

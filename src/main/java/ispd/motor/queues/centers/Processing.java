package ispd.motor.queues.centers;

import ispd.gui.utils.*;
import ispd.motor.metrics.*;
import ispd.motor.queues.centers.impl.*;
import ispd.policy.allocation.vm.*;
import java.util.*;

/**
 * Classe abstrata que representa os servidores de processamento do modelo de fila, Esta classe
 * possui atributos referente a este ripo de servidor, e indica como calcular o tempo gasto para
 * processar uma tarefa.
 */
public abstract class Processing implements Service {

    private final double Ocupacao;

    private final ProcessingMetrics metrica;

    private final List<ParesOrdenadosUso> lista_pares = new ArrayList<>();

    private final Double consumoEnergia;

    private double poderComputacional;

    private double PoderComputacionalDisponivelPorProcessador;

    /**
     * Constructor which specifies the configuration of processing server, specifying the id, owner,
     * computational power, core count, load factor and machine number.
     * <p><br />
     * Using this constructor the energy consumption is set as default to 0.
     *
     * @param id
     *     the id
     * @param owner
     *     the owner
     * @param computationalPower
     *     the computational power
     * @param coreCount
     *     the core count
     * @param loadFactor
     *     the load factor
     * @param machineNumber
     *     the machine number
     *
     * @see #Processing(String, String, double, int, double, int, double) for specify the
     * energy consumption
     */
    protected Processing (
        final String id, final String owner, final double computationalPower, final int coreCount,
        final double loadFactor, final int machineNumber
    ) {
        this(id, owner, computationalPower, coreCount, loadFactor, machineNumber, 0.0);
    }

    /**
     * Constructor which specifies the configuration of the processing server, specifying the id,
     * owner, computational power, core count, load factor, machine number and energy consumption.
     *
     * @param id
     *     the id
     * @param owner
     *     the owner
     * @param computationalPower
     *     the computational power
     * @param coreCount
     *     the core count
     * @param loadFactor
     *     the load factor
     * @param machineNumber
     *     the machine number
     * @param energy
     *     the energy
     */
    protected Processing (
        final String id, final String owner, final double computationalPower, final int coreCount,
        final double loadFactor, final int machineNumber, final double energy
    ) {
        this.poderComputacional                         = computationalPower;
        this.Ocupacao                                   = loadFactor;
        this.PoderComputacionalDisponivelPorProcessador = (computationalPower * (1.0 - loadFactor))
                                                          / coreCount;
        this.consumoEnergia                             = energy;
        this.metrica                                    =
            new ProcessingMetrics(id, machineNumber, owner);
    }

    /**
     * Retorna o menor caminho entre dois recursos de processamento
     *
     * @param origem
     *     recurso origem
     * @param destino
     *     recurso destino
     *
     * @return caminho completo a partir do primeiro link até o recurso destino
     */
    public static List<Service> getMenorCaminho (
        final Processing origem,
        final Processing destino
    ) {
        //cria vetor com distancia acumulada
        return method(origem, destino);
    }

    /**
     * Retorna o menor caminho entre dois recursos de processamento indiretamente conectados
     * passando por mestres no caminho
     *
     * @param origem
     *     recurso origem
     * @param destino
     *     recurso destino
     *
     * @return caminho completo a partir do primeiro link até o recurso destino
     */
    public static List<Service> getMenorCaminhoIndireto (
        final Processing origem, final Processing destino
    ) {
        //cria vetor com distancia acumulada
        final var nosExpandidos = new ArrayList<Service>();
        final var caminho       = new ArrayList<Object[]>();
        Service   atual         = origem;
        //armazena valor acumulado até atingir o nó atual
        var acumulado = 0.0;
        do {
            //busca valores das conexões de saida do recurso atual e coloca no vetor caminho
            if (atual instanceof Link) {
                final var caminhoItem = new Object[4];
                caminhoItem[0] = atual;
                if (atual.connections() instanceof final Communication cs) {
                    caminhoItem[1] = cs.tempoTransmitir(10000) + acumulado;
                    caminhoItem[2] = atual.connections();
                } else if (atual.connections() instanceof GridMaster
                           || atual.connections() == destino) {
                    caminhoItem[1] = 0.0 + acumulado;
                    caminhoItem[2] = atual.connections();
                } else {
                    caminhoItem[1] = Double.MAX_VALUE;
                    caminhoItem[2] = null;
                }
                caminhoItem[3] = acumulado;
                caminho.add(caminhoItem);
            } else {
                final var lista =
                    (ArrayList<Service>) atual.connections();
                for (final var cs : lista) {
                    final var caminhoItem = new Object[4];
                    caminhoItem[0] = atual;
                    if (cs instanceof final Communication comu) {
                        caminhoItem[1] = comu.tempoTransmitir(10000) + acumulado;
                        caminhoItem[2] = cs;
                    } else if (cs instanceof GridMaster || cs == destino) {
                        caminhoItem[1] = 0.0 + acumulado;
                        caminhoItem[2] = cs;
                    } else {
                        caminhoItem[1] = Double.MAX_VALUE;
                        caminhoItem[2] = null;
                    }
                    caminhoItem[3] = acumulado;
                    caminho.add(caminhoItem);
                }
            }
            //Marca que o nó atual foi expandido
            nosExpandidos.add(atual);
            //Inicia variavel de menor caminho com maior valor possivel
            var menorCaminho = new Object[4];
            menorCaminho[0] = null;
            menorCaminho[1] = Double.MAX_VALUE;
            menorCaminho[2] = null;
            menorCaminho[3] = Double.MAX_VALUE;
            //busca menor caminho não expandido
            for (final var obj : caminho) {
                final var menor    = (Double) menorCaminho[1];
                final var objAtual = (Double) obj[1];
                if (menor > objAtual && !nosExpandidos.contains(obj[2])) {
                    menorCaminho = obj;
                }
            }
            //atribui valor a atual com resultado da busca do menor caminho
            atual = (Service) menorCaminho[2];
            acumulado = (Double) menorCaminho[1];
        } while (atual != null && atual != destino);
        if (atual == destino) {
            final List<Service> menorCaminho = new ArrayList<>();
            final List<Service> inverso      = new ArrayList<>();
            Object[]            obj;
            while (atual != origem) {
                var i = 0;
                do {
                    obj = caminho.get(i);
                    i++;
                } while (obj[2] != atual);
                inverso.add(atual);
                atual = (Service) obj[0];
            }
            for (var j = inverso.size() - 1; j >= 0; j--) {
                menorCaminho.add(inverso.get(j));
            }
            return menorCaminho;
        }
        return null;
    }

    protected static List<Service> getMenorCaminhoCloud (
        final Processing origem, final Processing destino
    ) {
        return method(origem, destino);
    }

    private static List<Service> method (
        final Processing origem,
        final Processing destino
    ) {
        //cria vetor com distancia acumulada
        final List<Service>  nosExpandidos = new ArrayList<>();
        final List<Object[]> caminho       = new ArrayList<>();
        Service              atual         = origem;
        //armazena valor acumulado até atingir o nó atual
        var acumulado = 0.0;
        do {
            //busca valores das conexões de saida do recurso atual e coloca no vetor caminho
            if (atual instanceof Link) {
                final var caminhoItem = new Object[4];
                caminhoItem[0] = atual;
                if (atual.connections() instanceof Processing
                    && atual.connections() != destino) {
                    caminhoItem[1] = Double.MAX_VALUE;
                    caminhoItem[2] = null;
                } else if (atual.connections() instanceof final Communication cs) {
                    caminhoItem[1] = cs.tempoTransmitir(10000) + acumulado;
                    caminhoItem[2] = atual.connections();
                } else {
                    caminhoItem[1] = 0.0 + acumulado;
                    caminhoItem[2] = atual.connections();
                }
                caminhoItem[3] = acumulado;
                caminho.add(caminhoItem);
            } else {
                final var lista =
                    (ArrayList<Service>) atual.connections();
                for (final var cs : lista) {
                    final var caminhoItem = new Object[4];
                    caminhoItem[0] = atual;
                    if (cs instanceof Processing && cs != destino) {
                        caminhoItem[1] = Double.MAX_VALUE;
                        caminhoItem[2] = null;
                    } else if (cs instanceof final Communication comu) {
                        caminhoItem[1] = comu.tempoTransmitir(10000) + acumulado;
                        caminhoItem[2] = cs;
                    } else {
                        caminhoItem[1] = 0.0 + acumulado;
                        caminhoItem[2] = cs;
                    }
                    caminhoItem[3] = acumulado;
                    caminho.add(caminhoItem);
                }
            }
            //Marca que o nó atual foi expandido
            nosExpandidos.add(atual);
            //Inicia variavel de menor caminho com maior valor possivel
            var menorCaminho = new Object[4];
            menorCaminho[0] = null;
            menorCaminho[1] = Double.MAX_VALUE;
            menorCaminho[2] = null;
            menorCaminho[3] = Double.MAX_VALUE;
            //busca menor caminho não expandido
            for (final var obj : caminho) {
                final var menor    = (Double) menorCaminho[1];
                final var objAtual = (Double) obj[1];
                if (menor > objAtual && !nosExpandidos.contains(obj[2])) {
                    menorCaminho = obj;
                }
            }
            //atribui valor a atual com resultado da busca do menor caminho
            atual = (Service) menorCaminho[2];
            acumulado = (Double) menorCaminho[1];
        } while (atual != null && atual != destino);
        if (atual == destino) {
            final List<Service> menorCaminho = new ArrayList<>();
            final List<Service> inverso      = new ArrayList<>();
            Object[]            obj;
            while (atual != origem) {
                var i = 0;
                do {
                    obj = caminho.get(i);
                    i++;
                } while (obj[2] != atual);
                inverso.add(atual);
                atual = (Service) obj[0];
            }
            for (var j = inverso.size() - 1; j >= 0; j--) {
                menorCaminho.add(inverso.get(j));
            }
            return menorCaminho;
        }
        return new ArrayList<>();
    }

    /**
     * Retorna o menor caminho entre dois recursos de processamento indiretamente conectados
     * passando por mestres no caminho
     *
     * @param origem
     *     recurso origem
     * @param destino
     *     recurso destino
     *
     * @return caminho completo a partir do primeiro link até o recurso destino
     */
    public static List<Service> getMenorCaminhoIndiretoCloud (
        final Processing origem, final Processing destino
    ) {
        //cria vetor com distancia acumulada
        final var nosExpandidos = new ArrayList<Service>();
        final var caminho       = new ArrayList<Object[]>();
        Service   atual         = origem;
        //armazena valor acumulado até atingir o nó atual
        var acumulado = 0.0;
        do {
            //busca valores das conexões de saida do recurso atual e coloca no vetor caminho
            if (atual instanceof Link) {
                final var caminhoItem = new Object[4];
                caminhoItem[0] = atual;
                if (atual.connections() instanceof final Communication cs) {
                    caminhoItem[1] = cs.tempoTransmitir(10000) + acumulado;
                    caminhoItem[2] = atual.connections();
                } else if (atual.connections() instanceof VmMaster
                           || atual.connections() == destino) {
                    caminhoItem[1] = 0.0 + acumulado;
                    caminhoItem[2] = atual.connections();
                } else {
                    caminhoItem[1] = Double.MAX_VALUE;
                    caminhoItem[2] = null;
                }
                caminhoItem[3] = acumulado;
                caminho.add(caminhoItem);
            } else {
                final var lista =
                    (ArrayList<Service>) atual.connections();
                for (final var cs : lista) {
                    final var caminhoItem = new Object[4];
                    caminhoItem[0] = atual;
                    if (cs instanceof final Communication comu) {
                        caminhoItem[1] = comu.tempoTransmitir(10000) + acumulado;
                        caminhoItem[2] = cs;
                    } else if (cs instanceof VmMaster || cs == destino) {
                        caminhoItem[1] = 0.0 + acumulado;
                        caminhoItem[2] = cs;
                    } else {
                        caminhoItem[1] = Double.MAX_VALUE;
                        caminhoItem[2] = null;
                    }
                    caminhoItem[3] = acumulado;
                    caminho.add(caminhoItem);
                }
            }
            //Marca que o nó atual foi expandido
            nosExpandidos.add(atual);
            //Inicia variavel de menor caminho com maior valor possivel
            var menorCaminho = new Object[4];
            menorCaminho[0] = null;
            menorCaminho[1] = Double.MAX_VALUE;
            menorCaminho[2] = null;
            menorCaminho[3] = Double.MAX_VALUE;
            //busca menor caminho não expandido
            for (final var obj : caminho) {
                final var menor    = (Double) menorCaminho[1];
                final var objAtual = (Double) obj[1];
                if (menor > objAtual && !nosExpandidos.contains(obj[2])) {
                    menorCaminho = obj;
                }
            }
            //atribui valor a atual com resultado da busca do menor caminho
            atual = (Service) menorCaminho[2];
            acumulado = (Double) menorCaminho[1];
        } while (atual != null && atual != destino);
        if (atual == destino) {
            final List<Service> menorCaminho = new ArrayList<>();
            final List<Service> inverso      = new ArrayList<>();
            Object[]            obj;
            while (atual != origem) {
                var i = 0;
                do {
                    obj = caminho.get(i);
                    i++;
                } while (obj[2] != atual);
                inverso.add(atual);
                atual = (Service) obj[0];
            }
            for (var j = inverso.size() - 1; j >= 0; j--) {
                menorCaminho.add(inverso.get(j));
            }
            return menorCaminho;
        }
        return null;
    }

    /**
     * Utilizado para buscar as rotas entre os recursos e armazenar em uma tabela, deve retornar em
     * erro se não encontrar nenhum caminho
     */
    public abstract void determinarCaminhos ()
        throws LinkageError;

    @Override
    public String id () {
        return this.metrica.getId();
    }

    public int getnumeroMaquina () {
        return this.metrica.getnumeroMaquina();
    }

    public Double getConsumoEnergia () {
        return this.consumoEnergia;
    }

    public double getOcupacao () {
        return this.Ocupacao;
    }

    public double getPoderComputacional () {
        return this.poderComputacional;
    }

    protected void setPoderComputacional (final double poderComputacional) {
        this.poderComputacional = poderComputacional;
    }

    public String getProprietario () {
        return this.metrica.getProprietario();
    }

    public double tempoProcessar (final double Mflops) {
        return (Mflops / this.PoderComputacionalDisponivelPorProcessador);
    }

    protected double getMflopsProcessados (final double tempoProc) {
        return (tempoProc * this.PoderComputacionalDisponivelPorProcessador);
    }

    public ProcessingMetrics getMetrica () {
        return this.metrica;
    }

    protected void setPoderComputacionalDisponivelPorProcessador (
        final double PoderComputacionalDisponivelPorProcessador
    ) {
        this.PoderComputacionalDisponivelPorProcessador =
        PoderComputacionalDisponivelPorProcessador;
    }

    public void setTempoProcessamento (final double inicio, final double fim) {
        final var par = new ParesOrdenadosUso(inicio, fim);
        this.lista_pares.add(par);
    }

    public List<ParesOrdenadosUso> getListaProcessamento () {
        Collections.sort(this.lista_pares);
        return (this.lista_pares);
    }
}

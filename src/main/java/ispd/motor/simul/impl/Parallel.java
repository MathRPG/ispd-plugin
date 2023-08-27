package ispd.motor.simul.impl;

import ispd.motor.Event;
import ispd.motor.*;
import ispd.motor.queues.*;
import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.motor.simul.*;
import ispd.policy.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

public class Parallel extends Simulation {

    private final int numThreads;

    private final List<Service> recursos;

    private final Map<Service, PriorityBlockingQueue<ispd.motor.Event>> threadFilaEventos;

    private final HashMap<Service, ThreadTrabalhador> threadTrabalhador;

    private ExecutorService threadPool;

    /**
     * @throws IllegalArgumentException
     */
    public Parallel (
        final ProgressTracker window,
        final GridQueueNetwork queueNetwork,
        final List<GridTask> jobs,
        final int numThreads
    ) {
        super(window, queueNetwork, jobs);
        this.threadPool = Executors.newFixedThreadPool(numThreads);
        //Cria lista com todos os recursos da grade
        this.recursos = new ArrayList<>();
        this.recursos.addAll(queueNetwork.getMaquinas());
        this.recursos.addAll(queueNetwork.getLinks());
        this.recursos.addAll(queueNetwork.getInternets());
        //Cria um trabalhador e uma fila de evento para cada recurso
        this.threadFilaEventos = new HashMap<>();
        this.threadTrabalhador = new HashMap<>();
        for (final Service rec : queueNetwork.getMestres()) {
            this.threadFilaEventos.put(rec, new PriorityBlockingQueue<>());
            if (((GridMaster) rec).getEscalonador().getTempoAtualizar() != null) {
                this.threadTrabalhador.put(rec, new ThreadTrabalhadorDinamico(rec, this));
            } else {
                this.threadTrabalhador.put(rec, new ThreadTrabalhador(rec, this));
            }
        }

        for (final Service rec : this.recursos) {
            this.threadFilaEventos.put(rec, new PriorityBlockingQueue<>());
            this.threadTrabalhador.put(rec, new ThreadTrabalhador(rec, this));
        }

        this.recursos.addAll(queueNetwork.getMestres());
        this.numThreads = numThreads;
        if (this.getQueueNetwork() == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (this.getQueueNetwork().getMestres() == null || this
            .getQueueNetwork()
            .getMestres()
            .isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (this.getQueueNetwork().getLinks() == null || this
            .getQueueNetwork()
            .getLinks()
            .isEmpty()) {
            window.println("The model has no Networks.", Color.orange);
        }
        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }
    }

    @Override
    public void simulate () {
        this.threadPool = Executors.newFixedThreadPool(this.numThreads);
        this.initSchedulers();
        //Adiciona tarefas iniciais
        for (final Service mestre : this.getQueueNetwork().getMestres()) {
            this.threadPool.execute(new tarefasIniciais(mestre));
        }
        this.threadPool.shutdown();
        while (!this.threadPool.isTerminated()) {
        }
        this.threadPool = Executors.newFixedThreadPool(this.numThreads);

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        // Realizar a simulação
        boolean fim = false;
        while (!fim) {
            fim = true;
            for (final Service rec : this.recursos) {
                if (!this.threadFilaEventos.get(rec).isEmpty()
                    && !this.threadTrabalhador.get(rec).executando) {
                    this.threadTrabalhador.get(rec).executando = true;
                    this.threadPool.execute(this.threadTrabalhador.get(rec));
                    fim = false;
                } else if (!this.threadFilaEventos.get(rec).isEmpty()) {
                    fim = false;
                }
            }
        }
        this.threadPool.shutdown();
        while (!this.threadPool.isTerminated()) {
        }
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        this.getWindow().incProgresso(30);
        this.getWindow().println("Simulation completed.", Color.green);
    }

    @Override
    public void addFutureEvent (final ispd.motor.Event ev) {
        if (ev.getType() == EventType.ARRIVAL) {
            this.threadFilaEventos.get(ev.getServidor()).offer(ev);
        } else {
            this.threadFilaEventos.get(ev.getServidor()).offer(ev);
        }
    }

    @Override
    public boolean removeFutureEvent (
        final EventType eventType,
        final Service eventServer,
        final Client eventClient
    ) {
        //remover evento de saida do cliente do servidor
        for (final var ev : this.threadFilaEventos.get(eventServer)) {
            if (ev.getType() == eventType
                && ev.getServidor().equals(eventServer)
                && ev.getClient().equals(eventClient)) {
                this.threadFilaEventos.get(eventServer).remove(ev);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getTime (final Object origin) {
        if (origin != null) {
            return this.threadTrabalhador.get(origin).getRelogioLocal();
        } else {
            double val = 0;
            for (final Service rec : this.recursos) {
                if (this.threadTrabalhador.get(rec).getRelogioLocal() > val) {
                    val = this.threadTrabalhador.get(rec).getRelogioLocal();
                }
            }
            return val;
        }
    }

    @Override
    public void createRouting () {
        for (final Processing mst : this.getQueueNetwork().getMestres()) {
            final Simulable temp = (Simulable) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            temp.setSimulation(this);
            //Encontra menor caminho entre o mestre e seus escravos
            this.threadPool.execute(new determinarCaminho(mst));
        }
        if (this.getQueueNetwork().getMaquinas() == null || this
            .getQueueNetwork()
            .getMaquinas()
            .isEmpty()) {
            this.getWindow().println("The model has no processing slaves.", Color.orange);
        } else {
            for (final GridMachine maq : this.getQueueNetwork().getMaquinas()) {
                //Encontra menor caminho entre o escravo e seu mestre
                this.threadPool.execute(new determinarCaminho(maq));
            }
        }
        this.threadPool.shutdown();
        while (!this.threadPool.isTerminated()) {
        }
    }

    private static class determinarCaminho implements Runnable {

        private final Processing mst;

        private determinarCaminho (final Processing mst) {
            this.mst = mst;
        }

        @Override
        public void run () {
            this.mst.determinarCaminhos();
        }
    }

    private class ThreadTrabalhador implements Runnable {

        private final Service recurso;

        private final Simulation simulacao;

        private double relogioLocal = 0.0;

        private boolean executando = false;

        private ThreadTrabalhador (final Service rec, final Simulation sim) {
            this.recurso   = rec;
            this.simulacao = sim;
        }

        @Override
        public void run () {
            // bloqueia este trabalhador
            synchronized (this) {
                while (!Parallel.this.threadFilaEventos.get(this.recurso).isEmpty()) {
                    // Verificando ocorencia de erro
                    final ispd.motor.Event eventoAtual =
                        Parallel.this.threadFilaEventos.get(this.recurso).poll();
                    if (eventoAtual.getCreationTime() > this.relogioLocal) {
                        this.relogioLocal = eventoAtual.getCreationTime();
                    }
                    switch (eventoAtual.getType()) {
                        case ARRIVAL:
                            eventoAtual.getServidor()
                                .clientEnter(this.simulacao, (GridTask) eventoAtual.getClient());
                            break;
                        case SERVICE:
                            eventoAtual
                                .getServidor()
                                .clientProcessing(
                                    this.simulacao,
                                    (GridTask) eventoAtual.getClient()
                                );
                            break;
                        case EXIT:
                            eventoAtual
                                .getServidor()
                                .clientExit(this.simulacao, (GridTask) eventoAtual.getClient());
                            break;
                        case SCHEDULING:
                            eventoAtual
                                .getServidor()
                                .requestProcessing(this.simulacao, null, EventType.SCHEDULING);
                            break;
                        default:
                            eventoAtual.getServidor().requestProcessing(
                                this.simulacao,
                                (Request) eventoAtual.getClient(),
                                eventoAtual.getType()
                            );
                            break;
                    }
                }
                this.executando = false;
            }
        }

        public double getRelogioLocal () {
            return this.relogioLocal;
        }

        protected void setRelogioLocal (final double relogio) {
            this.relogioLocal = relogio;
        }

        public Simulation getSimulacao () {
            return this.simulacao;
        }

        protected void setExecutando (final boolean executando) {
            this.executando = executando;
        }

        public Service getRecurso () {
            return this.recurso;
        }
    }

    private class ThreadTrabalhadorDinamico extends ThreadTrabalhador {

        private Object[] item;

        private ThreadTrabalhadorDinamico (final Service rec, final Simulation sim) {
            super(rec, sim);
            if (rec instanceof final GridMaster mestre) {
                if (mestre.getEscalonador().getTempoAtualizar() != null) {
                    this.item    = new Object[3];
                    this.item[0] = mestre;
                    this.item[1] = mestre.getEscalonador().getTempoAtualizar();
                    this.item[2] = mestre.getEscalonador().getTempoAtualizar();
                }
            }
        }

        @Override
        public void run () {
            // bloqueia este trabalhador
            synchronized (this) {
                while (!Parallel.this.threadFilaEventos.get(this.getRecurso()).isEmpty()) {
                    if ((Double) this.item[2] <
                        Parallel.this.threadFilaEventos
                            .get(this.getRecurso())
                            .peek()
                            .getCreationTime()) {
                        final GridMaster mestre = (GridMaster) this.item[0];
                        for (final Processing maq :
                            mestre.getEscalonador().getEscravos()) {
                            mestre.atualizar(maq, (Double) this.item[2]);
                        }
                        this.item[2] =
                            (Double) this.item[2] + (Double) this.item[1];
                    }
                    final ispd.motor.Event eventoAtual =
                        Parallel.this.threadFilaEventos.get(this.getRecurso()).poll();
                    if (eventoAtual.getCreationTime() > this.getRelogioLocal()) {
                        this.setRelogioLocal(eventoAtual.getCreationTime());
                    }
                    switch (eventoAtual.getType()) {
                        case ARRIVAL:
                            eventoAtual.getServidor()
                                .clientEnter(
                                    this.getSimulacao(),
                                    (GridTask) eventoAtual.getClient()
                                );
                            break;
                        case SERVICE:
                            eventoAtual.getServidor()
                                .clientProcessing(
                                    this.getSimulacao(),
                                    (GridTask) eventoAtual.getClient()
                                );
                            break;
                        case EXIT:
                            eventoAtual.getServidor()
                                .clientExit(
                                    this.getSimulacao(),
                                    (GridTask) eventoAtual.getClient()
                                );
                            break;
                        case SCHEDULING:
                            eventoAtual
                                .getServidor()
                                .requestProcessing(this.getSimulacao(), null, EventType.SCHEDULING);
                            break;
                        default:
                            eventoAtual.getServidor().requestProcessing(
                                this.getSimulacao(),
                                (Request) eventoAtual.getClient(),
                                eventoAtual.getType()
                            );
                            break;
                    }
                }
                this.setExecutando(false);
            }
        }
    }

    private class tarefasIniciais implements Runnable {

        private final Service mestre;

        private tarefasIniciais (final Service mestre) {
            this.mestre = mestre;
        }

        @Override
        public void run () {
            synchronized (Parallel.this.threadFilaEventos.get(this.mestre)) {
                for (final GridTask tarefa : Parallel.this.getJobs()) {
                    if (tarefa.getOrigem() == this.mestre) {
                        //criar evento...
                        final ispd.motor.Event evt = new Event(
                            tarefa.getTimeCriacao(), EventType.ARRIVAL, tarefa.getOrigem(), tarefa
                        );
                        Parallel.this.threadFilaEventos.get(this.mestre).add(evt);
                    }
                }
            }
        }
    }
}

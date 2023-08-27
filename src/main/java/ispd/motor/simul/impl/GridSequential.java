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
import java.util.stream.*;

public class GridSequential extends Simulation {

    private final PriorityQueue<ispd.motor.Event> eventos = new PriorityQueue<>();

    private double time = 0;

    /**
     * @throws IllegalArgumentException
     */
    public GridSequential (
        final ProgressTracker window,
        final GridQueueNetwork queueNetwork,
        final List<GridTask> jobs
    ) {
        super(window, queueNetwork, jobs);

        if (queueNetwork == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (queueNetwork.getMestres() == null || queueNetwork.getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (queueNetwork.getLinks() == null || queueNetwork.getLinks().isEmpty()) {
            window.println("The model has no Networks.", Color.orange);
        }

        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }

        window.print("Creating routing.");
        window.print(" -> ");

        window.print("Creating failuresSS.");

        for (final Processing mst : queueNetwork.getMestres()) {
            final Simulable temp = (Simulable) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            temp.setSimulation(this);
            //Encontra menor caminho entre o mestre e seus escravos
            mst.determinarCaminhos(); //mestre encontra caminho para seus escravos
        }

        window.incProgresso(5);
        window.println("OK", Color.green);

        if (queueNetwork.getMaquinas() == null || queueNetwork.getMaquinas().isEmpty()) {
            window.println("The model has no processing slaves.", Color.orange);
        } else {
            for (final GridMachine maq : queueNetwork.getMaquinas()) {
                //Encontra menor caminho entre o escravo e seu mestre
                maq.determinarCaminhos();//escravo encontra caminhos para seu mestre
            }
        }
        //fim roteamento

        window.incProgresso(5);
    }

    @Override
    public void simulate () {
        //inicia os escalonadores
        this.initSchedulers();
        //adiciona chegada das tarefas na lista de eventos futuros
        this.addEventos(this.getJobs());
        if (this.atualizarEscalonadores()) {
            this.realizarSimulacaoAtualizaTime();
        } else {
            this.realizarSimulacao();
        }
        this.getWindow().incProgresso(30);
        this.getWindow().println("Simulation completed.", Color.green);
    }

    @Override
    public void addFutureEvent (final ispd.motor.Event ev) {
        this.eventos.offer(ev);
    }

    @Override
    public boolean removeFutureEvent (
        final EventType eventType,
        final Service eventServer,
        final Client eventClient
    ) {
        //remover evento de saida do cliente do servidor
        final Iterator<ispd.motor.Event> interator = this.eventos.iterator();
        while (interator.hasNext()) {
            final ispd.motor.Event ev = interator.next();
            if (ev.getType() == eventType
                && ev.getServidor().equals(eventServer)
                && ev.getClient().equals(eventClient)) {
                this.eventos.remove(ev);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getTime (final Object origin) {
        return this.time;
    }

    private void addEventos (final List<GridTask> tasks) {
        tasks.stream()
            .map(t -> new ispd.motor.Event(
                t.getTimeCriacao(),
                EventType.ARRIVAL,
                t.getOrigem(),
                t
            ))
            .forEach(this.eventos::add);
    }

    private boolean atualizarEscalonadores () {
        return this.getQueueNetwork().getMestres().stream()
            .map(GridMaster.class::cast)
            .anyMatch(m -> m.getEscalonador().getTempoAtualizar() != null);
    }

    /**
     * Executa o laço de repetição responsavel por atender todos eventos da simulação, e adiciona o
     * evento para atualizar os escalonadores.
     */
    private void realizarSimulacaoAtualizaTime () {
        final List<Object[]> updateArray = this.makeUpdateArray();

        while (!this.eventos.isEmpty()) {
            //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            for (final var ob : updateArray) {
                final var event = this.eventos.peek();
                Objects.requireNonNull(event);
                if ((Double) ob[2] < event.getCreationTime()) {
                    final GridMaster mestre = (GridMaster) ob[0];
                    for (final Processing maq :
                        mestre.getEscalonador().getEscravos()) {
                        mestre.atualizar(maq, (Double) ob[2]);
                    }
                    ob[2] = (Double) ob[2] + (Double) ob[1];
                }
            }
            this.processTopEvent();
        }
    }

    private void realizarSimulacao () {
        while (!this.eventos.isEmpty()) {
            //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            this.processTopEvent();
        }
    }

    private List<Object[]> makeUpdateArray () {
        return this.getQueueNetwork().getMestres().stream()
            .map(GridMaster.class::cast)
            .filter(m -> m.getEscalonador().getTempoAtualizar() != null)
            .map(m -> new Object[] {
                m,
                m.getEscalonador().getTempoAtualizar(),
                m.getEscalonador().getTempoAtualizar()
            })
            .collect(Collectors.toCollection(ArrayList::new));
    }

    private void processTopEvent () {
        final Event eventoAtual = this.eventos.poll();
        Objects.requireNonNull(eventoAtual);
        this.time = eventoAtual.getCreationTime();
        switch (eventoAtual.getType()) {
            case ARRIVAL -> eventoAtual.getServidor()
                .clientEnter(this, (GridTask) eventoAtual.getClient());
            case SERVICE -> eventoAtual.getServidor()
                .clientProcessing(this, (GridTask) eventoAtual.getClient());
            case EXIT -> eventoAtual
                .getServidor()
                .clientExit(this, (GridTask) eventoAtual.getClient());
            case SCHEDULING -> eventoAtual
                .getServidor()
                .requestProcessing(this, null, EventType.SCHEDULING);
            default -> eventoAtual.getServidor()
                .requestProcessing(this, (Request) eventoAtual.getClient(), eventoAtual.getType());
        }
    }
}

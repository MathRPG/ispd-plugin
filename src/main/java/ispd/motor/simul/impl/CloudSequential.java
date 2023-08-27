package ispd.motor.simul.impl;

import ispd.gui.*;
import ispd.motor.Event;
import ispd.motor.*;
import ispd.motor.faults.*;
import ispd.motor.metrics.*;
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

public class CloudSequential extends Simulation {

    private final PriorityQueue<ispd.motor.Event> eventos = new PriorityQueue<>();

    private double time = 0;

    /**
     * @throws IllegalArgumentException
     */
    public CloudSequential (
        final ProgressTracker window,
        final CloudQueueNetwork cloudQueueNetwork,
        final List<GridTask> jobs
    ) {
        super(window, cloudQueueNetwork, jobs);

        if (cloudQueueNetwork == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (cloudQueueNetwork.getMestres() == null || cloudQueueNetwork
            .getMestres()
            .isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (cloudQueueNetwork.getLinks() == null || cloudQueueNetwork.getLinks().isEmpty()) {
            window.println("The model has no Networks.", Color.orange);
        } else if (cloudQueueNetwork.getVMs() == null || cloudQueueNetwork.getVMs().isEmpty()) {
            window.println("The model has no virtual machines configured.", Color.orange);
        }

        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }

        window.print("Creating routing.");
        window.print(" -> ");

        System.out.println("---------------------------------------");
        for (final Processing mst : cloudQueueNetwork.getMestres()) {
            final Simulable temp = (Simulable) mst;
            final Simulable aux  = (Simulable) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            aux.setSimulation(this);
            temp.setSimulation(this);
            //Encontra menor caminho entre o mestre e seus escravos
            System.out.printf("Mestre %s encontrando seus escravos\n", mst.id());
            mst.determinarCaminhos(); //mestre encontra caminho para seus escravos
        }

        window.incProgresso(5);
        window.println("OK", Color.green);

        /*Injetando as falhas:
        verifica qual checkbox foi clicado quando escolheu a falha e executa*/
        //Injetar falhar de Omissão de Hardware: desligar uma máquina física
        final var selecionarFalhas = new PickSimulationFaultsDialog();

        if (selecionarFalhas.isActive()) {
            //-----------Injeção da Falha de Omissão de Hardware --------
            if (selecionarFalhas.cbkOmissaoHardware != null) {
                window.println("There are injected hardware omission failures.");
                window.println("Creating Hardware fault.");
                final Hardware fihardware = new Hardware();
                fihardware.showMessage(window, cloudQueueNetwork);
            } else {
                window.println("There aren't injected hardware omission failures.");
            }
            //-----------Injeção da  Falha de Omissão de Software --------
            if (selecionarFalhas.cbkOmissaoSoftware != null) {
                window.println("There are injected software omission failures.");
                window.println("Creating software fault.");
                window.println("Software failure created.");
                Software.showMessage(window);
            } else {
                window.println("There aren't injected software omission failures.");
            }
            //-----------Injeção da  Falha de Negação de serviço --------
            if (selecionarFalhas.cbxNegacaoService != null) {
                window.println("There are injected denial of service failures.");
                window.println("Creating Denial of service fault.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected denial of service failures.");
            }
            //-----------Injeção da  Falha de HD Cheio --------
            if (selecionarFalhas.cbxHDCheio != null) {
                window.println("There are injected Full HD failures.");
                window.println("Creating Full HD fault.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected Full HD failures.");
            }

            //-----------Injeção da  Falha de Valores --------
            if (selecionarFalhas.cbxValores != null) {
                window.println("There are injected Values failures.");
                window.println("Creating value fault.");
                Value.setFaults(window, cloudQueueNetwork, new Global());
            } else {
                window.println("There aren't injected Value failures.");
            }

            //-----------Injeção da  Falha de Estado --------
            if (selecionarFalhas.cbxEstado != null) {
                window.println("There are injected State failures.");
                window.println("Creating state fault.");
                final State state = new State();
                state.FIState1(window, cloudQueueNetwork);
            } else {
                window.println("There aren't injected State failures.");
            }
            //-----------Injeção da  Falha de Sobrecarga de Tempo --------
            if (selecionarFalhas.cbxSobrecargaTempo != null) {
                window.println("There are injected time overload failures.");
                window.println("Creating time overload fault.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected time overload failures.");
            }
            //-----------Injeção da  Falha de Interdependencia --------
            if (selecionarFalhas.cbxInterdependencia != null) {
                window.println("There are injected interdependencies failures.");
                window.println("Creating interdependencie fault.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected interdependencies " +
                               "failures.");
            }

            //-----------Injeção da  Falha de Incompatibilidade --------
            if (selecionarFalhas.cbxIncompatibilidade != null) {
                window.println("There are injected Incompatibility failures.");
                window.println("Creating Incompatibility fault.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected Incompatibility failures.");
            }

            //-----------Injeção de Falhas Pemanentes --------   
            if (selecionarFalhas.cbxFPermanentes != null) {
                window.println("There are injected permanents failures.");
                window.println("Creating permanents fault.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected permanents failures.");
            }

            //-----------Injeção da  Falha de Desenho incorreto --------
            if (selecionarFalhas.cbxDesenhoIncorreto != null) {
                window.println("There are injected bad design failures.");
                window.println("Creating bad design fault.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected bad design failures.");
            }

            //-----------Injeção da  Falha Precosse --------
            if (selecionarFalhas.cbxPrecoce != null) {
                window.println("There are injected early failures.");
                window.println("Creating early fault.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected early failures.");
            }

            //-----------Injeção da  Falha de Tardia --------
            if (selecionarFalhas.cbxTardia != null) {
                window.println("There are injected late failures.");
                window.println("Creating late fault.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected late failures.");
            }

            //-----------Injeção da  Falha Transiente --------
            if (selecionarFalhas.cbxTransiente != null) {
                window.println("There are injected transient failures.");
                window.println("Creating transient failure.");
                window.println("Development fault.");
            } else {
                window.println("There aren't injected transient failures.");
            }
        } else {
            window.println("There aren't selected faults.");
        }

        if (cloudQueueNetwork.getMaquinasCloud() == null || cloudQueueNetwork
            .getMaquinasCloud()
            .isEmpty()) {
            window.println("The model has no phisical machines.", Color.orange);
        } else {
            System.out.println("---------------------------------------");
            for (final CloudMachine maq : cloudQueueNetwork.getMaquinasCloud()) {
                // Encontra menor caminho entre o escravo e seu mestre
                maq.determinarCaminhos(); // escravo encontra caminhos para seu mestre
            }
        }
        //fim roteamento

        window.incProgresso(5);
    }

    @Override
    public void simulate () {
        //inicia os escalonadores
        System.out.println("---------------------------------------");
        this.initCloudSchedulers();
        System.out.println("---------------------------------------");

        this.initCloudAllocators();
        System.out.println("---------------------------------------");
        this.addEventos(this.getJobs());
        System.out.println("---------------------------------------");

        if (this.atualizarEscalonadores()) {
            this.realizarSimulacaoAtualizaTime();
        } else {
            this.realizarSimulacao();
        }

        this.desligarMaquinas(this, this.getCloudQueueNetwork());
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
        // remover evento de saida do cliente do servidor
        final var interator = this.eventos.iterator();
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

    public void addEventos (final List<GridTask> tarefas) {
        System.out.println("Tarefas sendo adicionadas na lista de eventos futuros");
        for (final GridTask tarefa : tarefas) {
            final var evt = new ispd.motor.Event(
                tarefa.getTimeCriacao(),
                EventType.ARRIVAL,
                tarefa.getOrigem(),
                tarefa
            );
            this.eventos.add(evt);
        }
    }

    private boolean atualizarEscalonadores () {
        for (final Processing mst : this.getCloudQueueNetwork().getMestres()) {
            final CloudMaster mestre = (CloudMaster) mst;
            if (mestre.getEscalonador().getTempoAtualizar() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executa o laço de repetição responsavel por atender todos eventos da simulação, e adiciona o
     * evento para atualizar os escalonadores.
     */
    private void realizarSimulacaoAtualizaTime () {
        final var updateArray = this.makeUpdateArray();

        while (!this.eventos.isEmpty()) {
            //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            for (final Object[] ob : updateArray) {
                final var event = this.eventos.peek();
                Objects.requireNonNull(event);
                if ((Double) ob[2] < event.getCreationTime()) {
                    final GridMaster mestre = (GridMaster) ob[0];
                    for (final Processing maq : mestre.getEscalonador().getEscravos()) {
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

    private void desligarMaquinas (final Simulation simulation, final CloudQueueNetwork qn) {
        for (final CloudMachine aux : qn.getMaquinasCloud()) {
            aux.desligar(simulation);
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
            case ALLOCATION -> eventoAtual
                .getServidor()
                .requestProcessing(this, null, EventType.ALLOCATION);
            default -> eventoAtual.getServidor()
                .requestProcessing(this, (Request) eventoAtual.getClient(), eventoAtual.getType());
        }
    }
}

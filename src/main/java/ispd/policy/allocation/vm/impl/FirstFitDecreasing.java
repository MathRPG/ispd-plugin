package ispd.policy.allocation.vm.impl;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.*;
import ispd.policy.allocation.vm.*;
import ispd.policy.allocation.vm.impl.util.*;
import java.util.*;

public class FirstFitDecreasing extends VmAllocationPolicy {

    private final ComparaRequisitos comparaReq;

    private boolean fit = false;

    private int maqIndex = 0;

    private List<VirtualMachine> VMsOrdenadas = null;

    public FirstFitDecreasing () {
        this.maquinasVirtuais = new ArrayList<>();
        this.escravos         = new ArrayList<>();
        this.VMsRejeitadas    = new ArrayList<>();
        this.comparaReq       = new ComparaRequisitos();
    }

    @Override
    public void iniciar () {
        this.fit          = true;
        this.maqIndex     = 0;
        this.VMsOrdenadas = new ArrayList<>(this.maquinasVirtuais);
        for (final VirtualMachine aux : this.VMsOrdenadas) {
            System.out.println(aux.id());
        }
        this.VMsOrdenadas.sort(this.comparaReq);
        System.out.println("Ordem crescente");
        for (final VirtualMachine aux : this.VMsOrdenadas) {
            System.out.println(aux.id());
        }
        Collections.reverse(this.VMsOrdenadas);
        System.out.println("Ordem decrescente");
        for (final VirtualMachine aux : this.VMsOrdenadas) {
            System.out.println(aux.id());
        }
        if (!this.escravos.isEmpty() && !this.maquinasVirtuais.isEmpty()) {
            this.escalonar();
        }
    }

    @Override
    public List<Service> escalonarRota (final Service destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<Service>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar () {
        while (!(this.maquinasVirtuais.isEmpty())) {
            System.out.println("------------------------------------------");
            int num_escravos = this.escravos.size();

            final VirtualMachine auxVM = this.escalonarVM();

            while (num_escravos >= 0) {
                if (num_escravos > 0) { // caso existam máquinas livres
                    final var auxMaq = this.escalonarRecurso();
                    // escalona o recurso
                    if (auxMaq instanceof CloudMaster) {

                        System.out.println(auxMaq.id()
                                           + " é um VMM, a VM "
                                           + "será redirecionada");
                        auxVM.setCaminho(this.escalonarRota(auxMaq));
                        // salvando uma lista de VMMs intermediarios no caminho da vm e seus respectivos caminhos
                        System.out.println(auxVM.id() + " enviada para " + auxMaq.id());
                        this.mestre.sendVm(auxVM);
                        System.out.println("---------------------------------------");
                        break;
                    } else {
                        System.out.println("Checagem de recursos:");
                        final CloudMachine maq        = (CloudMachine) auxMaq;
                        final double       memoriaMaq = maq.getMemoriaDisponivel();
                        System.out.println("memoriaMaq: " + memoriaMaq);
                        final double memoriaNecessaria = auxVM.getMemoriaDisponivel();
                        System.out.println("memorianecessaria: " + memoriaNecessaria);
                        final double discoMaq = maq.getDiscoDisponivel();
                        System.out.println("discoMaq: " + discoMaq);
                        final double discoNecessario = auxVM.getDiscoDisponivel();
                        System.out.println("disconecessario: " + discoNecessario);
                        final int maqProc = maq.getProcessadoresDisponiveis();
                        System.out.println("ProcMaq: " + maqProc);
                        final int procVM = auxVM.getProcessadoresDisponiveis();
                        System.out.println("ProcVM: " + procVM);

                        if ((
                            memoriaNecessaria <= memoriaMaq
                            && discoNecessario <= discoMaq
                            && procVM <= maqProc
                        )) {
                            maq.setMemoriaDisponivel(memoriaMaq - memoriaNecessaria);
                            System.out.println("Realizando o controle de " + "recurso:");
                            System.out.println("memoria atual da maq: " + (
                                memoriaMaq
                                - memoriaNecessaria
                            ));
                            maq.setDiscoDisponivel(discoMaq - discoNecessario);
                            System.out.println("disco atual maq: " + (discoMaq - discoNecessario));
                            maq.setProcessadoresDisponiveis(maqProc - procVM);
                            System.out.println("proc atual: " + (maqProc - procVM));
                            auxVM.setMaquinaHospedeira((CloudMachine) auxMaq);
                            auxVM.setCaminho(this.escalonarRota(auxMaq));
                            System.out.println(auxVM.id()
                                               + " enviada para"
                                               + " "
                                               + auxMaq.id());
                            this.mestre.sendVm(auxVM);
                            System.out.println("---------------------------------------");

                            break;
                        } else {
                            num_escravos--;
                        }
                    }
                } else {
                    System.out.println(auxVM.id() + " foi rejeitada");
                    auxVM.setStatus(VirtualMachineState.REJECTED);
                    this.VMsRejeitadas.add(auxVM);
                    System.out.println("Adicionada na lista de rejeitadas");
                    num_escravos--;
                    System.out.println("---------------------------------------");
                }
            }
        }
    }

    @Override
    public Processing escalonarRecurso () {
        if (this.fit) {
            return this.escravos.get(0);
        } else {
            return this.escravos.get(this.maqIndex);
        }
    }

    @Override
    public VirtualMachine escalonarVM () {
        return this.VMsOrdenadas.remove(0);
    }
}

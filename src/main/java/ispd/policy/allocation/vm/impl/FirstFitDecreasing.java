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
        this.VMsOrdenadas.sort(this.comparaReq);
        Collections.reverse(this.VMsOrdenadas);
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
            int num_escravos = this.escravos.size();

            final VirtualMachine auxVM = this.escalonarVM();

            while (num_escravos >= 0) {
                if (num_escravos > 0) { // caso existam máquinas livres
                    final var auxMaq = this.escalonarRecurso();
                    // escalona o recurso
                    if (auxMaq instanceof CloudMaster) {

                        auxVM.setCaminho(this.escalonarRota(auxMaq));
                        // salvando uma lista de VMMs intermediarios no caminho da vm e seus respectivos caminhos
                        this.mestre.sendVm(auxVM);
                        break;
                    } else {
                        final CloudMachine maq        = (CloudMachine) auxMaq;
                        final double       memoriaMaq = maq.getMemoriaDisponivel();
                        final double memoriaNecessaria = auxVM.getMemoriaDisponivel();
                        final double discoMaq = maq.getDiscoDisponivel();
                        final double discoNecessario = auxVM.getDiscoDisponivel();
                        final int maqProc = maq.getProcessadoresDisponiveis();
                        final int procVM = auxVM.getProcessadoresDisponiveis();

                        if ((
                            memoriaNecessaria <= memoriaMaq
                            && discoNecessario <= discoMaq
                            && procVM <= maqProc
                        )) {
                            maq.setMemoriaDisponivel(memoriaMaq - memoriaNecessaria);

                            maq.setDiscoDisponivel(discoMaq - discoNecessario);
                            maq.setProcessadoresDisponiveis(maqProc - procVM);
                            auxVM.setMaquinaHospedeira((CloudMachine) auxMaq);
                            auxVM.setCaminho(this.escalonarRota(auxMaq));
                            this.mestre.sendVm(auxVM);

                            break;
                        } else {
                            num_escravos--;
                        }
                    }
                } else {
                    auxVM.setStatus(VirtualMachineState.REJECTED);
                    this.VMsRejeitadas.add(auxVM);
                    num_escravos--;
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

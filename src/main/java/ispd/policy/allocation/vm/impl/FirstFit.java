package ispd.policy.allocation.vm.impl;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.*;
import ispd.policy.allocation.vm.*;
import java.util.*;

public class FirstFit extends VmAllocationPolicy {

    private boolean fit = false;

    private int maqIndex = 0;

    public FirstFit () {
        this.maquinasVirtuais = new ArrayList<>();
        this.escravos         = new ArrayList<>();
        this.VMsRejeitadas    = new ArrayList<>();
    }

    private static boolean canMachineFitVm (final CloudMachine machine, final VirtualMachine vm) {
        return vm.getMemoriaDisponivel() <= machine.getMemoriaDisponivel()
               && vm.getDiscoDisponivel() <= machine.getDiscoDisponivel()
               && vm.getProcessadoresDisponiveis() <= machine.getProcessadoresDisponiveis();
    }

    private static void makeMachineHostVm (final CloudMachine machine, final VirtualMachine vm) {
        machine.setMemoriaDisponivel(machine.getMemoriaDisponivel() - vm.getMemoriaDisponivel());
        machine.setDiscoDisponivel(machine.getDiscoDisponivel() - vm.getDiscoDisponivel());
        machine.setProcessadoresDisponiveis(machine.getProcessadoresDisponiveis()
                                            - vm.getProcessadoresDisponiveis());
        vm.setMaquinaHospedeira(machine);
    }

    @Override
    public void iniciar () {
        this.fit      = true;
        this.maqIndex = 0;

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
            var       slaveCount = this.escravos.size();
            final var auxVM      = this.escalonarVM();

            do {
                if (slaveCount == 0) {
                    auxVM.setStatus(VirtualMachineState.REJECTED);
                    this.VMsRejeitadas.add(auxVM);
                    this.maqIndex = 0;
                    slaveCount--;
                    continue;
                }

                final var auxMaq = this.escalonarRecurso();
                this.maqIndex++;

                if (auxMaq instanceof CloudMaster) {
                    auxVM.setCaminho(this.escalonarRota(auxMaq));
                    this.mestre.sendVm(auxVM);
                    break;
                } else {
                    final var maq = (CloudMachine) auxMaq;
                    if (canMachineFitVm(maq, auxVM)) {
                        makeMachineHostVm(maq, auxVM);
                        auxVM.setCaminho(this.escalonarRota(auxMaq));

                        this.mestre.sendVm(auxVM);
                        this.maqIndex = 0;
                        this.fit      = true;

                        break;
                    } else {
                        slaveCount--;
                        this.fit = false;
                    }
                }
            } while (slaveCount >= 0);
        }
    }

    @Override
    public Processing escalonarRecurso () {
        return this.escravos.get(this.fit ? 0 : this.maqIndex);
    }

    @Override
    public VirtualMachine escalonarVM () {
        return this.maquinasVirtuais.remove(0);
    }
}

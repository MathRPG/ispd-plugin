package ispd.policy.allocation.vm.impl;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.*;
import ispd.policy.allocation.vm.*;
import java.util.*;

public class RoundRobin extends VmAllocationPolicy {

    private ListIterator<Processing> physicalMachine = null;

    public RoundRobin () {
        this.maquinasVirtuais = new ArrayList<>();
        this.escravos         = new LinkedList<>();
        this.VMsRejeitadas    = new ArrayList<>();
    }

    @Override
    public void iniciar () {
        this.test();

        this.physicalMachine = this.escravos.listIterator(0);

        if (this.maquinasVirtuais.isEmpty()) {
            return;
        }

        this.escalonar();
    }

    @Override
    public List<Service> escalonarRota (final Service destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<Service>) this.caminhoEscravo.get(index));
    }

    @Override
    public void escalonar () {
        while (!(this.maquinasVirtuais.isEmpty())) {
            this.findMachineForTask(this.escalonarVM());
        }
    }

    @Override
    public Processing escalonarRecurso () {
        if (!this.physicalMachine.hasNext()) {
            this.physicalMachine = this.escravos.listIterator(0);
        }
        return this.physicalMachine.next();
    }

    @Override
    public VirtualMachine escalonarVM () {
        return this.maquinasVirtuais.remove(0);
    }

    private void test () {
        if (this.maquinasVirtuais.isEmpty()) {
            System.out.println("sem vms setadas");
        }
        System.out.println("Lista de VMs");
        this.maquinasVirtuais.stream()
            .map(Processing::id)
            .forEach(System.out::println);
    }

    private void findMachineForTask (final VirtualMachine vm) {
        int machines = this.escravos.size();
        while (machines >= 0) {
            if (machines == 0) {
                this.rejectVm(vm);
                machines--;
                continue;
            }

            final var resource = this.escalonarRecurso();

            if (resource instanceof CloudMaster) {
                this.redirectVm(vm, resource);
                return;
            }

            final var    maq          = (CloudMachine) resource;
            final double memory       = maq.getMemoriaDisponivel();
            final double neededMemory = vm.getMemoriaDisponivel();
            final double disk         = maq.getDiscoDisponivel();
            final double neededDisk   = vm.getDiscoDisponivel();
            final int    processors   = maq.getProcessadoresDisponiveis();
            final int    procVM       = vm.getProcessadoresDisponiveis();

            if ((neededMemory > memory || neededDisk > disk || procVM > processors)) {
                machines--;
                continue;
            }

            final double newMemory = memory - neededMemory;
            maq.setMemoriaDisponivel(newMemory);
            final double newDisk = disk - neededDisk;
            maq.setDiscoDisponivel(newDisk);
            final int newProcessors = processors - procVM;
            maq.setProcessadoresDisponiveis(newProcessors);
            vm.setMaquinaHospedeira((CloudMachine) resource);
            vm.setCaminho(this.escalonarRota(resource));
            this.mestre.sendVm(vm);

            return;
        }
    }

    private void rejectVm (final VirtualMachine auxVm) {
        auxVm.setStatus(VirtualMachineState.REJECTED);
        this.VMsRejeitadas.add(auxVm);
    }

    private void redirectVm (
        final VirtualMachine vm, final Processing resource
    ) {
        vm.setCaminho(this.escalonarRota(resource));
        this.mestre.sendVm(vm);
    }
}

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
        System.out.println("---------------------------------------");
        System.out.println("Alocador RR iniciado");

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
            System.out.println("------------------------------------------");
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

            System.out.println("Checagem de recursos:");
            final var maq = (CloudMachine) resource;
            final double memory = maq.getMemoriaDisponivel();
            System.out.println("memoriaMaq: " + memory);
            final double neededMemory = vm.getMemoriaDisponivel();
            System.out.println("memorianecessaria: " + neededMemory);
            final double disk = maq.getDiscoDisponivel();
            System.out.println("discoMaq: " + disk);
            final double neededDisk = vm.getDiscoDisponivel();
            System.out.println("disconecessario: " + neededDisk);
            final int processors = maq.getProcessadoresDisponiveis();
            System.out.println("ProcMaq: " + processors);
            final int procVM = vm.getProcessadoresDisponiveis();
            System.out.println("ProcVM: " + procVM);

            if ((neededMemory > memory || neededDisk > disk || procVM > processors)) {
                machines--;
                continue;
            }

            System.out.println("Realizando o controle de recurso:");
            final double newMemory = memory - neededMemory;
            maq.setMemoriaDisponivel(newMemory);
            System.out.printf("memoria atual da maq: %s%n", newMemory);
            final double newDisk = disk - neededDisk;
            maq.setDiscoDisponivel(newDisk);
            System.out.printf("disco atual maq: %s%n", newDisk);
            final int newProcessors = processors - procVM;
            maq.setProcessadoresDisponiveis(newProcessors);
            System.out.printf("proc atual: %d%n", newProcessors);
            vm.setMaquinaHospedeira((CloudMachine) resource);
            vm.setCaminho(this.escalonarRota(resource));
            System.out.printf(
                "%s enviada para %s%n", vm.id(), resource.id());
            this.mestre.sendVm(vm);
            System.out.println("---------------------------------------");

            return;
        }
    }

    private void rejectVm (final VirtualMachine auxVm) {
        System.out.printf("%s foi rejeitada%n", auxVm.id());
        auxVm.setStatus(VirtualMachineState.REJECTED);
        this.VMsRejeitadas.add(auxVm);
        System.out.println("Adicionada na lista de rejeitadas");
        System.out.println("---------------------------------------");
    }

    private void redirectVm (
        final VirtualMachine vm, final Processing resource
    ) {
        System.out.printf(
            "%s é um VMM, a VM será redirecionada%n", resource.id());
        vm.setCaminho(this.escalonarRota(resource));
        System.out.printf("%s enviada para %s%n", vm.id(), resource.id());
        this.mestre.sendVm(vm);
        System.out.println("---------------------------------------");
    }
}

package ispd.motor.queues.task;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.centers.impl.*;

public class CloudTask extends GridTask {

    private final VirtualMachine VM_enviada;

    public CloudTask (
        final Service origem,
        final VirtualMachine VM,
        final double arquivoEnvio,
        final double tempoCriacao
    ) {
        super(0, VM.getProprietario(), VM.id(), origem, arquivoEnvio, 0.0, tempoCriacao);
        this.VM_enviada = VM;
    }

    public VirtualMachine getVM_enviada () {
        return this.VM_enviada;
    }
}

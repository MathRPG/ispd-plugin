package ispd.motor.filas;

import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

public class TarefaVM extends Tarefa {

    private CS_VirtualMac VM_enviada;

    public TarefaVM (
            final CentroServico origem, final CS_VirtualMac VM, final double arquivoEnvio, final double tempoCriacao
    ) {
        super(0, VM.getProprietario(), VM.getId(), origem, arquivoEnvio, 0, tempoCriacao);
        this.VM_enviada = VM;
    }

    public CS_VirtualMac getVM_enviada () {
        return this.VM_enviada;
    }

    public void setVM_enviada (final CS_VirtualMac VM_enviada) {
        this.VM_enviada = VM_enviada;
    }
}

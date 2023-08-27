package ispd.policy.scheduling.grid.impl;

import ispd.motor.queues.centers.*;
import ispd.policy.*;
import ispd.policy.scheduling.grid.*;
import ispd.policy.scheduling.grid.impl.util.*;
import java.util.*;

public abstract class AbstractOSEP <T extends UserProcessingControl> extends GridSchedulingPolicy {

    private static final double REFRESH_TIME = 15.0;

    protected final Map<Processing, SlaveControl> slaveControls = new HashMap<>();

    protected final Map<String, T> userControls = new HashMap<>();

    protected AbstractOSEP () {
        this.tarefas     = new ArrayList<>();
        this.escravos    = new ArrayList<>();
        this.filaEscravo = new ArrayList<>();
    }

    @Override
    public void iniciar () {
        this.mestre.setSchedulingConditions(Conditions.ALL);

        for (final var userId : this.metricaUsuarios.getUsuarios()) {
            final var uc = this.makeUserControlFor(userId);
            this.userControls.put(userId, uc);
        }

        for (final var slave : this.escravos) {
            this.slaveControls.put(slave, new SlaveControl());
        }
    }

    @Override
    public List<Service> escalonarRota (final Service destino) {
        final int index = this.escravos.indexOf(destino);
        return new ArrayList<>((List<Service>) this.caminhoEscravo.get(index));
    }

    @Override
    public Double getTempoAtualizar () {
        return REFRESH_TIME;
    }

    protected T makeUserControlFor (final String userId) {
        return (T) new UserProcessingControl(userId, this.escravos);
    }
}

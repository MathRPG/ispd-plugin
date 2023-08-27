package ispd.policy;

import ispd.motor.simul.*;

public interface Simulable {

    Simulation getSimulation ();

    void setSimulation (Simulation newSimulation);
}

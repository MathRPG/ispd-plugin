package ispd.policy;

import ispd.motor.*;

public interface Simulable {

    Simulation getSimulation ();

    void setSimulation (Simulation newSimulation);
}

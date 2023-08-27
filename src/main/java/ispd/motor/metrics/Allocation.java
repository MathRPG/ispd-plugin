package ispd.motor.metrics;

import java.io.*;

public class Allocation implements Serializable {

    private final String id;

    private int numVMs;

    public Allocation (final String id) {
        this.id     = id;
        this.numVMs = 0;
    }

    public void incVMsAlocadas () {
        this.numVMs++;
    }

    public int getNumVMs () {
        return this.numVMs;
    }

    public String getId () {
        return this.id;
    }
}

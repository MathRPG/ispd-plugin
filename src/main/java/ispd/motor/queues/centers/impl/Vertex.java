package ispd.motor.queues.centers.impl;

import ispd.motor.queues.centers.*;

public interface Vertex {

    void addOutboundConnection (final Communication comm);

    default void addInboundConnection (final Communication comm) {
    }
}

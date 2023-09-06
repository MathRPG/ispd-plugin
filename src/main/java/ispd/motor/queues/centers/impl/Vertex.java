package ispd.motor.queues.centers.impl;

public interface Vertex {

    void addOutboundConnection (final Link link);

    default void addInboundConnection (final Link link) {
    }
}

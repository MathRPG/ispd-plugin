package ispd.motor.queues.centers.impl;

public interface Vertex {

    void addConexoesSaida (Link conexao);

    default void addConexoesEntrada (final Link conexao) {
    }
}

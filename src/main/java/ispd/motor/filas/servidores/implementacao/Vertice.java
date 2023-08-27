package ispd.motor.filas.servidores.implementacao;

public interface Vertice {

    void addConexoesSaida (CS_Link conexao);

    default void addConexoesEntrada (final CS_Link conexao) {
    }
}

package ispd.motor.metrics;

import ispd.motor.queues.task.*;
import java.util.*;
import java.util.stream.*;

public class User {

    private final HashMap<String, Integer> usuarios = new HashMap<>();

    private final List<String> listaUsuarios = new ArrayList<>();

    private final List<Double> poderComputacional = new ArrayList<>();

    private final List<HashSet<GridTask>> tarefasSubmetidas = new ArrayList<>();

    private final List<HashSet<GridTask>> tarefasConcluidas = new ArrayList<>();

    private final HashMap<String, Double> limites = new HashMap<>();

    @Override
    public String toString () {
        var texto = "";
        for (var i = 0; i < this.usuarios.size(); i++) {
            texto += "Usuario: %d tarefas: sub %d con %d\n".formatted(
                this.usuarios.get(i),
                this.tarefasSubmetidas.get(i).size(),
                this.tarefasConcluidas.get(i).size()
            );
        }
        return texto;
    }

    public void addAllUsuarios (final List<String> nomes, final List<Double> poderComputacional) {
        IntStream.range(0, nomes.size())
            .forEach(i -> this.addSingleUser(nomes, poderComputacional, i));
    }

    private void addSingleUser (
        final List<String> nomes,
        final List<Double> poderComputacional,
        final int i
    ) {
        this.listaUsuarios.add(nomes.get(i));
        this.usuarios.put(nomes.get(i), i);
        this.poderComputacional.add(poderComputacional.get(i));
        this.tarefasSubmetidas.add(new HashSet<>());
        this.tarefasConcluidas.add(new HashSet<>());
    }

    public void addAllUsuarios (
        final List<String> nomes, final List<Double> poderComputacional, final List<Double> perfis
    ) {
        for (var i = 0; i < nomes.size(); i++) {
            this.limites.put(nomes.get(i), perfis.get(i));
            this.addSingleUser(nomes, poderComputacional, i);
        }
    }

    public void incTarefasSubmetidas (final GridTask tarefa) {
        final int index = this.usuarios.get(tarefa.getProprietario());
        this.tarefasSubmetidas.get(index).add(tarefa);
    }

    public void incTarefasConcluidas (final GridTask tarefa) {
        final int index = this.usuarios.get(tarefa.getProprietario());
        this.tarefasConcluidas.get(index).add(tarefa);
    }

    public HashSet<GridTask> getTarefasConcluidas (final String user) {
        final var index = this.usuarios.get(user);
        if (index != null) {
            return this.tarefasConcluidas.get(index);
        }
        return null;
    }

    public List<String> getUsuarios () {
        return this.listaUsuarios;
    }

    public HashMap<String, Double> getLimites () {
        return this.limites;
    }
}

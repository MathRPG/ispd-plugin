package ispd.motor.metricas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ispd.motor.filas.Tarefa;

public class MetricasUsuarios {

    private final HashMap<String, Integer> usuarios;
    private final List<String>             listaUsuarios;
    private final List<Double>             poderComputacional;
    private final List<HashSet<Tarefa>>    tarefasSubmetidas;
    private final List<HashSet<Tarefa>>    tarefasConcluidas;
    private final HashMap<String, Double>  limites;

    public MetricasUsuarios () {
        this.usuarios           = new HashMap<>();
        this.limites            = new HashMap<>();
        this.listaUsuarios      = new ArrayList<>();
        this.poderComputacional = new ArrayList<>();
        this.tarefasSubmetidas  = new ArrayList<>();
        this.tarefasConcluidas  = new ArrayList<>();
    }

    public void addAllUsuarios (final List<String> nomes, final List<Double> poderComputacional) {
        for (int i = 0; i < nomes.size(); i++) {
            this.listaUsuarios.add(nomes.get(i));
            this.usuarios.put(nomes.get(i), i);
            this.poderComputacional.add(poderComputacional.get(i));
            this.tarefasSubmetidas.add(new HashSet<>());
            this.tarefasConcluidas.add(new HashSet<>());
        }
    }

    public void addAllUsuarios (
            final List<String> nomes, final List<Double> poderComputacional, final List<Double> perfis
    ) {
        for (int i = 0; i < nomes.size(); i++) {
            this.limites.put(nomes.get(i), perfis.get(i));
            this.listaUsuarios.add(nomes.get(i));
            this.usuarios.put(nomes.get(i), i);
            this.poderComputacional.add(poderComputacional.get(i));
            this.tarefasSubmetidas.add(new HashSet<>());
            this.tarefasConcluidas.add(new HashSet<>());
        }
    }

    public void incTarefasSubmetidas (final Tarefa tarefa) {
        final int index = this.usuarios.get(tarefa.getProprietario());
        this.tarefasSubmetidas.get(index).add(tarefa);
    }

    public void incTarefasConcluidas (final Tarefa tarefa) {
        final int index = this.usuarios.get(tarefa.getProprietario());
        this.tarefasConcluidas.get(index).add(tarefa);
    }

    public HashSet<Tarefa> getTarefasConcluidas (final String user) {
        final Integer index = this.usuarios.get(user);
        if (index != null) {
            return this.tarefasConcluidas.get(index);
        }
        return null;
    }

    public double getPoderComputacional (final String user) {
        final Integer index = this.usuarios.get(user);
        if (index != -1) {
            return this.poderComputacional.get(index);
        } else {
            return -1;
        }
    }

    public List<String> getUsuarios () {
        return this.listaUsuarios;
    }

    public HashMap<String, Double> getLimites () {
        return this.limites;
    }

    @Override
    public String toString () {
        String texto = "";
        for (int i = 0; i < this.usuarios.size(); i++) {
            texto += "Usuario: %d tarefas: sub %d con %d\n".formatted(
                    this.usuarios.get(i),
                    this.tarefasSubmetidas.get(i).size(),
                    this.tarefasConcluidas.get(i).size()
            );
        }
        return texto;
    }
}

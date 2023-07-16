package ispd.motor.queueNetworks;

import java.util.Collection;
import java.util.HashSet;

public class CentrosDeServico {

    private final Collection<Object> servidores = new HashSet<>();

    private final Collection<Object> filas = new HashSet<>();

    /**
     * Capacidade maxima de servidores que podem ser adicionados ao CS. Esse numero considera apenas
     * servidores de processamento (mestre e escravos) ou seja, se esse numero for 100, o servidor
     * tera 101 servidores, sendo: 1 servidor de comunicacao, 1 mestre e 99 escravosm (os dois
     * ultimos sao do tipo "processamento!")
     */
    private final int numMaxServidores;

    private final int idCS;

    /**
     * Quantidade de servidores ja adicionados ao CS OBS: Em qualquer momento numAtualServidores e
     * no maximo igual a numMaxServidores.
     */
    private int numAtualServidores = 0;

    /**
     * Na inicializacao do CS nao ha adicao de servidores. Inicialmente, nao ha servidor, entao nao
     * ha servidor livre
     */
    private int numAtualFilas = 0;

    private boolean ehEscalonador;

    /**
     * Adiciona CSs Homogeneos ( maquinas (servidores de processamento), conexao de rede simples ou
     * Internet(servidores de comunicacao) ).
     */
    public CentrosDeServico (final int identCS, final int nMaxServ, final int[] vetEscravos) {
        this(identCS, nMaxServ);
        this.setVetorEscravos(vetEscravos);
    }

    public CentrosDeServico (final int identCS, final int nMaxServ) {
        this.idCS = Math.max(identCS, 0);
        this.numMaxServidores = Math.max(nMaxServ, 0);
    }

    public void setVetorEscravos (final int[] vetEscravos) {
        final int[] vetorEscravos;
        vetorEscravos = vetEscravos;
        System.out.printf("\nthis.vetorEscravos.length - " + vetorEscravos.length);
        for (int i = 0; i < vetorEscravos.length; i++) {
            System.out.printf("\nthis.vetorEscravos[" + i + "] - " + vetorEscravos[i]);
        }
    }

    public void adicionaServidorProcto (final boolean msOuEsc) {

        this.ehEscalonador = msOuEsc;

        if (!this.ehEscalonador) {
            System.out.printf("o CS %d NAO eh escalonador\n", this.idCS);
        } else {
            System.out.printf("o CS %d eh escalonador\n", this.idCS);
        }
        this.servidores.add(new Object());

        System.out.printf(
            "|\tServidor ID: %2d adicionado ao Centro de Servico ID: %2d |\n",
            this.numAtualServidores, this.idCS
        );
        this.numAtualServidores += 1;
    }

    public int getIdCS () {
        return this.idCS;
    }

    public void adicionaServidorCom () {

        this.ehEscalonador = false;
        System.out.printf("o CS %d NAO eh escalonador\n", this.idCS);
        this.servidores.add(new Object());

        System.out.printf(
            "|\tServidor ID: %2d adicionado ao Centro de Servico ID: %2d |\n",
            this.numAtualServidores, this.idCS
        );
        this.numAtualServidores += 1;
    }

    //+--------------------------------+
    public void adicionaServidoresClr () {
        this.ehEscalonador = true;
        System.out.printf("o CS %d eh escalonador\n", this.idCS);

        this.servidores.add(new Object());

        System.out.printf(
            "|\tServidor ID: %2d adicionado ao Centro de Servico ID: %2d |\n",
            this.numAtualServidores, this.idCS
        );
        this.numAtualServidores += 1;

        //Adiciona os escravos
        while (this.numAtualServidores < this.numMaxServidores) {
            this.servidores.add(new Object());

            System.out.printf(
                "|\tServidor ID: %2d adicionado ao Centro de Servico ID: %2d |\n",
                this.numAtualServidores, this.idCS
            );
            //Soma 1 ao numero atual de servidores
            this.numAtualServidores += 1;
        }
        //Apos adicionar todos os servidores de processamento, adiciono o
        // servidor de comunicacao
        //que, pelo menos por enquanto, e unico (no cluster - barramento)

        //O id do servidor de comunicacao eh getNumAtualServidores()
        //pois os servidores de comunicacao sao numerados de 0 a
        // getNumAtualServidores()-1
        //Ou seja, o proximo numero inteiro e getNumAtualServidores()
        System.out.printf(
            "|\tServidor DE COMUNICACAO ID: %2d adicionado ao Centro de Servico ID: %2d |\n",
            this.numAtualServidores, this.idCS
        );
        this.servidores.add(new Object());

        //Nao precisa adicionar valor a  numAtualServidores, pois o servidor
        // e de comunicacao e essa
        //variavel controla o numero de servidores de processamento (ate pq,
        // como dito logo acima, o
        //servidor de comunicacao e unitario!
    }

    public void adicionaFila () {
        this.filas.add(new Object());
        System.out.printf(
            "|\tFila ID: %2d adicionada ao Centro de Servico ID: %2d     |\n",
            this.numAtualFilas,
            this.idCS
        );
        this.numAtualFilas += 1;
    }
}

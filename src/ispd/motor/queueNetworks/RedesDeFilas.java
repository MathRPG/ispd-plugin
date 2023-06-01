package ispd.motor.queueNetworks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RedesDeFilas {

    private final List<Integer>             escalonadores;
    private final HashSet<CentrosDeServico> centroDeServicos;
    private       int                       numCS;
    private       int[][]                   matrizRF;
    private       int                       tam_mat;

    //+------------------------------------------Inicio dos metodos da
    // classe------------------------------------------+
/* OBSERVACAO: os metodos dessa classe sao analogos aos metodos da classe
CentroDeServico, soh que eles
funcionam em outra instancia (nivel). Aqui, funcionam para a rede de filas
toda, ou seja, eh necessario determinar
(pelo id) o centro de servico que se deseja trabalhar. Jah na classe
CentroDeServico, o centro de servico nao
precisa ser determinado (pelo id), pois isso jah eh feito chamando o metodo
pro objeto (da classe CentroDeServico)
que esta em questao. Nao serao feitos comentarios nos metodos desta classe,
pois ou o codigo pode ser facilmente
entendido (desde que se tenha conhecimento do projeto) ou os comentarios
feitos na classe CentroDeServico podem, por
si, ajudar no entendimento do codigos daqui. 
*/
//+-------------------------------------------------Metodo
// construtor----------------------------------------------+
    public RedesDeFilas () {
        this.numCS            = 0;
        this.escalonadores    = new ArrayList<>();
        this.centroDeServicos = new HashSet<>();
    }
//+----------------------------Metodos que adicionam um centro de servico a
// rede de filas----------------------------+
    /* EXPLICACAO SOBRE A DIFERENCA DO NUMERO DE PARAMETROS ENTRE O METODO
    adicionaCentroServico
	DA CLASSE RedesDeFilas E O CONSTRUTOR DA CLASSE CentrosDeServico:
	O metodo adicionaCentroServico da classe RedesDeFilas possui um argumento
	a menos do que o
	construtor da classe CentrosDeServico pois apesar desta ultima necessitar
	do parametro idCS, este
	parametro nao e lido pelo interpretador/arquivo, mas sim controlado pela
	classe RedesDeFilas (numCS)
	Ou seja, o construtor da classe CentrosDeServico recebe TODOS os
	parametros do metodo adicionaCentroServico
    da classe RedesDeFilas (que recebe/le esses argumentos do
    interpretador/arquivo) MAIS o idCS que
    e a variavel numCS controlada pela propria classe RedesDeFilas (ou seja
    NAO e recebido/lido do
    arquivo/interpretador)	
	*/

    //Para servidores do tipo maquina ou cluster 'heterogeneos'
    public int adicionaCentroServico (final int tp, final int nMaxServ, final int[] vetEscravos) {
        switch (tp) {
            case 0 -> {
                // Se for maquina
                final CentrosDeServico CS = new CentrosDeServico(this.numCS, nMaxServ, vetEscravos);
                this.centroDeServicos.add(CS);
                System.out.printf("|\tCS (tipo0) ID: %2d adicionado                    |\n", this.numCS);
            }
            case 1 -> {
                System.out.printf("\n nMaxServ = %d\n", nMaxServ);
                final CentrosDeServico CS = new CentrosDeServico(this.numCS, nMaxServ, vetEscravos);
                this.centroDeServicos.add(CS);
                System.out.printf("|\tCS ID (tip 1): %2d adicionado                    |\n", this.numCS);
            }
        }
        System.out.printf("|\tCS ID: %2d adicionado                    |\n", this.numCS);
        this.numCS++;
        return (this.numCS - 1);
    }

    //Para servidores do tipo rede (ponto-a-ponto ou internet) ou cluster 'homogeneo'
    public int adicionaCentroServico (final int tp, final int nMaxServ) {
        switch (tp) //Se for maquina
        {
            case 1 -> {
                final CentrosDeServico CS = new CentrosDeServico(this.numCS, nMaxServ);
                this.centroDeServicos.add(CS);
                System.out.printf("|\tCS ID (tp 1): %2d adicionado                    |\n", this.numCS);
            }
            case 2 -> {
                final CentrosDeServico CS = new CentrosDeServico(this.numCS, nMaxServ);
                this.centroDeServicos.add(CS);
            }
            case 3 -> {
                final CentrosDeServico CS = new CentrosDeServico(this.numCS, nMaxServ);
                this.centroDeServicos.add(CS);
            }
        }
        System.out.printf("|\tCS ID: %2d adicionado                    |\n", this.numCS);
        this.numCS++;
        return this.numCS - 1;
    }

    //+-------------Metodo que adiciona um servidor de processamento unico.
    // (Para CSs do tipo "maquina")----------------------------+
    public void adicionaServidorProcto (final int idCS, final boolean msOuEsc) {
        if (msOuEsc) {
            this.escalonadores.add(idCS);
        }
        for (final CentrosDeServico csTemp : this.centroDeServicos) {
            if (csTemp.getIdCS() == idCS) {
                csTemp.adicionaServidorProcto(msOuEsc);
            }
        }
    }

    public void adicionaServidorCom (final int idCS) {
        for (final CentrosDeServico csTemp : this.centroDeServicos) {
            if (csTemp.getIdCS() == idCS) {
                csTemp.adicionaServidorCom();
            }
        }
    }

    public void adicionaServidoresClr (final int idCS) {
        for (final CentrosDeServico csTemp : this.centroDeServicos) {
            if (csTemp.getIdCS() == idCS) {
                System.out.printf("CS PASSADO %d", idCS);

                csTemp.adicionaServidoresClr();
            }
        }
    }

    public void adicionaFila (final int idCS) {
        for (final CentrosDeServico csTemp : this.centroDeServicos) {
            if (csTemp.getIdCS() == idCS) {
                csTemp.adicionaFila();
            }
        }
    }

    public void inteligaCSs (final int origem, final int destino) {
        this.matrizRF[origem][destino] = 1;
    }

    public void instanciaMatrizVetor (final int tam_matriz) {
        this.tam_mat  = tam_matriz;
        this.matrizRF = new int[this.tam_mat][this.tam_mat];
    }

    public void setVetorEscravos (final int[] vetEscravos, final int idCs) {
        for (final CentrosDeServico temp : this.centroDeServicos) {
            if (temp.getIdCS() == idCs) {
                temp.setVetorEscravos(vetEscravos);
                break;
            }
        }
    }
}

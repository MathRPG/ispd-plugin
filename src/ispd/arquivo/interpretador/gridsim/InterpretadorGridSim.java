package ispd.arquivo.interpretador.gridsim;

import ispd.arquivo.xml.ManipuladorXML;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;

public class InterpretadorGridSim {

    private List<HashMap<String, JavaParser.ResourceChar>> ListaMetodo = null;

    private List<String> NomeMetodo = new ArrayList<>();

    private List<String> Nome = new ArrayList<>();

    private List<Integer> Quantidade = new ArrayList<>();

    private static String getInt (final String valor) {
        final var random = new Random();
        if ("random".equals(valor)) {
            return String.valueOf(Math.abs(random.nextInt()));
        } else {
            return valor;
        }
    }

    public void interpreta (final File file1) {
        try {
            final var fisfile = new FileInputStream(file1);
            final var parser  = new JavaParser(fisfile);
            parser.CompilationUnit();
            parser.escreverLista();
            this.ListaMetodo = parser.getListaMetodo();
            this.NomeMetodo  = parser.getNomeMetodo();
            this.Nome        = parser.getNome();
            this.Quantidade  = parser.getQuantidade();
        } catch (final ParseException ex) {
            System.err.println("Erro ao fechar arquivo1: " + ex.getMessage());
            Logger.getLogger(InterpretadorGridSim.class.getName()).log(Level.SEVERE, null, ex);
        } catch (final FileNotFoundException ex) {
            System.err.println("Erro ao fechar arquivo2: " + ex.getMessage());
            Logger.getLogger(InterpretadorGridSim.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Document getDescricao () {
        final var descricao = ManipuladorXML.newDocument();
        final var system    = descricao.createElement("system");
        final var load      = descricao.createElement("load");
        system.setAttribute("version", "1");
        descricao.appendChild(system);
        var cont_global = 0;
        for (var i = 0; i < this.Nome.size(); i++) {
            for (var j = 0; j < this.NomeMetodo.size(); j++) {
                if (this.NomeMetodo.get(j).equals(this.Nome.get(i))) {
                    for (var k = 1; k < this.Quantidade.get(i); k++) {
                        //obter o conteúdo do método
                        final var temp = this.ListaMetodo.get(j);
                        //criar uma cópia
                        final var tempAux =
                            new HashMap<String, JavaParser.ResourceChar>();
                        final var criar = new JavaParser();
                        for (final var entry : temp.entrySet()) {
                            final var string       = entry.getKey();
                            final var resourceChar = entry.getValue();
                            if ("GridResource".equals(resourceChar.getType())) {
                                final var aux =
                                    criar.Criar(resourceChar, resourceChar.getName() + k);
                                aux.setLink(criar.Criar(
                                    resourceChar.getLink(),
                                    resourceChar.getLink().getName_Link() + cont_global
                                ));
                                cont_global++;
                                var                                 l         = 100;
                                final List<JavaParser.ResourceChar> mach_list = new ArrayList<>();
                                for (final var mach : resourceChar
                                    .getResConfig()
                                    .getMLIST()
                                    .getMach_List()) {
                                    final var maq = criar.Criar(mach, resourceChar.getId() + k);
                                    mach_list.add(maq);
                                    tempAux.put(aux.getName() + k + l, maq);
                                    l++;
                                }
                                aux.setResConfig(criar.Criar(
                                    resourceChar.getResConfig(),
                                    resourceChar.getName() + k
                                ));
                                aux.getResConfig()
                                    .setMlist(criar.Criar(
                                        resourceChar.getResConfig().getMLIST(),
                                        "mlist" + k
                                    ));
                                aux.getResConfig().getMLIST().setMach_List(mach_list);
                                tempAux.put(aux.getName() + k, aux);
                            }
                        }
                        //adicionar cópia na lista
                        this.ListaMetodo.add(tempAux);
                    }
                }
            }
        }
        var       ident_global = 0;
        var       icon         = 0;
        final var idGlobal     = new HashMap<JavaParser.ResourceChar, String>();
        final var mestre       = new LinkedList<String>();
        final var user         = new LinkedList<String>();
        for (final var stringResourceCharHashMap : this.ListaMetodo) {
            //For para percorrer lista
            for (final var object : stringResourceCharHashMap.entrySet()) {
                final var res = object.getValue();
                switch (res.getType()) {
                    case "ResourceUser" -> {
                        final var owner = descricao.createElement("owner");
                        owner.setAttribute("id", res.getUserID());
                        user.add(res.getUserID());
                        system.appendChild(owner);
                    }
                    case "Machine", "Router" -> {
                        icon++;
                        idGlobal.put(res, Integer.toString(ident_global));
                        ident_global++;
                    }
                    case "GridResource" -> {
                        icon++;
                        idGlobal.put(res, Integer.toString(ident_global));
                        ident_global++;
                        mestre.add(res.getName());
                    }
                }
            }
        }
        final var num_linhas = Math.floor(Math.sqrt(icon));
        final var num_col    = num_linhas + 1;
        if (user.isEmpty()) {
            final var owner = descricao.createElement("owner");
            owner.setAttribute("id", "user1");
            user.add("user1");
            system.appendChild(owner);
        }
        if (mestre.isEmpty()) {
            mestre.add("---");
        }
        var col          = 0.0;
        var lin          = 0.0;
        var coluna       = 50;
        var linha        = 50;
        var cont_link    = 0;
        var cont_machine = 0;
        for (final var resourceCharHashMap : this.ListaMetodo) {
            var iUser   = user.iterator();
            var iMestre = mestre.iterator();
            //For para percorrer lista
            for (final var object : resourceCharHashMap.entrySet()) {
                final var res = object.getValue();
                if ("gridlet".equals(res.getType())) {
                    final var node = descricao.createElement("node");
                    node.setAttribute("owner", iUser.next().toString());
                    if (!iUser.hasNext()) {
                        iUser = user.iterator();
                    }
                    node.setAttribute("application", "application0");
                    node.setAttribute("id_master", iMestre.next().toString());
                    if (!iMestre.hasNext()) {
                        iMestre = mestre.iterator();
                    }
                    node.setAttribute("tasks", "1");
                    final var size1 = descricao.createElement("size");
                    size1.setAttribute("type", "computing");
                    final var computing = this.getDouble(res.getLenght());
                    size1.setAttribute("maximum", computing);
                    size1.setAttribute("minimum", computing);
                    final var size2 = descricao.createElement("size");
                    size2.setAttribute("type", "communication");
                    var comunication = Double.parseDouble(this.getDouble(res.getOutput_size()));
                    comunication += Double.parseDouble(this.getDouble(res.getFile_size()));
                    size2.setAttribute("maximum", String.valueOf(comunication));
                    size2.setAttribute("minimum", String.valueOf(comunication));
                    node.appendChild(size1);
                    node.appendChild(size2);
                    load.appendChild(node);
                }
                if ("Machine".equals(res.getType())) {
                    final var machine = descricao.createElement("machine");
                    machine.setAttribute("id", "maq" + cont_global);
                    cont_global++;
                    machine.setAttribute("power", InterpretadorGridSim.getInt(res.getMipsRating()));
                    machine.setAttribute("owner", iUser.next().toString());
                    if (!iUser.hasNext()) {
                        iUser = user.iterator();
                    }
                    machine.setAttribute("load", "0.0");
                    final var pos = descricao.createElement("position");
                    pos.setAttribute("x", Integer.toString(linha));
                    pos.setAttribute("y", Integer.toString(coluna));

                    if (lin < num_linhas) {
                        linha = linha + 100;
                        lin   = lin + 1.0;
                    } else {
                        if (col < num_col) {
                            coluna = coluna + 100;
                            lin    = 0.0;
                            linha  = 50;
                            col    = col + 1.0;
                        }
                    }
                    machine.appendChild(pos);
                    final var id = descricao.createElement("icon_id");
                    id.setAttribute("global", idGlobal.get(res));
                    id.setAttribute("local", Integer.toString(cont_machine));
                    cont_machine++;
                    machine.appendChild(id);
                    system.appendChild(machine);
                }
                if ("Router".equals(res.getType())) {
                    final var internet = descricao.createElement("internet");
                    internet.setAttribute("id", res.getName_Router());
                    internet.setAttribute("bandwidth", "1000.0");
                    internet.setAttribute("latency", "0.001");
                    internet.setAttribute("load", "0.0");
                    final var pos = descricao.createElement("position");
                    pos.setAttribute("x", Integer.toString(linha));
                    pos.setAttribute("y", Integer.toString(coluna));
                    internet.appendChild(pos);
                    final var id = descricao.createElement("icon_id");
                    id.setAttribute("global", idGlobal.get(res));
                    id.setAttribute("local", Integer.toString(cont_machine));
                    cont_machine++;
                    internet.appendChild(id);
                    system.appendChild(internet);
                    if (lin < num_linhas) {
                        linha = linha + 100;
                        lin   = lin + 1.0;
                    } else {
                        if (col < num_col) {
                            coluna = coluna + 100;
                            lin    = 0.0;
                            linha  = 50;
                            col    = col + 1.0;
                        }
                    }
                }
                if ("GridResource".equals(res.getType())) {
                    final var machine = descricao.createElement("machine");
                    machine.setAttribute("id", res.getName());
                    machine.setAttribute("power", "100.0");
                    machine.setAttribute("owner", iUser.next().toString());
                    if (!iUser.hasNext()) {
                        iUser = user.iterator();
                    }
                    machine.setAttribute("load", "0.0");
                    final var master = descricao.createElement("master");
                    master.setAttribute("scheduler", "RoundRobin");
                    for (final var slv : res
                        .getResConfig()
                        .getMLIST()
                        .getMach_List()) {
                        final var slave = descricao.createElement("slave");
                        slave.setAttribute("id", idGlobal.get(slv));
                        master.appendChild(slave);
                        //adiciona o link de ida
                        var link_gr = descricao.createElement("link");
                        link_gr.setAttribute(
                            "id",
                            "link_"
                            + res.getLink().getName_Link()
                            + cont_global
                        );
                        cont_global++;
                        link_gr.setAttribute(
                            "bandwidth",
                            this.getDouble(res.getLink().getBaud_rate())
                        );
                        link_gr.setAttribute(
                            "latency",
                            this.getDouble(res.getLink().getPropDelay())
                        );
                        link_gr.setAttribute("load", "0.0");
                        var connect = descricao.createElement("connect");
                        connect.setAttribute("origination", idGlobal.get(slv));
                        connect.setAttribute("destination", idGlobal.get(res));
                        link_gr.appendChild(connect);
                        var id = descricao.createElement("icon_id");
                        id.setAttribute("global", Integer.toString(ident_global));
                        id.setAttribute("local", Integer.toString(cont_link));
                        ident_global++;
                        cont_link++;
                        link_gr.appendChild(id);
                        system.appendChild(link_gr);
                        //adiciona link de volta
                        link_gr = descricao.createElement("link");
                        link_gr.setAttribute(
                            "id",
                            "link_"
                            + res.getLink().getName_Link()
                            + cont_global
                            + "_1"
                        );
                        cont_global++;
                        link_gr.setAttribute(
                            "bandwidth",
                            this.getDouble(res.getLink().getBaud_rate())
                        );
                        link_gr.setAttribute(
                            "latency",
                            this.getDouble(res.getLink().getPropDelay())
                        );
                        link_gr.setAttribute("load", "0.0");
                        connect = descricao.createElement("connect");
                        connect.setAttribute("origination", idGlobal.get(res));
                        connect.setAttribute("destination", idGlobal.get(slv));
                        link_gr.appendChild(connect);
                        id = descricao.createElement("icon_id");
                        id.setAttribute("global", Integer.toString(ident_global));
                        id.setAttribute("local", Integer.toString(cont_link));
                        ident_global++;
                        cont_link++;
                        link_gr.appendChild(id);
                        system.appendChild(link_gr);
                    }
                    machine.appendChild(master);
                    final var pos = descricao.createElement("position");
                    pos.setAttribute("x", Integer.toString(linha));
                    pos.setAttribute("y", Integer.toString(coluna));
                    if (lin < num_linhas) {
                        linha = linha + 100;
                        lin   = lin + 1.0;
                    } else {
                        if (col < num_col) {
                            coluna = coluna + 100;
                            lin    = 0.0;
                            linha  = 50;
                            col    = col + 1.0;
                        }
                    }
                    machine.appendChild(pos);
                    final var id = descricao.createElement("icon_id");
                    id.setAttribute("global", idGlobal.get(res));
                    id.setAttribute("local", Integer.toString(cont_machine));
                    cont_machine++;
                    machine.appendChild(id);
                    system.appendChild(machine);
                }
            }
        }
        for (final var stringResourceCharHashMap : this.ListaMetodo) {
            var iUser = user.iterator();
            //For para percorrer lista
            for (final var object : stringResourceCharHashMap.entrySet()) {
                final var res = object.getValue();
                if ("SimpleLink".equals(res.getType())) {
                    if (res.getOrigination() != null && res.getRouter() != null) {
                        //adiciona link de ida
                        var link = descricao.createElement("link");
                        link.setAttribute("id", res.getName_Link());
                        link.setAttribute("bandwidth", this.getDouble(res.getBaud_rate()));
                        link.setAttribute("latency", this.getDouble(res.getPropDelay()));
                        link.setAttribute("load", "0.0");
                        var connect = descricao.createElement("connect");
                        connect.setAttribute("origination", idGlobal.get(res.getOrigination()));
                        connect.setAttribute("destination", idGlobal.get(res.getRouter()));
                        link.appendChild(connect);
                        var id = descricao.createElement("icon_id");
                        id.setAttribute("global", Integer.toString(ident_global));
                        id.setAttribute("local", Integer.toString(cont_link));
                        ident_global++;
                        cont_link++;
                        link.appendChild(id);
                        system.appendChild(link);
                        //adiciona link de volta
                        link = descricao.createElement("link");
                        link.setAttribute("id", res.getName_Link() + "1");
                        link.setAttribute("bandwidth", this.getDouble(res.getBaud_rate()));
                        link.setAttribute("latency", this.getDouble(res.getPropDelay()));
                        link.setAttribute("load", "0.0");
                        connect = descricao.createElement("connect");
                        connect.setAttribute("destination", idGlobal.get(res.getOrigination()));
                        connect.setAttribute("origination", idGlobal.get(res.getRouter()));
                        link.appendChild(connect);
                        id = descricao.createElement("icon_id");
                        id.setAttribute("global", Integer.toString(ident_global));
                        id.setAttribute("local", Integer.toString(cont_link));
                        ident_global++;
                        cont_link++;
                        link.appendChild(id);
                        system.appendChild(link);
                    }
                }
                if ("attachHost".equals(res.getType()) && res.getCoreAttach().equals("ALL")) {
                    for (final var elem : idGlobal.keySet()) {
                        if ("GridResource".equals(elem.getType()) && elem.getLink() != null &&
                            res.getOrigination() != null) {
                            if (res.getOrigination() != null) {
                                //adiciona link de ida
                                var link = descricao.createElement("link");
                                link.setAttribute(
                                    "id",
                                    "link_all" + elem.getLink().getName_Link()
                                );
                                link.setAttribute(
                                    "bandwidth",
                                    this.getDouble(elem.getLink().getBaud_rate())
                                );
                                link.setAttribute(
                                    "latency",
                                    this.getDouble(elem.getLink().getPropDelay())
                                );
                                link.setAttribute("load", "0.0");
                                var connect = descricao.createElement("connect");
                                connect.setAttribute("origination", idGlobal.get(elem));
                                connect.setAttribute(
                                    "destination",
                                    idGlobal.get(res.getOrigination())
                                );
                                link.appendChild(connect);
                                var id = descricao.createElement("icon_id");
                                id.setAttribute("global", Integer.toString(ident_global));
                                id.setAttribute("local", Integer.toString(cont_link));
                                ident_global++;
                                cont_link++;
                                link.appendChild(id);
                                system.appendChild(link);
                                //adiciona link de volta
                                link = descricao.createElement("link");
                                link.setAttribute(
                                    "id",
                                    "link_all_" + elem.getLink().getName_Link() + "1"
                                );
                                link.setAttribute(
                                    "bandwidth",
                                    this.getDouble(elem.getLink().getBaud_rate())
                                );
                                link.setAttribute(
                                    "latency",
                                    this.getDouble(elem.getLink().getPropDelay())
                                );
                                link.setAttribute("load", "0.0");
                                connect = descricao.createElement("connect");
                                connect.setAttribute("destination", idGlobal.get(elem));
                                connect.setAttribute(
                                    "origination",
                                    idGlobal.get(res.getOrigination())
                                );
                                link.appendChild(connect);
                                id = descricao.createElement("icon_id");
                                id.setAttribute("global", Integer.toString(ident_global));
                                id.setAttribute("local", Integer.toString(cont_link));
                                ident_global++;
                                cont_link++;
                                link.appendChild(id);
                                system.appendChild(link);
                            }
                        }
                    }
                } else if ("attachHost".equals(res.getType()) &&
                           res.getCoreAttach() instanceof JavaParser.ResourceChar) {
                    var link = descricao.createElement("link");
                    link.setAttribute("id", "link_temp" + cont_global);
                    cont_global++;
                    link.setAttribute("bandwidth", "1000");
                    link.setAttribute("latency", "0.001");
                    link.setAttribute("load", "0.0");
                    var connect = descricao.createElement("connect");
                    connect.setAttribute("origination", idGlobal.get(res.getOrigination()));
                    connect.setAttribute("destination", idGlobal.get(res.getCoreAttach()));
                    link.appendChild(connect);
                    var id = descricao.createElement("icon_id");
                    id.setAttribute("global", Integer.toString(ident_global));
                    id.setAttribute("local", Integer.toString(cont_link));
                    ident_global++;
                    cont_link++;
                    link.appendChild(id);
                    system.appendChild(link);
                    //adiciona link de volta
                    link = descricao.createElement("link");
                    link.setAttribute("id", "link_temp" + cont_global);
                    cont_global++;
                    link.setAttribute("bandwidth", "1000");
                    link.setAttribute("latency", "0.001");
                    link.setAttribute("load", "0.0");
                    connect = descricao.createElement("connect");
                    connect.setAttribute("destination", idGlobal.get(res.getOrigination()));
                    connect.setAttribute("origination", idGlobal.get(res.getCoreAttach()));
                    link.appendChild(connect);
                    id = descricao.createElement("icon_id");
                    id.setAttribute("global", Integer.toString(ident_global));
                    id.setAttribute("local", Integer.toString(cont_link));
                    ident_global++;
                    cont_link++;
                    link.appendChild(id);
                    system.appendChild(link);
                } else if ("attachHost".equals(res.getType())) {
                    final var ident        = descricao.createElement("icon_id");
                    final var mestreGlobal = Integer.toString(ident_global);
                    ident.setAttribute("global", Integer.toString(ident_global));
                    ident.setAttribute("local", Integer.toString(cont_machine));
                    ident_global++;
                    cont_machine++;
                    final var machine = descricao.createElement("machine");
                    machine.setAttribute("id", res.getName() + "mestre");
                    machine.setAttribute("power", "100.0");
                    machine.setAttribute("owner", iUser.next().toString());
                    if (!iUser.hasNext()) {
                        iUser = user.iterator();
                    }
                    machine.setAttribute("load", "0.0");
                    final var master = descricao.createElement("master");
                    master.setAttribute("scheduler", "RoundRobin");
                    //adiciona escravos
                    for (final var slv : idGlobal.keySet()) {
                        if ("GridResource".equals(slv.getType())) {
                            final var slave = descricao.createElement("slave");
                            slave.setAttribute("id", idGlobal.get(slv));
                            master.appendChild(slave);
                        }
                    }
                    //adiciona o link de ida
                    var link_gr = descricao.createElement("link");
                    link_gr.setAttribute("id", "link_temp" + cont_global);
                    cont_global++;
                    link_gr.setAttribute("bandwidth", "1000");
                    link_gr.setAttribute("latency", "0.001");
                    link_gr.setAttribute("load", "0.0");
                    var connect = descricao.createElement("connect");
                    connect.setAttribute("origination", mestreGlobal);
                    connect.setAttribute("destination", idGlobal.get(res.getOrigination()));
                    link_gr.appendChild(connect);
                    var id = descricao.createElement("icon_id");
                    id.setAttribute("global", Integer.toString(ident_global));
                    id.setAttribute("local", Integer.toString(cont_link));
                    ident_global++;
                    cont_link++;
                    link_gr.appendChild(id);
                    system.appendChild(link_gr);
                    //adiciona link de volta
                    link_gr = descricao.createElement("link");
                    link_gr.setAttribute("id", "link_temp" + cont_global);
                    cont_global++;
                    link_gr.setAttribute("bandwidth", "1000");
                    link_gr.setAttribute("latency", "0.001");
                    link_gr.setAttribute("load", "0.0");
                    connect = descricao.createElement("connect");
                    connect.setAttribute("origination", idGlobal.get(res.getOrigination()));
                    connect.setAttribute("destination", mestreGlobal);
                    link_gr.appendChild(connect);
                    id = descricao.createElement("icon_id");
                    id.setAttribute("global", Integer.toString(ident_global));
                    id.setAttribute("local", Integer.toString(cont_link));
                    ident_global++;
                    cont_link++;
                    link_gr.appendChild(id);
                    system.appendChild(link_gr);
                    machine.appendChild(master);
                    final var pos = descricao.createElement("position");
                    pos.setAttribute("x", Integer.toString(linha));
                    pos.setAttribute("y", Integer.toString(coluna));
                    if (lin < num_linhas) {
                        linha = linha + 100;
                        lin   = lin + 1.0;
                    } else {
                        if (col < num_col) {
                            coluna = coluna + 100;
                            lin    = 0.0;
                            linha  = 50;
                            col    = col + 1.0;
                        }
                    }
                    machine.appendChild(pos);
                    machine.appendChild(ident);
                    system.appendChild(machine);
                }
            }
        }
        system.appendChild(load);
        return descricao;
    }

    private String getDouble (final String valor) {
        final var random = new Random();
        if ("random".equals(valor)) {
            final var low = 500000;
            // generate a value of a random variable from distribution uniform(a,b)
            final var tsu = (random.nextDouble() * (750000 - low)) + low;
            return String.valueOf(Math.abs(tsu));
        } else {
            return valor;
        }
    }
}

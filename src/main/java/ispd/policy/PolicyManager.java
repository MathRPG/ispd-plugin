package ispd.policy;

import java.io.*;
import java.util.*;

public interface PolicyManager {

    String NO_POLICY = "---";

    ArrayList<String> listar ();

    File directory ();

    List listarAdicionados ();

    List listarRemovidos ();
}

package ispd.policy;

import java.io.*;
import java.util.*;

public interface PolicyManager {

    String NO_POLICY = "---";

    ArrayList<String> listar ();

    File directory ();

    default List listarAdicionados () {
        return new ArrayList<String>();
    }

    default List listarRemovidos () {
        return new ArrayList<String>();
    }
}

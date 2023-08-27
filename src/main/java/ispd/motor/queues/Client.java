package ispd.motor.queues;

import ispd.motor.queues.centers.*;
import java.util.*;

public interface Client {

    double getTamComunicacao ();

    double getTamProcessamento ();

    double getTimeCriacao ();

    Service getOrigem ();

    List<Service> getCaminho ();

    void setCaminho (List<Service> caminho);
}

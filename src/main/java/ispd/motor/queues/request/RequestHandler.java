package ispd.motor.queues.request;

import ispd.motor.simul.*;

public interface RequestHandler {

    void handleReturn (Simulation simulacao, Request request);

    void handlePreemptiveReturn (Simulation simulacao, Request request);

    void handleUpdate (Simulation simulacao, Request request);

    void handleUpdateResult (Simulation simulacao, Request request);

    void handleFailure (Simulation simulacao, Request request);

    void handleAllocationAck (Simulation simulacao, Request request);

    void handleStop (Simulation simulacao, Request request);

    void handleCancel (Simulation simulacao, Request request);
}

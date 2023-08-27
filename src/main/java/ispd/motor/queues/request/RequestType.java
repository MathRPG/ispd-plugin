package ispd.motor.queues.request;

public enum RequestType {
    RETURN,
    PREEMPTIVE_RETURN,
    UPDATE,
    UPDATE_RESULT,
    FAILURE,

    ALLOCATION_ACK,

    STOP,
    CANCEL,
}

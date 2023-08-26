package ispd.motor;

public enum MessageType {
    RETURN,
    PREEMPTIVE_RETURN,
    UPDATE,
    UPDATE_RESULT,
    FAIL,

    ALLOCATION_ACK,

    STOP,
    CANCEL,
}

package ispd.motor.queues.task;

public enum TaskState {
    BLOCKED,
    PROCESSING,
    DONE,
    CANCELLED,
    FAILED,
}

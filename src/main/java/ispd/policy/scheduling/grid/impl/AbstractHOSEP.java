package ispd.policy.scheduling.grid.impl;

import ispd.motor.queues.centers.*;
import ispd.motor.queues.request.*;
import ispd.motor.queues.task.*;
import ispd.policy.scheduling.*;
import ispd.policy.scheduling.grid.impl.util.*;
import java.util.*;
import java.util.stream.*;

public abstract class AbstractHOSEP <T extends UserProcessingControl> extends AbstractOSEP<T> {

    private final Collection<GridTask> tasksToSchedule = new HashSet<>();

    private final Collection<PreemptionEntry> preemptionEntries = new HashSet<>();

    private static boolean hasProcessingCenter (final GridTask t) {
        return t.getLocalProcessamento() != null;
    }

    protected abstract Optional<T> findUserToPreemptFor (T taskOwner);

    /**
     * Attempts to schedule a task and a suitable machine for one of the users, giving preference to
     * users "first" in a sorted list according to the {@link #getUserComparator()} comparison
     * criteria} of {@link UserProcessingControl}.<br>
     * <p>
     * The method stops immediately upon any successful scheduling of a task in a resource, be it
     * 'normally' or through preemption.
     * </p>
     * For details on scheduling criteria, see:
     * <ul>
     * <li>{@link #findTaskSuitableFor(UserProcessingControl) Task selection}
     * </li>
     * <li>{@link #findMachineBestSuitedFor(GridTask, UserProcessingControl)
     * Machine
     * selection}</li>
     * </ul>
     */
    @Override
    public void escalonar () {
        for (final var uc : this.sortedUserControls()) {
            if (this.canScheduleTaskFor(uc)) {
                return;
            }
        }
    }

    /**
     * This algorithm's resource scheduling does not conform to the standard
     * {@link SchedulingPolicy} interface.<br> Therefore, calling this method on instances of this
     * algorithm will result in an {@link UnsupportedOperationException} being thrown.
     *
     * @return not applicable in this context, an exception is thrown instead.
     *
     * @throws UnsupportedOperationException
     *     whenever called.
     */
    @Override
    public Processing escalonarRecurso () {
        throw new UnsupportedOperationException(
            """
            Do not call method .escalonarRecurso() on HOSEP-like algorithms."""
        );
    }

    /**
     * This algorithm's task scheduling does not conform to the standard {@link SchedulingPolicy}
     * interface.
     * <p>
     * Therefore, calling this method on instances of this algorithm will result in an
     * {@link UnsupportedOperationException} being thrown.
     *
     * @return not applicable in this context, an exception is thrown instead.
     *
     * @throws UnsupportedOperationException
     *     whenever called.
     */
    @Override
    public GridTask escalonarTarefa () {
        throw new UnsupportedOperationException(
            """
            Do not call method .escalonarTarefa() on HOSEP-like algorithms."""
        );
    }

    @Override
    public void addTarefaConcluida (final GridTask tarefa) {
        super.addTarefaConcluida(tarefa);

        final var maq = tarefa.getCSLProcessamento();
        final var sc  = this.slaveControls.get(maq);

        if (sc.isOccupied()) {
            this.userControls
                .get(tarefa.getProprietario())
                .stopTaskFrom(maq);

            sc.setAsFree();
        } else if (sc.isBlocked()) {
            this.processPreemptedTask(tarefa);
        }
    }

    @Override
    public void resultadoAtualizar (final Request request) {
        final var sc = this.slaveControls
            .get((Processing) request.getOrigem());

        sc.setTasksInProcessing(request.getProcessadorEscravo());
        sc.updateStatusIfNeeded();
    }

    @Override
    public void adicionarTarefa (final GridTask tarefa) {
        super.adicionarTarefa(tarefa);

        this.userControls
            .get(tarefa.getProprietario())
            .increaseTaskDemand();

        Optional.of(tarefa)
            .filter(AbstractHOSEP::hasProcessingCenter)
            .ifPresent(this::processPreemptedTask);
    }

    private List<T> sortedUserControls () {
        return this.userControls.values().stream()
            .sorted(this.getUserComparator())
            .toList();
    }

    /**
     * Attempts to find a task and a resource to execute such task, for the user represented in
     * {@code uc}. If successful, will initiate the execution of the selected task in the selected
     * resource and return {@code true} if such procedure succeeds; otherwise, won't do anything and
     * will return {@code false}.<br>
     *
     * @param uc
     *     {@link UserProcessingControl} for the user whose tasks may need scheduling
     *
     * @return {@code true} if a task and resource were selected successfully, and the task was sent
     * to be executed in the resource successfully; {@code false} otherwise
     */
    private boolean canScheduleTaskFor (final T uc) {
        try {
            this.tryFindTaskAndResourceFor(uc);
            return true;
        } catch (final NoSuchElementException | IllegalStateException ignored) {
            return false;
        }
    }

    protected Comparator<T> getUserComparator () {
        return Comparator
            .<T>comparingDouble(UserProcessingControl::percentageOfProcessingPowerUsed)
            .thenComparingDouble(UserProcessingControl::getOwnedMachinesProcessingPower);
    }

    /**
     * Attempts to find a task and a resource for the user represented in {@code uc}, and initiate
     * the process of hosting the selected task in the selected resource.<br> If it fails in finding
     * either an appropriate task or a suitable resource for the selected task, will throw a
     * {@link NoSuchElementException}.<br> If hosting the selected task in the selected resource
     * fails, will echo the exception thrown in the process. Namely,
     * {@link IllegalStateException}.<br>
     *
     * @param uc
     *     {@link UserProcessingControl} representing the user whose tasks may need scheduling
     *
     * @throws NoSuchElementException
     *     if it cannot select either an appropriate task or a suitable resource for a selected
     *     task, for the given {@link UserProcessingControl}
     * @throws IllegalStateException
     *     if hosting the selected task in the selected resource fails
     */
    private void tryFindTaskAndResourceFor (final T uc) {
        final var task = this
            .findTaskSuitableFor(uc)
            .orElseThrow();

        final var machine = this
            .findMachineBestSuitedFor(task, uc)
            .orElseThrow();

        this.tryHostTaskFromUserInMachine(task, uc, machine);
    }

    private Optional<GridTask> findTaskSuitableFor (final T uc) {
        if (!uc.isEligibleForTask()) {
            return Optional.empty();
        }

        return this.tasksOwnedBy(uc)
            .min(Comparator.comparingDouble(GridTask::getTamProcessamento));
    }

    private Optional<Processing> findMachineBestSuitedFor (
        final GridTask task,
        final T taskOwner
    ) {
        return this
            .findAvailableMachineBestSuitedFor(task, taskOwner)
            .or(() -> this.findOccupiedMachineBestSuitedFor(taskOwner));
    }

    /**
     * Attempts to initiate the execution (host) of the given {@link GridTask task} in the given
     * {@link Processing processing center}.<br> If it is determined that the given
     * {@code machine}'s <i>status</i> {@link SlaveControl#canHostNewTask() is not suited} for
     * hosting a new task, an {@link IllegalStateException} is thrown; otherwise, will host the task
     * in the given machine.<br> Once it is determined that the machine is suitable for receiving a
     * new task, the hosting process is <i>guaranteed to succeed</i>.<br>
     *
     * @param task
     *     {@link GridTask task} to host in the given {@link Processing machine}
     * @param taskOwner
     *     {@link UserProcessingControl} representing the owner of the given {@link GridTask task}
     * @param machine
     *     {@link Processing processing center} that may host the task; it must be in a valid
     *     state to do so
     *
     * @throws IllegalStateException
     *     if the given {@link Processing machine} is not in a suitable state for hosting a
     *     new task
     * @see #canMachineHostNewTask(Processing) Machine Status Validation
     */
    private void tryHostTaskFromUserInMachine (
        final GridTask task,
        final T taskOwner,
        final Processing machine
    ) {
        if (!this.canMachineHostNewTask(machine)) {
            throw new IllegalStateException(
                """
                Scheduled machine %s can not host tasks""".formatted(machine)
            );
        }

        this.hostTaskFromUserInMachine(task, taskOwner, machine);
    }

    private Stream<GridTask> tasksOwnedBy (final T uc) {
        return this.tarefas.stream().filter(uc::isOwnerOf);
    }

    private Optional<Processing> findAvailableMachineBestSuitedFor (
        final GridTask task,
        final T taskOwner
    ) {
        return this.availableMachinesFor(taskOwner)
            .max(this.compareAvailableMachinesFor(task));
    }

    private Optional<Processing> findOccupiedMachineBestSuitedFor (final T taskOwner) {
        // If no available machine is found, preemption may be used to force
        // the task into one. However, if the task owner has excess
        // processing power, preemption will NOT be used to accommodate them
        if (taskOwner.hasExcessProcessingPower() ||
            !this.theBestUser().hasExcessProcessingPower()) {
            return Optional.empty();
        }

        return this.findMachineToPreemptFor(taskOwner);
    }

    private boolean canMachineHostNewTask (final Processing machine) {
        return this.slaveControls.get(machine).canHostNewTask();
    }

    private void hostTaskFromUserInMachine (
        final GridTask task,
        final T taskOwner,
        final Processing machine
    ) {
        this.sendTaskToResource(task, machine);
        this.tarefas.remove(task);

        if (this.isMachineAvailable(machine)) {
            this.hostTaskNormally(task, taskOwner, machine);
        } else if (this.isMachineOccupied(machine)) {
            this.hostTaskWithPreemption(task, taskOwner, machine);
        }

        this.slaveControls.get(machine).setAsBlocked();
    }

    protected Stream<Processing> availableMachinesFor (final T taskOwner) {
        return this.escravos.stream().filter(this::isMachineAvailable);
    }

    protected Comparator<Processing> compareAvailableMachinesFor (final GridTask task) {
        return Comparator.comparingDouble(Processing::getPoderComputacional);
    }

    protected T theBestUser () {
        return this.userControls.values().stream()
            .max(this.getUserComparator())
            .orElseThrow();
    }

    protected Optional<Processing> findMachineToPreemptFor (final T taskOwner) {
        return this.findUserToPreemptFor(taskOwner).flatMap(
            userToPreempt -> this.findMachineToTransferBetween(userToPreempt, taskOwner));
    }

    private void sendTaskToResource (
        final GridTask task, final Service resource
    ) {
        task.setLocalProcessamento(resource);
        task.setCaminho(this.escalonarRota(resource));
    }

    private boolean isMachineAvailable (final Processing machine) {
        return this.slaveControls.get(machine).isFree();
    }

    private void hostTaskNormally (final GridTask task, final T uc, final Processing machine) {
        this.sendTaskFromUserToMachine(task, uc, machine);
        uc.decreaseTaskDemand();
    }

    private boolean isMachineOccupied (final Processing machine) {
        return this.slaveControls.get(machine).isOccupied();
    }

    private void hostTaskWithPreemption (
        final GridTask taskToSchedule, final T taskOwner, final Processing machine
    ) {
        final var taskToPreempt = this.taskToPreemptIn(machine);

        this.preemptionEntries.add(
            new PreemptionEntry(taskToPreempt, taskToSchedule)
        );

        this.tasksToSchedule.add(taskToSchedule);

        this.mestre.sendMessage(
            taskToPreempt,
            machine,
            RequestType.PREEMPTIVE_RETURN
        );

        taskOwner.decreaseTaskDemand();
    }

    protected Optional<Processing> findMachineToTransferBetween (
        final T userToPreempt, final T taskOwner
    ) {
        return this.machinesTransferableBetween(userToPreempt, taskOwner)
            .min(this.compareOccupiedMachines())
            .filter(machine -> this.shouldTransferMachine(
                machine, userToPreempt, taskOwner));
    }

    private void sendTaskFromUserToMachine (
        final GridTask task,
        final T taskOwner,
        final Processing machine
    ) {
        this.mestre.sendTask(task);
        taskOwner.startTaskFrom(machine);
    }

    protected GridTask taskToPreemptIn (final Processing machine) {
        return this.slaveControls.get(machine).firstTaskInProcessing();
    }

    protected Stream<Processing> machinesTransferableBetween (
        final T userToPreempt, final T taskOwner
    ) {
        return this.machinesOccupiedBy(userToPreempt);
    }

    protected Comparator<Processing> compareOccupiedMachines () {
        return Comparator.comparingDouble(Processing::getPoderComputacional);
    }

    protected boolean shouldTransferMachine (
        final Processing machine,
        final T machineOwner, final T nextOwner
    ) {
        return machineOwner.canConcedeProcessingPower(machine);
    }

    private Stream<Processing> machinesOccupiedBy (final T userToPreempt) {
        return this.escravos.stream()
            .filter(this::isMachineOccupied)
            .filter(machine -> userToPreempt.isOwnerOf(this.taskToPreemptIn(machine)));
    }

    private void processPreemptedTask (final GridTask task) {
        final var pe = this.findEntryForPreemptedTask(task);

        this.tasksToSchedule.stream()
            .filter(pe::willScheduleTask)
            .findFirst()
            .ifPresent(t -> this
                .insertTaskIntoPreemptedTaskSlot(t, task));
    }

    private PreemptionEntry findEntryForPreemptedTask (final GridTask t) {
        return this.preemptionEntries.stream()
            .filter(pe -> pe.willPreemptTask(t))
            .findFirst()
            .orElseThrow();
    }

    private void insertTaskIntoPreemptedTaskSlot (
        final GridTask scheduled,
        final GridTask preempted
    ) {
        this.tasksToSchedule.remove(scheduled);

        final var mach = preempted.getCSLProcessamento();
        final var pe   = this.findEntryForPreemptedTask(preempted);

        final var user = this.userControls.get(pe.scheduledTaskUser());
        this.sendTaskFromUserToMachine(scheduled, user, mach);

        this.userControls
            .get(pe.preemptedTaskUser())
            .stopTaskFrom(mach);

        this.preemptionEntries.remove(pe);
    }
}

package ispd.gui.drawing;

public class VirtualMachine {

    /**
     * It represents the amount of cores in the chip.
     */
    private final int coreCount;

    /**
     * It represents the allocated memory in
     * <em>gigabytes (GB)</em>.
     */
    private final double allocatedMemory;

    /**
     * It represents the allocated disk in
     * <em>gigabytes (GB)</em>.
     */
    private final double allocatedDisk;

    /**
     * It represents its name.
     */
    private final String name;

    /**
     * It represents its owner.
     */
    private final String owner;

    /**
     * It represents the running operating system.s
     */
    private final String operatingSystem;

    private final String VMM;

    /**
     * Constructor of  which specifies the id, owner, VMM, core count, allocated memory, allocated
     * disk and the running operating system.
     *
     * @param id
     *     the id
     * @param owner
     *     the owner
     * @param VMM
     *     the VMM
     * @param coreCount
     *     the core count in the chip
     * @param allocatedMemory
     *     the allocated memory in
     *     <em>gigabytes (GB)</em>.
     * @param allocatedDisk
     *     the allocated disk in
     *     <em>gigabytes (GB)</em>.
     * @param operatingSystem
     *     the running operating system
     */
    public VirtualMachine (
        final String id, final String owner, final String VMM, final int coreCount,
        final double allocatedMemory, final double allocatedDisk, final String operatingSystem
    ) {
        this.name            = id;
        this.owner           = owner;
        this.VMM             = VMM;
        this.coreCount       = coreCount;
        this.allocatedMemory = allocatedMemory;
        this.allocatedDisk   = allocatedDisk;
        this.operatingSystem = operatingSystem;
    }

    /**
     * Returns the core count.
     *
     * @return the core count
     */
    public int getCoreCount () {
        return this.coreCount;
    }

    /**
     * Returns the allocated memory.
     *
     * @return the allocated memory
     */
    public double getAllocatedMemory () {
        return this.allocatedMemory;
    }

    /**
     * Returns the allocated disk.
     *
     * @return the allocated disk
     */
    public double getAllocatedDisk () {
        return this.allocatedDisk;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName () {
        return this.name;
    }

    /**
     * Returns the owner.
     *
     * @return the owner
     */
    public String getOwner () {
        return this.owner;
    }

    /**
     * Returns the running operating system.
     *
     * @return the running operating system
     */
    public String getOperatingSystem () {
        return this.operatingSystem;
    }

    /**
     * Returns the VMM.
     *
     * @return the VMM
     */
    public String getVMM () {
        return this.VMM;
    }
}

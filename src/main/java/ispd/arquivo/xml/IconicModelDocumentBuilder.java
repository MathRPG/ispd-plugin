package ispd.arquivo.xml;

import ispd.arquivo.xml.utils.*;
import ispd.gui.*;
import ispd.gui.iconico.grade.*;
import ispd.motor.workload.impl.*;
import java.util.*;
import java.util.stream.*;
import org.w3c.dom.Node;
import org.w3c.dom.*;

/**
 * Class responsible for manipulating xml files into iconic or simulable models, and building a
 * document with a model from an iconic one.
 */
public class IconicModelDocumentBuilder {

    private static final Element[] NO_CHILDREN = {};

    private static final Object[][] NO_ATTRS = {};

    private static final int DEFAULT_MODEL_TYPE = -1;

    private final WrappedDocument document = new WrappedDocument(ManipuladorXML.newDocument());

    private final Element system;

    private Element load = null;

    public IconicModelDocumentBuilder (final int modelType) {
        this.system = this.document.createElement("system");
        this.system.setAttribute("version", getVersionForModelType(modelType));
        this.document.appendChild(this.system);
    }

    /**
     * @throws IllegalArgumentException
     *     if modelType is not in -1, 0, 1 or 2
     */
    private static String getVersionForModelType (final int modelType) {
        return switch (modelType) {
            case PickModelTypeDialog.GRID -> "2.1";
            case PickModelTypeDialog.IAAS -> "2.2";
            case PickModelTypeDialog.PAAS -> "2.3";
            case DEFAULT_MODEL_TYPE -> "1.2";
            default -> throw new IllegalArgumentException("Invalid model type " + modelType);
        };
    }

    public void addPerNodeLoadCollection (final CollectionWorkloadGenerator collection) {
        for (final var load : collection.getList()) {
            this.addPerNodeLoad(load);
        }
    }

    public void addMachineIaas (final Machine m, final Collection<Integer> slaveIds) {
        final var id = m.getId();

        this.addMachineInner(
            m.getX(),
            m.getY(),
            id.getLocalId(),
            id.getGlobalId(),
            id.getName(),
            m.getComputationalPower(),
            m.getLoadFactor(),
            m.getSchedulingAlgorithm(),
            m.getOwner(),
            m.getCoreCount(),
            m.getRam(),
            m.getHardDisk(),
            m.getCostPerProcessing(),
            m.getCostPerMemory(),
            m.getCostPerDisk(),
            m.isMaster(),
            slaveIds,
            NO_ATTRS,
            new Object[][] { { "vm_alloc", m.getVmmAllocationPolicy() }, }
        );
    }

    public void addMachine (
        final Machine m,
        final List<Integer> slaves
    ) {
        this.addMachine(
            m.getX(),
            m.getY(),
            m.getId().getLocalId(),
            m.getId().getGlobalId(),
            m.getId().getName(),
            m.getComputationalPower(),
            m.getLoadFactor(),
            m.getSchedulingAlgorithm(),
            m.getOwner(),
            m.getCoreCount(),
            m.getRam(),
            m.getHardDisk(),
            m.isMaster(),
            slaves,
            m.getEnergyConsumption()
        );
    }

    private void addPerNodeLoad (final PerNodeWorkloadGenerator no) {
        this.addLoadNo(
            no.getApplication(),
            no.getOwner(),
            no.getSchedulerId(),
            no.getTaskCount(),
            no.getComputationMaximum(),
            no.getComputationMinimum(),
            no.getCommunicationMaximum(),
            no.getCommunicationMinimum()
        );
    }

    public void addTraceLoad (final TraceFileWorkloadGenerator trace) {
        this.addElementToLoad(this.anElement(
            "trace", new Object[][] {
                { "file_path", trace.getTraceFile().getPath() },
                { "tasks", trace.getTaskCount() },
                { "format", trace.getTraceType() },
            }
        ));
    }

    public void addGlobalWorkload (final GlobalWorkloadGenerator load) {
        this.addElementToLoad(this.anElement(
            "random", new Object[][] {
                { "tasks", load.getTaskCount() },
                { "time_arrival", load.getTaskCreationTime() },
            }, new Element[] {
                this.anElement("size", new Object[][] {
                    { "type", "computing" },
                    { "maximum", load.getComputationMaximum() },
                    { "average", load.getComputationAverage() },
                    { "minimum", load.getComputationMinimum() },
                    { "probability", load.getComputationProbability() },
                }),
                this.anElement("size", new Object[][] {
                    { "type", "communication" },
                    { "maximum", load.getCommunicationMaximum() },
                    { "average", load.getCommunicationAverage() },
                    { "minimum", load.getCommunicationMinimum() },
                    { "probability", load.getCommunicationProbability() },
                }),
            }
        ));
    }

    public void addVirtualMachine (final VirtualMachine virtualMachine) {
        final var id    = virtualMachine.getName();
        final var power = virtualMachine.getCoreCount();

        this.system.appendChild(this.anElement(
            "virtualMac", new Object[][] {
                { "id", id },
                { "owner", virtualMachine.getOwner() },
                { "vmm", virtualMachine.getVMM() },
                { "power", power },
                { "mem_alloc", virtualMachine.getAllocatedMemory() },
                { "disk_alloc", virtualMachine.getAllocatedDisk() },
                { "op_system", virtualMachine.getOperatingSystem() },
            }
        ));
    }

    public void addCluster (final Cluster cluster) {
        final var id = cluster.getId();

        this.system.appendChild(this.anElement(
            "cluster", new Object[][] {
                { "nodes", cluster.getSlaveCount() },
                { "power", cluster.getComputationalPower() },
                { "bandwidth", cluster.getBandwidth() },
                { "latency", cluster.getLatency() },
                { "scheduler", cluster.getSchedulingAlgorithm() },
                { "owner", cluster.getOwner() },
                { "master", cluster.isMaster() },
                { "id", id.getName() },
                { "energy", cluster.getEnergyConsumption() },
            }, new Node[] {
                this.aPositionElement(cluster.getX(), cluster.getY()),
                this.anIconIdElement(id.getGlobalId(), id.getLocalId()),
                this.aCharacteristic(
                    cluster.getComputationalPower(),
                    cluster.getCoreCount(),
                    cluster.getRam(),
                    cluster.getHardDisk(),
                    0.0,
                    0.0,
                    0.0
                ),
            }
        ));
    }

    public void addClusterIaas (final Cluster cluster) {
        final var id = cluster.getId();

        this.system.appendChild(this.anElement(
            "cluster", new Object[][] {
                { "id", id.getName() },
                { "nodes", cluster.getSlaveCount() },
                { "power", cluster.getComputationalPower() },
                { "bandwidth", cluster.getBandwidth() },
                { "latency", cluster.getLatency() },
                { "scheduler", cluster.getSchedulingAlgorithm() },
                { "vm_alloc", cluster.getVmmAllocationPolicy() },
                { "owner", cluster.getOwner() },
                { "master", cluster.isMaster() },
            }, new Node[] {
                this.aPositionElement(cluster.getX(), cluster.getY()),
                this.anIconIdElement(id.getGlobalId(), id.getLocalId()),
                this.aCharacteristic(
                    cluster.getComputationalPower(),
                    cluster.getCoreCount(),
                    cluster.getRam(),
                    cluster.getHardDisk(),
                    cluster.getCostPerProcessing(),
                    cluster.getCostPerMemory(),
                    cluster.getCostPerDisk()
                )
            }
        ));
    }

    public void addLink (final Link l) {
        final var source      = l.getSource();
        final var destination = l.getDestination();
        final var id          = l.getId();

        this.addLink(
            source.getX(),
            source.getY(),
            destination.getX(),
            destination.getY(),
            id.getLocalId(),
            id.getGlobalId(),
            id.getName(),
            l.getBandwidth(),
            l.getLoadFactor(),
            l.getLatency(),
            ((GridItem) source).getId().getGlobalId(),
            ((GridItem) destination).getId().getGlobalId()
        );
    }

    public void addInternet (final Internet i) {
        final var id = i.getId();

        this.addInternet(
            i.getX(),
            i.getY(),
            id.getLocalId(),
            id.getGlobalId(),
            id.getName(),
            i.getBandwidth(),
            i.getLoadFactor(),
            i.getLatency()
        );
    }

    /**
     * Add users' ids and power limit info to the current model being built.
     *
     * @param users
     *     collection of userIds
     * @param limits
     *     map with users' power limits
     *
     * @throws NullPointerException
     *     if a user id given in the collection is missing from the map of power limits
     */
    public void addUsers (final Collection<String> users, final Map<String, Double> limits) {
        users.stream()
            .map(user -> this.anElement(
                "owner", "id", user, "powerlimit", limits.get(user)
            ))
            .forEach(this.system::appendChild);
    }

    /**
     * Create an element with only two attributes, k1 and k2, with values v1 and v2, respectively.
     * See {@link #anElement(String, Object[][], Node[])} for further info.
     *
     * @return element with two attributes k1, k2 of value v1, v2
     */
    private Element anElement (
        final String name,
        final String k1,
        final Object v1,
        final String k2,
        final Object v2
    ) {
        return this.anElement(name, new Object[][] {
            { k1, v1 },
            { k2, v2 },
        });
    }

    /**
     * Create an element with no children. See {@link #anElement(String, Object[][], Node[])} for
     * further info.
     *
     * @return children-less element.
     */
    private Element anElement (final String name, final Object[][] attrs) {
        return this.anElement(name, attrs, NO_CHILDREN);
    }

    /**
     * Create an element in the {@link Document} being currently built. The element needs a name, a
     * map of attributes, and (optionally) children.
     *
     * @param name
     *     name of the element
     * @param attrs
     *     array of width 2 (key, value) with the attributes to be added to the element
     * @param children
     *     children to be appended to the built element
     *
     * @return Element with the given name, attributes and children
     */
    private Element anElement (final String name, final Object[][] attrs, final Node[] children) {
        final var e = this.document.createElement(name);

        for (final var attr : attrs) {
            final var key = attr[0];
            final var value = attr[1];
            e.setAttribute((String) key, value.toString());
        }

        Arrays.stream(children).forEach(e::appendChild);

        return e;
    }

    /**
     * Add internet icon with the given attributes to the current model being built.
     */
    public void addInternet (
        final int x,
        final int y,
        final int idLocal,
        final int idGlobal,
        final String name,
        final double bandwidth,
        final double internetLoad,
        final double latency
    ) {
        this.system.appendChild(this.anElement(
            "internet", new Object[][] {
                { "id", name },
                { "bandwidth", bandwidth },
                { "load", internetLoad },
                { "latency", latency },
            }, new Element[] {
                this.aPositionElement(x, y),
                this.anIconIdElement(idGlobal, idLocal),
            }
        ));
    }

    /**
     * Create an element with icon id information: those being the local and global ids.
     */
    private Element anIconIdElement (final int global, final int local) {
        return this.anElement("icon_id", "global", global, "local", local);
    }

    /**
     * Create an element with attributes describing the characteristics of a processing center, such
     * as computational power and cost, storage, etc.
     */
    private Node aCharacteristic (
        final Double power,
        final Integer coreCount,
        final Double memory,
        final Double disk,
        final Double processingCost,
        final Double memoryCost,
        final Double diskCost
    ) {
        return this.anElement("characteristic", NO_ATTRS, new Element[] {
            this.anElement("process", "power", power, "number", coreCount),
            this.anElement("memory", "size", memory),
            this.anElement("hard_disk", "size", disk),
            this.anElement("cost", new Object[][] {
                { "cost_proc", processingCost },
                { "cost_mem", memoryCost },
                { "cost_disk", diskCost },
            }),
        });
    }

    /**
     * Add a machine icon with the given attributes to the current model being built.
     */
    public void addMachine (
        final Integer x,
        final Integer y,
        final Integer localId,
        final Integer globalId,
        final String name,
        final Double power,
        final Double occupancy,
        final String scheduler,
        final String owner,
        final Integer coreCount,
        final Double memory,
        final Double disk,
        final boolean isMaster,
        final Collection<Integer> slaves,
        final Double energy
    ) {
        this.addMachineInner(
            x,
            y,
            localId,
            globalId,
            name,
            power,
            occupancy,
            scheduler,
            owner,
            coreCount,
            memory,
            disk,
            null,
            null,
            null,
            isMaster,
            slaves,
            new Object[][] { { "energy", energy } },
            NO_ATTRS
        );
    }

    /**
     * Create an element with attributes describing the characteristics of a processing center, but
     * without costs. See
     * {@link #aCharacteristic(Double, Integer, Double, Double, Double, Double, Double)} for further
     * info.
     */
    private Node aCharacteristic (
        final Double power, final Integer coreCount, final Double memory, final Double disk
    ) {
        return this.anElement("characteristic", NO_ATTRS, new Element[] {
            this.anElement("process", "power", power, "number", coreCount),
            this.anElement("memory", "size", memory),
            this.anElement("hard_disk", "size", disk),
        });
    }

    /**
     * Create a simple element with just a name and one attribute
     */
    private Element anElement (final String name, final String key, final Object value) {
        return this.anElement(name, new Object[][] { { key, value }, });
    }

    /**
     * Helper method to abstract away the addition of a machine element to the model being built. It
     * takes in all attributes in common between the methods
     * {@link #addMachine(Integer, Integer, Integer, Integer, String, Double, Double, String,
     * String, Integer, Double, Double, boolean, Collection, Double)}, but also two extra params,
     * {@code extraAttrs} and {@code extraMasterAttrs}, which are arrays containing the specific
     * attributes of each of the outer methods.
     *
     * @param isMaster
     *     indicates whether to include an inner 'master' element
     * @param extraAttrs
     *     extra attributes to be added ot the element
     * @param extraMasterAttrs
     *     extra attributes to be added ot the inner 'master' element, if the element is a master
     */
    private void addMachineInner (
        final Integer x,
        final Integer y,
        final Integer localId,
        final Integer globalId,
        final String name,
        final Double power,
        final Double occupancy,
        final String scheduler,
        final String owner,
        final Integer coreCount,
        final Double memory,
        final Double disk,
        final Double costPerProcessing,
        final Double costPerMemory,
        final Double costPerDisk,
        final boolean isMaster,
        final Collection<Integer> slaves,
        final Object[][] extraAttrs,
        final Object[][] extraMasterAttrs
    ) {
        // Note: Arrays.asList returns a fixed-size list, which throws on .add()
        final var attrList = Arrays.stream(new Object[][] {
            { "id", name },
            { "power", power },
            { "load", occupancy },
            { "owner", owner },
        }).collect(Collectors.toList());

        attrList.addAll(Arrays.asList(extraAttrs));

        final Node characteristic;

        if (costPerProcessing != null) {
            characteristic = this.aCharacteristic(
                power, coreCount, memory, disk, costPerProcessing, costPerMemory, costPerDisk
            );
        } else {
            characteristic = this.aCharacteristic(power, coreCount, memory, disk);
        }

        final var machine =
            this.anElement("machine", attrList.toArray(Object[][]::new), new Node[] {
                this.aPositionElement(x, y),
                this.anIconIdElement(globalId, localId),
                characteristic,
            });

        if (isMaster) {
            machine.appendChild(this.aMasterElement(scheduler, slaves, extraMasterAttrs));
        }

        this.system.appendChild(machine);
    }

    /**
     * Create a master element with given scheduling policy, and slaves as children.
     *
     * @param extraAttrs
     *     potential extra attributes to add to the element
     */
    private Element aMasterElement (
        final String scheduler, final Collection<Integer> slaves, final Object[][] extraAttrs
    ) {
        // Note: Arrays.asList returns a fixed-size list, which throws on .add()
        final var attrList = Arrays.stream(new Object[][] {
            { "scheduler", scheduler },
        }).collect(Collectors.toList());

        attrList.addAll(Arrays.asList(extraAttrs));

        return this.anElement(
            "master", attrList.toArray(Object[][]::new),
            slaves.stream()
                .map(this::aSlaveElement)
                .toArray(Element[]::new)
        );
    }

    /**
     * Simple slave element with given id
     */
    private Element aSlaveElement (final Integer id) {
        return this.anElement("slave", "id", id);
    }

    /**
     * Add a link icon with the given attributes to the current model being built.
     */
    public void addLink (
        final int x0,
        final int y0,
        final int x1,
        final int y1,
        final int localId,
        final int globalId,
        final String name,
        final double bandwidth,
        final double linkLoad,
        final double latency,
        final int origination,
        final int destination
    ) {
        this.system.appendChild(this.anElement(
            "link", new Object[][] {
                { "id", name },
                { "bandwidth", bandwidth },
                { "load", linkLoad },
                { "latency", latency },
            }, new Element[] {
                this.anElement("connect", "origination", origination, "destination", destination),
                this.aPositionElement(x0, y0),
                this.aPositionElement(x1, y1),
                this.anIconIdElement(globalId, localId),
            }
        ));
    }

    /**
     * Create a position element with position information (x, y)
     */
    private Element aPositionElement (final int x, final int y) {
        return this.anElement("position", "x", x, "y", y);
    }

    private void addElementToLoad (final Node elem) {
        this.createLoadIfNull();
        this.load.appendChild(elem);
    }

    private void createLoadIfNull () {
        if (this.load == null) {
            this.load = this.document.createElement("load");
            this.system.appendChild(this.load);
        }
    }

    /**
     * Add a per-node load to the current model being built.
     *
     * @apiNote This method may be called more than once per instance.
     */
    public void addLoadNo (
        final String application,
        final String owner,
        final String masterId,
        final Integer taskCount,
        final Double maxComp,
        final Double minComp,
        final Double maxComm,
        final Double minComm
    ) {
        this.addElementToLoad(this.anElement(
            "node", new Object[][] {
                { "application", application },
                { "owner", owner },
                { "id_master", masterId },
                { "tasks", taskCount },
            }, new Element[] {
                this.anElement("size", new Object[][] {
                    { "type", "computing" },
                    { "maximum", maxComp },
                    { "minimum", minComp },
                }),
                this.anElement("size", new Object[][] {
                    { "type", "communication" },
                    { "maximum", maxComm },
                    { "minimum", minComm },
                }),
            }
        ));
    }

    /**
     * Get {@link Document} with iconic model generated.
     */
    public Document finishDocument () {
        return this.document.getWrappedDocument();
    }
}

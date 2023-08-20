package ispd.arquivo.xml;

import ispd.arquivo.xml.models.builders.*;
import ispd.arquivo.xml.utils.*;
import ispd.motor.workload.*;
import ispd.motor.workload.impl.*;
import org.w3c.dom.*;

public enum WorkloadGeneratorFactory {
    ;

    /**
     * Get load configuration containing in the iconic model present in the {@link Document}
     *
     * @return {@link WorkloadGenerator} with load configuration from the model, if a valid one is
     * present, {@code null} otherwise
     *
     * @see LoadBuilder
     * @see TraceFileWorkloadGenerator
     * @see CollectionWorkloadGenerator
     * @see GlobalWorkloadGenerator
     */
    public static WorkloadGenerator fromDocument (final Document document) {
        return LoadBuilder.build(new WrappedDocument(document)).orElse(null);
    }
}

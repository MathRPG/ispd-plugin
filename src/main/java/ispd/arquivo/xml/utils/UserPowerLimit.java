package ispd.arquivo.xml.utils;

import ispd.motor.metrics.*;
import ispd.policy.scheduling.*;
import java.util.*;

/**
 * Utility class to process users' power limits, creating user metrics based on these values and
 * giving such information to schedulers.
 */
public class UserPowerLimit {

    private final List<String> owners;

    private final List<Double> limits;

    public UserPowerLimit (final Map<String, Double> powerLimits) {
        // Note: Constructing from powerLimits.keySet() and .values() is
        // tempting, however those methods do NOT guarantee the same relative
        // order in the returned elements

        this.owners = new ArrayList<>(powerLimits.size());
        this.limits = new ArrayList<>(powerLimits.size());

        for (final var entry : powerLimits.entrySet()) {
            this.owners.add(entry.getKey());
            this.limits.add(entry.getValue());
        }
    }

    /**
     * Sets the scheduler's {@link User} with an instance created internally, based on
     * the user power limits stored internally.
     *
     * @param scheduler
     *     scheduler to be set with new user metrics.
     */
    public void setSchedulerUserMetrics (final SchedulingPolicy<?> scheduler) {
        scheduler.setMetricaUsuarios(this.makeUserMetrics());
    }

    /**
     * @return {@link User} based on the values acquired.
     */
    private User makeUserMetrics () {
        final var metrics = new User();
        metrics.addAllUsuarios(this.owners, this.limits);
        return metrics;
    }

    /**
     * @return a reference to the list of owners (users).
     */
    public List<String> getOwners () {
        return this.owners;
    }
}

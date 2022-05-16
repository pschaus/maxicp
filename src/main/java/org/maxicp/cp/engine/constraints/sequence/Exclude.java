package org.maxicp.cp.engine.constraints.sequence;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPSequenceVar;

/**
 * Exclude a node from a {@link CPSequenceVar}
 */
public class Exclude extends AbstractCPConstraint {

    private final int[] excluded;
    private final CPSequenceVar sequenceVar;

    /**
     * exclusion constraint. Ensure that a node / set of nodes must belong to the set of excluded nodes
     * @param sequenceVar: sequence var to work on
     * @param nodes : node / set of nodes to exclude
     */
    public Exclude(CPSequenceVar sequenceVar, int... nodes) {
        super(sequenceVar.getSolver());
        excluded = nodes;
        this.sequenceVar = sequenceVar;
    }

    @Override
    public void post() {
        for (int node: excluded) {
            sequenceVar.exclude(node);
        }
    }
}

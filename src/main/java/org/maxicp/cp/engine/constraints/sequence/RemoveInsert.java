package org.maxicp.cp.engine.constraints.sequence;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPSequenceVar;

/**
 * remove a pair of predecessor and node from a {@link CPSequenceVar}
 */
public class RemoveInsert extends AbstractCPConstraint {

    private final CPSequenceVar sequenceVar;
    private final int pred;
    private final int node;

    public RemoveInsert(CPSequenceVar sequenceVar, int pred, int node) {
        super(sequenceVar.getSolver());
        this.sequenceVar = sequenceVar;
        this.pred = pred;
        this.node = node;
    }

    @Override
    public void post() {
        sequenceVar.removeInsertion(pred, node);
    }
}

package org.maxicp.cp.engine.constraints.sequence;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPSequenceVar;

/**
 * Remove the insertion having the specified node as predecessor
 */
public class NoEdgeFrom extends AbstractCPConstraint {

    private final int node;
    private final CPSequenceVar seq;

    public NoEdgeFrom(CPSequenceVar seq, int invalidPredecessor) {
        super(seq.getSolver());
        this.seq = seq;
        this.node = invalidPredecessor;
    }

    @Override
    public void post() {
        seq.removeInsertionAfter(node);
        setActive(false);
    }
}
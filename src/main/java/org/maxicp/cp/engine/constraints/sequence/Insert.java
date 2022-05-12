package org.maxicp.cp.engine.constraints.sequence;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPSequenceVar;

public class Insert extends AbstractCPConstraint {

    private CPSequenceVar sequenceVar;
    private int node;
    private int predecessor;

    public Insert(CPSequenceVar sequenceVar, int pred, int node) {
        super(sequenceVar.getSolver());
        this.sequenceVar = sequenceVar;
        this.node = node;
        this.predecessor = pred;
    }

    @Override
    public void post() {
        sequenceVar.insert(predecessor, node);
    }

}

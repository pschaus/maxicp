package org.maxicp.cp.engine.constraints.sequence;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.state.StateInt;

import java.util.HashSet;

import static org.maxicp.util.exception.InconsistencyException.INCONSISTENCY;

public class Disjoint extends AbstractCPConstraint {

    private final CPSequenceVar[] sequenceArray;
    private final boolean mustAppear;

    /**
     * disjoint constraint. Ensures that the same node id must be scheduled once and only once across all sequences
     * assumes that the same solver is used for all sequences
     * a node can be excluded from all sequences
     * @param sequenceArray array of CPSequenceVar to post the constraint on
     */
    public Disjoint(CPSequenceVar... sequenceArray) {
        this(true, sequenceArray);
    }

    /**
     * disjoint constraint. Ensures that the same node id must be scheduled once and only once across all sequences
     * assumes that the same solver is used for all sequences
     * @param sequenceArray array of CPSequenceVar to post the constraint on
     * @param mustAppear if true, each unique node must be scheduled in one sequence
     */
    public Disjoint(boolean mustAppear, CPSequenceVar... sequenceArray) {
        super(sequenceArray[0].getSolver());
        this.sequenceArray = sequenceArray;
        this.mustAppear = mustAppear;
    }

    @Override
    public void post() {

        if (mustAppear && sequenceArray.length == 1) { // all nodes must be visited and only one sequence is in the set
            if (sequenceArray[0].nExcluded() > 0) // no node can be excluded
                throw INCONSISTENCY;
            sequenceArray[0].whenExclude(() -> {
                throw INCONSISTENCY;
            });
            return;
        }

        // find the maximum number of nodes for the sequences
        int maxRequests = Integer.MIN_VALUE;
        int nbRequests;
        for (CPSequenceVar seq: sequenceArray) {
            nbRequests = seq.nNode(false);
            if (nbRequests > maxRequests)
                maxRequests = nbRequests;
        }

        HashSet<Integer> nodeSet = new HashSet<>();
        int[] insertions = new int[maxRequests];
        for (CPSequenceVar seq: sequenceArray) {
            int size = seq.fillPossible(insertions);
            for (int i=0; i < size; ++i) {
                int node = insertions[i];
                if (!nodeSet.contains(node)) {
                    nodeSet.add(node);
                    // handle the actions related to one node
                    HandleOneNode handle = new HandleOneNode(node);
                    getSolver().post(handle);
                }
            }
        }
    }

    /**
     * handle insertions / exclusion of one particular node across all sequences
     */
    private class HandleOneNode extends AbstractCPConstraint {

        private final int node;

        public HandleOneNode(int node) {
            super(Disjoint.this.getSolver());
            this.node = node;
        }

        @Override
        public void post() {
            propagate(); // check if the node has been scheduled once
            if (isActive()) { // the node is not yet scheduled
                if (mustAppear) {
                    // prevent exclusions from all sequences
                    HandleExclusions handleExclusions = new HandleExclusions();
                    handleExclusions.post();
                }
                for (CPSequenceVar seq: sequenceArray) {
                    seq.getInsertionVar(node).propagateOnInsert(this);
                }
            }
        }

        // triggered whenever a node has been scheduled
        @Override
        public void propagate() {
            // check that the node does not appear twice in the sequences
            CPSequenceVar foundScheduled = null;
            for (CPSequenceVar seq: sequenceArray) {
                if (seq.isMember(node)) {
                    if (foundScheduled != null) // the node was scheduled in two sequences
                        throw INCONSISTENCY;
                    foundScheduled = seq; // assign to the sequence where the node has been scheduled
                }
            }
            if (foundScheduled != null) {  // exclude the node from all related sequences and deactivate
                setActive(false);
                for (CPSequenceVar seq : sequenceArray) {
                    if (seq != foundScheduled)
                        seq.exclude(node);
                }
            }
        }

        /**
         * handle exclusions of a node across all sequences
         * only created and used when {@link Disjoint#mustAppear} is set to true
         */
        private class HandleExclusions extends AbstractCPConstraint {

            private StateInt nExcluded;

            public HandleExclusions() {
                super(HandleOneNode.this.getSolver());
            }

            @Override
            public boolean isActive() {
                return HandleOneNode.this.isActive(); // no need to propagate if the node has already been inserted
            }

            @Override
            public void post() {
                int cnt = 0;
                for (CPSequenceVar seq: sequenceArray) {
                    if (seq.isExcluded(node))
                        cnt += 1;
                    else
                        seq.getInsertionVar(node).propagateOnExclude(this);
                }
                if (cnt == sequenceArray.length) // the node is excluded from all sequences
                    throw INCONSISTENCY;
                nExcluded = getSolver().getStateManager().makeStateInt(cnt);
            }

            // triggered whenever a node has been excluded
            @Override
            public void propagate() {
                if (nExcluded.increment() == sequenceArray.length) {
                    // no sequence can host the node anymore
                    throw INCONSISTENCY;
                }
            }
        }
    }
    
}

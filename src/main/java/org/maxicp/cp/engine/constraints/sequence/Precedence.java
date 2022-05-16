package org.maxicp.cp.engine.constraints.sequence;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.state.StateInt;

import java.util.HashMap;
import java.util.Map;

import static org.maxicp.util.exception.InconsistencyException.INCONSISTENCY;

/**
 * The specified order of nodes must be respected within the {@link CPSequenceVar}
 */
public class Precedence extends AbstractCPConstraint {

    private CPSequenceVar seq;
    private int[] order;        // order that must appear within the sequence
    private int[] insertions;
    private boolean mustAppear;
    private Map<Integer, Integer> seqPosition;
    private Map<Integer, Integer> orderMap; // contains values for {node -> order of appearance} relative to order
    private int begin;
    private int end;

    private StateInt orderInserted; // 1 if a node in order is inserted, 0 otherwise

    private boolean masterConstraintPropagating = false;

    /**
     * ensures that a particular order of nodes appears within the sequence or is absent from the sequence
     * @param seq sequence concerned
     * @param order order that must appear in the sequence. Contains the ids of the insertions vars / nodes
     */
    public Precedence(CPSequenceVar seq, int... order) {
        this(seq, false, order);
    }

    /**
     * ensures that a particular order of nodes appears within the sequence
     * @param seq sequence concerned
     * @param order order that must appear in the sequence. Contains the ids of the insertions vars / nodes
     * @param mustAppear if false, exclude the whole order if one of the node is absent. if true, ensure that all nodes must be present
     */
    public Precedence(CPSequenceVar seq, boolean mustAppear, int... order) {
        super(seq.getSolver());
        this.seq = seq;
        this.mustAppear = mustAppear;
        this.order = order;
        seqPosition = new HashMap<>();
        insertions = new int[seq.nNode()];
        orderMap = new HashMap<>();
        for (int i = 0 ; i < order.length ; ++i) {
            orderMap.put(order[i], i);
        }
        begin = seq.begin();
        end = seq.end();
        orderInserted = getSolver().getStateManager().makeStateInt(0);
    }

    @Override
    public void post() {
        for (int node: order) {
            if (mustAppear) { // nodes must all appear in the sequence
                if (seq.isExcluded(node)) // cannot exclude
                    throw INCONSISTENCY;
                seq.getInsertionVar(node).whenExclude(() -> {
                    throw INCONSISTENCY;});
            } else { // nodes must not always appear in the sequence
                if (seq.isExcluded(node)) { // a node is excluded, exclude the remaining one
                    for (int exclude: order) {
                        if (seq.isMember(exclude))
                            throw INCONSISTENCY; // a node must be excluded but is already scheduled
                        else
                            seq.exclude(exclude);
                    }
                } else { // the node is not excluded, propagate when it is excluded
                    seq.getInsertionVar(node).propagateOnExclude(this);
                }
            }
        }
        // some insertions are already invalid simply because of the order asked. Prune them once and for all
        for (int i = 0; i < order.length ; ++i)
            for (int j = i+1; j < order.length; ++j)
                seq.removeInsertion(order[j], order[i]); // order[i] cannot have order[i+1...n] as predecessor
        propagate();
        if (isActive()) {
            //seq.propagateOnInsert(this);
            int size = seq.fillPossible(insertions);
            for (int i = 0 ; i < size ; ++i) {
                int node = insertions[i];
                if (orderMap.containsKey(node)) { // inserting a node in order triggers the full propagation
                    seq.getInsertionVar(node).propagateOnInsert(this);
                } else { // inserting another node triggers a lighter propagation
                    seq.getInsertionVar(node).propagateOnInsert(new PrecedenceFromNodeNotInOrder(node));
                }
            }
        }

    }

    @Override
    public void propagate() {
        //check that the elements of order present in sequence respect the precedences
        masterConstraintPropagating = true;
        int begin = seq.begin();
        int current = begin;
        int end = seq.end();
        int nNodes = seq.nNode();
        int nInserted = 0;
        for (int node: order) {
            if (seq.isMember(node)) {
                orderInserted.setValue(1);
                if (++nInserted == order.length) { // all nodes have been inserted
                    setActive(false);
                    return;
                }
                while (current != node && current != end) { // attempt to find the node in the sequence
                    current = seq.nextMember(current);
                }
                if (current == end && node != end)
                    throw INCONSISTENCY; // the end node has been reached without finalizing the sequence
            } else if (seq.isExcluded(node)) { // a node has been excluded, exclude the remaining ones
                for (int exclude: order) {
                    if (seq.isMember(exclude))
                        throw INCONSISTENCY; // a node must be excluded but is already scheduled
                    else
                        seq.exclude(exclude);
                }
                setActive(false);
                return;
            }
        }
        if (nInserted == 0) // no node has been inserted, exit the function
            return;
        if (nInserted == order.length) { // all nodes have been inserted
            setActive(false);
            return;
        }
        // create map of {nodes -> positions}
        seqPosition.clear();
        current = begin;
        int i;
        for (i = 0; current != end ; ++i) {
            seqPosition.put(current, i);
            current = seq.nextMember(current);
        }
        seqPosition.put(end, i+1);
        // filter scheduled insertions that are invalid
        // remove the insertions of order[i+1] based on order[i]
        int predPos = -1;
        for (int node: order) {
            if (seq.isMember(node)) {
                predPos = seqPosition.get(node); // register the position for the previous found node in order
            } else {
                int size = seq.fillMemberInsertion(node, insertions); // retrieve the insertions
                for (i = 0 ; i < size ; ++i) {
                    if (seqPosition.get(insertions[i]) < predPos) // if the insert is before the pred node in order, remove it
                        seq.removeInsertion(insertions[i], node);
                }
            }
        }

        // remove the insertions of order[i] based on order[i+1]
        int nextPos = seq.nMember() + 10;
        for (int j = order.length-1 ; j >= 0 ; --j) {
            int node = order[j];
            if (seq.isMember(node)) {
                nextPos = seqPosition.get(node); // register the position for the next found node in order
            } else {
                int size = seq.fillMemberInsertion(node, insertions); // retrieve the insertions
                for (i = 0 ; i < size ; ++i) {
                    if (seqPosition.get(insertions[i]) >= nextPos) // if the insert is before the pred node in order, remove it
                        seq.removeInsertion(insertions[i], node);
                }
            }
        }
        masterConstraintPropagating = false;
    }

    /**
     * handle insertions of nodes that are not in the order array
     */
    private class PrecedenceFromNodeNotInOrder extends AbstractCPConstraint {

        int id;

        public PrecedenceFromNodeNotInOrder(int id) {
            super(seq.getSolver());
            this.id = id;
        }

        @Override
        public boolean isActive() {
            return Precedence.this.isActive() && !Precedence.this.isScheduled() && orderInserted.value() != 0 && !masterConstraintPropagating;
        }

        /**
         * should only be called on insert
         * remove this node from the set of predecessors for nodes in order if it becomes invalid
         */
        @Override
        public void propagate() {
            //if (orderInserted.value() == 0) {
            //    //setActive(false);
            //    return;
            //}
            int closestIdOrderBefore = -1;
            int closestIdOrderAfter = -1;
            int pred = seq.predMember(id);
            int succ = seq.nextMember(id);

            // find closest node in order array
            while (pred != begin || succ != end) {
                if (orderMap.containsKey(pred)) {
                    closestIdOrderBefore = orderMap.get(pred);
                    break;
                } else if (pred != begin) {
                    pred = seq.predMember(pred);
                }

                if (orderMap.containsKey(succ)) {
                    closestIdOrderAfter = orderMap.get(succ);
                    break;
                } else if (succ != end) {
                    succ = seq.predMember(succ);
                }
            }

            int orderIdBefore;
            int orderIdAfter;
            if (closestIdOrderAfter != -1) { // the closest node is after this node
                orderIdBefore = closestIdOrderAfter - 1;
                orderIdAfter = closestIdOrderAfter;
            } else if (closestIdOrderBefore != -1) { // the closest node is before this node
                orderIdBefore = closestIdOrderBefore;  // -1 as closestIdOrderBefore is scheduled
                orderIdAfter = closestIdOrderBefore + 1;
            } else {
                return;
            }
            // node is inserted between order[orderIdBefore] and order[orderIdAfter]

            // remove insertion for order[0..orderIdBefore]
            boolean foundScheduled = false;
            for (int i = orderIdBefore; i >= 0 ; --i) {
                int node = order[i];
                if (seq.isMember(node))
                    foundScheduled = true;
                else if (seq.isPossible(node) && foundScheduled)
                    seq.removeInsertion(id, node);
            }
            // remove insertion for order[orderIdAfter..n]
            foundScheduled = false;
            for (int i = orderIdAfter; i < order.length; ++i) {
                int node = order[i];
                if (seq.isMember(node))
                    foundScheduled = true;
                else if (seq.isPossible(node) && foundScheduled)
                    seq.removeInsertion(id, node);
            }
            //setActive(false); // never called anymore
        }

    }
}

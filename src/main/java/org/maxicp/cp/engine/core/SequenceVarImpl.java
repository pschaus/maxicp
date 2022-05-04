package org.maxicp.cp.engine.core;

import org.maxicp.state.*;
import org.maxicp.state.datastructures.StateSequenceSet;
import org.maxicp.state.datastructures.StateStack;
import org.maxicp.util.Procedure;

import static org.maxicp.util.exception.InconsistencyException.INCONSISTENCY;

/**
 * a sequence var, where each node is represented by an integer and each insertion is an InsertionVar
 * all nodes are split into 3 categories: scheduled (in the sequence), possible (could be in the sequence) and excluded (cannot be in the sequence)
 * WARNING: the ids for the begin node and the end node MUST be >= number of nodes considered
 *
 * constraints remove insertions points from the InsertionVars contained within the sequence
 * if an InsertionVar cannot be inserted into the sequence, it is set as excluded
 */
public class SequenceVarImpl implements SequenceVar {

    private final CPSolver cp;
    private final int nNodes;                   // number of nodes available (omitting begin and end)
    private final int maxIndex;                 // max index used for the node (max between begin and end)
    private final int nOmitted;                // number of unused indexes in the representation for the domain
    private InsertionVarInSequence[] insertionVars;
    private StateInt[] succ;                    // successors of the nodes
    private StateInt[] pred;                    // predecessors of the nodes
    private StateSequenceSet domain;            // domain for the set of Scheduled, Possible and Excluded variables

    // TODO constructor from a set of specified edges
    // TODO checker for clusters of possibles nodes

    // constraints registered for this sequence
    private StateStack<CPConstraint> onInsert;    // a node has been inserted into the sequence
    private StateStack<CPConstraint> onBind;      // all nodes are scheduled or excluded: no possible node remain
    private StateStack<CPConstraint> onExclude;   // a node has been excluded from the sequence
    private final int begin;                    // beginning of the sequence
    private final int end;                      // end of the sequence

    private final int[] values;                 // used by fill methods

    /**
     * create a Sequence Variable, representing a path from begin until end in a complete graph
     * @param cp solver related to the variable
     * @param nNodes number of nodes in the graph
     * @param begin first node of the path
     * @param end last node of the path
     */
    public SequenceVarImpl(CPSolver cp, int nNodes, int begin, int end) {
        //assert (begin >= nNodes && end >= nNodes);
        this.cp = cp;
        this.nNodes = nNodes;
        this.maxIndex = Math.max(nNodes, Math.max(begin, end)+1);
        // number of unused nodes in the representation of the domain
        // belong to {nNodes...maxIndex} \ {begin, end}
        //this.nOmitted = maxIndex - nNodes - 2;
        this.nOmitted = maxIndex - nNodes - (begin >= nNodes ? 1 : 0) - (end >= nNodes ? 1 : 0);
        this.begin = begin;
        this.end = end;
        insertionVars = new InsertionVarInSequence[nNodes];
        succ = new StateInt[maxIndex];
        pred = new StateInt[maxIndex];
        for (int i=0; i < nNodes; ++i) {
            if (i == begin | i == end)
                continue;
            insertionVars[i] = new InsertionVarInSequence(i);
            succ[i] = cp.getStateManager().makeStateInt(i);
            pred[i] = cp.getStateManager().makeStateInt(i);
        }
        succ[begin] = cp.getStateManager().makeStateInt(end); // the sequence is a closed loop at the beginning
        succ[end] = cp.getStateManager().makeStateInt(begin);
        pred[begin] = cp.getStateManager().makeStateInt(end);
        pred[end] = cp.getStateManager().makeStateInt(begin);

        domain = new StateSequenceSet(cp.getStateManager(), maxIndex);
        for (int i=nNodes; i < maxIndex; ++i) {
            if (i != begin && i != end)
                domain.exclude(i);
        }
        domain.require(begin); // the beginning and ending nodes are always in the domain
        domain.require(end);
        onInsert = new StateStack<>(cp.getStateManager());
        onBind = new StateStack<>(cp.getStateManager());
        onExclude = new StateStack<>(cp.getStateManager());
        values = new int[nNodes];
    }

    /**
     * listener for the whole sequence. For more information about the changes (i.e. what insertion has occurred?),
     * use the listener within the insertionVars
     */
     private SequenceListener seqListener = new SequenceListener() {
        @Override
        public void bind() {
            scheduleAll(onBind);
        }

        @Override
        public void insert() {
            scheduleAll(onInsert);
        }

        @Override
        public void exclude() { scheduleAll(onExclude); }

    };

    /**
     * InsertionVar: represents a node in the sequence, its status and its predecessors
     */
    public class InsertionVarInSequence implements InsertionVar {

        // sparse set
        private StateInt nbPossible;  // number of possible insertions. Each value is included within the possible set of the sequence
        private StateInt nbScheduled; // number of scheduled insertions. Each value is included within the scheduled set of the sequence
        private int[] values;
        private int[] indexes;
        private int n;
        private int id;
        // constraints registered for this sequence
        private StateStack<CPConstraint> onInsert;
        private StateStack<CPConstraint> onDomain;
        private StateStack<CPConstraint> onExclude;
        private boolean seen = false; // used by canPrecede(), set to true if the insertion is seen during the recursive call

        private InsertionListener listener = new InsertionListener() {
            @Override
            public void insert() {
                scheduleAll(onInsert);
            }

            @Override
            public void exclude() {
                scheduleAll(onExclude);
            }

            @Override
            public void change() { scheduleAll(onDomain); }

        };

        public InsertionVarInSequence(int id) {
            // all insertions are valid at first
            // no insertion belongs to the set of scheduled insertions at first
            this.id = id;
            n = Math.max(begin+1, nNodes);
            nbPossible = cp.getStateManager().makeStateInt(n); // consider all nodes as possible
            nbScheduled = cp.getStateManager().makeStateInt(0);
            onDomain = new StateStack<>(cp.getStateManager());
            onInsert = new StateStack<>(cp.getStateManager());
            onExclude = new StateStack<>(cp.getStateManager());
            // valid insertions at first: {0...id-1...id+1...nNodes-1} + {begin}
            values = new int[n];
            indexes = new int[n];
            for (int i = 0; i < n; i++) {
                values[i] = i;
                indexes[i] = i;
            }
            for (int i = nNodes; i < begin; ++i) {
                remove(i); // remove nodes in nNodes...begin
                nbPossible.decrement(); // in order to have a correct removal operation afterwards
            }
            remove(id); // a node cannot have itself as predecessor
            nbPossible.decrement();

            // the begin node is always a scheduled predecessor at first
            nbScheduled.setValue(1);
            nbPossible.decrement();
            // if the end node is in the set, remove it
            if (end < nNodes) {
                if (remove(end))
                    nbPossible.decrement();
            }
            nbScheduled.setValue(1);        // the beginning node is always a valid predecessor at first
        }

        @Override
        public CPSolver getSolver() {
            return cp;
        }

        @Override
        public boolean isBound() {
            return !SequenceVarImpl.this.isPossible(node());
        }

        @Override
        public void removeInsert(int i) {
            SequenceVarImpl.this.removeInsertion(i, id);
        }

        private boolean checkVal(int val) {
            assert (val <= values.length - 1);
            return true;
        }

        private void exchangePositions(int val1, int val2) {
            assert (checkVal(val1));
            assert (checkVal(val2));
            int v1 = val1;
            int v2 = val2;
            int i1 = indexes[v1];
            int i2 = indexes[v2];
            values[i1] = v2;
            values[i2] = v1;
            indexes[v1] = i2;
            indexes[v2] = i1;
        }

        /**
         * remove an insertion point from the set of possible insertions
         * does not interact with nbScheduled nor nbPossible, and should only be called by the SequenceVarImpl
         * @param val insertion point to remove
         */
        private boolean remove(int val) {
            if (!contains(val))
                return false; //the setValue has already been removed
            assert (checkVal(val));
            int s = size();
            exchangePositions(val, values[s - 1]);
            return true;
        }

        /**
         * remove all insertions points except the one specified
         * does not schedule into the sequence, and should only be called by the SequenceVarImpl
         * @param v insertion point to remove
         */
        private void removeAllBut(int v, boolean isScheduled) {
            assert (contains(v));
            int val = values[0];
            int index = indexes[v];
            indexes[v] = 0;
            values[0] = v;
            indexes[val] = index;
            values[index] = val;
            if (isScheduled) {
                nbScheduled.setValue(1);
                nbPossible.setValue(0);
            } else {
                nbScheduled.setValue(0);
                nbPossible.setValue(1);
            }
        }

        /**
         * remove all insertions points from the set of insertions
         * does not exclude / schedule into the sequence, and should only be called by the SequenceVarImpl
         */
        private void removeAll() {
            nbPossible.setValue(0);
            nbScheduled.setValue(0);
        }

        @Override
        public void removeAllInsert() {
            SequenceVarImpl.this.exclude(id);
        }

        @Override
        public void removeAllInsertBut(int i) {
            if (isScheduled(i))  // equivalent to the scheduling of the variable
                schedule(i, id);
            else if (isPossible(i)) {
                removeAllBut(i, false);
            } else
                throw INCONSISTENCY; // trying to assign an invalid insertion point
        }

        @Override
        public boolean contains(int i) {
            if (i < 0 || i >= n)
                return false;
            else
                return indexes[i] < size();
        }

        @Override
        public int node() {
            return id;
        }

        @Override
        public int fillInsertions(int[] dest) {
            int s = size();
            if (s >= 0) System.arraycopy(values, 0, dest, 0, s);
            return s;
        }

        @Override
        public int size() {
            return nbPossible.value() + nbScheduled.value();
        }

        @Override
        public void whenInsert(Procedure f) {
            onInsert.push(constraintClosure(f));
        }

        @Override
        public void propagateOnInsert(CPConstraint c) {
            onInsert.push(c);
        }

        @Override
        public void whenDomainChange(Procedure f) {
            onDomain.push(constraintClosure(f));
        }

        @Override
        public void propagateOnDomainChange(CPConstraint c) {
            onDomain.push(c);
        }

        @Override
        public void whenExclude(Procedure f) {
            onExclude.push(constraintClosure(f));
        }

        @Override
        public void propagateOnExclude(CPConstraint c) {
            onExclude.push(c);
        }

        @Override
        public void whenBind(Procedure f) {
            onExclude.push(constraintClosure(f));
            onInsert.push(constraintClosure(f));
        }

        @Override
        public void propagateOnBind(CPConstraint c) {
            onInsert.push(c);
            onExclude.push(c);
        }

        public int nbScheduled() {return nbScheduled.value();}

        public int nbPossible() {return nbPossible.value();}

        @Override
        public String toString() {
            StringBuilder scheduled = new StringBuilder("S: {");
            StringBuilder possible = new StringBuilder(", P: {");
            StringBuilder excluded = new StringBuilder(", E: {");
            for (int i = 0 ; i < values.length ; ++i) {
                if (contains(values[i])) {
                    if (isScheduled(values[i])) {
                        scheduled.append(values[i]);
                        scheduled.append(',');
                    } else if (isPossible(values[i])) {
                        possible.append(values[i]);
                        possible.append(',');
                    }
                } else {
                    excluded.append(values[i]);
                    excluded.append(',');
                }
            }
            return scheduled.append('}').toString() + possible.append('}').toString() + excluded.append('}').toString();
        }
    }

    @Override
    public CPSolver getSolver() {
        return cp;
    }

    @Override
    public int begin() {
        return begin;
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public boolean isBound() {
        return domain.nPossible() == 0;
    }

    @Override
    public int nextMember(int node) {
        return succ[node].value();
    }

    @Override
    public int predMember(int node) {
        return pred[node].value();
    }

    @Override
    public int fillOrder(int[] dest) {
        return fillOrder(dest, true);
    }

    @Override
    public int fillOrder(int[] dest, boolean includeBounds) {
        dest[0] = includeBounds ? begin : succ[begin].value();
        int lastElem = includeBounds ? end : pred[end].value();
        int i = 1;
        for (;dest[i-1] != lastElem; ++i)
            dest[i] = succ[dest[i-1]].value();
        return i;
    }

    @Override
    public int nScheduledNode() {
        return nScheduledNode(false);
    }

    @Override
    public int nScheduledNode(boolean includeBounds) {
        if (includeBounds)
            return domain.nRequired();
        else
            return domain.nRequired() - 2;
    }

    @Override
    public int nPossibleNode() {
        return domain.nPossible();
    }

    @Override
    public int nExcludedNode() {
        return domain.nExcluded() - nOmitted;
    }

    @Override
    public int nNodes() {
        return nNodes(false);
    }

    @Override
    public int nNodes(boolean includeBounds) {
        if (includeBounds)
            return nNodes + 2;
        else
            return nNodes;
    }

    @Override
    public boolean canSchedule(int pred, int node) {
        return isPossible(node) && isScheduled(pred) && (insertionVars[node].contains(pred));
    }

    @Override
    public boolean canPrecede(int pred, int node) {
        if (isExcluded(node) || isExcluded(pred))
            return false;
        if (canSchedule(pred, node))
            return true;
        // look recursively into the InsertionsVar, to see if pred is an insertion of other nodes
        resetSeen();
        return recursiveCanPrecede(pred, node);
    }

    private boolean recursiveCanPrecede(int pred, int node) {
        if (isInsertion(pred, node))
            return true;
        int size = insertionVars[node].size();
        for (int i = 0 ; i < size ; ++i) {
            int n = insertionVars[node].values[i];
            if (isScheduled(n)) {
                if (n == begin)
                    return true;
                else if (precede(pred, n))
                    return true;
            }
            else if (!insertionVars[n].seen) {
                insertionVars[n].seen = true;
                if (recursiveCanPrecede(pred, n))
                    return true;
            }
        }
        return false;
    }

    private void resetSeen() {
        for (InsertionVarInSequence i: insertionVars) {
            i.seen = false;
        }
    }

    /**
     * tell if pred occurs before node in the sequence
     * @param pred node occuring before
     * @param node node occuring after
     * @return true if pred precedes node in the sequence
     */
    private boolean precede(int pred, int node) {
        if (!isScheduled(pred) || !isScheduled(node))
            return false;
        // look from the successor of pred until the end of sequence is met
        for (int succ = nextMember(pred); succ != begin; succ = nextMember(succ)) {
            if (succ == node)
                return true;
        }
        return false;
    }

    @Override
    public void schedule(int pred, int node) {
        if (!isScheduled(pred))
            throw INCONSISTENCY;
        if (!domain.require(node)) {
            // the node is either already scheduled or excluded
            if (succ[pred].value() != node || isExcluded(node)) // the insertion points asked differs from the current / the node is excluded
                throw INCONSISTENCY;
            else // trying to do the same insertion twice
                return;
        }
        else if (!insertionVars[node].contains(pred))
            throw INCONSISTENCY; // the insertion var did not contain the node
        int succNode = succ[pred].value();
        succ[pred].setValue(node);
        succ[node].setValue(succNode);
        this.pred[node].setValue(pred);
        this.pred[succNode].setValue(node);

        insertionVars[node].removeAll();
        int[] values = new int[nNodes];
        int size = domain.getPossible(values);
        for (int i = 0; i < size ; ++i) {
            if (insertionVars[values[i]].contains(node)) {
                // the insertion point related to this node belongs now a scheduled insertion point
                insertionVars[values[i]].nbPossible.decrement();
                insertionVars[values[i]].nbScheduled.increment();
            }
        }
        if (isBound())
            seqListener.bind();
        insertionVars[node].listener.insert();
        insertionVars[node].listener.change();
        seqListener.insert();
    }

    @Override
    public void exclude(int node) {
        if (isScheduled(node))
            throw INCONSISTENCY;
        if (isExcluded(node))
            return;
        int size = domain.getPossible(values);
        for (int i = 0; i < size ; ++i) {// remove this node for all others insertions
            insertionVars[values[i]].removeInsert(node);
        }
        if (domain.exclude(node)) {
            if (isBound())
                seqListener.bind();
            insertionVars[node].removeAll();
            insertionVars[node].listener.exclude();
            //insertionVars[node].listener.change();  // not called as it technically does not change its domain
            seqListener.exclude();
        }
    }

    @Override
    public void excludeAllPossible() {
        int size = domain.getPossible(values);
        domain.excludeAllPossible();
        seqListener.bind(); // notify that the variable is fixed
        seqListener.exclude(); // nodes have been excluded
        for (int i = 0 ; i < size; ++i) {
            insertionVars[values[i]].removeAll(); //
            insertionVars[values[i]].listener.exclude();
        }
    }

    @Override
    public boolean isScheduled(int node) {
        return domain.isRequired(node);
    }

    @Override
    public boolean isPossible(int node) {
        return domain.isPossible(node);
    }

    @Override
    public boolean isExcluded(int node) {
        return domain.isExcluded(node);
    }

    @Override
    public int fillScheduled(int[] dest) {
        return domain.getRequired(dest);
    }

    @Override
    public int fillPossible(int[] dest) {
        return domain.getPossible(dest);
    }

    @Override
    public int fillExcluded(int[] dest) {
        return domain.getExcluded(dest);
    }

    @Override
    public int fillScheduledInsertions(int node, int[] dest) {
        if (!isPossible(node))
            return 0;
        int j = 0; // indexing used for dest
        if (insertionVars[node].nbScheduled() > domain.nRequired()) { // quicker to iterate over the current sequence
            int size = domain.nRequired();
            int current = end; // the end of the sequence can never be a valid insertion
            for (int i=0; i<size; ++i) {
                current = nextMember(current);
                // does node in the current sequence belong to a valid insertion?
                if (insertionVars[node].contains(current))
                    dest[j++] = current;
            }
        } else { // quicker to iterate over the remaining insertions inside the insertion var
            int s = insertionVars[node].size();
            for (int i = 0; i < s; i++) {
                // does insertion belong effectively to the scheduled sequence?
                if (domain.isRequired(insertionVars[node].values[i]))
                    dest[j++] = insertionVars[node].values[i];
            }
        }
        return j;
    }

    @Override
    public int fillPossibleInsertions(int node, int[] dest) {
        if (!isPossible(node))
            return 0;
        int j = 0; // indexing used for dest
        if (insertionVars[node].nbPossible() > domain.nPossible()) { // quicker to iterate over the current sequence
            int size = domain.nPossible();
            int current = end;
            for (int i=0; i<size; ++i) {
                current = nextMember(current);
                // does node in the current sequence belong to a valid insertion?
                if (insertionVars[node].contains(current))
                    dest[j++] = current;
            }
        } else { // quicker to iterate over the remaining insertions inside the insertion var
            int s = insertionVars[node].size();
            for (int i = 0; i < s; i++) {
                // does insertion belong effectively to the scheduled sequence?
                if (domain.isPossible(insertionVars[node].values[i]))
                    dest[j++] = insertionVars[node].values[i];
            }
        }
        return j;
    }

    @Override
    public int nPossibleInsertions(int node) {
        return insertionVars[node].nbPossible.value();
    }

    @Override
    public int nScheduledInsertions(int node) {
        return insertionVars[node].nbScheduled.value();
    }

    @Override
    public int nInsertions(int node) {
        return insertionVars[node].size();
    }

    @Override
    public int fillInsertions(int node, int[] dest) {
        return insertionVars[node].fillInsertions(dest);
    }

    @Override
    public boolean isInsertion(int pred, int node) {
        return insertionVars[node].contains(pred);
    }

    /**
     * remove an insertion point for an insertion var. The insertion trying to be removed cannot be in the excluded
     * set of the domain yet!
     * trigger the propagation
     * @param insertion
     * @param node
     */
    @Override
    public void removeInsertion(int insertion, int node) {
        if (insertionVars[node].remove(insertion)) {
            // update the counters for the number of scheduled and possible insertions
            if (isScheduled(insertion))
                insertionVars[node].nbScheduled.decrement();
            else if (isPossible(insertion))
                insertionVars[node].nbPossible.decrement();
            switch (insertionVars[node].size()) {
                case 0:
                    exclude(node);
                    break;
                case 1:  // only one insertion point remains
                    // do nothing
                    /*
                    if (isScheduled(insertionVars[node].values[0])) {
                        schedule(node, insertionVars[node].values[0]);
                    } else {
                        // the only insertion remaining belongs to the set of possible insertions
                    }
                     */
                    break;
                default: // more than 1 insertion point remain
                    ;
            }
            insertionVars[node].listener.change();
        }
    }

    @Override
    public void removeInsertionAfter(int node) {
        int n = fillPossible(values);
        for (int i = 0; i < n ; ++i) {
            removeInsertion(node, values[i]);
        }
    }

    @Override
    public InsertionVar getInsertionVar(int i) {
        return insertionVars[i];
    }

    /**  =====  propagation methods  =====  */

    @Override
    public void whenBind(Procedure f) {
        onBind.push(constraintClosure(f));
    }

    @Override
    public void whenInsert(Procedure f) {
        onInsert.push(constraintClosure(f));
    }

    @Override
    public void whenExclude(Procedure f) {
        onExclude.push(constraintClosure(f));
    }

    @Override
    public void propagateOnBind(CPConstraint c) {
        onBind.push(c);
    }

    @Override
    public void propagateOnInsert(CPConstraint c) {
        onInsert.push(c);
    }

    @Override
    public void propagateOnExclude(CPConstraint c) {
        onExclude.push(c);
    }

    protected void scheduleAll(StateStack<CPConstraint> constraints) {
        for (int i = 0; i < constraints.size(); i++)
            cp.schedule(constraints.get(i));
    }

    private CPConstraint constraintClosure(Procedure f) {
        CPConstraint c = new CPConstraintClosure(cp, f);
        getSolver().post(c, false);
        return c;
    }

    @Override
    public String toString() {
        return ordering(true, " -> ");
    }

    @Override
    public String ordering() {
        return ordering(false);
    }

    @Override
    public String ordering(boolean includeBounds) {
        return ordering(includeBounds, " -> ");
    }

    @Override
    public String ordering(boolean includeBounds, String join) {
        if (nScheduledNode(includeBounds) == 0)
            return "";
        int current = includeBounds ? begin : nextMember(begin);
        int last = includeBounds ? end : predMember(end);
        StringBuilder description = new StringBuilder(String.format("%d", current));
        while (current != last) {
            current = nextMember(current);
            description.append(join);
            description.append(current);
        }
        return description.toString();
    }
}

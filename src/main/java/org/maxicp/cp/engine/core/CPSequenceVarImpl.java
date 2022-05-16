package org.maxicp.cp.engine.core;

import org.maxicp.state.*;
import org.maxicp.state.datastructures.StateSequenceSet;
import org.maxicp.state.datastructures.StateStack;
import org.maxicp.util.Procedure;

import static org.maxicp.util.exception.InconsistencyException.INCONSISTENCY;

/**
 * a sequence var, where each node is represented by an integer and each insertion is an InsertionVar
 * all nodes are split into 3 categories: members (in the sequence), possible (could be in the sequence) and excluded (cannot be in the sequence)
 * WARNING: the ids for the begin node and the end node MUST be >= number of nodes considered
 *
 * constraints remove insertions points from the InsertionVars contained within the sequence
 * if an InsertionVar cannot be inserted into the sequence, it is set as excluded
 */
public class CPSequenceVarImpl implements CPSequenceVar {

    private final CPSolver cp;
    private final int nNodes;                   // number of nodes available (omitting begin and end)
    private final int maxIndex;                 // max index used for the node (max between begin and end)
    private final int nOmitted;                // number of unused indexes in the representation for the domain
    private CPInsertionVarInSequence[] insertionVars;
    private StateInt[] succ;                    // successors of the nodes
    private StateInt[] pred;                    // predecessors of the nodes
    private StateSequenceSet domain;            // domain for the set of Member, Possible and Excluded variables

    // TODO constructor from a set of specified edges
    // TODO checker for clusters of possibles nodes

    // constraints registered for this sequence
    private StateStack<CPConstraint> onInsert;    // a node has been inserted into the sequence
    private StateStack<CPConstraint> onFix;      // all nodes are members or excluded: no possible node remain
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
    public CPSequenceVarImpl(CPSolver cp, int nNodes, int begin, int end) {
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
        insertionVars = new CPInsertionVarInSequence[nNodes];
        succ = new StateInt[maxIndex];
        pred = new StateInt[maxIndex];
        for (int i=0; i < nNodes; ++i) {
            if (i == begin | i == end)
                continue;
            insertionVars[i] = new CPInsertionVarInSequence(i);
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
        onFix = new StateStack<>(cp.getStateManager());
        onExclude = new StateStack<>(cp.getStateManager());
        values = new int[nNodes];
    }

    /**
     * listener for the whole sequence. For more information about the changes (i.e. what insertion has occurred?),
     * use the listener within the insertionVars
     */
     private SequenceListener seqListener = new SequenceListener() {
        @Override
        public void fix() {
            scheduleAll(onFix);
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
    public class CPInsertionVarInSequence implements CPInsertionVar {

        // sparse set
        private StateInt nPossible;  // number of possible insertions. Each value is included within the possible set of the sequence
        private StateInt nMember; // number of member insertions. Each value is included within the member set of the sequence
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

        public CPInsertionVarInSequence(int id) {
            // all insertions are valid at first
            // no insertion belongs to the set of member insertions at first
            this.id = id;
            n = Math.max(begin+1, nNodes);
            nPossible = cp.getStateManager().makeStateInt(n); // consider all nodes as possible
            nMember = cp.getStateManager().makeStateInt(0);
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
                nPossible.decrement(); // in order to have a correct removal operation afterwards
            }
            remove(id); // a node cannot have itself as predecessor
            nPossible.decrement();

            // the begin node is always a member predecessor at first
            nMember.setValue(1);
            nPossible.decrement();
            // if the end node is in the set, remove it
            if (end < nNodes) {
                if (remove(end))
                    nPossible.decrement();
            }
            nMember.setValue(1);        // the beginning node is always a valid predecessor at first
        }

        @Override
        public CPSolver getSolver() {
            return cp;
        }

        @Override
        public boolean isFixed() {
            return !CPSequenceVarImpl.this.isPossible(node());
        }

        @Override
        public void removeInsert(int i) {
            CPSequenceVarImpl.this.removeInsertion(i, id);
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
         * does not interact with nMember nor nPossible, and should only be called by the SequenceVarImpl
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
         * does not insert into the sequence, and should only be called by the SequenceVarImpl
         * @param v insertion point to remove
         */
        private void removeAllBut(int v, boolean isMember) {
            assert (contains(v));
            int val = values[0];
            int index = indexes[v];
            indexes[v] = 0;
            values[0] = v;
            indexes[val] = index;
            values[index] = val;
            if (isMember) {
                nMember.setValue(1);
                nPossible.setValue(0);
            } else {
                nMember.setValue(0);
                nPossible.setValue(1);
            }
        }

        /**
         * remove all insertions points from the set of insertions
         * does not exclude / insert into the sequence, and should only be called by the SequenceVarImpl
         */
        private void removeAll() {
            nPossible.setValue(0);
            nMember.setValue(0);
        }

        @Override
        public void removeAllInsert() {
            CPSequenceVarImpl.this.exclude(id);
        }

        @Override
        public void removeAllInsertBut(int i) {
            if (isMember(i))  // equivalent to the scheduling of the variable
                insert(i, id);
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
        public int fillInsertion(int[] dest) {
            int s = size();
            if (s >= 0) System.arraycopy(values, 0, dest, 0, s);
            return s;
        }

        @Override
        public int size() {
            return nPossible.value() + nMember.value();
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
        public void whenFixed(Procedure f) {
            onExclude.push(constraintClosure(f));
            onInsert.push(constraintClosure(f));
        }

        @Override
        public void propagateOnFix(CPConstraint c) {
            onInsert.push(c);
            onExclude.push(c);
        }

        public int nMember() {return nMember.value();}

        public int nPossible() {return nPossible.value();}

        @Override
        public String toString() {
            StringBuilder member = new StringBuilder("M: {");
            StringBuilder possible = new StringBuilder(", P: {");
            StringBuilder excluded = new StringBuilder(", E: {");
            for (int i = 0 ; i < values.length ; ++i) {
                if (contains(values[i])) {
                    if (isMember(values[i])) {
                        member.append(values[i]);
                        member.append(',');
                    } else if (isPossible(values[i])) {
                        possible.append(values[i]);
                        possible.append(',');
                    }
                } else {
                    excluded.append(values[i]);
                    excluded.append(',');
                }
            }
            return member.append('}').toString() + possible.append('}').toString() + excluded.append('}').toString();
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
    public boolean isFixed() {
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
    public int nMember() {
        return nMember(true);
    }

    @Override
    public int nMember(boolean includeBounds) {
        if (includeBounds)
            return domain.nRequired();
        else
            return domain.nRequired() - 2;
    }

    @Override
    public int nPossible() {
        return domain.nPossible();
    }

    @Override
    public int nExcluded() {
        return domain.nExcluded() - nOmitted;
    }

    @Override
    public int nNode() {
        return nNode(true);
    }

    @Override
    public int nNode(boolean includeBounds) {
        if (includeBounds)
            return nNodes + 2;
        else
            return nNodes;
    }

    @Override
    public boolean canInsert(int pred, int node) {
        return isPossible(node) && isMember(pred) && (insertionVars[node].contains(pred));
    }

    @Override
    public boolean canPrecede(int pred, int node) {
        if (isExcluded(node) || isExcluded(pred))
            return false;
        if (canInsert(pred, node))
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
            if (isMember(n)) {
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
        for (CPInsertionVarInSequence i: insertionVars) {
            i.seen = false;
        }
    }

    public boolean precede(int pred, int node) {
        if (!isMember(pred) || !isMember(node))
            return false;
        // look from the successor of pred until the end of sequence is met
        for (int succ = nextMember(pred); succ != begin; succ = nextMember(succ)) {
            if (succ == node)
                return true;
        }
        return false;
    }

    @Override
    public void insert(int pred, int node) {
        if (!isMember(pred))
            throw INCONSISTENCY;
        if (!domain.require(node)) {
            // the node is either already member or excluded
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
                // the insertion point related to this node belongs now a member insertion point
                insertionVars[values[i]].nPossible.decrement();
                insertionVars[values[i]].nMember.increment();
            }
        }
        if (isFixed())
            seqListener.fix();
        insertionVars[node].listener.insert();
        insertionVars[node].listener.change();
        seqListener.insert();
    }

    @Override
    public void exclude(int node) {
        if (isMember(node))
            throw INCONSISTENCY;
        if (isExcluded(node))
            return;
        int size = domain.getPossible(values);
        for (int i = 0; i < size ; ++i) {// remove this node for all others insertions
            insertionVars[values[i]].removeInsert(node);
        }
        if (domain.exclude(node)) {
            if (isFixed())
                seqListener.fix();
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
        seqListener.fix(); // notify that the variable is fixed
        seqListener.exclude(); // nodes have been excluded
        for (int i = 0 ; i < size; ++i) {
            insertionVars[values[i]].removeAll(); //
            insertionVars[values[i]].listener.exclude();
        }
    }

    @Override
    public boolean isMember(int node) {
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
    public int fillMember(int[] dest) {
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
    public int fillMemberInsertion(int node, int[] dest) {
        if (!isPossible(node))
            return 0;
        int j = 0; // indexing used for dest
        int s = insertionVars[node].size();
        if (s > domain.nRequired()) { // quicker to iterate over the current sequence
            int size = domain.nRequired();
            int current = end; // the end of the sequence can never be a valid insertion
            for (int i=0; i<size; ++i) {
                current = nextMember(current);
                // does node in the current sequence belong to a valid insertion?
                if (insertionVars[node].contains(current))
                    dest[j++] = current;
            }
        } else { // quicker to iterate over the remaining insertions inside the insertion var
            for (int i = 0; i < s; i++) {
                // does insertion belong effectively to the member sequence?
                if (domain.isRequired(insertionVars[node].values[i]))
                    dest[j++] = insertionVars[node].values[i];
            }
        }
        return j;
    }

    @Override
    public int fillPossibleInsertion(int node, int[] dest) {
        if (!isPossible(node))
            return 0;
        int j = 0; // indexing used for dest
        int s = insertionVars[node].size();
        if (s > domain.nPossible()) { // quicker to iterate over the possible nodes
            int size = domain.getPossible(values);
            for (int i=0; i<size; ++i) {
                int current = values[i];
                // does node in the current sequence belong to a valid insertion?
                if (insertionVars[node].contains(current))
                    dest[j++] = current;
            }
        } else { // quicker to iterate over the remaining insertions inside the insertion var
            for (int i = 0; i < s; i++) {
                // does insertion belong effectively to the possible values?
                if (domain.isPossible(insertionVars[node].values[i]))
                    dest[j++] = insertionVars[node].values[i];
            }
        }
        return j;
    }

    @Override
    public int nPossibleInsertion(int node) {
        return insertionVars[node].nPossible.value();
    }

    @Override
    public int nMemberInsertion(int node) {
        return insertionVars[node].nMember.value();
    }

    @Override
    public int nInsertion(int node) {
        return insertionVars[node].size();
    }

    @Override
    public int fillInsertion(int node, int[] dest) {
        return insertionVars[node].fillInsertion(dest);
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
            // update the counters for the number of member and possible insertions
            if (isMember(insertion))
                insertionVars[node].nMember.decrement();
            else if (isPossible(insertion))
                insertionVars[node].nPossible.decrement();
            if (insertionVars[node].size() == 0) {
                if (node == 71) {
                    int a = 0;
                }
                exclude(node);
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
    public CPInsertionVar getInsertionVar(int i) {
        return insertionVars[i];
    }

    /**  =====  propagation methods  =====  */

    @Override
    public void whenFixed(Procedure f) {
        onFix.push(constraintClosure(f));
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
    public void propagateOnFix(CPConstraint c) {
        onFix.push(c);
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
        if (nMember(includeBounds) == 0)
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

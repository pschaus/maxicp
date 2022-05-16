package org.maxicp.cp.engine.constraints.sequence;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSequenceVar;
import org.maxicp.state.StateInt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.maxicp.util.exception.InconsistencyException.INCONSISTENCY;

/**
 * Capacity constraint on a {@link CPSequenceVar}, using positive load change
 */
public class Capacity extends AbstractCPConstraint {

    private CPSequenceVar sequenceVar;
    //private int maxCapacity;
    private int initCapacity;
    private int[] load;
    private CPIntVar capacity;

    private Supplier<Integer> currentCapaGetter;
    private Supplier<Integer> maxCapacity;
    private Consumer<Integer> currentCapaSetter;

    private Integer[] sortedNode; // index of the nodes corresponding to the capacity -> sortedNode[i] = argmin(load)[i]

    //private StateInt currentCapa; // store the current capacity of the sequence (capacity before ending the sequence)
    // only used if capacity is set to null. Otherwise, capacity is used instead
    private StateInt capaCursor; // position of the current capacity being filtered

    /**
     *
     * @param sequenceVar
     * @param capacity
     * @param load
     */
    public Capacity(CPSequenceVar sequenceVar, CPIntVar capacity, int[] load) {
        this(sequenceVar, capacity, 0, 0, load);
    }

    /**
     *
     * @param sequenceVar
     * @param capacity
     * @param initCapacity
     * @param load
     */
    public Capacity(CPSequenceVar sequenceVar, CPIntVar capacity, int initCapacity, int[] load) {
        this(sequenceVar, capacity, 0, initCapacity, load);
    }

    /**
     * assign a maximum capacity to a sequence, where nodes only have a positive load change
     * @param sequenceVar sequence restrained by a capacity
     * @param maxCapacity capacity of the sequence
     * @param initCapacity initial capacity of the sequence
     * @param load load change occurring at each node (always positive)
     */
    public Capacity(CPSequenceVar sequenceVar, int maxCapacity, int initCapacity, int[] load) {
        this(sequenceVar, null, maxCapacity, initCapacity, load);
    }

    /**
     * assign a maximum capacity to a sequence, where nodes only have a positive load change
     * @param sequenceVar sequence restrained by a capacity
     * @param maxCapacity capacity of the sequence
     * @param load load change occurring at each node (always positive)
     */
    public Capacity(CPSequenceVar sequenceVar, int maxCapacity, int[] load) {
        this(sequenceVar, maxCapacity, 0, load);
    }

    private Capacity(CPSequenceVar sequenceVar, CPIntVar capacity, int maxCapacity, int initCapacity, int[] load) {
        super(sequenceVar.getSolver());
        if (initCapacity > maxCapacity)
            throw new IllegalArgumentException("Initial capacity must be lower or equal to maximum capacity");
        //this.maxCapacity = maxCapacity;
        this.sequenceVar = sequenceVar;
        this.initCapacity = initCapacity;
        this.capacity = capacity;
        this.load = load;

        if (capacity == null) {
            StateInt currentCapa = getSolver().getStateManager().makeStateInt(initCapacity);
            currentCapaGetter = currentCapa::value;
            currentCapaSetter = currentCapa::setValue;
            this.maxCapacity = () -> maxCapacity;
        } else { // using an CPIntVar to register the capacity in the sequence
            currentCapaGetter = capacity::min;
            currentCapaSetter = capacity::removeBelow;
            this.maxCapacity = capacity::max;
        }

        // sort the capacity by increasing order
        sortedNode = new Integer[load.length];
        for (int i = 0 ; i < load.length ; ++i) {
            sortedNode[i] = i;
        }
        Arrays.sort(sortedNode, Comparator.comparingInt(i -> load[i]));
        capaCursor = getSolver().getStateManager().makeStateInt(load.length - 1);
        if (load[sortedNode[0]] < 0)
            throw new IllegalArgumentException("Cannot use negative capacity in a Capacity constraints\n" +
                    "Consider using a Cumulative constraint instead");
    }

    @Override
    public void post() {
        if (capacity != null) {
            capacity.removeBelow(initCapacity); // initial capacity occupied by the vehicle
        }
        propagate();
        int[] possible = new int[sequenceVar.nPossible()];
        sequenceVar.fillPossible(possible);
        for (int i : possible) {
            sequenceVar.getInsertionVar(i).propagateOnInsert(new CapacityFromNode(i));
        }
    }

    @Override
    public void propagate() {
        // compute the current capacity of the sequence
        int current = sequenceVar.nextMember(sequenceVar.begin());
        int end = sequenceVar.end();
        int capa = currentCapaGetter.get();
        while (current != end) {
            capa += load[current];
            if (capa > maxCapacity.get())
                throw INCONSISTENCY;
            current = sequenceVar.nextMember(current);
        }
        int remainingCapacity = maxCapacity.get() - capa;
        // look at the nodes exceeding the capacity and decrement the cursor
        filterNodes(remainingCapacity);
        currentCapaSetter.accept(capa);
    }

    /**
     * filer the nodes that would exceed the maximum capacity when put into the sequence
     * @param remainingCapacity remaining capacity allowed within the constraint
     */
    private void filterNodes(int remainingCapacity) {
        int cursor = capaCursor.value();
        while (cursor >= 0 && load[sortedNode[cursor]] > remainingCapacity) {
            if (sequenceVar.isPossible(sortedNode[cursor])) {
                sequenceVar.exclude(sortedNode[cursor]);
            }
            cursor--;
        }
        // set the values for the cursor
        capaCursor.setValue(cursor);
    }

    /**
     * handle updates when inserting a single node
     */
    private class CapacityFromNode extends AbstractCPConstraint {

        private int node;

        private CapacityFromNode(int node) {
            super(sequenceVar.getSolver());
            this.node = node;
        }

        @Override
        public void propagate() {
            // increment the capacity
            int capacity = load[node] + currentCapaGetter.get();
            if (capacity > maxCapacity.get())
                throw INCONSISTENCY;
            // filter the nodes
            filterNodes(maxCapacity.get() - capacity);
            currentCapaSetter.accept(capacity);
        }
    }
}

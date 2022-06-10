package org.maxicp.cp.engine.constraints.sequence;

import org.maxicp.cp.engine.core.AbstractCPConstraint;
import org.maxicp.cp.engine.core.CPIntVar;
import org.maxicp.cp.engine.core.CPSequenceVar;

/**
 * A {@link CPSequenceVar} respects temporal transitions between nodes
 */
public class TransitionTimes extends AbstractCPConstraint {
    
    private final CPIntVar[] time;
    private final CPIntVar distance; // maximum distance allowed, possibly null
    private final int[][] transition;
    private final int[] serviceTime;
    private final CPSequenceVar seq;
    private final boolean listenDistanceChange;
    private final int[] insertionsVar;
    private final int[] insertionsPoint;

    private boolean masterConstraintPropagating = false;

    /**
     * connect transition in a sequence with the service time of nodes and distances between them
     * remove values of insertions points based on the transition in the sequence and their timing
     * only update the values of time if the node is inserted within the sequence
     * Waiting at a node is permitted!
     * @param seq sequence to post the constraint on
     * @param time time of visit available for each node
     * @param transition transition time from node to mode
     * @param serviceTime service time of each node
     */
    public TransitionTimes(CPSequenceVar seq, CPIntVar[] time, int[][] transition, int[] serviceTime) {
        this(seq, time, null, transition, serviceTime);
    }

    /**
     * connect transition in a sequence with the service time of nodes and distances between them
     * remove values of insertions points based on the transition in the sequence and their timing
     * only update the values of time if the node is inserted within the sequence
     * Waiting at a node is permitted!
     * @param seq sequence to post the constraint on
     * @param time time of visit available for each node
     * @param distance maximum distance allowed for the sequence
     * @param transition transition time from node to mode
     * @param serviceTime service time of each node
     */
    public TransitionTimes(CPSequenceVar seq, CPIntVar[] time, CPIntVar distance, int[][] transition, int[] serviceTime) {
        this(seq, time, distance, transition, serviceTime, true);
    }


    /**
     * connect transition in a sequence with the service time of nodes and distances between them
     * remove values of insertions points based on the transition in the sequence and their timing
     * only update the values of time if the node is inserted within the sequence
     * Waiting at a node is permitted!
     * @param seq sequence to post the constraint on
     * @param time time of visit available for each node
     * @param distance maximum distance allowed for the sequence
     * @param transition transition time from node to mode
     * @param serviceTime service time of each node
     * @param listenDistanceChange if true, propagates on bound changes for distance
     */
    public TransitionTimes(CPSequenceVar seq, CPIntVar[] time, CPIntVar distance, int[][] transition, int[] serviceTime,
                           boolean listenDistanceChange) {
        super(seq.getSolver());
        this.seq = seq;
        this.time = time;
        this.distance = distance;
        this.transition = transition;
        this.serviceTime = serviceTime;
        insertionsVar = new int[seq.nNode()]; // upper bound on the number of nodes being member of the sequence
        insertionsPoint = new int[seq.nNode()]; // upper bound on the number of insertions for one node
        this.listenDistanceChange = listenDistanceChange;
    }

    @Override
    public void post() {
        updatePossibleInsertions();
        propagate();
        // register propagators
        int size = seq.fillPossible(insertionsVar);
        for (int i = 0; i < size; ++i) {
            int insert = insertionsVar[i];
            //time[insert].propagateOnBoundChange(this);
            new TransitionFromTimeWindow(insert).post();
        }
        size = seq.fillMember(insertionsVar);
        for (int i = 0; i < size; ++i) { // the time updates for the scheduled nodes also has an impact on the changes
            int insert = insertionsVar[i];
            time[insert].propagateOnBoundChange(this);
        }
        if (distance != null && listenDistanceChange) {
            distance.propagateOnBoundChange(this);
        }
        seq.propagateOnInsert(this);
        seq.whenFixed(this::updateMinTimeScheduledAndDist); // set the value for the distance
    }

    /**
     * update the lower bound on the time windows of the sequence and the distance
     */
    private int updateMinTimeScheduledAndDist() {
        int n = seq.fillOrder(insertionsVar, true);
        int distance = 0;
        int current = insertionsVar[1];
        int pred = insertionsVar[0];
        int predTime = time[pred].min() + serviceTime[pred];

        for (int i = 1 ; i < n ; ++i) {
            int dist = transition[pred][current];
            predTime += dist;
            distance += dist;

            time[current].removeBelow(predTime);
            predTime = Math.max(predTime, time[current].min()) + serviceTime[current];

            pred = current;
            if (i < n-1)
                current = insertionsVar[i+1];
        }
        
        if (this.distance != null) {
            if (seq.isFixed())
                this.distance.fix(distance);
            else {
                this.distance.removeBelow(distance);
            }
        }
        
        return distance;
    }

    // precondition: insertionsVar is populated with the current order of the sequence
    private void updateMaxTimeScheduled() {
        int n = seq.nMember();
        int succ = insertionsVar[n-1];
        int current = insertionsVar[n-2];
        int succTime = time[succ].max();
        for (int i = n - 2 ; i >= 0 ; --i) {
            succTime -= transition[current][succ] + serviceTime[current]; // departure from the successor
            time[current].removeAbove(succTime);
            succTime = time[current].max();

            succ = current;
            if (i > 0)
                current = insertionsVar[i-1];
        }
    }

    /**
     * for each possible insertion var, update their scheduled insertions if their time window does not allow to reach the node
     */
    private void updateScheduledInsertions(int currentDistance) {
        int size = seq.fillPossible(insertionsVar);
        int maxDetourAllowed = distance != null ? distance.max() - currentDistance : Integer.MAX_VALUE;
        for (int i = 0; i < size; ++i) { // for all possible insertion ...
            int current = insertionsVar[i];
            updateMemberInsertionsForOneNode(current, maxDetourAllowed);
        }
    }

    private void updatePossibleInsertions() {
        int size = seq.fillPossible(insertionsVar);
        for (int i = 0; i < size; ++i) {
            int current = insertionsVar[i];
            int nbInsert = seq.fillPossibleInsertion(current, insertionsPoint); // only use the possible insertions
            for (int j = 0; j < nbInsert; ++j) {
                int pred = insertionsPoint[j];
                // if the min time of the insert does not allow to reach the node within its own max time, remove it
                if (time[pred].min() + serviceTime[pred] + transition[pred][current] > time[current].max())
                    seq.removeInsertion(pred, current);
            }
        }
    }

    private void updateMemberInsertionsForOneNode(int node, int maxDetourAllowed) {
        int nInsert = seq.fillMemberInsertion(node, insertionsPoint);
        boolean foundInsert = false;
        for (int j = 0; j < nInsert; ++j) { // for all of its scheduled insertion point candidate ...
            int pred = insertionsPoint[j];  // check that .. -> pred -> current -> succ -> .. is feasible
            int succ = seq.nextMember(pred); // successor of the insertion
            int timeReachingNode = time[pred].min() + serviceTime[pred] + transition[pred][node];
            if (timeReachingNode > time[node].max()) // check that pred -> current is feasible
                seq.removeInsertion(pred, node);
            else { // check that current -> succ is feasible
                int timeDeparture = Math.max(timeReachingNode, time[node].min());
                if (timeDeparture + serviceTime[node] + transition[node][succ] > time[succ].max())
                    seq.removeInsertion(pred, node);
                else if (distance != null) { // check that doing the transition does not exceed the maximum distance
                    int detour = transition[pred][node] + transition[node][succ] - transition[pred][succ];
                    if (detour > maxDetourAllowed) // detour is too long
                        seq.removeInsertion(pred, node);
                    else
                        foundInsert = true;
                } else {
                    foundInsert = true;
                }
            }
        }
        if (!foundInsert) { // no scheduled insertion point existed for this node, remove it
            seq.exclude(node);
        }
    }

    @Override
    public void propagate() {
        setActive(false);
        masterConstraintPropagating = true;
        //System.out.println("propagating for " + seq.toString());
        int currentDistance = updateMinTimeScheduledAndDist();
        updateMaxTimeScheduled();
        if (!seq.isFixed())
            updateScheduledInsertions(currentDistance); // remove insertions candidates affected by the insertion
        //updatePossibleInsertions();
        setActive(true);
        masterConstraintPropagating = false;
}

    /**
     * inner class for updates of time windows. Use incremental update depending on the status of the node
     * - the node is excluded from the sequence -> set as inactive
     * - the node is scheduled -> call the full propagation
     * - the node is possible -> check its current insertions
     */
    private class TransitionFromTimeWindow extends AbstractCPConstraint {

        private final int node;

        public TransitionFromTimeWindow(int node) {
            super(seq.getSolver());
            this.node = node;
        }

        @Override
        public void post() {
            // propagation is called from the outer class, no need to propagate here
            if (!seq.isExcluded(node))
                time[node].propagateOnBoundChange(this);
        }

        @Override
        public boolean isActive() {
            return (!masterConstraintPropagating && !seq.isExcluded(node) && TransitionTimes.this.isActive() && !TransitionTimes.this.isScheduled());
        }

        @Override
        public void propagate() {
            if (seq.isMember(node)) {
                if (!TransitionTimes.this.isScheduled())
                    getSolver().schedule(TransitionTimes.this);
            } else if (seq.isPossible(node)) {
                // only update the insertions for this node
                int maxDetourAllowed = distance != null ? distance.max() - distance.min() : Integer.MAX_VALUE;
                updateMemberInsertionsForOneNode(node, maxDetourAllowed);
            }
        }
    }
    
}
